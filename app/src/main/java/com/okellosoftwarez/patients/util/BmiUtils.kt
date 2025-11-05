package com.okellosoftwarez.patients.util

object BmiUtils {

    fun calculateBmi(weightKg: Double, heightCm: Double): Double {
        if (heightCm <= 0) return 0.0
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        return String.format("%.1f", bmi).toDouble()
    }


    fun getBmiStatus(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal"
            else -> "Overweight"
        }
    }

}
