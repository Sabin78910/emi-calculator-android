package com.sabin.emicalculator

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory

/** Persistence for [RatingPrompt] state. */
interface RatingStore {
    var lastAskedMs: Long?
    var launches: Int
}

class PrefsRatingStore(context: Context) : RatingStore {
    private val prefs = context.getSharedPreferences(RatingPrompt.PREFS, Context.MODE_PRIVATE)
    override var lastAskedMs: Long?
        get() = if (prefs.contains(RatingPrompt.KEY_LAST_ASKED)) prefs.getLong(RatingPrompt.KEY_LAST_ASKED, 0) else null
        set(v) { prefs.edit().apply { if (v == null) remove(RatingPrompt.KEY_LAST_ASKED) else putLong(RatingPrompt.KEY_LAST_ASKED, v) }.apply() }
    override var launches: Int
        get() = prefs.getInt(RatingPrompt.KEY_LAUNCHES, 0)
        set(v) { prefs.edit().putInt(RatingPrompt.KEY_LAUNCHES, v).apply() }
}

/** Applies [RatingPrompt] rules and launches the Play In-App Review flow; failures are silent. */
class ReviewRequester(private val manager: ReviewManager, private val store: RatingStore) {
    constructor(context: Context) : this(ReviewManagerFactory.create(context), PrefsRatingStore(context))

    fun recordLaunch() { store.launches += 1 }

    /** Returns true if a review flow was requested. */
    fun maybeAsk(activity: Activity, moment: HappyMoment?, hadError: Boolean, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (!RatingPrompt.shouldAsk(nowMs, store.lastAskedMs, store.launches, hadError, moment)) return false
        store.lastAskedMs = nowMs
        try {
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) manager.launchReviewFlow(activity, task.result)
            }
        } catch (_: RuntimeException) {
            // Review is best-effort; never surface failures to the user.
        }
        return true
    }
}
