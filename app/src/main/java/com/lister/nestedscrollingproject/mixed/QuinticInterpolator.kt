
package com.lister.nestedscrollingproject.mixed

import android.view.animation.Interpolator

class QuinticInterpolator: Interpolator {
    override fun getInterpolation(input: Float): Float {
        val t = input - 1.0f
        return t * t * t * t * t + 1.0f
    }
}
