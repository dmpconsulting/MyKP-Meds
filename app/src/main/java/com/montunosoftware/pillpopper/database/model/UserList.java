package com.montunosoftware.pillpopper.database.model;
import java.util.Arrays;
/**
 * @author
 * Created by M1023050 on 3/14/2016.
 */
public class UserList
{
    private String[] deletedPillList;

    private String expires;

    private String created;

    private String userId;

    private PillList[] pillList;

    private String subscriptionType;

    private String bedTimeEnd;

    private String bedTimeStart;

    private MemberPreferences preferences;

    private String lastSyncToken;

    private boolean hasChanges;

    public String[] getDeletedPillList ()
    {
        return deletedPillList;
    }

    public void setDeletedPillList (String[] deletedPillList)
    {
        this.deletedPillList = deletedPillList;
    }

    public String getExpires ()
    {
        return expires;
    }

    public void setExpires (String expires)
    {
        this.expires = expires;
    }

    public String getCreated ()
    {
        return created;
    }

    public void setCreated (String created)
    {
        this.created = created;
    }

    public String getUserId ()
    {
        return userId;
    }

    public void setUserId (String userId)
    {
        this.userId = userId;
    }

    public PillList[] getPillList ()
    {
        return pillList;
    }

    public void setPillList (PillList[] pillList)
    {
        this.pillList = pillList;
    }

    public String getSubscriptionType ()
    {
        return subscriptionType;
    }

    public void setSubscriptionType (String subscriptionType)
    {
        this.subscriptionType = subscriptionType;
    }

    public String getBedTimeEnd ()
    {
        return bedTimeEnd;
    }

    public void setBedTimeEnd (String bedTimeEnd)
    {
        this.bedTimeEnd = bedTimeEnd;
    }

    public String getBedTimeStart ()
    {
        return bedTimeStart;
    }

    public void setBedTimeStart (String bedTimeStart)
    {
        this.bedTimeStart = bedTimeStart;
    }

    public MemberPreferences getPreferences ()
    {
        return preferences;
    }

    public void setPreferences (MemberPreferences preferences)
    {
        this.preferences = preferences;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [deletedPillList = "+ Arrays.toString(deletedPillList) +", expires = "+expires+", created = "+created+", userId = "+userId+", pillList = "+ Arrays.toString(pillList) +", subscriptionType = "+subscriptionType+", bedTimeEnd = "+bedTimeEnd+", bedTimeStart = "+bedTimeStart+", preferences = "+preferences+"]";
    }

    public String getLastSyncToken() {
        return lastSyncToken;
    }

    public void setLastSyncToken(String lastSyncToken) {
        this.lastSyncToken = lastSyncToken;
    }

    public boolean hasChanges() {
        return hasChanges;
    }

    public void setHasChanges(boolean hasChanges) {
        this.hasChanges = hasChanges;
    }
}
