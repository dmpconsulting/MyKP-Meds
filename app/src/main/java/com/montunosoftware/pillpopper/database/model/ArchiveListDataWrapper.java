package com.montunosoftware.pillpopper.database.model;

import com.montunosoftware.pillpopper.model.ArchiveListDrug;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author
 * Created by adhithyaravipati on 9/27/16.
 */
public class ArchiveListDataWrapper {

    private ArrayList<ArchiveListUserDropDownData> userDropDownList;

    private HashMap<String,ArrayList<ArchiveListDrug>> archivedDrugsHashMap;

    public ArrayList<ArchiveListUserDropDownData> getUserDropDownList() {
        return userDropDownList;
    }

    public void setUserDropDownList(ArrayList<ArchiveListUserDropDownData> userDropDownList) {
        this.userDropDownList = userDropDownList;
    }

    public HashMap<String, ArrayList<ArchiveListDrug>> getArchivedDrugsHashMap() {
        return archivedDrugsHashMap;
    }

    public void setArchivedDrugsHashMap(HashMap<String, ArrayList<ArchiveListDrug>> archivedDrugsHashMap) {
        this.archivedDrugsHashMap = archivedDrugsHashMap;
    }
}
