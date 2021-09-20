package com.montunosoftware.pillpopper.database.model;

/**
 * @author
 * Created by M1023050 on 5/16/2016.
 */
import java.util.Arrays;

public class IntermittentMultiPillpopperResponse {

    private String dataSyncResult;
    private String errorStatus;
    private String replayId;
    private String currentTime;
    private String[] productList;
    private String action;
    private String pillpopperVersion;
    private UserList[] userList;
    private String errorText;
    private String devInfo;

    public String getReplayId ()
    {
        return replayId;
    }

    public void setReplayId (String replayId)
    {
        this.replayId = replayId;
    }

    public String getDataSyncResult ()
    {
        return dataSyncResult;
    }

    public void setDataSyncResult (String dataSyncResult)
    {
        this.dataSyncResult = dataSyncResult;
    }

    public String getAction ()
    {
        return action;
    }

    public void setAction (String action)
    {
        this.action = action;
    }

    public String getPillpopperVersion ()
    {
        return pillpopperVersion;
    }

    public void setPillpopperVersion (String pillpopperVersion)
    {
        this.pillpopperVersion = pillpopperVersion;
    }

    public String getErrorStatus ()
    {
        return errorStatus;
    }

    public void setErrorStatus (String errorStatus)
    {
        this.errorStatus = errorStatus;
    }

    public String getCurrentTime ()
    {
        return currentTime;
    }

    public void setCurrentTime (String currentTime)
    {
        this.currentTime = currentTime;
    }

    public String[] getProductList ()
    {
        return productList;
    }

    public void setProductList (String[] productList)
    {
        this.productList = productList;
    }



    public UserList[] getUserList ()
    {
        return userList;
    }

    public void setUserList (UserList[] userList)
    {
        this.userList = userList;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }
    public String getDevInfo() {
        return devInfo;
    }

    public void setDevInfo(String devInfo) {
        this.devInfo = devInfo;
    }


    @Override
    public String toString()
    {
        return "ClassPojo [replayId = "+replayId+", currentTime = "+currentTime+", productList = "+Arrays.toString(productList)+", action = "+action+", pillpopperVersion = "+pillpopperVersion+", userList = "+ Arrays.toString(userList) +"]";
    }
}
