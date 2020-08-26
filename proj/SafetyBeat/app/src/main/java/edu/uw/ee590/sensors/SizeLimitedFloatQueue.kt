package edu.uw.ee590.sensors

class SizeLimitedFloatQueue(val maxSize: Int) {
    // private val maxSize : Int;

    private var values : FloatArray; // Array<Float>;
    var size: Int = 0;

    init
    {
        values = FloatArray(maxSize, {i -> i.toFloat()}); // arrayOf(maxSize, i);
    }

    fun add(k: Float): Boolean {

        if (size >= maxSize) { // already full
            for (i in 1 until size) {
                values[i-1] = values[i];
            }
            values[size - 1] = k;
        } else {
            values[size] = k;
            ++size;
        }
        return true;
    }

    fun average() : Float {
        if (values == null) {
            return 0f;
        }
        return values.average().toFloat();
    }

    fun averageOverLast(numDataPoints: Int) : Float {
        if (values == null || numDataPoints <=0 ) {
            return 0f;
        }
        var total : Float = 0f;
        for (i in size-1 downTo size-numDataPoints) {
            if (i >= 0) {
                total += values[i];
            }
        }

        return total / numDataPoints;
    }

    fun min() : Float {
        if (values == null) {
            return 0f;
        }
        return values.min()!!;
    }

    fun max() : Float {
        if (values == null) {
            return 0f;
        }
        return values.max()!!;
    }

    operator fun get (i: Int) : Float {
        return values[i];
    }

    operator fun set (i: Int, v: Float) : Unit {
        values[i] = v;
    }

}

