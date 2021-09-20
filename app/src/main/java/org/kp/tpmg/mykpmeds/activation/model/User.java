package org.kp.tpmg.mykpmeds.activation.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * @author Created by M1020417 on 12/4/2015.
 */
public class User implements Parcelable, Comparable<User> {

    private String displayName;
    private String enabled;
    private String firstName;
    private String lastName;
    private String middleName;
    private String nickName;
    private String relationDesc;
    private String relId;
    private String userId;
    private boolean selected;
    private String lastSyncToken;
    private boolean hasChanges;
    private String mrn;
    private String genderCode;
    private String age;
    private boolean isTeen;
    private boolean isTeenToggleEnabled;

    public User() {

    }

    public User(Parcel in) {
        displayName = in.readString();
        enabled = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        middleName = in.readString();
        nickName = in.readString();
        relationDesc = in.readString();
        relId = in.readString();
        userId = in.readString();
        selected = in.readByte() != 0;
        lastSyncToken = in.readString();
        hasChanges = in.readByte() != 0;
        userType = in.readString();
        mrn = in.readString();
        genderCode = in.readString();
        age = in.readString();
        isTeen = in.readInt() != 0;
        isTeenToggleEnabled = in.readInt() !=0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getRelationDesc() {
        return relationDesc;
    }

    public void setRelationDesc(String relationDesc) {
        this.relationDesc = relationDesc;
    }

    public String getRelId() {
        return relId;
    }

    public void setRelId(String relId) {
        this.relId = relId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    private String userType;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    public String getLastSyncToken() {
        return lastSyncToken;
    }

    public void setLastSyncToken(String lastSyncToken) {
        this.lastSyncToken = lastSyncToken;
    }

    public boolean hasSyncChanges() {
        return hasChanges;
    }

    public void setHasSyncChanges(boolean hasChanges) {
        this.hasChanges = hasChanges;
    }

    public String getMrn() {
        return mrn;
    }

    public void setMrn(String mrn) {
        this.mrn = mrn;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(displayName);
        parcel.writeString(enabled);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(middleName);
        parcel.writeString(nickName);
        parcel.writeString(relationDesc);
        parcel.writeString(relId);
        parcel.writeString(userId);
        parcel.writeByte((byte) (selected ? 1 : 0));
        parcel.writeString(lastSyncToken);
        parcel.writeByte((byte) (hasChanges ? 1 : 0));
        parcel.writeString(userType);
        parcel.writeString(mrn);
        parcel.writeString(genderCode);
        parcel.writeString(age);
        parcel.writeInt(isTeen ? 1: 0);
        parcel.writeInt(isTeenToggleEnabled ? 1:0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(displayName, user.displayName) &&
                Objects.equals(userId, user.userId) &&
                Objects.equals(userType, user.userType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, userId, userType);
    }

    @Override
    public int compareTo(User user) {
        int value = this.getUserType().compareTo(user.getUserType());
        if (value != 0) {
            return value;
        }
        int nameValue = this.getDisplayName().compareTo(user.getDisplayName());
        if (nameValue != 0) {
            return nameValue;
        }
        return this.getUserId().compareTo(user.getUserId());
    }

    public String getGenderCode() {
        return genderCode;
    }

    public void setGenderCode(String genderCode) {
        this.genderCode = genderCode;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public boolean isTeen() {
        return isTeen;
    }

    public void setTeen(boolean teen) {
        isTeen = teen;
    }

    public boolean isTeenToggleEnabled(){
        return isTeenToggleEnabled;
    }

    public void setTeenToggleEnabled(boolean teenToggleEnabled){
        this.isTeenToggleEnabled = teenToggleEnabled;
    }
}
