package edu.uw.ee590.sensors

import kotlin.math.round
import kotlin.math.sin
import kotlin.random.Random

class HeartBeatSimulator() {
    private var theta:Float = 0f; // theta is w*t (omega * t)
    private var bias:Int = 0; // 60 bpm resting heart rate
    private var phaseShift: Float = 0f; // dynamic phase shift so it is not pure sin curve
    private val amplitude:Float = 50f;
    private val period:Float = 90f; // 90 seconds period
    private var perCallIncr: Float = 0f;

    init {
        // bias will be min 60, max 160, so median value (the bias) is 110
        bias = 110;
        perCallIncr = (6.28318 / (90*2)).toFloat(); // assume 2Hz sampling
    }

    fun getCurrentBPM() : Int {
        // assumeing the calls come every 1 second, so 2*pi in 90 seconds
        theta += perCallIncr;
        phaseShift = Random.nextFloat() * 0.1f; // 0.1 radian is maximum phase shift
        val retval : Int = bias + round(amplitude * sin(theta + phaseShift)).toInt();
        return retval;
    }
}