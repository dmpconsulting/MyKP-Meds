package org.kp.tpmg.mykpmeds.activation.service;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.AppData;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.SetupProxyPillPopperResponse;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * Created by santhosh on 7/19/2016.
 */
public class SetUpProxyEnableService extends AsyncTask<String, Void, Integer> {

    private final Context mContext;
    private final AppData sAppData;
    private List<User> mSelectedUsers = new ArrayList<>();

    private SetUpProxyEnableResponseListener mCallback;

    public interface SetUpProxyEnableResponseListener{
        void onSetUpProxyResponseReceived(int result);
    }

    public SetUpProxyEnableService(Context context, List<User> selectedUserIds,SetUpProxyEnableResponseListener callback){
        mContext = context;
        sAppData = AppData.getInstance();
        mSelectedUsers = selectedUserIds;
        mCallback = callback;
    }

    @Override
    protected Integer doInBackground(String... url) {
        int status = -1;

        String response = sAppData.getHttpResponse(url[0],
                AppConstants.POST_METHOD_NAME,
                null,
                ActivationUtil.buildHeaders(mContext),prepareCrateProxyrequest(),
                mContext);
        if (null != response
                && !response.equals(AppConstants.HTTP_DATA_ERROR)) {
            Gson gson = new Gson();
            SetupProxyPillPopperResponse result = gson.fromJson(response,
                    SetupProxyPillPopperResponse.class);
            LoggerUtils.info("setupProxyResponse: " + response);
            if (result != null && result.getPillPopperResponse() != null
                    && result.getPillPopperResponse().getDataSyncResult() != null) {
                String dataSyncResult = result.getPillPopperResponse()
                        .getDataSyncResult();
                if (null != dataSyncResult
                        && ("success").equalsIgnoreCase(dataSyncResult)) {
                    RunTimeData.getInstance().setEnabledUsersList(mSelectedUsers);
                    updateSelectedUsersAsEnabled(mSelectedUsers);


                    status = 0;
                }
            }
        }
        return status;
    }

    private void updateSelectedUsersAsEnabled(List<User> selectedUsersList) {
        List<String> usersList = new ArrayList<>();
        for(User user : selectedUsersList){
            usersList.add(user.getUserId());
        }
        RunTimeData.getInstance().setSelectedUsersList(usersList);
    }

    public JSONObject prepareCrateProxyrequest()
    {
        JSONObject request = new JSONObject();
        JSONObject finalRequest = new JSONObject();
        JSONArray requestArray=new JSONArray();
        List<User> userID = null ;
        String accountID = "";
        if(null!= RunTimeData.getInstance().getRegistrationResponse()){
            //userID = RunTimeData.getInstance().getRegistrationResponse().getResponse().getUsers();
            accountID = RunTimeData.getInstance().getRegistrationResponse().getResponse().getUsers().get(0).getUserId();
        }
        try{
            request.put("action","SetProxyEnable");
            request.put("clientVersion",ActivationUtil.getAppVersion(mContext));
            request.put("userId",accountID);

            // We are passing only selected usersList for setup Proxy enables API
            if(mSelectedUsers.isEmpty()){
                LoggerUtils.info("Single User is there. So consider only Primary User");
//                mSelectedUsers.add(RunTimeData.getInstance().getEnabledUsersList().get(0).getUserId());
                JSONObject userJsonObject =new JSONObject();
                userJsonObject.put("userGUID", RunTimeData.getInstance().getEnabledUsersList().get(0).getUserId());
//                if(null != RunTimeData.getInstance().getEnabledUsersList().get(0).getLastSyncToken()) {
//                    userJsonObject.put("lastSyncToken", RunTimeData.getInstance().getEnabledUsersList().get(0).getLastSyncToken());
//                }else{
//                    userJsonObject.put("lastSyncToken", "-1");
//                }
                requestArray.put(userJsonObject);
            }else{
                LoggerUtils.info("Multiple Users");
                for (User user : mSelectedUsers) {
                    JSONObject userJsonObject =new JSONObject();
                    userJsonObject.put("userGUID", user.getUserId());
//                    userJsonObject.put("lastSyncToken",
//                            Util.getLastSyncTokenValue(FrontController.getInstance(Util.persistentActivity).getLastSyncTokenForUser(user.getUserId())));
                    requestArray.put(userJsonObject);
                }
            }

//            RunTimeData.getInstance().setSelectedUsersList(mSelectedUsers);
            request.put("proxyUserList",requestArray);
            request.put("partnerId",AppConstants.EDITION);
            request.put("language", "en_US"); // Need to modify
            request.put("hardwareId",ActivationUtil.getDeviceId(mContext));
            request.put("replayId", ActivationUtil.generateUUID());
            request.put("apiVersion", "Version 6.0.4");
            request.put("deviceToken", "");

            finalRequest.put("pillpopperRequest", request);
        }catch(Exception e) {
            LoggerUtils.exception("Oops! Exception: " + e.getMessage());
        }
        LoggerUtils.info("SetupProxy JsonObject : " + finalRequest.toString());
        return finalRequest;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        mCallback.onSetUpProxyResponseReceived(result);
    }
}

