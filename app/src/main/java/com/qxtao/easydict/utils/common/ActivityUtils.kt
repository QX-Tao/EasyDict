package com.qxtao.easydict.utils.common

import android.app.Activity
import com.qxtao.easydict.application.EasyDictApplication

object ActivityUtils {
    fun finishAllActivitiesExceptNewest() {
        val activities = mutableListOf<Activity>()
        for (activity in EasyDictApplication.instance.activities) {
            activities.add(activity)
        }
        val newestActivity = activities.lastOrNull()
        if (newestActivity != null) {
            for (activity in activities) {
                if (activity != newestActivity) {
                    activity.finish()
                }
            }
        }
    }

    fun restartAllActivities() {
        for (activity in EasyDictApplication.instance.activities){
            activity.recreate()
        }
    }

}