package edu.uw.ee590.sensors

enum class TriggerType {
        Normal,
        Interested,
        Elevated,
        Critical
}

class Trigger(val triggerType:TriggerType, val bpm:Int) {

}