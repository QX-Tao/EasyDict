package com.qxtao.easydict.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.AppBarLayout


class FixScrollingBehavior : AppBarLayout.ScrollingViewBehavior {
    private var appBarLayout: AppBarLayout? = null

    constructor() : super()
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        if (appBarLayout == null) {
            appBarLayout = dependency as AppBarLayout
        }
        val result = super.onDependentViewChanged(parent, child, dependency)
        val bottomPadding = calculateBottomPadding(appBarLayout)
        val paddingChanged = bottomPadding != child.paddingBottom
        if (paddingChanged) {
            child.setPadding(
                child.paddingLeft,
                child.paddingTop,
                child.paddingRight,
                bottomPadding
            )
            child.requestLayout()
        }
        return paddingChanged || result
    }

    private fun calculateBottomPadding(dependency: AppBarLayout?): Int {
        val totalScrollRange = dependency!!.totalScrollRange
        return totalScrollRange + dependency.top
    }
}
