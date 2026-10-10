package com.sabin.emicalculator

data class OnboardingPage(@androidx.annotation.StringRes val title: Int, @androidx.annotation.StringRes val benefit: Int, val isAction: Boolean = false)

object Onboarding {
    const val PREFS = "emi_onboarding"
    const val KEY_DONE = "done"

    val PAGES = listOf(
        OnboardingPage(R.string.onboarding_title_1, R.string.onboarding_body_1),
        OnboardingPage(R.string.onboarding_title_2, R.string.onboarding_body_2),
        OnboardingPage(R.string.onboarding_title_3, R.string.onboarding_body_3, isAction = true),
    )

    /** [done] is the stored completion flag; absent or false means first run. */
    fun shouldShow(done: Boolean?): Boolean = done != true

    /** Skip is hidden on the last page, which already leads into the calculator. */
    fun showSkip(page: Int): Boolean = page < PAGES.lastIndex
}
