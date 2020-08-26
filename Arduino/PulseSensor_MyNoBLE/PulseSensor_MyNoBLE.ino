#include <Arduino.h>
#include <SPI.h>
#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_SPI.h"
#include "Adafruit_BluefruitLE_UART.h"
#include <Adafruit_CircuitPlayground.h>

#include "BluefruitConfig.h"

#if SOFTWARE_SERIAL_AVAILABLE
  #include <SoftwareSerial.h>
#endif

//  Variables
int PulseSensorPurplePin = 10;        // Pulse Sensor PURPLE WIRE connected to ANALOG PIN 10
int LED13 = 13;   //  The on-board Arduion LED

#define WINDOWSIZE 200     // 20Hz * 20 seconds, so about 400 samples (could be less)
int Signal;                // holds the incoming raw data. Signal value can range from 0-1024
int Threshold = 550;       // Determine which Signal to "count as a beat", and which to ingore.

float smoothedAvgAccels[WINDOWSIZE];
int rawAccels[WINDOWSIZE];
unsigned int numSamples = 0;
bool dbg = true;

unsigned long lastCountTime = 0;
unsigned long lastSendTime  = 0;
unsigned long lastRightButton = 0;

/*=========================================================================
    APPLICATION SETTINGS
    -----------------------------------------------------------------------*/
    #define FACTORYRESET_ENABLE         0
    #define MINIMUM_FIRMWARE_VERSION    "0.6.6"
    #define MODE_LED_BEHAVIOUR          "MODE"
/*=========================================================================*/

// Create the bluefruit object, either software serial...uncomment these lines

//Adafruit_BluefruitLE_UART ble(BLUEFRUIT_HWSERIAL_NAME, BLUEFRUIT_UART_MODE_PIN);

/* ...hardware SPI, using SCK/MOSI/MISO hardware SPI pins and then user selected CS/IRQ/RST */
// Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);

/* ...software SPI, using SCK/MOSI/MISO user-defined SPI pins and then user selected CS/IRQ/RST */
//Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_SCK, BLUEFRUIT_SPI_MISO,
//                             BLUEFRUIT_SPI_MOSI, BLUEFRUIT_SPI_CS,
//                             BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);


// A small helper
void error(const __FlashStringHelper*err) {
  // Serial.println(err);
  // while (1);
}

/**************************************************************************/
/*!
    @brief  Sets up the HW an the BLE module (this function is called
            automatically on startup)
*/
/**************************************************************************/
void setup() {
  
  while (!Serial);
  delay(500);
  
  // pinMode(LED13,OUTPUT);         // pin that will blink to your heartbeat!
  CircuitPlayground.begin();

  Serial.begin(9600);
  Serial.println(F("Adafruit Bluefruit Command <-> Data Mode Example"));
  Serial.println(F("------------------------------------------------"));

  /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module: "));

//  if ( !ble.begin(VERBOSE_MODE) )
//  {
//    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
//  }
//  Serial.println( F("OK!") );
//
//  if ( FACTORYRESET_ENABLE )
//  {
//    /* Perform a factory reset to make sure everything is in a known state */
//    Serial.println(F("Performing a factory reset: "));
//    if ( ! ble.factoryReset() ){
//      error(F("Couldn't factory reset"));
//    }
//  }
//
//  /* Disable command echo from Bluefruit */
//  ble.echo(false);
//
//  Serial.println("Requesting Bluefruit info:");
//  /* Print Bluefruit information */
//  ble.info();
//
//  Serial.println(F("Please use Adafruit Bluefruit LE app to connect in UART mode"));
//  Serial.println(F("Then Enter characters to send to Bluefruit"));
//  Serial.println();
//
//  ble.verbose(false);  // debug info is a little annoying after this point!
//
//  /* Wait for connection */
//  while (! ble.isConnected()) {
//      delay(500);
//  }
//
//  Serial.println(F("******************************"));
//
//  // LED Activity command is only supported from 0.6.6
//  if ( ble.isVersionAtLeast(MINIMUM_FIRMWARE_VERSION) )
//  {
//    // Change Mode LED Activity
//    Serial.println(F("Change LED activity to " MODE_LED_BEHAVIOUR));
//    ble.sendCommandCheckOK("AT+HWModeLED=" MODE_LED_BEHAVIOUR);
//  }
//
//  // Set module to DATA mode
//  Serial.println( F("Switching to DATA mode!") );
//  ble.setMode(BLUEFRUIT_MODE_DATA);
//
  Serial.println(F("******************************"));

  CircuitPlayground.clearPixels();
  lastCountTime = millis();
  resetDataCounters();
  
}

void addSampleToSlidingWindow(int sample) {
  // add sample to sliding window
  // note that numSamples alwasy points to the insertion point in the sliding window array
  // as well as keeps a count of how many items are in the sliding window
  if (numSamples < WINDOWSIZE) { // up until the last element, which is size-1
    rawAccels[numSamples] = sample;
    ++numSamples; 
  } else {
    // numSamples = WINDOWSIZE (or greater). this means array is full, we need to shift out the oldest value
    for (int i = 0; i < WINDOWSIZE - 1; ++i) {
      rawAccels[i] = rawAccels[i+1];
    }
    // array is already full, no need to increment numSamples
    rawAccels[WINDOWSIZE - 1] = sample;
  }
  // this check should be really necessary, bit left here for safety should code change in future
  if (numSamples > WINDOWSIZE) {
    numSamples = WINDOWSIZE;
  }
}

void resetDataCounters( ){
  for (int i = 0; i < WINDOWSIZE; ++i) {
    smoothedAvgAccels[i] = 0.0;
    rawAccels[i] = 0;
  }
  numSamples = 0;
}

float smoothCurves() {
  int averageInterval = 3; // 3 sample average
  for (int i = 0; i < numSamples; ++i) {   // only consider up to numSamples, not window size since window may not be full
    // for 3 average size, we will need to consider i-1, i and i+1 and average those values
    int sum = 0; // should not overflow over 65K for just 3 samples, but we can be careful if needed later
    int denom = 0;
    for (int j = 0; j <= 0; ++j) {
      int idx = i + j;
      if (idx >= 0 && idx < numSamples) {
        // in range
        ++denom;
        sum += rawAccels[idx];
      }
    }
    // now set the avg value
    if (denom > 0) {
      // must be at least one sample, or else we will divide by zero      
      smoothedAvgAccels[i] = (float) sum / ((float) denom);
    } else {
      // should not get here
      smoothedAvgAccels[i] = rawAccels[i];
    }
  }
}

int maxSignal = -1;

void updateThreshold () {
  Threshold = maxSignal * 0.8;
  if (Threshold < 500) { Threshold = 500; } // min threshold
//  if (dbg) {
//    Serial.println("New thr:" + String(Threshold));      
//  }
}

int findNumBeatsPerMin() {
  smoothCurves();
  updateThreshold();
  int numBeats = findNumPeaks(Threshold);
  int numRetries = 1;
  // in 20sec of data, assuming max 300bpm, and min 30bpm, there can be max 100 peaks and min 10 peaks, otherwise our preset
  // threshold is too low or too high
  // full sample holds 20 seconds of data
  // for 20 sec, numSamples will be approx 10*20 for 10Hz
  // for 5 sec, numSamples will be approx 10*5 and min number of beats will be 30bpm * 5/60, i.e. 30 * (numsamples/10) / 60 
  // e.g. for full 20 sec, min number of beats will be 30 * (200 / 10) / 60, or 10 beats as discussed above
  //                   and max number of beats will be 300 * (200 / 10) / 60 or 100 beats in 20 sec, or 300 bpm
  int minBeats = 30 * (numSamples / 10) / 60; // note div and mod for integer division
  int maxBeats = 300 * (numSamples / 10) / 60;
  while ((numBeats > maxBeats || numBeats < minBeats) && numRetries <= 1) {
    updateThreshold();
    numBeats = findNumPeaks(Threshold);
    ++numRetries;
  }
  if (numBeats < minBeats || numBeats > maxBeats) {
    // unrealistic
    return -1;
  }
  // really should clear the arrays here, but let's not bother yet
  
  return numBeats * 3; // 20 sec data, so multiply by 3
  
}

int findNumPeaks(int threshold) {
  int numPeaks = 0;
  int lastPeak = 0;
  int maxVal = -1000;
//  if (dbg) {
//    Serial.println("NumSamples " + String(numSamples));
//  }
  for (int i = 1; i < numSamples - 1; ++i) {
    // we only need to look up to the numSamples and not the entire array
    // it is a peak if sample i is greater than sample i-1 and i+1
    // peaks at edges will not be considered e.g. [ 5 2 0 0 0 3 ] has no peaks.
    // that is why we start the index at 1 and end at numSamples - 2, instead of 0 and numSamples - 1
    float curVal = smoothedAvgAccels[i];
    if (curVal > maxVal) {
      maxVal = curVal;
    }
    // does it cross the threshold?
    if (curVal < threshold) {
      continue;
    }
    // over the threshold, is it a peak?
    if (curVal >= smoothedAvgAccels[i-1] && curVal >= smoothedAvgAccels[i+1]) {
      // is a peak
      if (i - lastPeak < 6) {
        continue;
      }
      ++numPeaks;
      lastPeak = i;
    }
  }  
  maxSignal = maxVal;
  return numPeaks;
}

int numBpm = 0;
String s2 = "";

// The Main Loop Function
void loop() {
  // Serial.print("3");
  bool panic = false;
  unsigned long now = millis();
  
  if (CircuitPlayground.rightButton()) { // panic button!
    if ((now - lastRightButton) > 500) {
      lastRightButton = now;
      panic = true;
    }    
  }

  // Save received data to string
  String received = "";

//  while ( ble.available() )
//  {
//    int c = ble.read();
//    Serial.print((char)c);
//    received += (char)c;
//        delay(50);
//    
//  }
  
  if(received != "" ){
    Serial.println("");
    Serial.println("RECEIVED: " + received); 
    delay(50);  
  }
  
  Signal = analogRead(PulseSensorPurplePin);  // Read the PulseSensor's value.
                                              // Assign this value to the "Signal" variable.
   // continuously calibrate
  addSampleToSlidingWindow(Signal);
  // if 2 seconds have passed, find bpm
  if (now - lastCountTime >= 2000) {
    numBpm = findNumBeatsPerMin();
    // if (dbg) {
      // Serial.println("BPM: " + String(numBpm));
    // }
    lastCountTime = now;
  }
  // Serial.print("4");

  if (now - lastSendTime >= 500) {
      lastSendTime = now;
      // send ble data
      String bp = "-1";
      if (numBpm > 0) {
        bp = String(numBpm);
      }
      if (panic) {
        s2 = "critical:" + bp; // + String(numBpm);
      } else {
        s2 = "bpm:" + bp; // + String(numBpm);        
      }
      Serial.println(s2);
      // now send over BLE
      // char output[32] = {};
      // String data = s;
      // data.toCharArray(output,32); // why do we need output?? We don't seem to use it..
      // ble.print(data);
            
  }
  // Serial.print("5");
  delay(100); // approx 10Hz

}
