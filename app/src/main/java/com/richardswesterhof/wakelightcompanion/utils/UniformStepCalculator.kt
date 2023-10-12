package com.richardswesterhof.wakelightcompanion.utils

class UniformStepCalculator(
        private val steps: Double,
        private val minBright: Double,
        private val maxBright: Double,
        private val minTemp: Double,
        private val maxTemp: Double,
        private val minDuration: Double,
        private val maxDuration: Double
) {

    private lateinit var brightnessFunction: LinearFunction
    private lateinit var temperatureFunction: LinearFunction
    private lateinit var durationFunction: LinearFunction

    init {
        brightnessFunction =
                LinearFunction.givenPoints(Pair(0.0, minBright), Pair(steps, maxBright))
        temperatureFunction = LinearFunction.givenPoints(Pair(0.0, minTemp), Pair(steps, maxTemp))
        durationFunction =
                LinearFunction.givenPoints(Pair(0.0, minDuration), Pair(steps, maxDuration))
    }

    class LinearFunction(val a: Double, val b: Double) {
        public fun evaluate(x: Double): Double {
            return a * x + b
        }

        override public fun toString(): String {
            return "$a x + $b"
        }

        companion object {
            @JvmStatic
            public fun givenPoints(
                    p1: Pair<Double, Double>,
                    p2: Pair<Double, Double>
            ): LinearFunction {
                val slope = (p2.second - p1.second) / (p2.first - p1.first)
                val offset = p1.second - slope * p1.first
                return LinearFunction(slope, offset)
            }
        }
    }

    companion object {
        @JvmStatic
        public fun main(args: Array<String>) {
            println(LinearFunction.givenPoints(Pair(0.0, 0.0), Pair(2.0, 1.0)))
        }
    }
}
