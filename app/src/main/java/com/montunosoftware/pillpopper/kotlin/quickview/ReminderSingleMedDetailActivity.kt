package com.montunosoftware.pillpopper.kotlin.quickview

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.ReminderSinglemedDetailActivityBinding
import com.montunosoftware.pillpopper.android.ReminderContainerActivity
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.controller.FrontController
import com.montunosoftware.pillpopper.model.ArchiveDetailDrug
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import java.text.SimpleDateFormat
import java.util.*


class ReminderSingleMedDetailActivity : ReminderContainerActivity() {
    private lateinit var binding: ReminderSinglemedDetailActivityBinding
    private var mActionPill = PillpopperConstants.ACTION_CREATE_PILL
    private var mPillId: String? = null
    private var drugGuid: String? = null
    private var launchMode: String? = null
    private lateinit var drug: Drug


    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        binding = DataBindingUtil.setContentView(this, R.layout.reminder_singlemed_detail_activity)
        val intent = intent
        if (intent != null) {
            mPillId = intent.getStringExtra("pill_id")
            drugGuid = intent.getStringExtra("drug_guid")
            launchMode = intent.getStringExtra("launchMode")
        }
        mActionPill = PillpopperConstants.ACTION_CREATE_PILL
        initValues()
        setupView()
        setListeners()

    }

    private fun setupView() {
        binding.reminderScreenToolbar.reminderOverDueDateTime.text = SimpleDateFormat("MMM dd").format(Date(PillpopperRunTime.getInstance().headerTime)) + ", " + Util.getTime(PillpopperRunTime.getInstance().headerTime)
        binding.reminderScreenToolbar.signInBtn.visibility = if (AppConstants.isByPassLogin()) View.VISIBLE else View.GONE
        drug = FrontController.getInstance(_thisActivity).getDrugByPillId(mPillId)
        binding.childDrug = drug
        binding.patientName.text = PillpopperRunTime.getInstance().proxyName
        binding.drugNameText.text = drug.firstName;
        if (!Util.isEmptyString(drug.dose)) {
            binding.doseStrengthAndBrandnameText.visibility = View.VISIBLE
            if (Util.isEmptyString(drug.genericName)) {
                binding.doseStrengthAndBrandnameText.text = drug.dose
            } else {
                binding.doseStrengthAndBrandnameText.text = drug.genericName+ "  " + drug.dose
            }
        } else {
            if (!Util.isEmptyString(drug.genericName)) {
                binding.doseStrengthAndBrandnameText.visibility = View.VISIBLE
                binding.doseStrengthAndBrandnameText.text = drug.genericName
            } else {
                binding.doseStrengthAndBrandnameText.visibility = View.GONE
            }
        }
        if(!Util.isEmptyString(drug.notes)){
            binding.personalNotesText.text = drug.notes
        }
        ImageUILoaderManager.getInstance().loadDrugImage(this, drug.imageGuid, mPillId, binding.pillImage, this.getDrawable(R.drawable.pill_default))
    }

    private fun initValues() {
        val mFontMedium = ActivationUtil.setFontStyle(_thisActivity, AppConstants.FONT_ROBOTO_MEDIUM)
        val mFontRegular = ActivationUtil.setFontStyle(_thisActivity, AppConstants.FONT_ROBOTO_REGULAR)
        val mFontBold = ActivationUtil.setFontStyle(_thisActivity, AppConstants.FONT_ROBOTO_BOLD)

        binding.reminderScreenToolbar.medicationRemainder.typeface = mFontRegular
        binding.reminderScreenToolbar.signInBtn.typeface = mFontRegular
        binding.reminderScreenToolbar.reminderOverDueDateTime.typeface = mFontBold
        binding.robotoMedium = mFontMedium
        binding.robotoBold = mFontBold
        binding.robotoRegular = mFontRegular

        binding.reminderScreenToolbar.medicationRemainder.text =
                if (launchMode.equals(resources.getString(R.string.late_reminder), ignoreCase = true)) resources.getString(R.string.late_reminders)
                else resources.getString(R.string.med_reminder_notification_title)
        binding.reminderScreenToolbar.backNavigation.visibility = View.VISIBLE
        binding.reminderScreenToolbar.backNavigation.setOnClickListener { finish() }
        binding.reminderScreenToolbar.signInBtn.setOnClickListener { ActivationController.getInstance().performSignoff(_thisActivity) }
    }

    private fun setListeners() {
        binding.pillImage.setOnClickListener{
            val expandImageIntent = Intent(this, EnlargeImageActivity::class.java)
            expandImageIntent.putExtra("pillId", mPillId)
            expandImageIntent.putExtra("imageId", drugGuid)
            expandImageIntent.putExtra("isFromReminderDrugDetailActivity", true)
            startActivity(expandImageIntent)
        }
    }

}



