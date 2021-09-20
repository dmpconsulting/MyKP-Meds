package com.montunosoftware.pillpopper.model;

import android.content.Context;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.util.WDHM;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.model.HistoryEvent;
import com.montunosoftware.pillpopper.database.persistence.DatabaseUtils;
import com.montunosoftware.pillpopper.model.PillpopperDay.DayOfWeek;
import com.montunosoftware.pillpopper.model.Schedule.SchedType;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class DoseEventCollection
{
	private Drug _drug;
	private PillpopperTime _maxPostponeTime; // latest time to which the dose can be postponed
	private DoseEvent _overdueEvent;
	private DoseEvent _nextEvent;
	private PillpopperTime _nextAlarmTime;
	private PillpopperTime _sortDate;
	private List<DoseEvent> _eventList;
	private List<PillpopperTime> _passedEventsList = new ArrayList<>();
	private PillpopperTime _date;
	private Context context;


	public PillpopperTime getMaxPostponeTime() { return _maxPostponeTime; }
	public DoseEvent getOverdueEvent() { return _overdueEvent; }
	public DoseEvent getNextEvent() { return _nextEvent; }
	public PillpopperTime getNextAlarmTime() { return _nextAlarmTime; }
	public PillpopperTime getSortDate() { return _sortDate; }
	public List<DoseEvent> getEventList() { return _eventList; }
	public List<PillpopperTime> pastEventsList() { return _passedEventsList; }

	public DoseEventCollection(Context context, Drug drug, PillpopperTime now, long secondaryReminderPeriodSecs)
	{
		this._drug = drug;
		computeDBDoseEvents(now, secondaryReminderPeriodSecs);
		this.context = context;
	}

	public DoseEventCollection(Context context,Drug drug, PillpopperTime now)
	{
		this._drug = drug;
		computePastReminderDrugs(drug, now);
		this.context=context;
	}

	private void computePastReminderDrugs(Drug drug, PillpopperTime now) {
		PillpopperDay today = now.getLocalDay();
		_eventList = getDBValidEventsNearDay(today);


		for (DoseEvent doseEvent: _eventList) {

			if (_drug.get_effLastTaken() == null || doseEvent.getDate().after(_drug.get_effLastTaken())) {
				if (doseEvent.getDate().after(now)) {
					doseEvent.setFuture(true);
				} else {
					doseEvent.setOverdue(true);
					_date = doseEvent.getDate();
				}
			}

			if (doseEvent.isOverdue()) {
				_overdueEvent = doseEvent;
			}


			if (doseEvent.isOverdue()) {
				if (isPendingPassedReminderPresent(_drug, _overdueEvent)) {
					if (!_passedEventsList.contains(doseEvent.getDate())
							&& !doseEvent.getDate().before(drug.getCreated())
							&& doseEvent.getDate().before(PillpopperTime.now())) {
						_passedEventsList.add(doseEvent.getDate());
					}
				}
			}

		}
	}

	private synchronized boolean isPendingPassedReminderPresent(Drug drug, DoseEvent endCheckPointEvent) {
		String lastMissedDosesValue = drug.getPreferences().getPreference("missedDosesLastChecked");
		if(null!=lastMissedDosesValue){

		}

		return null != endCheckPointEvent && !(endCheckPointEvent.getDate().before(drug.getCreated()))
				&& !endCheckPointEvent.getDate().after(PillpopperTime.now());
	}


	private void computeDBDoseEvents(PillpopperTime now, long secondaryReminderPeriodSecs) {

		PillpopperDay today = now.getLocalDay();
		Schedule sched = _drug.getSchedule();

		// Generate a list of candidate upcoming events
		_eventList = getDBValidEventsNearDay(today);

		LoggerUtils.info("_eventList size-- " + _eventList.size());

		for (DoseEvent doseEvent: _eventList) {
			// Is there a dose scheduled that we haven't yet taken?  If so,
			// the dose is either overdue or in the future.
			if (_drug.get_effLastTaken() == null || doseEvent.getDate().after(_drug.get_effLastTaken()) || !(Util.getScheduleChoice(_drug).equalsIgnoreCase(SchedType.SCHEDULED.toString()))) {
				if (doseEvent.getDate().after(now)) {
					doseEvent.setFuture(true);
				} else {
					doseEvent.setOverdue(true);
					_date = doseEvent.getDate();
				}
			}
			// If there's no postpone, stop processing this event.
			if (_drug.get_notifyAfter() == null || _drug.get_notifyAfter().before(doseEvent.getDate())){
				continue;
			}

			if (doseEvent.getDate().before(PillpopperTime.now())
					&& _drug.get_notifyAfter().after(doseEvent.getDate())
					&& !FrontController.getInstance(context).isHistoryEventAvailable(doseEvent.getDate(), _drug.getGuid())) {
				continue;
			}

			doseEvent.setOriginal_date(new PillpopperTime(doseEvent.getDate()));
			//check if there is a history event(Taken or skipped or missed) recorded for the original time, if so, ignore the dose event
			if (DatabaseUtils.getInstance(context).isHistoryEventForOriginalScheduleRecorded(String.valueOf(doseEvent.getDate().getGmtSeconds()), _drug.getGuid())) {
				continue;
			}
			HistoryEvent latestPostponeEvent = DatabaseUtils.getInstance(context).getLastPostponedHistoryEventForSpecificTime(_drug.getGuid(), String.valueOf(doseEvent.getDate().getGmtSeconds()));
			if (null != latestPostponeEvent && null != latestPostponeEvent.getPreferences() && latestPostponeEvent.getPreferences().isPostponedEventActive()) {
				doseEvent.setDate(new PillpopperTime(Long.parseLong(Util.convertDateIsoToLong(latestPostponeEvent.getPreferences().getFinalPostponedDateTime()))));
			} else {
				doseEvent.setDate(new PillpopperTime(_drug.get_notifyAfter()));
			}
			doseEvent.setPostponed(true);


			if(null!= doseEvent.getDate()){
				PillpopperLog.say("Dose Event Information : " + PillpopperTime.getDebugString(doseEvent.getDate())
						+ " isHistoryEvent Available : "
						+ FrontController.getInstance(context).isHistoryEventAvailable(doseEvent.getDate(), doseEvent.getDrug().getGuid())
						+ " is After now  : " + doseEvent.getDate().after(now));
			}


			if (doseEvent.isOverdue() && doseEvent.getDate().after(now) /*&&
					!FrontController.getInstance(context).isHistoryEventAvailable(doseEvent.getDate(), doseEvent.getDrug().getGuid())*/) {
				doseEvent.setOverdue(false);
				doseEvent.setFuture(true);

				LoggerUtils.info("doseEvent.overdue is set to false for " + doseEvent.getDrug().getName()
						+ " : And Evnt : " + PillpopperTime.getDebugString(doseEvent.getDate()));
			}

			if(/*!FrontController.getInstance(context).isHistoryEventAvailable(doseEvent.getDate(), doseEvent.getDrug().getGuid())
					&&*/ doseEvent.getDate().before(now)){
				doseEvent.setOverdue(true);
				doseEvent.setFuture(false);

				LoggerUtils.info("doseEvent.overdue is set to true for " + doseEvent.getDrug().getName()
				 + " : And Evnt : " + PillpopperTime.getDebugString(doseEvent.getDate()));
			}


		}

		// Final computations: now that we've enumerated dose events and processed
		// postpones, compute whether this drug is overdue, when the next dose is
		// scheduled to be alarmed, whether we can currently take or skip the pill,
		// etc.
		_overdueEvent = null;
		DoseEvent postponedFutureEvent = null;
		List<DoseEvent> futureDoses = new ArrayList<>();

		for (DoseEvent doseEvent: _eventList) {

			if (doseEvent.isOverdue()) {
				_overdueEvent = doseEvent;
			}

			try {
				if (doseEvent.isOverdue()) {
					if (!_passedEventsList.contains(doseEvent.getDate()) && (null != doseEvent.getDate() && null != doseEvent.getDrug().getCreated() && (null != doseEvent.getDrug() && null != doseEvent.getDrug().getPreferences().getPreference("missedDosesLastChecked")
									&& (doseEvent.getDate().getGmtSeconds() == Long.parseLong(doseEvent.getDrug().getPreferences().getPreference("missedDosesLastChecked")) || doseEvent.getDate().after(Util.convertStringtoPillpopperTime(doseEvent.getDrug().getPreferences().getPreference("missedDosesLastChecked"))))))) {
						_passedEventsList.add(doseEvent.getDate());
					} else {
						PillpopperLog.say("---- Adding Past Reminder Second time---");
					}
				}
			} catch (NumberFormatException ex){
				LoggerUtils.exception("DEBUG-- " + ex.getMessage());
			}


			if (doseEvent.isFuture()) {
				if (doseEvent.isPostponed()) {
					postponedFutureEvent = doseEvent;
				} else {
					futureDoses.add(doseEvent);
				}
			}
		}

		if (postponedFutureEvent != null) {
			futureDoses.add(0, postponedFutureEvent);
		}

		// Compute next dose
		if (futureDoses.isEmpty()) {
			_nextEvent = null;
		} else {
			_nextEvent = futureDoses.get(0);
		}

		// Compute the maximum time a pill can be postponed
		PillpopperTime thisTimeTomorrow = new PillpopperTime(now, WDHM.SecPerDay);
		if (!Util.getScheduleChoice(_drug).equalsIgnoreCase(SchedType.SCHEDULED.toString())) { // interval and asneeded pills
			_maxPostponeTime = thisTimeTomorrow;
		} else if (futureDoses.isEmpty()) { // no future doses
			_maxPostponeTime = null;
		} else if (_overdueEvent != null && null != _nextEvent) { // overdue pill - postpone until next dose
			_maxPostponeTime = _nextEvent.getDate();
		} else if (futureDoses.size() == 1) { // just one dose left
			_maxPostponeTime = thisTimeTomorrow;
		} else { // can't postpone past next dose
			_maxPostponeTime = futureDoses.get(1).getDate();
		}

		// Compute the date to use for sorted-by-next-dose
		if (sched.getEnd() != null && today.after(sched.getEnd())) {
			// If today is after the end date: the end date
			_sortDate = sched.getEnd().atLocalTime(new HourMinute(0, 0));
		} else if (_nextEvent != null) {
			// For scheduled & interval drugs with a next scheduled time: the next scheduled time
			_sortDate = _nextEvent.getDate();
		} else {
			switch (Util.getScheduleChoice(_drug)) {
				case AppConstants.SCHEDULE_CHOICE_SCHEDULED:  // scheduled drugs: jan 1, 1970 (so they appear last)
					_sortDate = PillpopperTime.epoch();
					break;
				case "interval": // Interval drugs: 1 second ago
					_sortDate = new PillpopperTime(now, -1);
					break;
				case AppConstants.SCHEDULE_CHOICE_UNDEFINED: // Interval drugs: 1 second ago
					_sortDate = new PillpopperTime(now, -1);
					break;
				case AppConstants.SCHEDULE_CHOICE_AS_NEEDED: // As-needed drugs: 2 second ago
					_sortDate = new PillpopperTime(now, -2);
			}
		}

		// Compute the time the next alarm for this drug should be generated
		if (!_drug.getRemindersActive() || _overdueEvent == null || !_drug.getSecondaryRemindersActive() || secondaryReminderPeriodSecs <= 0) {
			// if the drug isn't overdue, or we're not using secondary reminders,
			// the next alarm time is the time of the next dose (if any).
			// Even for drugs for which reminders are off, we still fire the
			// alarm so we can update missed doses in the history.
			_nextAlarmTime = _nextEvent == null ? null : _nextEvent.getDate();
		} else {
			// Secondary reminders are active for this drug,
			// and there's a dose overdue. Compute the earlier of
			// the next dose time and the next secondary reminder.
			long secsSinceOverdue = now.compareTo(_overdueEvent.getDate());
			long secsFromOverdueToNextReminder = secsSinceOverdue + (secondaryReminderPeriodSecs - secsSinceOverdue%secondaryReminderPeriodSecs);
			PillpopperTime nextSecondaryReminder = new PillpopperTime(_overdueEvent.getDate(), secsFromOverdueToNextReminder);

			// the next alarm is the sooner of:
			// 1-- the next time this dose is due (if any) and
			// 2-- the next time we're scheduled to get a reminder for this dose
			if (_nextEvent == null || _nextEvent.getDate().after(nextSecondaryReminder)) {
				_nextAlarmTime = nextSecondaryReminder;
			} else {
				_nextAlarmTime = _nextEvent.getDate();
			}
		}
	}

	public List<DoseEvent> 	getDBValidEventsNearDay(PillpopperDay day) {
		ArrayList<DoseEvent> eventList = new ArrayList<>();
		Schedule sched = _drug.getSchedule();

		if (Util.getScheduleChoice(_drug).equalsIgnoreCase(SchedType.SCHEDULED.toString())) {
			int dayPeriod = (int) sched.getDayPeriod();

			// Weekly drugs are scheduled separately.
			if (dayPeriod == 7) {
				boolean isCheckDay = false;
				//PillpopperDay startOfWeek;
				long dayNumber = 0;
				PillpopperDay startingDay = _drug.getSchedule().getStart();
				try {
					PillpopperDay startOfWeek = day.addDays(-day.getDayOfWeek().getDayNumber());

					for (int weekNumber = -1; weekNumber <= 3; weekNumber++) {
						ArrayList<String> aList = new ArrayList(Arrays.asList(_drug.getSchedule().getDays().trim().split(",")));
						for (int i = 0; i < aList.size(); i++) {
							DayOfWeek dayOfWeek = getDayOfWeek(Integer.parseInt(aList.get(i)));
							PillpopperDay eventDay = startOfWeek.addDays(weekNumber * 7 + dayOfWeek.getDayNumber());
							PillpopperLog.say("--Week Days If: " + dayOfWeek.toString() + " : weekNumber : " + weekNumber
									+ " : startOfWeek : " + startOfWeek.getDay() + " : eventDay : " + eventDay.getDayName());
							for (HourMinute gmtTimeOfDay : sched.getTimeList()) {
								eventList.add(new DoseEvent(_drug, eventDay.atLocalTime(gmtTimeOfDay)));
							}
						}
					}
				} catch (Exception e) {
					PillpopperLog.say(e.getMessage());
				}
			} else {
				PillpopperDay recentValidDay;

				// Find a recent day on which this pill was taken.  For pills that don't
				// have a dayPeriod, that's today.
				if (dayPeriod > 1) {
					PillpopperDay startRef;

					// If a start date for this pill was defined, start counting the
					// day periods from the start date.  Otherwise, start counting from
					// the pill's creation date.
					if (sched.getStart() != null) {
						startRef = sched.getStart();
					} else {
						startRef = _drug.getCreated().getLocalDay();
					}

					if (startRef.after(day)) {
						recentValidDay = startRef;
					} else {
						// valid day will be the same day in the next month in case the start date is before today.
						// eg if the startdate is 10th April and today is 13th April, so in this case the valid day will be 10th May.
						if (dayPeriod == 30 && startRef.getDay() <= 28) {
							recentValidDay = increaseMonth(day, 1);
							Calendar calendar = Calendar.getInstance();
							//date should be equivalent to start date and year should be same as today date
							//and month should be next month of today date which is calculated and stored in recent valid day in line 367.
							calendar.set(recentValidDay.getYear(), recentValidDay.getMonth(), startRef.getDay());
							recentValidDay = new PillpopperDay(calendar);
						} else if (dayPeriod == 30 && (startRef.getDay() == 29 || startRef.getDay() == 30)) {
							recentValidDay = increaseMonth(day, 1);
							Calendar cal = Calendar.getInstance();
							cal.set(Calendar.MONTH, recentValidDay.getMonth());
							// If the month is feb then the recentValidDay will be last day of the month
							if (recentValidDay.getMonth() == 1) {
								cal.set(recentValidDay.getYear(), recentValidDay.getMonth(), getValidDay(recentValidDay).getDay());
							} else {
								cal.set(recentValidDay.getYear(), recentValidDay.getMonth(), startRef.getDay());
							}
							recentValidDay = new PillpopperDay(cal);
						} else if (dayPeriod == 30 && startRef.getDay() == 31) {
							//get the last day of the month
							recentValidDay = getValidDay(increaseMonth(day, 1));
						} else {
							long daysElapsed = day.daysAfter(startRef);
							long completePeriods = daysElapsed / dayPeriod;
							recentValidDay = startRef.addDays(dayPeriod * completePeriods);
						}
					}
				} else {
					if (sched.getStart() != null && sched.getStart().after(day)) {
						recentValidDay = sched.getStart();
					} else {
						recentValidDay = day;
					}
				}

				// Now, for one period in the past and 3 in the future, enumerate the days
				// this pill is scheduled, and create a pill event for each.
				for (int i = -1; i <= 1; i++) {
					PillpopperDay eventDay;
					if (dayPeriod == 30) {
						if (_drug.getSchedule().getStart().getDay() <= 28) {
							eventDay = increaseMonth(recentValidDay, i);
						} else if (_drug.getSchedule().getStart().getDay() == 29 || _drug.getSchedule().getStart().getDay() == 30) {
							Calendar cal = Calendar.getInstance();
							if (recentValidDay.getMonth() == 1) {
								PillpopperDay newValidDay = increaseMonth(recentValidDay, i);
								cal.set(newValidDay.getYear(), newValidDay.getMonth(),
										newValidDay.getMonth() != 1 ? _drug.getSchedule().getStart().getDay() : recentValidDay.getDay());
							} else {
								eventDay = increaseMonth(recentValidDay, i);
								cal.set(eventDay.getYear(), eventDay.getMonth(), eventDay.getMonth() == 1 ? getValidDay(eventDay).getDay() : eventDay.getDay());
							}
							eventDay = new PillpopperDay(cal);
						} else if (_drug.getSchedule().getStart().getDay() == 31) {
							// replacing the date with the Maximum date of the month.
							eventDay = getValidDay(increaseMonth(recentValidDay, i));
						} else {
							int dayOffset = dayPeriod * i;
							eventDay = recentValidDay.addDays(dayOffset);
						}
					} else {
						int dayOffset = dayPeriod * i;
						eventDay = recentValidDay.addDays(dayOffset);
					}

					for (HourMinute gmtTimeOfDay : _drug.getSchedule().getTimeList()) {
						eventList.add(new DoseEvent(_drug, eventDay.atLocalTime(gmtTimeOfDay)));
					}
				}
			}
		}

		ArrayList<DoseEvent> newEventList = new ArrayList<>();

		for (DoseEvent doseEvent: eventList) {

			if (sched.getEnd() != null && doseEvent.getDate().getLocalDay().after(sched.getEnd())) {
				continue;
			}

			if (sched.getStart() != null && doseEvent.getDate().getLocalDay().before(sched.getStart())) {
				continue;
			}

			newEventList.add(doseEvent);
		}

		// Sort events by time
		eventList = newEventList;
		Collections.sort(eventList, (lhs, rhs) -> lhs.getDate().after(rhs.getDate()) ? 1 : (lhs.getDate().equals(rhs.getDate()) ? 0 : -1));

		// link each event to the next
		for (int i = 0; i < eventList.size()-1; i++) {
			eventList.get(i).setNext(eventList.get(i+1));
		}

		return eventList;
	}

	private static PillpopperDay getValidDay(PillpopperDay day) {
		PillpopperDay validDay = PillpopperDay.today();
		switch (day.getMonth()){
			case 0:
			case 2:
			case 4:
			case 6:
			case 7:
			case 9:
			case 11:
				validDay = new PillpopperDay(day.getYear(), day.getMonth(), 31);
				break;
			case 3:
			case 5:
			case 8:
			case 10:
				validDay = new PillpopperDay(day.getYear(), day.getMonth(), 30);
				break;
			case 1:
				validDay = new PillpopperDay(day.getYear(), day.getMonth(), checkIsLeapYear(day.getYear()) ? 29 : 28);
				break;
			default:
				break;
		}
		return validDay;
	}

	private static boolean checkIsLeapYear(int year) {
		if (year % 4 == 0) {
			if (year % 100 == 0) {
				return year % 400 == 0;
			}
			return true;
		} else {
			return false;
		}
	}

	private static PillpopperDay increaseMonth(PillpopperDay recentValidDay, int i) {
		Calendar cal = Calendar.getInstance();
		cal.set(recentValidDay.getYear(),
				recentValidDay.getMonth(),
				recentValidDay.getDay());
		cal.add(Calendar.MONTH, i);
		return new PillpopperDay(cal);
	}

	public static List<DoseEvent> getDBValidEventsNearDayToLogMissedDoses(Drug _drug, PillpopperDay startDay, PillpopperDay endDay) {
		ArrayList<DoseEvent> eventList = new ArrayList<>();
		Schedule sched = _drug.getSchedule();

		switch (Util.getScheduleChoice(_drug)) {
			case AppConstants.SCHEDULE_CHOICE_SCHEDULED:
				int dayPeriod = (int) sched.getDayPeriod();

				// Weekly drugs are scheduled separately.
				if (dayPeriod == 7) {
					try{
						PillpopperDay startOfWeek = startDay.addDays(-startDay.getDayOfWeek().getDayNumber());

						for (int weekNumber = 0; weekNumber <= endDay.daysAfter(startDay)/7; weekNumber++) {
							ArrayList<String> aList = new ArrayList(Arrays.asList(_drug.getSchedule().getDays().trim().split(",")));
							for (int i = 0; i < aList.size(); i++) {
								DayOfWeek dayOfWeek = getDayOfWeek(Integer.parseInt(aList.get(i)));
								PillpopperDay eventDay = startOfWeek.addDays(weekNumber*7 + dayOfWeek.getDayNumber());
								PillpopperLog.say("--Week Days If: " + dayOfWeek.toString() + " : weekNumber : " + weekNumber
										+ " : startOfWeek : "+ startOfWeek.getDay()+ " : eventDay : " + eventDay.getDayName());
								for (HourMinute gmtTimeOfDay: sched.getTimeList()) {
									eventList.add(new DoseEvent(_drug, eventDay.atLocalTime(gmtTimeOfDay)));
								}
							}
						}
					}catch (Exception e) {
						PillpopperLog.say(e.getMessage());
					}
				} else {
					PillpopperDay recentValidDay;
					// Find a recent day on which this pill was taken.  For pills that don't
					// have a dayPeriod, that's today.
					if (dayPeriod > 1) {
						PillpopperDay startRef;
						// If a start date for this pill was defined, start counting the
						// day periods from the start date.  Otherwise, start counting from
						// the pill's creation date.
						if (sched.getStart() != null) {
							startRef = sched.getStart();
						} else {
							startRef = _drug.getCreated().getLocalDay();
						}

						if (startRef.after(startDay)) {
							recentValidDay = startRef;
						} else {
							if (dayPeriod == 30 && startRef.getDay() <= 28) {
								recentValidDay = increaseMonth(startDay,1);
								Calendar calendar = Calendar.getInstance();
								/* date should be equivalent to start date and year should be same as today date
									and month should be next month of today date which is calculated and stored in recent valid day in line 367.
								*/
								calendar.set(startDay.getYear(), recentValidDay.getMonth(), startRef.getDay());
								recentValidDay = new PillpopperDay(calendar);
							} else if (dayPeriod == 30 && (startRef.getDay() == 29 || startRef.getDay() == 30)) {
								recentValidDay = increaseMonth(startDay, 1);
								Calendar cal = Calendar.getInstance();
								cal.set(Calendar.MONTH, recentValidDay.getMonth() + 1);
								// If the month is feb then the recentValidDay will be last day of the month
								if (recentValidDay.getMonth() == 1) {
									cal.set(startDay.getYear(), recentValidDay.getMonth(), cal.getActualMaximum(Calendar.DAY_OF_MONTH));
								} else {
									cal.set(startDay.getYear(), recentValidDay.getMonth(), startRef.getDay());
								}
								recentValidDay = new PillpopperDay(cal);
							}  else if (dayPeriod == 30 && startRef.getDay() == 31) {
								//get the last day of the month
								recentValidDay = getValidDay(increaseMonth(startDay, 1));
							} else {
								long daysElapsed = startDay.daysAfter(startRef);
								long completePeriods = daysElapsed / dayPeriod;
								recentValidDay = startRef.addDays(dayPeriod * completePeriods);
							}
						}
					}else {
						if (sched.getStart() != null && sched.getStart().after(startDay)) {
							recentValidDay = sched.getStart();
						} else {
							recentValidDay = startDay;
						}
					}

					//calculate difference of months for monthly scheduled drug
					long index;
					if(dayPeriod == 30){
						Calendar startDateCalendar = Calendar.getInstance();
						startDateCalendar.set(startDay.getYear(),startDay.getMonth(),startDay.getDay());
						Calendar endDateCalendar = Calendar.getInstance();
						endDateCalendar.set(endDay.getYear(), endDay.getMonth(), endDay.getDay());
						int monthsBetween = 0;
						int dateDiff = endDateCalendar.get(Calendar.DAY_OF_MONTH) - startDateCalendar.get(Calendar.DAY_OF_MONTH);

						if (dateDiff < 0) {
							int borrow = endDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
							dateDiff = (endDateCalendar.get(Calendar.DAY_OF_MONTH) + borrow) - startDateCalendar.get(Calendar.DAY_OF_MONTH);
							monthsBetween--;

							if (dateDiff > 0) {
								monthsBetween++;
							}
						} else {
							monthsBetween++;
						}
						monthsBetween += endDateCalendar.get(Calendar.MONTH) - startDateCalendar.get(Calendar.MONTH);
						monthsBetween += (endDateCalendar.get(Calendar.YEAR) - startDateCalendar.get(Calendar.YEAR)) * 12;
						index = monthsBetween;
					}else{
						index = endDay.daysAfter(startDay);
					}

					// Now, for one period in the past and 3 in the future, enumerate the days
					// this pill is scheduled, and create a pill event for each.
					for (int i = 0; i <= index; i++) {
						PillpopperDay eventDay;
						if (dayPeriod == 30) {
							if (_drug.getSchedule().getStart().getDay() <= 28) {
								eventDay = increaseMonth(_drug.getSchedule().getStart(), i);
							} else if (_drug.getSchedule().getStart().getDay() == 29 || _drug.getSchedule().getStart().getDay() == 30) {
								eventDay = increaseMonth(_drug.getSchedule().getStart(), i);
								Calendar cal = Calendar.getInstance();
								//DE20982
								cal.set(eventDay.getYear(), eventDay.getMonth(),eventDay.getMonth() == 1 ? getValidDay(eventDay).getDay() : eventDay.getDay());
								eventDay = new PillpopperDay(cal);
							} else if (_drug.getSchedule().getStart().getDay() == 31) {
								// replacing the date with the Maximum date of the month.
								eventDay = getValidDay(increaseMonth(_drug.getSchedule().getStart(), i));

							} else {
								int dayOffset = dayPeriod * i;
								eventDay = recentValidDay.addDays(dayOffset);
							}
						} else {
							int dayOffset = dayPeriod * i;
							eventDay = recentValidDay.addDays(dayOffset);
						}

						for (HourMinute gmtTimeOfDay : _drug.getSchedule().getTimeList()) {
							eventList.add(new DoseEvent(_drug, eventDay.atLocalTime(gmtTimeOfDay)));
						}
					}
				}
				break;
		}

		return eventList;
	}

	private static DayOfWeek getDayOfWeek(int day)
	{
		switch (day) {
			case Calendar.SUNDAY: return DayOfWeek.Sunday;
			case Calendar.MONDAY: return DayOfWeek.Monday;
			case Calendar.TUESDAY: return DayOfWeek.Tuesday;
			case Calendar.WEDNESDAY: return DayOfWeek.Wednesday;
			case Calendar.THURSDAY: return DayOfWeek.Thursday;
			case Calendar.FRIDAY: return DayOfWeek.Friday;
			case Calendar.SATURDAY: return DayOfWeek.Saturday;
			default: return DayOfWeek.Sunday;
		}
	}
}
