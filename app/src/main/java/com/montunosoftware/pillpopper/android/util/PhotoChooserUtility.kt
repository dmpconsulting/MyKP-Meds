package com.montunosoftware.pillpopper.android.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import com.montunosoftware.pillpopper.android.PillpopperActivity
import com.montunosoftware.pillpopper.android.view.AttachmentFileProvider
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import java.io.File
import java.io.IOException
import kotlin.jvm.Throws

class PhotoChooserUtility {
    companion object {
        lateinit var contentUri: Uri
        @JvmStatic lateinit var photoFile: File
        private const val requestChoosePhoto = 19

        @JvmStatic
        fun takePhoto(mContext: PillpopperActivity, isCamera: Boolean) {
            val packageManager = mContext.packageManager
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                return
            }
            val takePictureIntent: Intent
            if (!isCamera) {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                takePictureIntent = Intent(Intent.ACTION_CHOOSER)
                takePictureIntent.putExtra(Intent.EXTRA_INTENT, intent)
                takePictureIntent.putExtra(Intent.EXTRA_TITLE, "Select Image")
            } else {
                takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(mContext.packageManager) != null) {
                    val packageName = takePictureIntent.resolveActivity(packageManager).packageName
                    try {
                        AppConstants.photoFile = createImageFile(getCapturedImage(mContext))
                        val code = System.currentTimeMillis().toString() + ""
                        RunTimeData.getInstance().cpCode = code
                        if (AppConstants.photoFile != null) {
                            AppConstants.contentUri = AttachmentFileProvider.getUriForFile(mContext, getFileAuthority(mContext), AppConstants.photoFile)
                        }
                    } catch (e: java.lang.IllegalArgumentException) {
                        return
                    } catch (e: IOException) {
                        return
                    }
                    if (AppConstants.contentUri != null) {
                        mContext.grantUriPermission(packageName, AppConstants.contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, AppConstants.contentUri)
                        takePictureIntent.putExtra("return-data", true)
                    }
                }
            }
            mContext.startActivityForResult(takePictureIntent, requestChoosePhoto)
        }

        /**
         * Get File authority
         * @param context application context
         * @return authority string
         */
        fun getFileAuthority(context: Context): String {
            return """${context.packageName}.msgs.fileprovider"""
        }

        @Throws(IOException::class, IllegalArgumentException::class)
        fun createImageFile(fileDir: File?): File? {
            var imageFile: File? = null

            // File.createTempFile will add a unique number as a suffix to imageFileName.
            imageFile = File.createTempFile("Photo_", ".jpg", fileDir)
            return imageFile
        }

        /**
         *
         * @param context application context
         * @return file
         */
        fun getCapturedImage(context: Context) : File{
            val cacheFolder = context.filesDir.path
            val file = File(cacheFolder)
            if (!file.exists()) {
                file.mkdir()
            }
            val attachmentFolder= File(file.absolutePath + File.separator + "capturedImages")
            if (!attachmentFolder.exists()) {
                attachmentFolder.mkdir()
            }
            return attachmentFolder
        }
    }
}