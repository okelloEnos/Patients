package com.okellosoftwarez.patients.util

/**
 * BmiUtils
 *
 * Provides helper methods for:
 *  - Calculating Body Mass Index (BMI)
 *  - Categorizing BMI status into Underweight / Normal / Overweight
 */
object BmiUtils {

    /**
     * Calculate BMI using metric formula:
     *     BMI = weight(kg) / [height(m)]²
     *
     * @param weightKg Weight in kilograms
     * @param heightCm Height in centimeters
     * @return BMI rounded to 1 decimal place
     */
    fun calculateBmi(weightKg: Double, heightCm: Double): Double {
        if (heightCm <= 0) return 0.0
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        return String.format("%.1f", bmi).toDouble()
    }

    /**
     * Get BMI status based on the standard WHO classification:
     *
     *  - Underweight: BMI < 18.5
     *  - Normal: 18.5 <= BMI < 25
     *  - Overweight: BMI >= 25
     *
     * @param bmi BMI value
     * @return status string ("Underweight", "Normal", "Overweight")
     */
    fun getBmiStatus(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal"
            else -> "Overweight"
        }
    }

    /**
     * Returns a short summary combining BMI value and status.
     * Example: "22.8 (Normal)"
     */
    fun formatBmiSummary(bmi: Double): String {
        val status = getBmiStatus(bmi)
        return "${String.format("%.1f", bmi)} ($status)"
    }
}
