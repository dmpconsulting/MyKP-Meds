package com.montunosoftware.pillpopper.model;

public class DoseEvent
{
	private Drug drug;
	private PillpopperTime date;
	private PillpopperTime original_date; // before it was postponed
	private boolean postponed;
	private boolean future;
	private boolean overdue;
	private DoseEvent next;
	private String headerText;
	// Schedule fields
	private String scheduleLabel;
	private boolean this_is_next_dose;
	private boolean this_is_undo_dose;
	private boolean showFutureIntervalDrugAsWhenReady;
	private String _dbgStr;
	private String findRemainderFuture;
	private String proxyName;
	private boolean isDoseNextEventHeader = false;


    public boolean isDoseNextEventHeader()
    {
          return isDoseNextEventHeader;
    }


    public void setDoseNextEventHeader(boolean isDoseNextEventHeader)
    {
          this.isDoseNextEventHeader = isDoseNextEventHeader;
    }

	
	public DoseEvent(String proxyName,Drug drug, PillpopperTime date) {
		this.setDrug(drug);
		this.setDate(date);
		this.setProxyName(proxyName);
		setOriginal_date(null);
		setPostponed(false);
		setFuture(false);
		setOverdue(false);
		setNext(null);
		setScheduleLabel("");
		setThis_is_next_dose(false);
		setThis_is_undo_dose(false);
		setShowFutureIntervalDrugAsWhenReady(false);
		set_dbgStr("");
		setFindRemainderFuture("");
	}

	public DoseEvent(Drug drug, PillpopperTime date) {
		this.setDrug(drug);
		this.setDate(date);
		setOriginal_date(null);
		setPostponed(false);
		setFuture(false);
		setOverdue(false);
		setNext(null);
		setScheduleLabel("");
		setThis_is_next_dose(false);
		setThis_is_undo_dose(false);
		setShowFutureIntervalDrugAsWhenReady(false);
		set_dbgStr("");
		setFindRemainderFuture("");
	}
	
//	public DoseEvent(String headerTime){
//		this.setHeaderText(headerTime);
//	}

	public String getProxyName() {
		return proxyName;
	}

	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}

	public String getHeaderText()
	{
		return headerText;
	}


	public void setHeaderText(String headerText)
	{
		this.headerText = headerText;
	}

	public Drug getDrug() {
		return drug;
	}

	public void setDrug(Drug drug) {
		this.drug = drug;
	}

	public PillpopperTime getDate() {
		return date;
	}

	public void setDate(PillpopperTime date) {
		this.date = date;
	}

	public PillpopperTime getOriginal_date() {
		return original_date;
	}

	public void setOriginal_date(PillpopperTime original_date) {
		this.original_date = original_date;
	}

	public boolean isPostponed() {
		return postponed;
	}

	public void setPostponed(boolean postponed) {
		this.postponed = postponed;
	}

	public boolean isFuture() {
		return future;
	}

	public void setFuture(boolean future) {
		this.future = future;
	}

	public boolean isOverdue() {
		return overdue;
	}

	public void setOverdue(boolean overdue) {
		this.overdue = overdue;
	}

	public DoseEvent getNext() {
		return next;
	}

	public void setNext(DoseEvent next) {
		this.next = next;
	}

	public String getScheduleLabel() {
		return scheduleLabel;
	}

	public void setScheduleLabel(String scheduleLabel) {
		this.scheduleLabel = scheduleLabel;
	}

	public boolean isThis_is_next_dose() {
		return this_is_next_dose;
	}

	public void setThis_is_next_dose(boolean this_is_next_dose) {
		this.this_is_next_dose = this_is_next_dose;
	}

	public boolean isThis_is_undo_dose() {
		return this_is_undo_dose;
	}

	public void setThis_is_undo_dose(boolean this_is_undo_dose) {
		this.this_is_undo_dose = this_is_undo_dose;
	}

	public boolean isShowFutureIntervalDrugAsWhenReady() {
		return showFutureIntervalDrugAsWhenReady;
	}

	public void setShowFutureIntervalDrugAsWhenReady(boolean showFutureIntervalDrugAsWhenReady) {
		this.showFutureIntervalDrugAsWhenReady = showFutureIntervalDrugAsWhenReady;
	}

	public String get_dbgStr() {
		return _dbgStr;
	}

	public void set_dbgStr(String _dbgStr) {
		this._dbgStr = _dbgStr;
	}

	public String getFindRemainderFuture() {
		return findRemainderFuture;
	}

	public void setFindRemainderFuture(String findRemainderFuture) {
		this.findRemainderFuture = findRemainderFuture;
	}
}
