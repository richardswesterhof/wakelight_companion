package com.richardswesterhof.wakelightcompanion.utils

class UniformStepCalculator(
    steps: Double,
    minBright: Double,
    maxBright: Double,
    minTemp: Double,
    maxTemp: Double,
    minDuration: Double,
    maxDuration: Double
) {

    private var brightnessFunction: LinearFunction =
        LinearFunction.givenPoints(Pair(0.0, minBright), Pair(steps, maxBright))
    private var temperatureFunction: LinearFunction =
        LinearFunction.givenPoints(Pair(0.0, minTemp), Pair(steps, maxTemp))
    private var durationFunction: LinearFunction =
        LinearFunction.givenPoints(Pair(0.0, minDuration), Pair(steps, maxDuration))

    class LinearFunction(val a: Double, val b: Double) {
        fun evaluate(x: Double): Double {
            return a * x + b
        }

        override fun toString(): String {
            return "$a x + $b"
        }

        companion object {
            @JvmStatic
            fun givenPoints(
                p1: Pair<Double, Double>,
                p2: Pair<Double, Double>
            ): LinearFunction {
                val slope = (p2.second - p1.second) / (p2.first - p1.first)
                val offset = p1.second - slope * p1.first
                return LinearFunction(slope, offset)
            }
        }
    }

    fun calcBrightness(step: Int): Double {
        return brightnessFunction.evaluate(step.toDouble())
    }

    fun calcTemperature(step: Int): Double {
        return temperatureFunction.evaluate(step.toDouble())
    }

    fun calcDuration(step: Int): Double {
        return durationFunction.evaluate(step.toDouble())
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println(LinearFunction.givenPoints(Pair(0.0, 0.0), Pair(2.0, 1.0)))
        }
    }
}
