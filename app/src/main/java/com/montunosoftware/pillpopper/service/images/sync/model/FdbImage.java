package com.montunosoftware.pillpopper.service.images.sync.model;

import androidx.annotation.NonNull;

import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by M1032896 on 6/14/2018.
 */

public class FdbImage implements Comparable<FdbImage> {

    private String id;
    private String imageBytes;
    private boolean isCurrent;
    private String startDate;
    private String endDate;
    private String description;
    private String pillId;
    private String fdbImageId;
    private String fdbImageDesc;
    private String imageUrlExpireAtUTC;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(String imageBytes) {
        this.imageBytes = imageBytes;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPillId() {
        return pillId;
    }

    public void setPillId(String pillId) {
        this.pillId = pillId;
    }

    public String getFdbImageId() {
        return fdbImageId;
    }

    public void setFdbImageId(String fdbImageId) {
        this.fdbImageId = fdbImageId;
    }

    public String getFdbImageDesc() {
        return fdbImageDesc;
    }

    public void setFdbImageDesc(String fdbImageDesc) {
        this.fdbImageDesc = fdbImageDesc;
    }

    public String getImageUrlExpireAtUTC() {
        return imageUrlExpireAtUTC;
    }

    public void setImageUrlExpireAtUTC(String imageUrlExpireAtUTC) {
        this.imageUrlExpireAtUTC = imageUrlExpireAtUTC;
    }

    @Override
    public int compareTo(@NonNull FdbImage fdbImage) {
        return getCalendarFromISO(startDate).after(getCalendarFromISO(fdbImage.getStartDate())) ? 1 : 0;
    }

    public Calendar getCalendarFromISO(String isoDate) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss", Locale.getDefault());
        try {
            Date date = dateFormat.parse(isoDate);
            calendar.setTime(date);
        } catch (ParseException e) {
            LoggerUtils.exception(e.getMessage());
        }
        return calendar;
    }
}
