package com.sabin.emicalculator

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RatingPromptTest {
    private val day = 24L * 60 * 60 * 1000
    private val now = 1_000 * day

    @Test fun asksAfterHappyMomentWhenNeverAsked() {
        assertTrue(RatingPrompt.shouldAsk(now, null, 2, false, HappyMoment.LOAN_SAVED))
        assertTrue(RatingPrompt.shouldAsk(now, null, 5, false, HappyMoment.PAYMENT_MILESTONE))
    }

    @Test fun neverWithoutHappyMoment() {
        assertFalse(RatingPrompt.shouldAsk(now, null, 5, false, null))
    }

    @Test fun neverOnFirstLaunch() {
        assertFalse(RatingPrompt.shouldAsk(now, null, 1, false, HappyMoment.LOAN_SAVED))
        assertFalse(RatingPrompt.shouldAsk(now, null, 0, false, HappyMoment.LOAN_SAVED))
    }

    @Test fun neverAfterError() {
        assertFalse(RatingPrompt.shouldAsk(now, null, 5, true, HappyMoment.LOAN_SAVED))
    }

    @Test fun atMostOncePerSixtyDays() {
        assertFalse(RatingPrompt.shouldAsk(now, now - 59 * day, 5, false, HappyMoment.LOAN_SAVED))
        assertTrue(RatingPrompt.shouldAsk(now, now - 60 * day, 5, false, HappyMoment.LOAN_SAVED))
    }
}

class ReviewRequesterTest {
    private class MemStore(override var lastAskedMs: Long? = null, override var launches: Int = 2) : RatingStore

    private val activity = android.app.Activity()
    private val manager = com.google.android.play.core.review.testing.FakeReviewManager(android.content.ContextWrapper(null))

    @Test fun asksOnceThenBlocksWithinSixtyDays() {
        val store = MemStore()
        val r = ReviewRequester(manager, store)
        assertTrue(r.maybeAsk(activity, HappyMoment.LOAN_SAVED, false, 1000))
        assertFalse(r.maybeAsk(activity, HappyMoment.PAYMENT_MILESTONE, false, 2000))
        org.junit.Assert.assertEquals(1000L, store.lastAskedMs)
    }

    @Test fun doesNotAskOnFirstLaunchOrError() {
        val first = ReviewRequester(manager, MemStore(launches = 1))
        assertFalse(first.maybeAsk(activity, HappyMoment.LOAN_SAVED, false, 1000))
        val err = ReviewRequester(manager, MemStore())
        assertFalse(err.maybeAsk(activity, HappyMoment.LOAN_SAVED, true, 1000))
    }

    @Test fun recordLaunchIncrements() {
        val store = MemStore(launches = 0)
        ReviewRequester(manager, store).recordLaunch()
        org.junit.Assert.assertEquals(1, store.launches)
    }
}
