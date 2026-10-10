package com.sabin.emicalculator

/** Positive moments after which asking for a rating is acceptable. */
enum class HappyMoment { LOAN_SAVED, PAYMENT_MILESTONE }

/** Pure trigger rules for the Play Store in-app review prompt. */
object RatingPrompt {
    const val PREFS = "emi_rating"
    const val KEY_LAST_ASKED = "last_asked"
    const val KEY_LAUNCHES = "launches"
    const val MIN_INTERVAL_MS = 60L * 24 * 60 * 60 * 1000

    /**
     * [lastAskedMs] is null if never asked. [launches] counts app opens including this one; the first
     * launch never asks. [hadError] is true when the current screen shows an error.
     */
    fun shouldAsk(nowMs: Long, lastAskedMs: Long?, launches: Int, hadError: Boolean, moment: HappyMoment?): Boolean =
        moment != null && !hadError && launches > 1 &&
            (lastAskedMs == null || nowMs - lastAskedMs >= MIN_INTERVAL_MS)
}
