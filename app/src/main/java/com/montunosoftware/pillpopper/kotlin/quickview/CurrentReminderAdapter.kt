package com.montunosoftware.pillpopper.kotlin.quickview

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.EmptyLayoutBinding
import com.montunosoftware.mymeds.databinding.ProxyReminderChildListRedesignBinding
import com.montunosoftware.mymeds.databinding.ProxyReminderHeaderRedesignBinding
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import java.util.*


public class CurrentReminderAdapter(var mContext: Context, val mOverdueHashMap: LinkedHashMap<String, MutableList<Drug>>, val currentReminderViewModel: CurrentReminderActivityViewModel) : BaseExpandableListAdapter() {
    var mUserIds: MutableList<String> = ArrayList()
    var mDueHashMap = LinkedHashMap<String, List<Drug>>()
    private lateinit var childBinding: ProxyReminderChildListRedesignBinding
    private lateinit var parentBinding: ProxyReminderHeaderRedesignBinding
    var mFontMedium: Typeface = ActivationUtil.setFontStyle(mContext, AppConstants.FONT_ROBOTO_MEDIUM)
    var mFontRegular: Typeface = ActivationUtil.setFontStyle(mContext, AppConstants.FONT_ROBOTO_REGULAR)


    override fun getGroup(groupPosition: Int): Any? {
        return mUserIds[groupPosition]
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var headerView: View?

        if(getChildrenCount(groupPosition) != 0) {
            parentBinding = DataBindingUtil.inflate(LayoutInflater.from(parent?.context), R.layout.proxy_reminder_header_redesign, parent, false)
            headerView = parentBinding.root
            parentBinding.robotoBold = ActivationUtil.setFontStyle(parent?.context, AppConstants.FONT_ROBOTO_BOLD)
            parentBinding.tvProxyName.text = currentReminderViewModel.mFrontController.getUserFirstNameByUserId(mUserIds[groupPosition])
            parentBinding.carrotImage.visibility = if(checkForNonActedDrugs(groupPosition)) View.VISIBLE else View.INVISIBLE
            headerView.tag = parentBinding.root
            val list = parent as ExpandableListView
            list.expandGroup(groupPosition)
        } else{
            // For gradient decor for the expandable list. Making the last cell in the list display above the buttons.
            val view : EmptyLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent?.context), R.layout.empty_layout, parent, false)
            headerView = view.root
            headerView.isClickable = false
            headerView.setOnClickListener(null)
        }
        return headerView.rootView
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {


        childBinding = DataBindingUtil.inflate(LayoutInflater.from(parent?.context), R.layout.proxy_reminder_child_list_redesign, parent, false)
       var childView = childBinding.root

        childView.tag = childBinding.root

        childBinding.robotoMedium = mFontMedium
        childBinding.robotoRegular = mFontRegular

        val childDrug = getChild(groupPosition, childPosition) as Drug
        val strength = StringBuilder()
        val name = StringBuilder()
        if (childDrug.name.contains("(") || childDrug.name.contains(")")) {
            val index: Int = childDrug.name.indexOf("(")
            val displayName: String = childDrug.name.substring(0, index)
            name.append(displayName)
        } else {
            name.append(childDrug.name)
        }
        if (!Util.isEmptyString(childDrug.genericName)) {
            strength.append(childDrug.genericName)
        }
        if (!Util.isEmptyString(childDrug.dose)) {
            strength.append(childDrug.dose)
        }

        if (!Util.isEmptyString(strength.toString())) {
            childBinding.tvDrugDose.visibility = View.VISIBLE
            childBinding.tvDrugDose.text = strength.toString()
        } else{
            childBinding.tvDrugDose.visibility = View.GONE
        }

        childBinding.currentReminderViewModel = currentReminderViewModel
        childBinding.childDrug = childDrug

        childBinding.tvDrugName.text = name.toString()
        ImageUILoaderManager.getInstance().loadDrugImage(mContext, childDrug.imageGuid, childDrug.guid, childBinding.imgDrugImg, Util.getDrawableWrapper(parent?.context, R.drawable.pill_default))

        // causes blink issue, if written in xml file
        childBinding.personalNotes.visibility =if (Util.isEmptyString(childDrug.notes)) View.GONE else View.VISIBLE

        when (childDrug.getmAction()) {

            PillpopperConstants.TAKEN, PillpopperConstants.TAKE_EARLIER -> addActionImage(parent?.context, R.drawable.ic_taken, mContext.getString(R.string.taken))
            PillpopperConstants.SKIPPED -> addActionImage(parent?.context, R.drawable.ic_skipped, mContext.getString(R.string.skipped))
            PillpopperConstants.TAKE_LATER -> addActionImage(parent?.context, R.drawable.ic_postpone, mContext.getString(R.string.remind_later))
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
        return childView.rootView
    }

    private fun addActionImage(context: Context?, actionImage: Int, action: String) {
        childBinding.actionImage.visibility = View.VISIBLE
        childBinding.actionImage.contentDescription = action +", "+ context?.getString(R.string.content_description_current_and_Late_Reminder_image)
//        childBinding.imgDrugImg.contentDescription = action
        childBinding.actionImage.setImageDrawable(context, context?.let { ContextCompat.getDrawable(it,actionImage) })
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return mDueHashMap[mUserIds[groupPosition]]!!.size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return mDueHashMap[mUserIds[groupPosition]]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return mUserIds.size
    }

    init {
        mDueHashMap.putAll(mOverdueHashMap)
        mUserIds.addAll(mDueHashMap.keys)
    }

    private fun checkForNonActedDrugs(groupPosition: Int) : Boolean{
        var isAvailable = true
        val drugsList: List<Drug>? = mDueHashMap[mUserIds[groupPosition]]
        if(null != drugsList && drugsList.isNotEmpty()){
            for(drug in drugsList){
                if (drug.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                    isAvailable = true
                    break
                } else isAvailable = false
            }
        }
        return isAvailable
    }
}

