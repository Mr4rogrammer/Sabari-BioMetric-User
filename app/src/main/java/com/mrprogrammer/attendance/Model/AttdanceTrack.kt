package com.mrprogrammer.attendance.Model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class AttdanceTrack (
    @PrimaryKey
    var day:Long = 0,

):RealmObject()