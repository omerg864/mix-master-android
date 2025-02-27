package com.example.mixmaster.utils

import com.google.firebase.Timestamp
import java.util.Date

val Long.toFirebaseTimestamp: Timestamp
    get() {
        val date = Date(this)
        return Timestamp(date)
    }