package eu.thermz.java.utils;

import java.util.Calendar;
import static java.util.Calendar.*;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author riccardo
 */
public class DateUtils {

	//START - helpers
	public static Date todayAtMidnight() {
		Calendar calendar = Calendar.getInstance();
		return new GregorianCalendar(
				calendar.get(YEAR),
				calendar.get(MONTH),
				calendar.get(DAY_OF_MONTH)).getTime();
	}
	public static Date firstOfCurrentMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(DAY_OF_MONTH, 1);
		return new GregorianCalendar(
				calendar.get(YEAR),
				calendar.get(MONTH),
				calendar.get(DAY_OF_MONTH)).getTime();
	}
	public static Date nextHourTick() {
		Date now = new Date();
		int nextHour = For.date(now).getHourOfDay() + 1;
		return JavaDateBuilder.at(now)
				.hour( nextHour )
				.minute(0)
				.second(0)
				.get();
	}
	//END - helpers

	/** Fluent interface to get a date from params */
	public static class JavaDateBuilder {
		Calendar cal = new GregorianCalendar();
		private JavaDateBuilder() {}
		private JavaDateBuilder(Date date) { cal.setTime(date); }
		
		public static JavaDateBuilder dateOf() {
			return new JavaDateBuilder();
		}
		public static JavaDateBuilder at(Date date){
			return new JavaDateBuilder(date);
		}

		private void setCal(int measure, int amount) {
			cal.set(measure, amount);
		}

		//YEAR
		public JavaDateBuilder year(int year) {
			setCal(YEAR, year);
			return this;
		}
		//MONTH
		public JavaDateBuilder month(int month) {
			setCal(MONTH, month);
			return this;
		}
		//DAYS
		public JavaDateBuilder day(int day) {
			setCal(DAY_OF_MONTH, day);
			return this;
		}
		//HOUR
		public JavaDateBuilder hour(int hour) {
			setCal(HOUR_OF_DAY, hour);
			return this;
		}
		//MINUTE
		public JavaDateBuilder minute(int min) {
			setCal(MINUTE, min);
			return this;
		}
		//SECOND
		public JavaDateBuilder second(int second) {
			setCal(SECOND, second);
			return this;
		}
		
		public Date get() {
			return cal.getTime();
		}
		
	}
	
	/** Fluent interface to get a date from another */
	public static class JavaDateDSL {

		Calendar cal = new GregorianCalendar();
		int calendarTimeMeasureUnit;
		int time;

		public JavaDateDSL(int time) {
			this.time = time;
		}

		public static JavaDateDSL dateOf(int time) {
			return new JavaDateDSL(time);
		}

		public JavaDateDSL years() {
			calendarTimeMeasureUnit = YEAR;
			return this;
		}

		public JavaDateDSL months() {
			calendarTimeMeasureUnit = MONTH;
			return this;
		}

		public JavaDateDSL days() {
			calendarTimeMeasureUnit = DAY_OF_MONTH;
			return this;
		}

		public JavaDateDSL hours() {
			calendarTimeMeasureUnit = HOUR_OF_DAY;
			return this;
		}

		public JavaDateDSL minutes() {
			calendarTimeMeasureUnit = MINUTE;
			return this;
		}

		public JavaDateDSL seconds() {
			calendarTimeMeasureUnit = SECOND;
			return this;
		}

		public Date ago() {
			cal.add(calendarTimeMeasureUnit, -time);
			return cal.getTime();
		}

		public Date fromNow() {
			cal.add(calendarTimeMeasureUnit, time);
			return cal.getTime();
		}

		public Date agoFrom(Date date) {
			cal.setTime(date);
			cal.add(calendarTimeMeasureUnit, -time);
			return cal.getTime();
		}

		public Date startingFrom(Date date) {
			cal.setTime(date);
			cal.add(calendarTimeMeasureUnit, time);
			return cal.getTime();
		}

	}
	
	public static class For {
		Calendar cal = Calendar.getInstance();
		private For(Date date) {
			cal.setTime(date);
		}
		public static For date(Date date){
			return new For(date);
		}
		public static For now(){
			return new For(new Date());
		}
		
		public int getHourOfDay() {
			return cal.get(HOUR_OF_DAY);
		}

		public int getMinute() {
			return cal.get(MINUTE);
		}
		
		public int getSeconds() {
			return cal.get(SECOND);
		}
		
		public int getYear(){
			return cal.get(YEAR);
		}
		
		public int getMonth(){
			return cal.get(MONTH)+1;
		}
		
		public int getDayOfMonth(){
			return cal.get(DAY_OF_MONTH);
		}

		public int getSecond() {
			return cal.get(SECOND);
		}
		
	}

	/** Fluent interface to get a particular current timing */
	public static class Current {

		public static int seconds() {
			return Calendar.getInstance().get(SECOND);
		}
		
		public static int minute() {
			return Calendar.getInstance().get(MINUTE);
		}
		
		public static int hourOfDay() {
			return Calendar.getInstance().get(HOUR_OF_DAY);
		}

		public static int dayOfMonth() {
			return Calendar.getInstance().get(DAY_OF_MONTH);
		}
		
		public static int year(){
			return Calendar.getInstance().get(YEAR);
		}
		
		public static int month(){
			return Calendar.getInstance().get(MONTH)+1;
		}
		
	}

	public static Date now(){
		return new Date();
	}
	
}
