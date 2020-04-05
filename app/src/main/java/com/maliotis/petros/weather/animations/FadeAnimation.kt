package com.maliotis.petros.weather.animations

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import java.util.*

class FadeAnimation {

    private val mObjectAnimators = ArrayList<ObjectAnimator>()
    private val mSet = AnimatorSet()

    fun add(view: View?) {
        val objectAnimator = ObjectAnimator()
        objectAnimator.target = view
        objectAnimator.setProperty(View.ALPHA)
        objectAnimator.setFloatValues(0.0f, 1.0f)
        objectAnimator.duration = 1200
        mObjectAnimators.add(objectAnimator)
        addMove(view)
    }

    private fun addMove(view: View?) {
        val objectAnimator = ObjectAnimator()
        objectAnimator.target = view
        objectAnimator.setProperty(View.TRANSLATION_Y)
        objectAnimator.setFloatValues(30f, 0f)
        objectAnimator.duration = 1000
        mObjectAnimators.add(objectAnimator)
    }

    val animatorSet: AnimatorSet
        get() {
            for (objectAnimator in mObjectAnimators) {
                mSet.playSequentially(objectAnimator)
            }
            return mSet
        }
}