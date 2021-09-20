package org.kp.tpmg.mykpmeds.activation.envswitch;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.BuildConfig;
import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.Splash;
import com.montunosoftware.pillpopper.android.fingerprint.FingerprintUtils;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.envswitch.model.EnvironmentListItem;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttgmobilelib.TTGRuntimeData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnvironmentSwitchActivity extends AppCompatActivity {

    private Context mContext;

    private RecyclerView mEnvironmentListRecyclerView;
    private EnvironmentListRecyclerViewAdapter mRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environment_switch);
        LoggerUtils.info("ENV SWITCH - EnvironmentSwitchActivity - entering onCreate()");

        mContext = this;

        initUi();
    }

    private void initUi() {
        mEnvironmentListRecyclerView = findViewById(R.id.select_environment_list_recycler_view);
        mEnvironmentListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewAdapter = new EnvironmentListRecyclerViewAdapter();
        mEnvironmentListRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    public class ChangeEnvironmentTask extends AsyncTask<String, Void, Boolean> {

        String envKeyName;

        public ChangeEnvironmentTask(String envKeyName) {
            this.envKeyName = envKeyName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            EnvSwitchUtils.setCurrentEnvironmentToSharedPrefs(mContext, envKeyName);
            setResult(RESULT_OK);
            Toast.makeText(mContext, "Application environment: " + envKeyName, Toast.LENGTH_LONG).show();
            TTGRuntimeData.getInstance().getConfigListParams().clear();

            //clear fingerprint
            FingerprintUtils.resetAndPurgeKeyStore(EnvironmentSwitchActivity.this);

            Intent i = new Intent(mContext, Splash.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            EnvSwitchUtils.initCurrentSelectedEnvironmentEndpoint(mContext);
            startActivity(i);
            finish();
        }

        @Override
        protected Boolean doInBackground(String... envKey) {
            if (envKey != null
                    && envKey.length > 0) {
                String newEnvironmentUrl = BuildConfig.ENVIRONMENT_MAP.get(envKeyName);
                if (Util.isEmptyString(newEnvironmentUrl)) {
                    return false;
                }
                SharedPreferenceManager.getInstance(mContext, AppConstants.AUTH_CODE_PREF_NAME).clearPreferences();
                ActivationUtil.resetDevice(mContext);
                SharedPreferenceManager.getInstance(mContext, AppConstants.AUTH_CODE_PREF_NAME).putLong(AppConstants.APP_PROFILE_INVOKED_TIMESTAMP, 0L, false);
                AppConstants.baseURL = newEnvironmentUrl;
                EnvSwitchUtils.setCurrentEnvironmentToSharedPrefs(mContext, envKeyName);
                return true;
            }
            return false;
        }
    }

    public class EnvironmentListRecyclerViewAdapter extends RecyclerView.Adapter<EnvironmentListRecyclerViewAdapter.EnvironmentListItemViewHolder> {


        private List<EnvironmentListItem> mEnvironmentList;

        EnvironmentListRecyclerViewAdapter() {
            this.mEnvironmentList = new ArrayList<>();
            initEnvironmentList();
        }

        private void initEnvironmentList() {
            Map<String, String> environmentMap = BuildConfig.ENVIRONMENT_MAP;
            for (Map.Entry<String, String> entry : environmentMap.entrySet()) {
                EnvironmentListItem listItem = new EnvironmentListItem();
                listItem.setEnvironmentLabel(entry.getKey());
                listItem.setEnvironmentEndpointUrl(entry.getValue());
                listItem.setSelected(AppConstants.baseURL.contentEquals(listItem.getEnvironmentEndpointUrl()));
                mEnvironmentList.add(listItem);
            }
        }

        @Override
        public EnvironmentListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup itemView = (ViewGroup) inflater.inflate(R.layout.activity_environment_switch_list_item, parent, false);
            return new EnvironmentListItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(EnvironmentListItemViewHolder holder, final int position) {
            EnvironmentListItem listItem = mEnvironmentList.get(position);
            holder.mEnvironmentLabel.setText(listItem.getEnvironmentLabel());
            if (listItem.isSelected()) {
                holder.mEnvironmentSelectedIcon.setVisibility(View.VISIBLE);
            } else {
                holder.mEnvironmentSelectedIcon.setVisibility(View.GONE);
            }

            holder.mItemRootLayout.setOnClickListener(v -> {
                if (!mEnvironmentList.get(position).getEnvironmentLabel().equalsIgnoreCase(EnvSwitchUtils.getCurrentEnvironmentFromSharedPrefs(mContext))) {
                    new ChangeEnvironmentTask(mEnvironmentList.get(position).getEnvironmentLabel()).execute();
                } else {
                    setResult(RESULT_CANCELED);
                    Intent i = new Intent(mContext, Splash.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    EnvSwitchUtils.initCurrentSelectedEnvironmentEndpoint(mContext);
                    startActivity(i);
                    finish();
                }
            });

        }

        @Override
        public int getItemCount() {
            return mEnvironmentList.size();
        }

        class EnvironmentListItemViewHolder extends RecyclerView.ViewHolder {
            ViewGroup mItemRootLayout;
            TextView mEnvironmentLabel;
            ImageView mEnvironmentSelectedIcon;

            EnvironmentListItemViewHolder(View view) {
                super(view);
                mItemRootLayout = view.findViewById(R.id.environment_list_item_root_layout);
                mEnvironmentLabel = view.findViewById(R.id.environment_name_text_view);
                mEnvironmentSelectedIcon = view.findViewById(R.id.environment_selected_icon_image_view);
            }
        }
    }
}
