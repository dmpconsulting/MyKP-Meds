package com.montunosoftware.pillpopper.kotlin.lateremider

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.model.Drug

class LateReminderDetailViewModel : ViewModel(){
    var onMedSkippedClicked = MutableLiveData<Pair<Drug, Int>>()
    var onMedTakenClicked = MutableLiveData<Pair<Drug, Int>>()
    var onItemClicked = MutableLiveData<Drug>()

    var onImageClicked = MutableLiveData<Drug>()
    fun setBindingData(drug: Drug) : Drug{
        if (null != drug.name) {
            var name = drug.name
            if (name.contains("(") || name.contains(")")) {
                val indexofBrace = name.indexOf('(')
                name = name.substring(0, indexofBrace)
            }
        }
        if (!Util.isEmptyString(drug.dose)) {
            if (!Util.isEmptyString(drug.genericName)) {
                drug.dose = drug.genericName + "  " + drug.dose
            }
        } else {
            if (!Util.isEmptyString(drug.genericName)) {
                drug.dose = drug.genericName
            }
        }
        return drug
    }

    fun onMedSkippedClicked(drugs: Drug, position: Int) {
        val drugPosition = Pair(drugs, position)
        onMedSkippedClicked.postValue(drugPosition)
    }

    fun onMedTakenClicked(drugs: Drug, position: Int){
        val drugPosition = Pair(drugs, position)
        onMedTakenClicked.postValue(drugPosition)
    }

    fun onItemClick(drug : Drug){
        onItemClicked.postValue(drug)
    }

    fun onImageClicked(drug: Drug){
        onImageClicked.postValue(drug)
    }
}