package com.qxtao.easydict.utils.common

import com.qxtao.easydict.application.EasyDictApplication

object ActivityUtils {
    fun finishAllActivitiesExceptNewest() {
        val activities = EasyDictApplication.instance.activities
        val newestActivity = activities.lastOrNull()
        activities.forEach { activity ->
            if (activity != newestActivity) {
                activity.finish()
            }
        }
    }

    fun restartAllActivities() {
        val activities = EasyDictApplication.instance.activities
        activities.forEach { activity ->
            activity.recreate()
        }
    }

}