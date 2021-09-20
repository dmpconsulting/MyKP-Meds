package com.montunosoftware.pillpopper.kotlin.quickview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.controller.FrontController
import com.montunosoftware.pillpopper.model.Drug
import org.kp.tpmg.mykpmeds.activation.AppConstants
import java.util.*
import kotlin.collections.ArrayList

class CurrentReminderActivityViewModel : ViewModel(){
    var finalDrugs: ArrayList<Drug> = ArrayList()
    lateinit var mFrontController: FrontController
    lateinit var listOfDrugsToBeTaken: MutableList<Drug>
    var isCurrentReminderRefreshRequired = MutableLiveData<Boolean>()
    var isByPassLogin = MutableLiveData<Boolean>()
    val mOverdueHashMap = LinkedHashMap<String, MutableList<Drug>>()
    var mSingleTimeHashMap = LinkedHashMap<String, MutableList<Drug>>()
    var onDrugImageClicked = MutableLiveData<Drug>()
    var onPersonalNotesClicked = MutableLiveData<Drug>()
    private lateinit var temp: MutableList<Drug>

    fun isCurrentReminderRefreshRequired() {
        var needToLogin = false
        if (PillpopperConstants.isCurrentReminderRefreshRequired() && getDrugsCountWithNoActionTaken() == 0) {
            needToLogin = true
//            isByPassLogin.postValue(AppConstants.isByPassLogin())
        }
        if (!needToLogin) {
            isCurrentReminderRefreshRequired.postValue(PillpopperConstants.isCurrentReminderRefreshRequired())
        }
    }

    fun getDrugsCountWithNoActionTaken(): Int {
        finalDrugs = ArrayList()
        //here correct form
        createFinalDrugList()
        return finalDrugs.size
    }
    fun createFinalDrugList() {
        if (listOfDrugsToBeTaken.isNotEmpty()) {
            for (d in listOfDrugsToBeTaken) {
                //If Not acted upon this drug then add it to final drugs list
                if (null != d.guid && d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                    finalDrugs.add(d)
                }
            }
        }
    }
    fun isActionTakenDrugAvailable():Boolean {
        if (listOfDrugsToBeTaken.isNotEmpty()) {
            for (d in listOfDrugsToBeTaken) {
                if (null != d.guid && d.getmAction() != PillpopperConstants.NO_ACTION_TAKEN) {
                    return true
                }
            }
        }
        return false
    }
    fun getPrimaryUserData(userId: String, mDrugs: List<Drug>) {
        temp = java.util.ArrayList()
        for (d in mDrugs) {
            if (userId == d.userID) {
                temp.add(d)
            }
        }
        if (temp.isNotEmpty()) {
            mOverdueHashMap[userId] = temp
        }
    }
    fun buildProxyUsersData(userId: String, mDrugs: List<Drug>) {
        temp = java.util.ArrayList()
        for (d in mDrugs) {
            if (userId == d.userID) {
                temp.add(d)
            }
        }
        if (temp.isNotEmpty()) {
            mOverdueHashMap[userId] = temp
        }
    }

    fun onImgClicked(drug : Drug){
        onDrugImageClicked.postValue(drug)
    }
}