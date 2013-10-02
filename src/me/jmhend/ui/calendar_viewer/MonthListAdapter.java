package me.jmhend.ui.calendar_viewer;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

/**
 * Adapter for presenting a List of months.
 * 
 * @author jmhend
 */
public class MonthListAdapter extends BaseAdapter implements OnDayClickListener {
	
	private static final String TAG = MonthListAdapter.class.getSimpleName();
	
////=====================================================================================
//// Member variables.
////=====================================================================================
	
	private final Context mContext;
	private int mFirstDayOfWeek;
	private CalendarDay mStartDay;
	private CalendarDay mEndDay;
	private CalendarDay mSelectedDay;
	private final CalendarDay mCurrentDay;
	private int mCount;
	
	private OnDayClickListener mExternalListener;
	
////=====================================================================================
//// Constructor.
////=====================================================================================
	
	/**
	 * Constructor.
	 * @param context
	 * @param controller
	 */
	public MonthListAdapter(Context context, CalendarViewerConfig config, OnDayClickListener listener) {
		mContext = context;
		mExternalListener = listener;
		mCurrentDay = CalendarDay.currentDay();
		init(config);
		calculateCount();
	}
	
////=====================================================================================
//// Init
////=====================================================================================
	
	/**
	 * Initialize.
	 */
	private void init(CalendarViewerConfig config) {
		mFirstDayOfWeek = config.getFirstDayOfWeek();
		mStartDay = config.getStartDay();
		mEndDay = config.getEndDay();
		mSelectedDay = config.getSelectedDay();
	}
	
////=====================================================================================
//// BaseAdapter
////=====================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return mCount;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MonthView monthView;
		if (convertView == null) {
			monthView = new MonthView(mContext);
			monthView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			monthView.setClickable(true);
			monthView.setOnDayClickListener(this);
		} else {
			monthView = (MonthView) convertView;
		}
		
		Map<String, Integer> params = (Map<String, Integer>) monthView.getTag();
		if (params == null) {
			params = new HashMap<String, Integer>();
		}
		
		// Generate MonthView data.
		final int month = getMonthForPosition(position);
		final int year = getYearForPosition(position);
		final int selectedDay = isSelectedDayInMonth(year, month) ? mSelectedDay.dayOfMonth : -1;
		params.put(MonthView.KEY_MONTH, Integer.valueOf(month));
		params.put(MonthView.KEY_YEAR, Integer.valueOf(year));
		params.put(MonthView.KEY_SELECTED_DAY, Integer.valueOf(selectedDay));
		params.put(MonthView.KEY_WEEK_START, Integer.valueOf(mFirstDayOfWeek));
		params.put(MonthView.KEY_CURRENT_YEAR, Integer.valueOf(mCurrentDay.year));
		params.put(MonthView.KEY_CURRENT_MONTH, Integer.valueOf(mCurrentDay.month));
		params.put(MonthView.KEY_CURRENT_DAY_OF_MONTH, Integer.valueOf(mCurrentDay.dayOfMonth));

		monthView.reset();
		monthView.setMonthParams(params);
		monthView.invalidate();
		return monthView;
	}
	
////=====================================================================================
//// Positioning.
////=====================================================================================
	
	/**
	 * Calculates how many months the list will contain.
	 */
	private void calculateCount() {
		int startMonths = mStartDay.year * 12 + mStartDay.month;
		int endMonths = mEndDay.year * 12 + mEndDay.month;
		int months = endMonths - startMonths + 1;
		mCount = months;
	}
	
	/**
	 * Gets which month to display for 'position'
	 * @param position
	 * @return
	 */
	private int getMonthForPosition(int position) {
		int month = (position + mStartDay.month) % 12;
		return month;
	}
	
	/**
	 * Gets which year to display for 'position'
	 * @param position
	 * @return
	 */
	private int getYearForPosition(int position) {
		int year = (position + mStartDay.month) / 12 + mStartDay.year;
		return year;
	}
	
	/**
	 * Sets which day to select.
	 * @param day
	 */
	public void setSelectedDay(CalendarDay day) {
		mSelectedDay = day;
		notifyDataSetChanged();
	}
	
	/**
	 * 
	 * @param day
	 * @return
	 */
	public int getPositionForDay(CalendarDay day) {
		if (day.isBeforeDay(mStartDay) || day.isAfterDay(mEndDay)) {
			return -1;
		}
		int monthDiff = day.month - mStartDay.month;
		int yearDiff = day.year - mStartDay.year;
		int position = yearDiff * 12 + monthDiff;
		return position;
	}
	
	/**
	 * @param year
	 * @param month
	 * @return True if the currently selected day is the month.
	 */
	private boolean isSelectedDayInMonth(int year, int month) {
		return mSelectedDay.year == year && mSelectedDay.month == month;
	}
	
////=====================================================================================
//// OnDayClickListener
////=====================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.MonthView.OnDayClickListener#onDayClick(me.jmhend.ui.calendar_viewer.MonthView, me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay)
	 */
	@Override
	public void onDayClick(View calendarView, CalendarDay day) {
		if (day != null) {
			if (!Utils.isDayCurrentOrFuture(day)) {
				return;
			}
			setSelectedDay(day);
			if (mExternalListener != null) {
				mExternalListener.onDayClick(calendarView, day);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.MonthView.OnDayClickListener#onDayLongClick(me.jmhend.ui.calendar_viewer.MonthView, me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay)
	 */
	@Override
	public void onDayLongClick(View calendarView, CalendarDay day) {
		if (day != null) {
			if (!Utils.isDayCurrentOrFuture(day)) {
				return;
			}
			if (mExternalListener != null) {
				mExternalListener.onDayLongClick(calendarView, day);
			}
		}
	}

////=====================================================================================
//// CalendarDay
////=====================================================================================
	
	/**
	 * Represents a day on the Calendar.
	 * 
	 * @author jmhend
	 */
	public static class CalendarDay {
		int year;
		int month;
		int dayOfMonth;
		
		/**
		 * @return CalendarDay initialized to the current day.
		 */
		public static CalendarDay currentDay() {
			return fromCalendar(Calendar.getInstance());
		}
		
		/**
		 * @param calendar
		 * @return CalendarDay initialized to the same day has 'calendar'
		 * is current set to.
		 */
		public static CalendarDay fromCalendar(Calendar calendar) {
			return new CalendarDay(calendar.get(Calendar.YEAR), 
					calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH));
		}
		
		/**
		 * @param time
		 * @return CalendarDay initialized to the same day as 'time'.
		 */
		public static CalendarDay fromTime(long time) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(time);
			return fromCalendar(calendar);
		}
		
		/**
		 * Constructor with all args.
		 * @param year
		 * @param month
		 * @param dayOfMonth
		 */
		public CalendarDay(int year, int month, int dayOfMonth) { 
			this.year = year;
			this.month = month;
			this.dayOfMonth = dayOfMonth;
		}
		
		/**
		 * Fill from another CalendarDay.
		 * @param day
		 */
		public void set(CalendarDay day) {
			this.year = day.year;
			this.month = day.month;
			this.dayOfMonth = day.dayOfMonth;
		}
		
		/**
		 * Set date fields.
		 * @param year
		 * @param month
		 * @param dayOfMonth
		 */
		public void set(int year, int month, int dayOfMonth) {
			this.year = year;
			this.month = month;
			this.dayOfMonth = dayOfMonth;
		}
		
		/**
		 * @param day
		 * @return True if this CalendarDay is the same day as 'day'.
		 */
		public boolean isSameDay(CalendarDay day) {
			return (year == day.year) && (month == day.month) && (dayOfMonth == day.dayOfMonth);
		}
		
		/**
		 * @param day
		 * @return True if this CalendarDay is before 'day'.
		 */
		public boolean isBeforeDay(CalendarDay day) {
			if (year < day.year) {
				return true;
			}
			if (year > day.year) {
				return false;
			}
			if (month < day.month) {
				return true;
			}
			if (month > day.month) {
				return false;
			}
			return dayOfMonth < day.dayOfMonth;
		}
		
		/**
		 * @param day
		 * @return True if this CalendarDay is after 'day'.
		 */
		public boolean isAfterDay(CalendarDay day) {
			return !isSameDay(day) && !isBeforeDay(day);
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return (month + 1) + "/" + dayOfMonth + "/" + year;
		}
	}
}