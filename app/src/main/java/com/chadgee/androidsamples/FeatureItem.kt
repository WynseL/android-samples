package com.chadgee.androidsamples

import android.content.Intent

data class FeatureItem(
        val title: String,
        val description: String,
        var clazzToIntent: Class<*>
)
