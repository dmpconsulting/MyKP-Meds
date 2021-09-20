package com.montunosoftware.pillpopper.kotlin.lateremider

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.montunosoftware.pillpopper.android.util.PillpopperLog
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.model.Drug
import java.util.*

class LateReminderViewModel : ViewModel()
{
    var mPassedRemindersUserId: LinkedHashMap<String, LinkedHashMap<Long, MutableList<Drug>>>? = null
    val masterHashMap = LinkedHashMap<String, LinkedHashMap<Long, MutableList<Drug>>>()
    var insertPastReminder = MutableLiveData<LinkedHashMap<String, LinkedHashMap<Long, MutableList<Drug>>>>()
    var onDrugImageClicked = MutableLiveData<Drug>()
    var mRemovalTimeList: MutableList<Long> = ArrayList()

    fun createMasterHashMap(pendingPassedReminders: String?, mPassedRemindersHashMapByUserId: LinkedHashMap<String, LinkedHashMap<Long, MutableList<Drug>>>) {
        mPassedRemindersUserId = mPassedRemindersHashMapByUserId

        if (null != pendingPassedReminders && "LaunchPendingPassedReminders".equals(pendingPassedReminders, ignoreCase = true)) {
            PillpopperLog.say("Past Reminder - Previous past reminders are present needs to be shown with that")
            if (null != mPassedRemindersUserId && mPassedRemindersUserId!!.isNotEmpty()) {
                masterHashMap.putAll(mPassedRemindersUserId!!)
            }
        } else {
            PillpopperLog.say("Past Reminder - No Previous past reminders are present needs to be shown with regular past reminders")
            if (null != this.mPassedRemindersUserId) {
                masterHashMap.putAll(mPassedRemindersUserId!!)
            }
            insertPastReminder.postValue(masterHashMap)

        }
    }

    fun getName(childDrug: Drug) : String {
        val name = StringBuilder()
        if (childDrug.name.contains("(") || childDrug.name.contains(")")) {
            val index = childDrug.name.indexOf("(")
            name.append(childDrug.name.substring(0, index))
        } else {
            name.append(childDrug.name)
        }

        val strength = StringBuilder()
        if (!Util.isEmptyString(childDrug.genericName)) {
            strength.append(childDrug.genericName)
        }
        if (!Util.isEmptyString(childDrug.dose)) {
            strength.append(childDrug.dose)
        }
        return "$name~$strength"
    }

    fun onImageClicked(drug: Drug){
        onDrugImageClicked.postValue(drug)
    }

}