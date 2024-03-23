package com.mrprogrammer.attendance

import android.app.Application
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mrprogrammer.mrshop.ObjectHolder.ObjectHolder
import info.mrprogrammer.admin_bio.Model.AttdanceModel
import io.realm.Realm
import io.realm.RealmConfiguration

class MainApplication:Application() {

    override fun onCreate() {
        super.onCreate()

        Realm.init(applicationContext)
        val DB = RealmConfiguration.Builder()
            .name("LocalRealmDb.realm")
            .schemaVersion(1)
            .allowWritesOnUiThread(true)
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(DB)
        getDataAndStoreLocally()
        getDataAndSpStoreLocally()
    }

    private fun getDataAndStoreLocally() {
        val db = FirebaseDatabase.getInstance().getReference("attdances").child("data")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(AttdanceModel::class.java)?.let { ObjectHolder.getInstance()?.setAttdanceModel(it) }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getDataAndSpStoreLocally() {
        val db = FirebaseDatabase.getInstance().getReference("attdances").child("spdata")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(AttdanceModel::class.java)?.let { ObjectHolder.getInstance()?.setAttdanceSpModel(it) }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}