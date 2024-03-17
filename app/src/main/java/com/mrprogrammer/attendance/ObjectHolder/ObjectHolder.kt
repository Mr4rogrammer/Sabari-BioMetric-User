package com.mrprogrammer.mrshop.ObjectHolder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mrprogrammer.Utils.Toast.MrToast
import info.mrprogrammer.admin_bio.Model.AttdanceModel

class ObjectHolder {
    private  var mrToast: MrToast? = null
    private  var imageUrl: String? = null
    private var attdanceModel: MutableLiveData<AttdanceModel> =  MutableLiveData()
    private  var attdanceSpModel: AttdanceModel? = null

    fun setImageUrl(image: String) {
        this.imageUrl = imageUrl
    }

    fun getImageUrl(): String? {
        return  this.imageUrl
    }

    fun getAttdanceModel(): LiveData<AttdanceModel>? {
        return attdanceModel
    }

    fun setAttdanceModel(data: AttdanceModel) {
        attdanceModel?.postValue(data)
    }

    fun getAttdanceSpModel(): AttdanceModel? {
        return attdanceSpModel
    }

    fun setAttdanceSpModel(data: AttdanceModel) {
        this.attdanceSpModel = data
    }

    companion object {
        private var instance:ObjectHolder? = null

        fun getInstance():ObjectHolder? {
            if(instance == null) {
                instance = ObjectHolder()
            }
            return instance
        }
    }


    fun MrToast():MrToast{
        if(mrToast == null){
            mrToast = com.mrprogrammer.Utils.Toast.MrToast()
        }
        return com.mrprogrammer.Utils.Toast.MrToast()
    }

}