package com.sabin.emicalculator

data class OnboardingPage(val title: String, val benefit: String, val isAction: Boolean = false)

object Onboarding {
    const val PREFS = "emi_onboarding"
    const val KEY_DONE = "done"

    val PAGES = listOf(
        OnboardingPage("Know your EMI", "See your monthly payment in seconds."),
        OnboardingPage("Plan smarter", "Compare loans and see how prepaying saves interest."),
        OnboardingPage("Try it now", "Move the sliders to calculate your first EMI.", isAction = true),
    )

    /** [done] is the stored completion flag; absent or false means first run. */
    fun shouldShow(done: Boolean?): Boolean = done != true

    /** Skip is hidden on the last page, which already leads into the calculator. */
    fun showSkip(page: Int): Boolean = page < PAGES.lastIndex
}
