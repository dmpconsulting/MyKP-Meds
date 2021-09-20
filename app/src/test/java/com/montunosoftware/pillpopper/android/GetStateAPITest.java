package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;

import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.PillpopperParseException;
import com.montunosoftware.pillpopper.android.util.PillpopperServer;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.database.model.PillpopperResponse;
import com.montunosoftware.pillpopper.database.model.UserList;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.State;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.SignonResponse;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.test.core.app.ApplicationProvider;

/**
 * Created by M1024581 on 2/10/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = PillpopperApplicationShadow.class,sdk = TestConfigurationProperties.BUILD_SDK_VERSION,shadows = {DatabaseHandlerShadow.class,SecurePreferencesShadow.class})
public class GetStateAPITest {

    private Context context;
    private PillpopperAppContext pillpopperAppContext;
    private ActivationController activationController;

    private PillpopperServer server;
    private HomeContainerActivity homeContainerActivity;
    private List<String> enabledUsers = new ArrayList<>();

    @Before
    public void SetUp(){
        TestUtil.setupTestEnvironment();

        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity.class).create().get();
        context = ApplicationProvider.getApplicationContext().getApplicationContext();
        pillpopperAppContext = PillpopperAppContext.getGlobalAppContext(context);
        activationController = ActivationController.getInstance();

        String path = null;
        try {
            path = GetStateAPITest.class.getResource("/RegisterResponse.txt").toURI().getPath();
            File file = new File(path);
            JSONObject response = TestUtil.readFromFile(new FileInputStream(file));
            Gson gson = new Gson();
            SignonResponse result = gson.fromJson(response.toString(), SignonResponse.class);
            RunTimeData.getInstance().setRegistrationResponse(result);

            List<User> enabledUsersList = new ArrayList<>();
            for(User user : result.getResponse().getUsers()){
                LoggerUtils.info("User info : " + user.getFirstName() + " Enabled Status : " + user.getEnabled());
                if(user.getEnabled().equalsIgnoreCase("Y")){
                    enabledUsers.add(user.getUserId());
                    enabledUsersList.add(user);
                }
            }

            RunTimeData.getInstance().setSelectedUsersList(enabledUsers);
            RunTimeData.getInstance().setEnabledUsersList(enabledUsersList);

        } catch (Exception e) {
            System.err.print(e.getMessage());
        }
    }

    @Test
    public void testGetStateAPIRequest(){
        try {

            server = PillpopperServer.getInstance(context,pillpopperAppContext);

            JSONObject request = new JSONObject();

            request.put("action", "GetState");
            request.put("apiVersion", "Version 6.0.4");

            Assert.assertNotNull(activationController);

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("secureToken", activationController.getSSOSessionId(context));
            //TODO : inspect if we need this as we will be mocking n/w req and responses
//            Assert.assertNotNull("secureToken is null", activationController.getSSOSessionId(context));

            headers.put("guid", RunTimeData.getInstance().getRegistrationResponse().getResponse().getKpGUID());
            Assert.assertNotNull("guid is null", RunTimeData.getInstance().getRegistrationResponse().getResponse().getKpGUID());

            headers.put("hardwareId", UniqueDeviceId.getHardwareId(context));
            Assert.assertNotNull("hardwareId is null", UniqueDeviceId.getHardwareId(context));

            headers.put("userId", RunTimeData.getInstance().getRegistrationResponse().getResponse().getPrimaryUserId());

            JSONObject serverResponse = null;
            serverResponse = server.makeRequest(request, headers);

            PillpopperLog.say("Server Response : " + serverResponse.toString());

            Assert.assertNotNull("serverResponse is not Null", serverResponse);
            pillpopperAppContext.getState(context).getAccountId();

        } catch (JSONException e) {
            System.err.print(e.getMessage());
        } catch (Exception e){
            System.err.print(e.getMessage());
        }

        //TODO : [M1028309] Henceforth no dependecy on main working code : so here response to be read from the json file later some time::::


       /* try {
            Assert.assertEquals(finalRequest.getJSONArray("proxyUserList").length(),
                    RunTimeData.getInstance().getRegistrationResponse().getResponse().getUsers().size());
        } catch (JSONException e) {
            System.err.print(e.getMessage());
        }*/
    }


    @Test
    public void testMakeGetStateAPIServices(){
        Intent intent = new Intent(homeContainerActivity, StateDownloadIntentService.class);
        intent.setAction(StateDownloadIntentService.ACTION_GET_STATE);
        homeContainerActivity.startService(intent);
    }

    /*@Test
    public void testGetStateApiService(){
        Intent intent = Shadows.shadowOf(homeContainerActivity).getNextStartedService();
        assertEquals(StateDownloadIntentService.class.getCanonicalName(),intent.getComponent().getClassName());
    }*/

    @Test
    public void testGetStateAPIResponse(){
        try {
            String path = GetStateAPITest.class.getResource("/GetStateSuccessResponse.txt").toURI().getPath();

            File file = new File(path);

            JSONObject response = TestUtil.readFromFile(new FileInputStream(file));

            State serverState = new State((JSONObject) response.get("pillpopperResponse"), pillpopperAppContext.getEdition(), pillpopperAppContext.getFDADrugDatabase(), context);

            pillpopperAppContext.setState(context,serverState);

            // assert statements
            Gson gson = new Gson();
            PillpopperResponse result = gson.fromJson(response.get("pillpopperResponse").toString(), PillpopperResponse.class);
            UserList[] userList = result.getUserList();

            Assert.assertNotNull("Pillpopper response is null", result);

            Assert.assertNotNull("User list is null", userList);

            Assert.assertTrue(userList.length > 0);

            for(UserList user : userList) {
                Assert.assertNotNull(user.getPillList());
            }

        } catch (FileNotFoundException e) {
            System.err.print(e.getMessage());
        } catch (URISyntaxException e) {
            System.err.print(e.getMessage());
        } catch (PillpopperParseException e) {
            System.err.print(e.getMessage());
        } catch(Exception e){
            System.err.print(e.getMessage());
        }
    }
}
