package com.montunosoftware.pillpopper.database.model;

/**
 * @author
 * Created by M1023050 on 3/14/2016.
 */
import java.util.Arrays;
public class PillpopperResponse
{
    private String replayId;

    private String currentTime;

    private String[] productList;

    private String action;

    private String pillpopperVersion;

    private UserList[] userList;

    public String getReplayId ()
    {
        return replayId;
    }

    public void setReplayId (String replayId)
    {
        this.replayId = replayId;
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

    public UserList[] getUserList ()
    {
        return userList;
    }

    public void setUserList (UserList[] userList)
    {
        this.userList = userList;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [replayId = "+replayId+", currentTime = "+currentTime+", productList = "+ Arrays.toString(productList) +", action = "+action+", pillpopperVersion = "+pillpopperVersion+", userList = "+ Arrays.toString(userList) +"]";
    }
}

