package com.montunosoftware.pillpopper.kotlin.lateremider

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import androidx.databinding.DataBindingUtil
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.EmptyLayoutBinding
import com.montunosoftware.mymeds.databinding.PassedReminderChildItemBinding
import com.montunosoftware.mymeds.databinding.PassedReminderHeaderRedesignBinding
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import java.text.SimpleDateFormat
import java.util.*

class LateReminderAdapter(var mListDataChild: HashMap<Long, MutableList<Drug>>, var mGroupActionDrugTimes: MutableList<Long>,var lateReminderViewModel: LateReminderViewModel) :  BaseExpandableListAdapter(){
    private lateinit var childBinding: PassedReminderChildItemBinding
    private lateinit var parentBinding: PassedReminderHeaderRedesignBinding

    override fun getGroup(groupPosition: Int): Any {
        return mGroupActionDrugTimes.size
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
       return true
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var headerView: View?
        val actionTakenTime = lateReminderViewModel.mRemovalTimeList
        parentBinding = DataBindingUtil.inflate(LayoutInflater.from(parent?.context), R.layout.passed_reminder_header_redesign,parent,false)
        headerView = parentBinding.root
        parentBinding.robotoBold = ActivationUtil.setFontStyle(parent?.context, AppConstants.FONT_ROBOTO_BOLD)
        if(mListDataChild[mGroupActionDrugTimes[groupPosition]]?.get(0)?.name != null) {
                for(y in actionTakenTime.indices){
                    if(actionTakenTime[y] == mGroupActionDrugTimes[groupPosition]){
                        parentBinding.imgArrow.visibility = View.GONE
                    }
                }

            parentBinding.tvReminderTime.text = Util.getTime(mGroupActionDrugTimes[groupPosition])
            val time = mGroupActionDrugTimes[groupPosition]
            val sdf = SimpleDateFormat("d/MMM/yyyy")
            val compareDate = sdf.format(Date(time))
            val mGroupDate = sdf.parse(compareDate)
            parentBinding.tvReminderDay.text = if (mGroupDate.before(sdf.parse(sdf.format(Calendar.getInstance().timeInMillis))))
                parent?.context?.getString(R.string._yesterday) else parent?.context?.getString(R.string._today)
            val list = parent as ExpandableListView
            list.expandGroup(groupPosition)
        }
        else{
            // For gradient decor for the expandable list. Making the last cell in the list display above the buttons.
            val view : EmptyLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent?.context), R.layout.empty_layout, parent, false)
            headerView = view.root
            headerView.isClickable = false
            headerView.setOnClickListener(null)
        }
        return headerView.rootView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return mListDataChild[mGroupActionDrugTimes[groupPosition]]?.size!!
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return mListDataChild[mGroupActionDrugTimes[groupPosition]]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        childBinding = DataBindingUtil.inflate(LayoutInflater.from(parent?.context),R.layout.passed_reminder_child_item,parent,false)
       var childView = childBinding.root
        childBinding.robotoMedium = ActivationUtil.setFontStyle(parent?.context, AppConstants.FONT_ROBOTO_MEDIUM)
        childBinding.robotoRegular = ActivationUtil.setFontStyle(parent?.context, AppConstants.FONT_ROBOTO_REGULAR)

        val childDrug = getChild(groupPosition, childPosition) as Drug

        childBinding.childDrug = childDrug
        childBinding.lateReminderViewModel = lateReminderViewModel

        if(null != childDrug.name) {
            childBinding.imgDrugImg.visibility = View.VISIBLE
            childBinding.tvDrugName.text = childDrug.firstName

            // causes blink issue, if written in xml file
            childBinding.personalNotes.visibility = if (Util.isEmptyString(childDrug.notes)) View.GONE else View.VISIBLE

            if (!Util.isEmptyString(childDrug.dose)) {
                childBinding.tvDrugDose.visibility = View.VISIBLE
                if (!Util.isEmptyString(childDrug.genericName)) {
                    childBinding.tvDrugDose.text = childDrug.genericName .plus(" ").plus(childDrug.dose)
                } else {
                    childBinding.tvDrugDose.text = childDrug.dose
                }
            } else {
                if (!Util.isEmptyString(childDrug.genericName)) {
                    childBinding.tvDrugDose.visibility = View.VISIBLE
                    childBinding.tvDrugDose.text = childDrug.genericName
                } else {
                    childBinding.tvDrugDose.visibility = View.GONE
                }
            }
            ImageUILoaderManager.getInstance().loadDrugImage(parent?.context ,childDrug.imageGuid, childDrug.guid, childBinding.imgDrugImg, Util.getDrawableWrapper(parent?.context, R.drawable.pill_default))
            when (childDrug.getmAction()) {

                PillpopperConstants.TAKEN -> parent?.context?.getString(R.string.taken)?.let { setVisibility(R.drawable.ic_taken, parent.context, it) }
                PillpopperConstants.SKIPPED -> parent?.context?.getString(R.string.skipped)?.let { setVisibility(R.drawable.ic_skipped, parent.context, it) }
                else -> {
                    ImageUILoaderManager.getInstance().loadDrugImage(parent?.context, childDrug.imageGuid, childDrug.guid, childBinding.imgDrugImg, Util.getDrawableWrapper(parent?.context, R.drawable.pill_default))
                    if (null != childDrug.imageGuid) {
                        childBinding.imgDrugImg.contentDescription = parent?.context?.getString(R.string.content_description_current_and_Late_Reminder_image)
                    } else {
                        childBinding.imgDrugImg.contentDescription = parent?.context?.getString(R.string.tap_to_enlarge_default_pill_image)
                    }
                    childBinding.actionImage.visibility = View.GONE
                }
            }
            childBinding.actionImage.setOnClickListener {
                EnlargeImageActivity.expandPillImage(parent?.context, childDrug.guid, childDrug.imageGuid)
            }
        }
        else{
            childBinding.imgDrugImg.visibility = View.GONE
            childBinding.actionImage.visibility = View.GONE
        }
        return childView.rootView
    }

    private fun setVisibility(icon: Int, context: Context?, action :String) {
        childBinding.actionImage.visibility = View.VISIBLE
        childBinding.actionImage.contentDescription = action +", "+ context?.getString(R.string.content_description_current_and_Late_Reminder_image)
        childBinding.actionImage.setImageDrawable(context,context?.getDrawable(icon))
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return mGroupActionDrugTimes.size
    }
}