package org.kp.tpmg.mykpmeds.activation.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.camera.CropImageIntentBuilder;
import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.StateListenerActivity;
import com.montunosoftware.pillpopper.android.util.PhotoChooserUtility;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.TouchImageView;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.service.images.loader.ImageLoaderUtil;
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.PermissionUtils;
import org.kp.tpmg.ttg.utils.MultiClickViewPreventHandler;

import java.lang.ref.WeakReference;

import static org.kp.tpmg.mykpmeds.activation.AppConstants.BROADCAST_REFRESH_FOR_MED_IMAGES;
import static org.kp.tpmg.mykpmeds.activation.AppConstants.BUNDLE_EXTRA_DRUG_TO_REFRESH;

public class EnlargeImageActivity extends StateListenerActivity implements View.OnClickListener {

    private String mImageId, mPillId;
    private TouchImageView imageView;
    private TextView mDisclaimerText;
    private Button mChangeImageBtn;
    private static final int REQ_CHOOSE_PHOTO = 19;
    private Drug _editDrug;
    private boolean isImageTaken;
    private Context mContext;
    private FrontController mFrontController;
    private static final int REQ_CROP_PHOTO = 20;
    private String cropImageGuid;
    private boolean isClickable = true;
    private String mActionPill = PillpopperConstants.ACTION_CREATE_PILL;
    private boolean isNewDrug;
    private boolean isDefaultImage;
    private ImageView defaultImageView;
    private TextView drugName;
    private boolean showChangeImageButton = true;


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(!RunTimeData.getInstance().isFromHistory()) {
            onBackPressed();
        }else{
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enlarge_image_activity);
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(_thisActivity, FireBaseConstants.ScreenEvent.SCREEN_EXPAND_IMAGE);
        mContext = _thisActivity;
        mFrontController = FrontController.getInstance(mContext);
        findViewById(R.id.close).setOnClickListener(this);
        findViewById(R.id.container).setOnClickListener(this);
        mDisclaimerText = findViewById(R.id.image_text);
        mDisclaimerText.setOnClickListener(this);
        mChangeImageBtn = findViewById(R.id.change_btn);
        mChangeImageBtn.setOnClickListener(this);
        drugName = findViewById(R.id.name_text);
        imageView = findViewById(R.id.enlargeImage);
        defaultImageView = findViewById(R.id.default_enlargeImage);
        if (null != getIntent()) {
            if (getIntent().getBooleanExtra("isFromReminderDrugDetailActivity", false)) {
                mChangeImageBtn.setVisibility(View.GONE);
                showChangeImageButton = false;
            }
            mPillId = getIntent().getStringExtra("pillId");
            mImageId = getIntent().getStringExtra("imageId");
            loadImage(mPillId,mImageId);
            _editDrug = mFrontController.getDrugByPillId(mPillId);
        }
        if(!Util.isEmptyString(_editDrug.getFirstName())) {
            drugName.setText(_editDrug.getFirstName());
        }else{
            _editDrug.setId(mPillId);
            if (!Util.isEmptyString(RunTimeData.getInstance().getMedName())) {
                drugName.setText(RunTimeData.getInstance().getMedName());
            } else {
                drugName.setText(!Util.isEmptyString(getIntent().getStringExtra("pillName"))
                        ? getIntent().getStringExtra("pillName")
                        : getResources().getString(R.string.txt_otc_name));
            }
        }
        RunTimeData.getInstance().setIsImageDeleted(false);
    }

    public static void expandPillImage(Context context, String pillId, String imageId) {
        Intent intent = new Intent(context, EnlargeImageActivity.class);
        intent.putExtra("pillId", pillId);
        intent.putExtra("imageId", imageId);
        context.startActivity(intent);
    }

    private void loadImage(String pillId,String imageGuid) {
        FrontController frontController = FrontController.getInstance(this);
        Drug drug = frontController.getDrugForImageLoad(pillId);
        if(RunTimeData.getInstance().isFromHistory() && showChangeImageButton) {
            mChangeImageBtn.setVisibility(null != drug && null != drug.getGuid() ? View.VISIBLE : View.GONE);
        }
        if(null != drug) {
            if (null != drug.getImageGuid()) {
                imageGuid = drug.getImageGuid();
            }
            if (!drug.isManaged() && imageGuid != null && !Util.isEmptyString(imageGuid)) {
                String encodeImage = frontController.getCustomImage(imageGuid);
                setDrugEncodedImage(encodeImage, drug);
            } else {
                String choiceType = drug.getPreferences().getPreference("defaultImageChoice");
                if (AppConstants.IMAGE_CHOICE_FDB.equalsIgnoreCase(choiceType)) {
                    String encodeImage = frontController.getFdbImageByPillId(pillId);
                    setDrugEncodedImage(encodeImage, drug);
                } else if (AppConstants.IMAGE_CHOICE_CUSTOM.equalsIgnoreCase(choiceType)) {
                    if (drug.getImageGuid() != null && !Util.isEmptyString(drug.getImageGuid())) {
                        String encodeImage = frontController.getCustomImage(drug.getImageGuid());
                        setDrugEncodedImage(encodeImage, drug);
                    } else {
                        saveDefaultPreferences(drug);
                    }
                } else {
                    imageView.setVisibility(View.GONE);
                    defaultImageView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setDrugEncodedImage(String encodeImage,Drug drug) {
        if (null != encodeImage) {
            imageView.setVisibility(View.VISIBLE);
            defaultImageView.setVisibility(View.GONE);
            new ImageLoaderTask(imageView).execute(encodeImage);
        } else {
            saveDefaultPreferences(drug);
        }
    }

    private void saveDefaultPreferences(Drug drug) {
        imageView.setVisibility(View.GONE);
        defaultImageView.setVisibility(View.VISIBLE);
        mImageId=null;
        drug.setImageGuid(null);
        drug.getPreferences().setPreference("defaultImageChoice", AppConstants.IMAGE_CHOICE_NO_IMAGE);
        FrontController.getInstance(this).updatePillImagePreferences(drug.getGuid(), AppConstants.IMAGE_CHOICE_NO_IMAGE, drug.getPreferences().getPreference("defaultServiceImageID"));
        FrontController.getInstance(mContext).addLogEntry(mContext, Util.prepareLogEntryForAction("EditPill", drug, mContext));
    }

    public static class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {

        private final WeakReference<ImageView> mImageViewWeakReference;

        public ImageLoaderTask(ImageView imageView) {
            this.mImageViewWeakReference = new WeakReference<>(imageView);

        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            byte[] decodedString = Base64.decode(strings[0], Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (null != mImageViewWeakReference && null != bitmap) {
                ImageView imageView = mImageViewWeakReference.get();
                if (null != imageView) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        hideStatusAndNavigationBar();
        if (null != _editDrug.getImageGuid()) {
            loadImage(_editDrug.getGuid(),_editDrug.getImageGuid());
        }
    }

    @Override
    public void onClick(View v) {
        MultiClickViewPreventHandler.preventMultiClick(v);
        if (v.getId() == R.id.close || v.getId() == R.id.container || v.getId() == R.id.image_text) {
             saveImageDetailsInRuntime();
            finish();
        } else if (v.getId() == R.id.change_btn) {
            FireBaseAnalyticsTracker.getInstance().logEvent(getActivity(),
                    FireBaseConstants.Event.CHANGE_IMAGE,
                    FireBaseConstants.ParamName.SOURCE,
                    FireBaseConstants.ParamValue.VIEW_IMAGE_SCREEN);
            showImageTakingMenu(_thisActivity, v, _editDrug);
        }
    }

    private void saveImageDetailsInRuntime() {
        if (null != _editDrug.getGuid()) {
            RunTimeData.getInstance().setDrugGuidFromEnlargeAct(mPillId);
            RunTimeData.getInstance().setDrugImageGuidFromEnlargeAct(mImageId);
        }
    }

    private void hideStatusAndNavigationBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

    }

    private void showImageTakingMenu(PillpopperActivity activity, View view, final Drug drug) {
        PopupMenu imageTakingMenu = new PopupMenu(activity, view);
        imageTakingMenu.getMenuInflater().inflate(R.menu.fdb_image_menu, imageTakingMenu.getMenu());
        isDefaultImage = !drug.isManaged() && !Util.isEmptyString(mImageId) ||!drug.isManaged() && !Util.isEmptyString(drug.getImageGuid()) || drug.isManaged() && drug.getPreferences().getPreference("defaultImageChoice").equalsIgnoreCase(AppConstants.IMAGE_CHOICE_FDB)
                || drug.isManaged() && drug.getPreferences().getPreference("defaultImageChoice").equalsIgnoreCase(AppConstants.IMAGE_CHOICE_CUSTOM);
        Util.getInstance().setChangeImagePopUpMenuVisibility(this, _editDrug, imageTakingMenu, isDefaultImage);
        _editDrug = drug;

        imageTakingMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().toString().equalsIgnoreCase(getResources().getString(R.string.use_camera))) {
                if (PermissionUtils.checkVersionCode()) {
                    if (PermissionUtils.checkRuntimePermission(AppConstants.PERMISSION_CAMERA, Manifest.permission.CAMERA, _thisActivity)) {
                        PhotoChooserUtility.takePhoto(_thisActivity, true);
                    }
                } else {
                    PhotoChooserUtility.takePhoto(_thisActivity, true);
                }
            } else if (item.getTitle().toString().equalsIgnoreCase(getResources().getString(R.string.photo_gallery))) {
                if (PermissionUtils.checkVersionCode()) {
                    if (PermissionUtils.checkRuntimePermission(AppConstants.PERMISSION_READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, EnlargeImageActivity.this)) {
                        PhotoChooserUtility.takePhoto(_thisActivity, false);
                    }
                } else {
                    PhotoChooserUtility.takePhoto(_thisActivity, false);
                }
            } else if (item.getTitle().toString().equalsIgnoreCase(getResources().getString(R.string.delete_image))) {
                isImageTaken = false;
                changeDrugImageState(R.string.delete_image);
            } else if (item.getTitle().toString().equalsIgnoreCase(getResources().getString(R.string.my_kp_meds_image))) {
                changeDrugImageState(R.string.my_kp_meds_image);
            }
            return true;
        });
        imageTakingMenu.show();
    }

    private void changeDrugImageState(int selectedOption) {
        switch (selectedOption) {
            case R.string.delete_image:
                _editDrug.getPreferences().setPreference("defaultImageChoice", AppConstants.IMAGE_CHOICE_NO_IMAGE);
                Drug editDrug = FrontController.getInstance(mContext).getDrugByPillId(_editDrug.getGuid());
                deleteImage(_editDrug.getGuid(), _editDrug.getImageGuid());
                if (null != editDrug.getImageGuid() || editDrug.isManaged()) {
                    FrontController.getInstance(_thisActivity).updatePillImagePreferences(_editDrug.getGuid(), AppConstants.IMAGE_CHOICE_NO_IMAGE, _editDrug.getPreferences().getPreference("defaultServiceImageID"));
                    FrontController.getInstance(mContext).addLogEntry(mContext, Util.prepareLogEntryForAction("EditPill", editDrug, mContext));
                }
                _editDrug.setImageGuid(null);
                ImageUILoaderManager.getInstance().loadDrugImage(getActivity(), _editDrug.getImageGuid(), _editDrug.getGuid(), imageView, Util.getDrawableWrapper(getActivity(), R.drawable.pill_default));
                imageView.setImageResource(R.drawable.pill_default);
                imageView.setVisibility(View.GONE);
                defaultImageView.setVisibility(View.VISIBLE);
                break;
            case R.string.my_kp_meds_image:
                if (null != imageView) {
                    imageView.resetZoom();
                }
                _editDrug.getPreferences().setPreference("defaultImageChoice", AppConstants.IMAGE_CHOICE_FDB);
                deleteImage(_editDrug.getGuid(), _editDrug.getImageGuid());
                FrontController.getInstance(_thisActivity).updatePillImagePreferences(_editDrug.getGuid(), AppConstants.IMAGE_CHOICE_FDB, _editDrug.getPreferences().getPreference("defaultServiceImageID"));
                Drug editKPHCDrug = FrontController.getInstance(mContext).getDrugByPillId(_editDrug.getGuid());
                FrontController.getInstance(mContext).addLogEntry(mContext, Util.prepareLogEntryForAction("EditPill", editKPHCDrug, mContext));
                ImageUILoaderManager.getInstance().loadDrugImage(getActivity(), _editDrug.getImageGuid(), _editDrug.getGuid(), imageView, Util.getDrawableWrapper(getActivity(), R.drawable.pill_default));
                setDrugEncodedImage(FrontController.getInstance(mContext).getFdbImageByPillId(_editDrug.getGuid()),_editDrug);
                updateImage(_editDrug.getGuid(),_editDrug.getImageGuid(),FrontController.getInstance(mContext).getFdbImageByPillId(_editDrug.getGuid()));
                break;
        }
    }

    private void deleteImage(String pillId, String imageGuid) {
        FrontController.getInstance(mContext).deleteCustomImage(pillId, imageGuid);
        mImageId = null;
        RunTimeData.getInstance().setIsImageDeleted(true);
        RunTimeData.getInstance().setDrugImageGuidFromEnlargeAct(null);
//        RunTimeData.getInstance().getScheduleData().getSelectedDrug().setImageGuid(null);
    }

    private void updateImage(String pillId, String imageGuid, String encodedImage) {
        if (!Util.isEmptyString(pillId) && !Util.isEmptyString(imageGuid)) {
            FrontController.getInstance(mContext).updateCustomImage(pillId, imageGuid, encodedImage);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_CHOOSE_PHOTO:
                    if (Util.isEmptyString(_editDrug.getImageGuid())) {
                        cropImageGuid = Util.getRandomGuid();
                    } else {
                        cropImageGuid = _editDrug.getImageGuid();
                    }
                    if (resultIntent != null && null != resultIntent.getData()) {
                        performCropFromLibrary(resultIntent.getData());
                    } else {
                        if (AppConstants.contentUri != null) {
                            getActivity().revokeUriPermission(AppConstants.contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                        RunTimeData.getInstance().setCpCode(null);
                        performCropFromLibrary(Uri.parse(AppConstants.photoFile.getAbsolutePath()));
                    }
                    break;
                case REQ_CROP_PHOTO:
                    _editDrug.setImageGuid(cropImageGuid);
                    mImageId = cropImageGuid;
                    if (null != imageView) {
                        imageView.resetZoom();
                    }
                    refreshImage(cropImageGuid, resultIntent.getParcelableExtra("data"));
                    cropImageGuid = null;
                    isImageTaken = true;
                    RunTimeData.getInstance().setIsImageDeleted(false);
                    break;
                default:
                    break;
            }
        }
        //used to restrict double taps for As needed med
        isClickable = true;
    }

    private void performCropFromLibrary(Uri selectedImage){
        final int outputX = 150;
        final int outputY = 150;
        Intent cropIntent = new CropImageIntentBuilder(outputX, outputY, null)
                .setScale(true)
                .setScaleUpIfNeeded(true)
                .setSourceImage(selectedImage)
                .getIntent(this);
        cropIntent.putExtra("return-data", true);
        cropIntent.putExtra("image-path", selectedImage.toString());
        cropIntent.putExtra("scale", true);
        startActivityForResult(cropIntent, REQ_CROP_PHOTO);
    }

    private void refreshImage(String imageGuid, Bitmap bitmap) {
        /*File file = Drug.getImageCacheFile(EnlargeImageActivity.this, imageGuid);
        InputStream imageStream = new FileInputStream(file);*/
        String encodedImage = ImageLoaderUtil.encodeImage(bitmap);

        if (null != _editDrug.getGuid()) {
            // edit case
            _editDrug.getPreferences().setPreference("defaultImageChoice", AppConstants.IMAGE_CHOICE_CUSTOM);
            FrontController.getInstance(EnlargeImageActivity.this).updatePillImagePreferences(_editDrug.getGuid(), AppConstants.IMAGE_CHOICE_CUSTOM, _editDrug.getPreferences().getPreference("defaultServiceImageID"));
            updateImage(_editDrug.getGuid(), imageGuid, encodedImage);

            Drug editDrug = FrontController.getInstance(mContext).getDrugByPillId(_editDrug.getGuid());
            if(!Util.isEmptyString(_editDrug.getName())){
                FrontController.getInstance(mContext).addLogEntry(mContext, Util.prepareLogEntryForAction("EditPill", editDrug, mContext));
            }
            imageView.setVisibility(View.VISIBLE);
            defaultImageView.setVisibility(View.GONE);
            imageView.setImageBitmap(bitmap);
            ImageUILoaderManager.getInstance().loadDrugImage(getActivity(), imageGuid, _editDrug.getGuid(), imageView, Util.getDrawableWrapper(getActivity(), R.drawable.pill_default));
        } else {
            // add new drug
            //_editDrug.setId(Util.getRandomGuid());
            isNewDrug = true;
            storeImageToDatabase(_editDrug, encodedImage);
            imageView.setVisibility(View.VISIBLE);
            defaultImageView.setVisibility(View.GONE);
            imageView.setImageBitmap(bitmap);
            ImageUILoaderManager.getInstance().loadDrugImage(getActivity(), _editDrug.getImageGuid(), _editDrug.getGuid(), imageView, Util.getDrawableWrapper(getActivity(), R.drawable.pill_default));
        }

//            file.delete();
    }

    private void storeImageToDatabase(Drug drug, String encodedImage) {
        try {
            if (!Util.isEmptyString(drug.getGuid()) && !Util.isEmptyString(drug.getImageGuid()) && !Util.isEmptyString(encodedImage)) {
                FrontController.getInstance(EnlargeImageActivity.this).updateCustomImage(drug.getGuid(), drug.getImageGuid(), encodedImage);
            }
        } catch (Exception e) {
            PillpopperLog.exception("storeImageToDatabase -- " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        saveImageDetailsInRuntime();
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // permission was granted,
            if (requestCode == AppConstants.PERMISSION_CAMERA) {
                PhotoChooserUtility.takePhoto(_thisActivity, true);

            } else if (requestCode == AppConstants.PERMISSION_READ_EXTERNAL_STORAGE) {
                PhotoChooserUtility.takePhoto(_thisActivity, false);
            }

        } else {
            //permission Denied
            if (permissions.length > 0) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    onPermissionDenied(requestCode);
                } else {
                    onPermissionDeniedNeverAskAgain(requestCode);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (RunTimeData.getInstance().getIsImageDeleted()) {
            _editDrug.setImageGuid(null);
            mImageId = null;
        }
        getIntent().putExtra("imageId", _editDrug.getImageGuid());

        Intent intent = new Intent(BROADCAST_REFRESH_FOR_MED_IMAGES);
        intent.putExtra(BUNDLE_EXTRA_DRUG_TO_REFRESH, mPillId);
        intent.putExtra("IS_FROM_ENLARGE_ACTIVITY", true);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

    }
}

