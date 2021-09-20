package com.montunosoftware.pillpopper.model.genericCardAndBanner;


import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

public class AnnouncementsItem implements Serializable {
    private List<String> regions;
    private List<ButtonsItem> buttons;
    private String color;
    private String iconName;
    private int id;
    private String type;
    private int priority;
    private String text_color;
    private String title;
    private String message;
    private String retention;
    private String baseScreen;
    private String subTitle;


    public String getText_color() { return text_color; }

    public List<String> getRegions() {
        return regions;
    }

    public List<ButtonsItem> getButtons() {
        return buttons;
    }

    public String getColor() {
        return color;
    }

    public String getIconName() {
        return iconName;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getRetention() {
        return retention;
    }

    public String getBaseScreen() {
        return baseScreen;
    }

    public String getSubTitle() {
        return subTitle;
    }


    public static class AnnouncementComparator implements Comparator<AnnouncementsItem>
    {
        @Override
        public int compare(AnnouncementsItem lhs, AnnouncementsItem rhs)
        {
            return lhs.getPriority().compareTo(rhs.getPriority());
        }
    }
}