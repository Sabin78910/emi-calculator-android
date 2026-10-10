package com.sabin.emicalculator

data class SavedInputs(
    val principal: String,
    val rate: String,
    val tenure: String,
    val unit: TenureUnit,
    val fee: String = "0",
) {
    companion object {
        val DEFAULT = SavedInputs("500000", "12", "60", TenureUnit.MONTHS)

        /** Builds inputs from raw stored strings, falling back to defaults per field when missing or not a positive number. */
        fun parse(principal: String?, rate: String?, tenure: String?, unit: String?, fee: String? = null): SavedInputs =
            SavedInputs(
                principal.positiveOr(DEFAULT.principal),
                rate.positiveOr(DEFAULT.rate),
                tenure.positiveOr(DEFAULT.tenure),
                TenureUnit.values().firstOrNull { it.name == unit } ?: DEFAULT.unit,
                fee?.takeIf { ProcessingFee.parse(it) != null && it.isNotBlank() } ?: DEFAULT.fee,
            )

        private fun String?.positiveOr(default: String): String =
            if (this?.toDoubleOrNull()?.let { it.isFinite() && it > 0 } == true) this else default
    }
}
