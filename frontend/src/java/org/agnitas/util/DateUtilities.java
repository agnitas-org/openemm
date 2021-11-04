/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

public class DateUtilities {
	private static final transient Logger logger = Logger.getLogger(DateUtilities.class);

	public static final String DD_MM_YYYY_HH_MM_SS = "dd.MM.yyyy HH:mm:ss";
	public static final String DD_MM_YYYY_HH_MM = "dd.MM.yyyy HH:mm";
	public static final String DD_MM_YYYY_HH = "dd.MM.yyyy HH";
	public static final String DD_MM_YYYY = "dd.MM.yyyy";
	public static final String MM_DD_YYYY = "MM/dd/yyyy";
	public static final String MM_DD_YYYY_HH_MM = "MM/dd/yyyy HH:mm";
	public static final String MM_DD_YYYY_HH_MM_SS = "MM/dd/yyyy HH:mm:ss";
	public static final String DDMMYYYY = "ddMMyyyy";
	public static final String YYYY_MM_DD_HH_MM_SS_MS = "yyyy-MM-dd_HH:mm:ss,SSS";
	public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String DD_MM_YYYY_HH_MM_HYPHEN = "dd-MM-yyyy HH:mm";
	public static final String YYYY_MM_DD_HH_MM_SS_FORFILENAMES = "yyyy-MM-dd_HH-mm-ss";
	public static final String YYYY_MM_DD = "yyyy-MM-dd";
	public static final String YYYYMMDD = "yyyyMMdd";
	public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	public static final String YYYYMD = "yyyy_M_d";
	public static final String HHMM = "HHmm";
	public static final String HH_MM = "HH:mm";
	public static final String RFC822FORMAT = "E, d MMM yyyy HH:mm:ss Z";
	public static final Locale RFC822FORMAT_LOCALE = Locale.US;
	public static final Locale DUTCH = new Locale("nl", "NL");
	public static final Locale PORTUGAL = new Locale("pt", "PT");
	public static final Locale SPAIN = new Locale("es", "ES");

	/** Date format for SOAP Webservices (ISO 8601) */
	public static final String ISO_8601_DATE_FORMAT_NO_TIMEZONE = "yyyy-MM-dd";
	/** Date format for SOAP Webservices (ISO 8601) */
	private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-ddX";
	/** DateTime format for SOAP Webservices (ISO 8601) */
	private static final String ISO_8601_DATETIME_FORMAT_NO_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss";
	/** DateTime format for SOAP Webservices (ISO 8601) */
	public static final String ISO_8601_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";
	/** DateTime format for SOAP Webservices (ISO 8601) */
	private static final String ISO_8601_DATETIME_FORMAT_WITH_MILLIS_NO_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	/** DateTime format for SOAP Webservices (ISO 8601) */
	public static final String ISO_8601_DATETIME_FORMAT_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

	public static final TimeZone UTC = TimeZone.getTimeZone("UTC");
	public static final ZoneId UTC_ZONE = UTC.toZoneId();

	private static final Pattern MONTH_RULE_PATTERN = Pattern.compile("\\d{0,2}M\\d{2}:\\d{4}");
	private static final Pattern WEEKDAILY_RULE_PATTERN = Pattern.compile("\\d\\D\\D:\\d{4}");

	public static long A_DAYS_MILLISECONDS = 1000 * 60 * 60 * 24;

	private final static Pattern MONTH_REGEX = Pattern.compile("m+",  Pattern.CASE_INSENSITIVE);
	private final static Pattern DAY_REGEX = Pattern.compile("d+",  Pattern.CASE_INSENSITIVE);
	private final static Pattern YEAR_REGEX = Pattern.compile("y+",  Pattern.CASE_INSENSITIVE);
	
	private final static List<Locale> SUPPORTED_LOCALES = Arrays.asList(
			Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH, SPAIN, PORTUGAL, DUTCH, Locale.ITALIAN);

	public enum TimespanID {
		previous_week,
		previous_7_days,
		previous_month,
		current_year,
		previous_year;
		
		public static TimespanID fromString(String value) {
			if (value != null) {
				for (TimespanID item : TimespanID.values()) {
					if (item.toString().replace("_", "").equalsIgnoreCase(value.replace("_", ""))) {
						return item;
					}
				}
			}
			throw new IllegalArgumentException("Invalid TimespanID");
		}
	}

	public static Tuple<Date, Date> getTimespan(String timespanId) {
		return getTimespan(TimespanID.fromString(timespanId));
	}

	public static Tuple<Date, Date> getTimespan(TimespanID timespanId) {
		return getTimespan(timespanId, null);
	}

	public static Tuple<Date, Date> getTimespan(TimespanID timespanId, Date calculationBase) {
		Date now = calculationBase;
		if (now == null) {
			now = new Date();
		}

		Calendar today = new GregorianCalendar();
		today.setTime(now);
		today = removeTime(today);

		Date start;
		Date end;
		if (TimespanID.previous_week == timespanId) {
			Calendar previousWeekStart = (GregorianCalendar) today.clone();
			previousWeekStart.add(Calendar.DAY_OF_MONTH, -7);
			previousWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			start = previousWeekStart.getTime();
			Calendar previousWeekEnd = (GregorianCalendar) today.clone();
			previousWeekEnd.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			previousWeekEnd.add(Calendar.MILLISECOND, -1);
			end = previousWeekEnd.getTime();
		} else if (TimespanID.previous_7_days == timespanId) {
			Calendar sevenDaysAgo = (GregorianCalendar) today.clone();
			sevenDaysAgo.add(Calendar.DAY_OF_MONTH, -7);
			start = sevenDaysAgo.getTime();
			Calendar yesterdayEnd = (GregorianCalendar) today.clone();
			yesterdayEnd.add(Calendar.MILLISECOND, -1);
			end = yesterdayEnd.getTime();
		} else if (TimespanID.previous_month == timespanId) {
			Calendar oneMonthAgo = (GregorianCalendar) today.clone();
			oneMonthAgo.set(Calendar.DAY_OF_MONTH, 1);
			oneMonthAgo.add(Calendar.MONTH, -1);
			start = oneMonthAgo.getTime();
			Calendar monthEnd = (GregorianCalendar) today.clone();
			monthEnd.set(Calendar.DAY_OF_MONTH, 1);
			monthEnd.add(Calendar.MILLISECOND, -1);
			end = monthEnd.getTime();
		} else if (TimespanID.current_year == timespanId) {
			Calendar yearStart = (GregorianCalendar) today.clone();
			yearStart.set(Calendar.DAY_OF_MONTH, 1);
			yearStart.set(Calendar.MONTH, Calendar.JANUARY);
			start = yearStart.getTime();
			Calendar oneMillisecondAgo = (GregorianCalendar) today.clone();
			oneMillisecondAgo.add(Calendar.MILLISECOND, -1);
			end = oneMillisecondAgo.getTime();
		} else if (TimespanID.previous_year == timespanId) {
			Calendar previousYearStart = (GregorianCalendar) today.clone();
			previousYearStart.set(Calendar.DAY_OF_MONTH, 1);
			previousYearStart.set(Calendar.MONTH, Calendar.JANUARY);
			previousYearStart.add(Calendar.YEAR, -1);
			start = previousYearStart.getTime();
			Calendar previousYearEnd = (GregorianCalendar) today.clone();
			previousYearEnd.set(Calendar.DAY_OF_MONTH, 1);
			previousYearEnd.set(Calendar.MONTH, Calendar.JANUARY);
			previousYearEnd.add(Calendar.MILLISECOND, -1);
			end = previousYearEnd.getTime();
		} else  {
			throw new IllegalArgumentException("TimespanID is invalid");
		}

		return new Tuple<>(start, end);
	}

	public static Calendar getNowWithoutMilliseconds() {
		return removeMilliseconds(new GregorianCalendar());
	}

	public static Calendar calendar(Date date) {
		return calendar(date, UTC);
	}

	public static Calendar calendar(Date date, TimeZone zone) {
		Calendar calendar = Calendar.getInstance(zone);
		calendar.setTime(date);
		return calendar;
	}

	public static Calendar removeTime(Calendar calendar) {
		Calendar returnCalendar = (Calendar) calendar.clone();
		returnCalendar.set(Calendar.HOUR_OF_DAY, 0);
		returnCalendar.set(Calendar.MINUTE, 0);
		returnCalendar.set(Calendar.SECOND, 0);
		returnCalendar.set(Calendar.MILLISECOND, 0);
		return returnCalendar;
	}

	public static Date removeTime(Date date, TimeZone zone) {
		if (date == null) {
			return null;
		}

		Calendar calendar = Calendar.getInstance(zone);
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static Calendar removeMilliseconds(Calendar calendar) {
		Calendar returnCalendar = (Calendar) calendar.clone();
		returnCalendar.set(Calendar.MILLISECOND, 0);
		return returnCalendar;
	}

	public static Date removeMilliseconds(Date date) {
		Calendar returnCalendar = new GregorianCalendar();
		returnCalendar.setTime(date);
		returnCalendar.set(Calendar.MILLISECOND, 0);
		return returnCalendar.getTime();
	}

	public static Date setMaximumTime(Date date) {
		return setMaximumTime(date, UTC);
	}

	public static Date setMaximumTime(Date date, TimeZone zone) {
		if (date == null) {
			return null;
		}

		Calendar calendar = Calendar.getInstance(zone);
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	public static Date now() {
		return new Date();
	}

	public static Date midnight() {
		return midnight(new Date());
	}

	public static Date midnight(Date date) {
		return removeTime(date, UTC);
	}

	public static Date midnight(TimeZone zone) {
		return removeTime(new Date(), zone);
	}

	public static Date midnight(Date date, TimeZone zone) {
		return removeTime(date, zone);
	}

	public static Date midnight(ZoneId zoneId) {
	    return toDate(LocalDate.now(), zoneId);
    }

	public static Date mergeDateTime(Date date, Date time, TimeZone zone) {
		Calendar calendar = Calendar.getInstance(zone);
		calendar.setTime(date);

		Calendar dateTime = Calendar.getInstance(zone);
		dateTime.setTime(time);
		dateTime.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

		return dateTime.getTime();
	}

	public static LocalDateTime merge(LocalDate date, LocalTime time) {
		if (date == null) {
			return null;
		}

		if (time == null) {
			return date.atStartOfDay();
		}

		return LocalDateTime.of(date, time);
	}

	public static Date merge(Date date, int hours, int minutes, TimeZone timezone) {
		if (date == null) {
			return null;
		}

		Calendar calendar = DateUtilities.calendar(date, timezone);
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, minutes);

		return calendar.getTime();
	}

	public static Date calculateNextJobStart(String timingString) {
		GregorianCalendar now = new GregorianCalendar();
		return calculateNextJobStart(now, timingString, TimeZone.getDefault());
	}

	public static Date calculateNextJobStart(String timingString, String timeZone) {
		GregorianCalendar now = new GregorianCalendar();
		TimeZone zone;
		if (!StringUtils.isEmpty(timeZone)) {
			zone = TimeZone.getTimeZone(timeZone);
		} else {
			zone = TimeZone.getDefault();
		}
		return calculateNextJobStart(now, timingString, zone);
	}
	
	/**
	 * Calculation of next scheduled job start
	 * Timingparameter may contain weekdays, clocktimes, months, quarters and holidays
	 * 
	 * Allowed parameters:
	 * "ONCE"                      => only once (returns null)
	 * "0600;0800"                 => daily at 06:00 and 08:00
	 * "MoMi:1700"                 => Every monday and wednesday at 17:00
	 * "M05:1600"                  => every 05th day of month at 16:00
	 * "Q:1600"                    => every first day of quarter at 16:00
	 * "QW:1600"                   => every first working day of quarter at 16:00
	 * "MoDiMiDoFr:1700;!23012011" => mondays to fridays at 17:00 exept for 23.01.2011 (Holidays marked by '!')
	 * 
	 * All values may be combined separated by semicolons.
	 * 
	 * @param timingString
	 * @return
	 * @throws Exception
	 */
	public static Date calculateNextJobStart(GregorianCalendar now, String timingString, TimeZone timeZone) {
		if (StringUtils.isBlank(timingString) || "once".equalsIgnoreCase(timingString)) {
			return null;
		}
		
		GregorianCalendar returnStart = null;
		
		// Holidays to exclude
		List<GregorianCalendar> excludedDays = new ArrayList<>();
		
		String[] timingParameterList = timingString.split(";|,| ");
		for (String timingParameter : timingParameterList) {
			if (timingParameter.startsWith("!")) {
				try {
					GregorianCalendar exclusionDate = new GregorianCalendar(timeZone);
					SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
					format.setTimeZone(timeZone);
					exclusionDate.setTime(format.parse(timingParameter.substring(1)));
					excludedDays.add(exclusionDate);
				} catch (ParseException e) {
					logger.error("Error occured: " + e.getMessage(), e);
				}
			}
		}
		
		for (String timingParameter : timingParameterList) {
			GregorianCalendar nextStartByThisParameter = new GregorianCalendar(timeZone);
			nextStartByThisParameter.setTime(now.getTime());
			// Make "week of year" ISO-8601 compliant
			makeWeekOfYearISO8601Compliant(nextStartByThisParameter);

			if (timingParameter.startsWith("!")) {
				// Exclusions are done previously
				continue;
			} else if (!timingParameter.contains(":")) {
				if (AgnUtils.isDigit(timingParameter)) {
					if (timingParameter.length() == 4) {
						// daily execution on given time
						nextStartByThisParameter.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(timingParameter.substring(0, 2)));
						nextStartByThisParameter.set(GregorianCalendar.MINUTE, Integer.parseInt(timingParameter.substring(2)));
						nextStartByThisParameter.set(GregorianCalendar.SECOND, 0);
						nextStartByThisParameter.set(GregorianCalendar.MILLISECOND, 0);
		
						// Move next start into future (+1 day) until rule is matched
						// Move also when meeting holiday rule
						while (!nextStartByThisParameter.after(now) && (returnStart == null || nextStartByThisParameter.before(returnStart))
								|| AgnUtils.dayListIncludes(excludedDays, nextStartByThisParameter)) {
							nextStartByThisParameter.add(GregorianCalendar.DAY_OF_MONTH, 1);
						}
					} else if (timingParameter.length() == 8) {
						// execution on given day
						try {
							SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
							format.setTimeZone(timeZone);
							nextStartByThisParameter.setTime(format.parse(timingParameter));
						} catch (ParseException e) {
							throw new RuntimeException("Invalid interval description");
						}
						
						if (AgnUtils.dayListIncludes(excludedDays, nextStartByThisParameter)) {
							continue;
						}
					}
				} else if (timingParameter.contains("*") && timingParameter.length() == 4) {
					// daily execution on given time with wildcards '*' like '*4*5'
					nextStartByThisParameter.set(GregorianCalendar.SECOND, 0);
					nextStartByThisParameter.set(GregorianCalendar.MILLISECOND, 0);
	
					// Move next start into future (+1 minute) until rule is matched
					// Move also when meeting holiday rule
					while (!nextStartByThisParameter.after(now) && (returnStart == null || nextStartByThisParameter.before(returnStart))
							|| AgnUtils.dayListIncludes(excludedDays, nextStartByThisParameter)
							|| !checkTimeMatchesPattern(timingParameter, nextStartByThisParameter.getTime())) {
						nextStartByThisParameter.add(GregorianCalendar.MINUTE, 1);
					}
				} else {
					// Fr: weekly execution on Friday at 00:00 Uhr
					boolean onlyWithinOddWeeks = false;
					boolean onlyWithinEvenWeeks = false;
					List<Integer> weekdayIndexes = new ArrayList<>();
					for (String weekDay : AgnUtils.chopToChunks(timingParameter, 2)) {
						if (weekDay.equalsIgnoreCase("ev")) {
							onlyWithinEvenWeeks = true;
						} else if (weekDay.equalsIgnoreCase("od")) {
							onlyWithinOddWeeks = true;
						} else {
							weekdayIndexes.add(getWeekdayIndex(weekDay));
						}
					}
					nextStartByThisParameter.set(GregorianCalendar.HOUR_OF_DAY,0);
					nextStartByThisParameter.set(GregorianCalendar.MINUTE, 0);
					nextStartByThisParameter.set(GregorianCalendar.SECOND, 0);
					nextStartByThisParameter.set(GregorianCalendar.MILLISECOND, 0);

					// Move next start into future (+1 day) until rule is matched
					// Move also when meeting holiday rule
					while ((!nextStartByThisParameter.after(now)
							|| !weekdayIndexes.contains(nextStartByThisParameter.get(Calendar.DAY_OF_WEEK))) && (returnStart == null || nextStartByThisParameter.before(returnStart))
							|| AgnUtils.dayListIncludes(excludedDays, nextStartByThisParameter)
							|| (onlyWithinOddWeeks && (nextStartByThisParameter.get(Calendar.WEEK_OF_YEAR) % 2 == 0))
							|| (onlyWithinEvenWeeks && (nextStartByThisParameter.get(Calendar.WEEK_OF_YEAR) % 2 != 0))) {
						nextStartByThisParameter.add(GregorianCalendar.DAY_OF_MONTH, 1);
					}
				}
			} else if (MONTH_RULE_PATTERN.matcher(timingParameter).matches()) {
				// month rule "M99:1700" (every month at ultimo)
				// month rule "06M01:1700" (every half a year at months first day)
				String xMonth = timingParameter.substring(0, timingParameter.indexOf("M"));
				if (xMonth.length() == 0) {
					xMonth = "1";
				}
				String day = timingParameter.substring(timingParameter.indexOf("M") + 1, timingParameter.indexOf(":"));
				String time = timingParameter.substring(timingParameter.indexOf(":") + 1);

				nextStartByThisParameter.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
				nextStartByThisParameter.set(GregorianCalendar.MINUTE, Integer.parseInt(time.substring(2)));
				nextStartByThisParameter.set(GregorianCalendar.SECOND, 0);
				nextStartByThisParameter.set(GregorianCalendar.MILLISECOND, 0);
				
				if (day.equals("99")) {
					// special day ultimo
					nextStartByThisParameter.set(GregorianCalendar.DAY_OF_MONTH, nextStartByThisParameter.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
					// ensure that the first estimated "next time" is in the past, before making forward steps
					if (nextStartByThisParameter.after(now)) {
						nextStartByThisParameter.set(GregorianCalendar.DAY_OF_MONTH, 1);
						nextStartByThisParameter.add(GregorianCalendar.MONTH, -1);
						nextStartByThisParameter.set(GregorianCalendar.DAY_OF_MONTH, nextStartByThisParameter.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
					}
				} else {
					nextStartByThisParameter.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(day));
					// ensure that the first estimated "next time" is in the past, before making forward steps
					if (nextStartByThisParameter.after(now)) {
						nextStartByThisParameter.add(GregorianCalendar.MONTH, -1);
					}
				}
				
				// Make forward step
				if (day.equals("99")) {
					// special day ultimo
					nextStartByThisParameter.set(GregorianCalendar.DAY_OF_MONTH, 1);
					nextStartByThisParameter.add(GregorianCalendar.MONTH, Integer.parseInt(xMonth));
					nextStartByThisParameter.set(GregorianCalendar.DAY_OF_MONTH, nextStartByThisParameter.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
				} else {
					nextStartByThisParameter.add(GregorianCalendar.MONTH, Integer.parseInt(xMonth));
				}
				
				// Move also when meeting holiday rule
				while (AgnUtils.dayListIncludes(excludedDays, nextStartByThisParameter)) {
					nextStartByThisParameter.add(GregorianCalendar.DAY_OF_YEAR, 1);
				}
			} else if (timingParameter.startsWith("Q:")) {
				// quarterly execution (Q:1200) at first day of month
				if (nextStartByThisParameter.get(GregorianCalendar.MONTH) < GregorianCalendar.APRIL) {
					nextStartByThisParameter.set(GregorianCalendar.MONTH, GregorianCalendar.APRIL);
				} else if (nextStartByThisParameter.get(GregorianCalendar.MONTH) < GregorianCalendar.JULY) {
					nextStartByThisParameter.set(GregorianCalendar.MONTH, GregorianCalendar.JULY);
				} else if (nextStartByThisParameter.get(GregorianCalendar.MONTH) < GregorianCalendar.OCTOBER) {
					nextStartByThisParameter.set(GregorianCalendar.MONTH, GregorianCalendar.OCTOBER);
				} else {
					nextStartByThisParameter.set(GregorianCalendar.MONTH, GregorianCalendar.JANUARY);
					nextStartByThisParameter.add(GregorianCalendar.YEAR, 1);
				}
				
				nextStartByThisParameter.set(GregorianCalendar.DAY_OF_MONTH, 1);
				String time = timingParameter.substring(timingParameter.indexOf(":") + 1);
				nextStartByThisParameter.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
				nextStartByThisParameter.set(GregorianCalendar.MINUTE, Integer.parseInt(time.substring(2)));
				nextStartByThisParameter.set(GregorianCalendar.SECOND, 0);
				nextStartByThisParameter.set(GregorianCalendar.MILLISECOND, 0);
				
				// Move also when meeting holiday rule
				while (AgnUtils.dayListIncludes(excludedDays, nextStartByThisParameter)) {
					nextStartByThisParameter.add(GregorianCalendar.DAY_OF_YEAR, 1);
				}
			} else if (timingParameter.startsWith("QW:")) {
				// quarterly execution (QW:1200) at first workingday of month
				if (nextStartByThisParameter.get(GregorianCalendar.MONTH) < GregorianCalendar.APRIL) {
					nextStartByThisParameter.set(GregorianCalendar.MONTH, GregorianCalendar.APRIL);
				} else if (nextStartByThisParameter.get(GregorianCalendar.MONTH) < GregorianCalendar.JULY) {
					nextStartByThisParameter.set(GregorianCalendar.MONTH, GregorianCalendar.JULY);
				} else if (nextStartByThisParameter.get(GregorianCalendar.MONTH) < GregorianCalendar.OCTOBER) {
					nextStartByThisParameter.set(GregorianCalendar.MONTH, GregorianCalendar.OCTOBER);
				} else {
					nextStartByThisParameter.set(GregorianCalendar.MONTH, GregorianCalendar.JANUARY);
					nextStartByThisParameter.add(GregorianCalendar.YEAR, 1);
				}
				
				nextStartByThisParameter.set(GregorianCalendar.DAY_OF_MONTH, 1);

				// Move also when meeting holiday rule
				while (nextStartByThisParameter.get(GregorianCalendar.DAY_OF_WEEK) == java.util.Calendar.SATURDAY
					|| nextStartByThisParameter.get(GregorianCalendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY
					|| AgnUtils.dayListIncludes(excludedDays, nextStartByThisParameter)) {
					nextStartByThisParameter.add(GregorianCalendar.DAY_OF_MONTH, 1);
				}

				String time = timingParameter.substring(timingParameter.indexOf(":") + 1);
				nextStartByThisParameter.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
				nextStartByThisParameter.set(GregorianCalendar.MINUTE, Integer.parseInt(time.substring(2)));
				nextStartByThisParameter.set(GregorianCalendar.SECOND, 0);
				nextStartByThisParameter.set(GregorianCalendar.MILLISECOND, 0);
			} else if (WEEKDAILY_RULE_PATTERN.matcher(timingParameter).matches()) {
				// every xth of a weekday in a month
				int weekDayOrder = Integer.parseInt(timingParameter.substring(0, 1));
				if (weekDayOrder < 1 || 5 < weekDayOrder) {
					throw new RuntimeException("Invalid interval description");
				}
				String weekDaySign = timingParameter.substring(1, 3);
				String time = timingParameter.substring(timingParameter.indexOf(":") + 1);
				int weekdayIndex = getWeekdayIndex(weekDaySign);
				nextStartByThisParameter.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
				nextStartByThisParameter.set(GregorianCalendar.MINUTE, Integer.parseInt(time.substring(2)));
				nextStartByThisParameter.set(GregorianCalendar.SECOND, 0);
				nextStartByThisParameter.set(GregorianCalendar.MILLISECOND, 0);

				// Move next start into future (+1 day) until rule is matched
				// Move also when meeting holiday rule
				while ((!nextStartByThisParameter.after(now)
						|| weekdayIndex != nextStartByThisParameter.get(Calendar.DAY_OF_WEEK)) && (returnStart == null || nextStartByThisParameter.before(returnStart))
						|| AgnUtils.dayListIncludes(excludedDays, nextStartByThisParameter)
						|| weekDayOrder != getNumberOfWeekdayInMonth(nextStartByThisParameter.get(Calendar.DAY_OF_MONTH))) {
					nextStartByThisParameter.add(GregorianCalendar.DAY_OF_MONTH, 1);
				}
			} else {
				// weekday execution (also allows workingday execution, german: "Werktagssteuerung" by "MoTuWeThFr:0000")
				String weekDays = timingParameter.substring(0, timingParameter.indexOf(":"));
				boolean onlyWithinOddWeeks = false;
				boolean onlyWithinEvenWeeks = false;
				String time = timingParameter.substring(timingParameter.indexOf(":") + 1);
				List<Integer> weekdayIndexes = new ArrayList<>();
				for (String weekDay : AgnUtils.chopToChunks(weekDays, 2)) {
					if (weekDay.equalsIgnoreCase("ev")) {
						onlyWithinEvenWeeks = true;
					} else if (weekDay.equalsIgnoreCase("od")) {
						onlyWithinOddWeeks = true;
					} else {
						int weekdayIndex = getWeekdayIndex(weekDay);
						if (weekdayIndex < 0) {
							throw new RuntimeException("Invalid weekday in timing data: " + timingString);
						}
						weekdayIndexes.add(weekdayIndex);
					}
				}
				if (weekdayIndexes.isEmpty()) {
					throw new RuntimeException("Invalid timing data: " + timingString);
				}
				nextStartByThisParameter.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
				nextStartByThisParameter.set(GregorianCalendar.MINUTE, Integer.parseInt(time.substring(2)));
				nextStartByThisParameter.set(GregorianCalendar.SECOND, 0);
				nextStartByThisParameter.set(GregorianCalendar.MILLISECOND, 0);

				// Move next start into future (+1 day) until rule is matched
				// Move also when meeting holiday rule
				while ((!nextStartByThisParameter.after(now)
						|| !weekdayIndexes.contains(nextStartByThisParameter.get(Calendar.DAY_OF_WEEK))) && (returnStart == null || nextStartByThisParameter.before(returnStart))
						|| AgnUtils.dayListIncludes(excludedDays, nextStartByThisParameter)
						|| (onlyWithinOddWeeks && (nextStartByThisParameter.get(Calendar.WEEK_OF_YEAR) % 2 == 0))
						|| (onlyWithinEvenWeeks && (nextStartByThisParameter.get(Calendar.WEEK_OF_YEAR) % 2 != 0))) {
					nextStartByThisParameter.add(GregorianCalendar.DAY_OF_MONTH, 1);
				}
			}

			if (returnStart == null || nextStartByThisParameter.before(returnStart)) {
				returnStart = nextStartByThisParameter;
			}
		}

		if (returnStart == null) {
			throw new RuntimeException("Invalid interval description");
		}

		return returnStart.getTime();
	}

	public static int getWeekdayIndex(String weekday) {
		if (StringUtils.isBlank(weekday)) {
			return -1;
		} else {
			weekday = weekday.toLowerCase().trim();
			String[] localeWeekdays = DateFormatSymbols.getInstance().getWeekdays();
			for (int i = 0; i < localeWeekdays.length; i++) {
				if (localeWeekdays[i].toLowerCase().startsWith(weekday)) {
					return i;
				}
			}
			
			if (weekday.startsWith("so") || weekday.startsWith("su")) {
				return Calendar.SUNDAY;
			} else if (weekday.startsWith("mo")) {
				return Calendar.MONDAY;
			} else if (weekday.startsWith("di") || weekday.startsWith("tu")) {
				return Calendar.TUESDAY;
			} else if (weekday.startsWith("mi") || weekday.startsWith("we")) {
				return Calendar.WEDNESDAY;
			} else if (weekday.startsWith("do") || weekday.startsWith("th")) {
				return Calendar.THURSDAY;
			} else if (weekday.startsWith("fr")) {
				return Calendar.FRIDAY;
			} else if (weekday.startsWith("sa")) {
				return Calendar.SATURDAY;
			} else {
				return -1;
			}
		}
	}
	
	public static boolean isWeekDayActive(String intervalPattern, int weekDay) {
		return isWeekDayActive(intervalPattern, weekDay, null);
	}
	
	public static boolean isWeekDayActive(String intervalPattern, int weekDay, Locale locale) {
		if (StringUtils.isNotEmpty(intervalPattern)) {
			String pattern = intervalPattern.toLowerCase();
			
			if (locale == null) {
				locale = Locale.getDefault();
			}
			
			if (SUPPORTED_LOCALES.contains(locale)) {
				String localizedWeekDayName = getWeekdayShortname(weekDay, locale).toLowerCase();
				if (pattern.contains(localizedWeekDayName)) {
					return true;
				}
			}
			
			String weekDayUS = getWeekdayShortname(weekDay, Locale.ENGLISH).toLowerCase();
			String weekDayDE = getWeekdayShortname(weekDay, Locale.GERMAN).toLowerCase();
			return pattern.contains(weekDayUS) || pattern.contains(weekDayDE);
		}
		
		return false;
	}
	
	public static boolean checkTimeMatchesPattern(String pattern, Date time) {
		Pattern timePattern = Pattern.compile(pattern.replace("*", "."));
		String timeString = new SimpleDateFormat(HHMM).format(time);
		return timePattern.matcher(timeString).matches();
	}
	
	/**
	 * Get week day shortname according to default locale
	 * Support only {@link Locale#GERMAN} and {@link Locale#ENGLISH}
	 * @param weekdayId
	 * @return two first letter of week day name
	 */
	public static String getWeekdayShortnameIgnoreOtherLocale(int weekdayId) {
		Locale defaultLocale = Locale.getDefault();
		Locale locale = defaultLocale.getLanguage().equals("de") ? defaultLocale : Locale.ENGLISH;
		return getWeekdayShortname(weekdayId, locale);
	}
	
	/**
	 * Get week day shortname according to passed locale
	 * @param weekdayId week day identifier from 1 (Sunday) to 7 (Saturday)
	 * @return two first letter of week day name
	 */
	public static String getWeekdayShortname(int weekdayId) {
		return getWeekdayShortname(weekdayId, Locale.ENGLISH);
	}
	
	public static String getWeekdayShortname(int weekdayId, Locale locale) {
		if (weekdayId > 0 && weekdayId < 8) {
			if (weekdayId == 1) {
				weekdayId = 8;
			}
			
			if (locale == null) {
				locale = Locale.getDefault();
			}
			return getWeekdayShortname(DayOfWeek.of(weekdayId - 1), locale);
		}
		return "";
	}
	
	private static String getWeekdayShortname(DayOfWeek weekday, Locale locale) {
		String displayName = weekday.getDisplayName(TextStyle.FULL, locale);
		return StringUtils.substring(displayName, 0, 2);
	}

	public static Date addDaysToDate(Date initDate, int daysToAdd) {
		GregorianCalendar returnDate = new GregorianCalendar();
		returnDate.setTime(initDate);
		returnDate.add(Calendar.DAY_OF_MONTH, daysToAdd);
		return returnDate.getTime();
    }

	public static Date addMinutesToDate(Date initDate, int minutesToAdd) {
		GregorianCalendar returnDate = new GregorianCalendar();
		returnDate.setTime(initDate);
		returnDate.add(Calendar.MINUTE, minutesToAdd);
		return returnDate.getTime();
    }
	
	public static Date getDateOfYearsAgo(Date initDate, int yearsAgo) {
		GregorianCalendar returnDate = new GregorianCalendar();
		returnDate.setTime(initDate);
		returnDate.add(Calendar.YEAR, -yearsAgo);
		return returnDate.getTime();
    }
    
    public static Date getDateOfYearsAgo(int yearsAgo) {
    	return getDateOfYearsAgo(new Date(), yearsAgo);
    }

    public static Date getDateOfDaysAgo(Date initDate, int daysAgo) {
		GregorianCalendar returnDate = new GregorianCalendar();
		returnDate.setTime(initDate);
		returnDate.add(Calendar.DAY_OF_MONTH, -daysAgo);
		return returnDate.getTime();
    }
    
    public static Date getDateOfDaysAgo(int daysAgo) {
    	return getDateOfDaysAgo(new Date(), daysAgo);
    }
    
    public static Date getDateOfHoursAgo(Date initDate, int hoursAgo) {
		GregorianCalendar returnDate = new GregorianCalendar();
		returnDate.setTime(initDate);
		returnDate.add(Calendar.HOUR, -hoursAgo);
		return returnDate.getTime();
    }
    
    public static Date getDateOfHoursAgo(int hoursAgo) {
    	return getDateOfHoursAgo(new Date(), hoursAgo);
    }
    
    public static Date getDateOfMinutesAgo(Date initDate, int minutesAgo) {
		GregorianCalendar returnDate = new GregorianCalendar();
		returnDate.setTime(initDate);
		returnDate.add(Calendar.MINUTE, -minutesAgo);
		return returnDate.getTime();
    }
    
    public static Date getDateOfMinutesAgo(int minutesAgo) {
    	return getDateOfMinutesAgo(new Date(), minutesAgo);
    }
    
    public static Date getDateOfMillisAgo(Date initDate, int millisAgo) {
		GregorianCalendar returnDate = new GregorianCalendar();
		returnDate.setTime(initDate);
		returnDate.add(Calendar.MILLISECOND, -millisAgo);
		return returnDate.getTime();
    }
    
    public static Date getDateOfMillisAgo(int millisAgo) {
    	return getDateOfMillisAgo(new Date(), millisAgo);
    }
    
    public static Date parseUnknownDateFormat(String value) throws Exception {
		String[] patterns = new String[]{DD_MM_YYYY_HH_MM_SS, DD_MM_YYYY_HH_MM, DD_MM_YYYY, DD_MM_YYYY_HH_MM_HYPHEN, YYYY_MM_DD_HH_MM_SS, YYYY_MM_DD_HH_MM, YYYY_MM_DD, YYYYMMDDHHMMSS, DDMMYYYY, MM_DD_YYYY_HH_MM_SS, MM_DD_YYYY_HH_MM, MM_DD_YYYY};

		for (String pattern : patterns) {
			try {
				return new SimpleDateFormat(pattern).parse(value);
			} catch (ParseException e) {
				// Do nothing.
			}
		}

		throw new Exception("Unknown date format");
    }

    public static String detectSimpleFormat(String value) throws Exception {
		String[] patterns = new String[]{DD_MM_YYYY_HH_MM_SS, DD_MM_YYYY_HH_MM, DD_MM_YYYY, YYYY_MM_DD_HH_MM_SS, YYYY_MM_DD_HH_MM, YYYY_MM_DD, YYYYMMDDHHMMSS, DDMMYYYY, MM_DD_YYYY_HH_MM_SS, MM_DD_YYYY_HH_MM, MM_DD_YYYY};

		for (String pattern : patterns) {
			try {
				new SimpleDateFormat(pattern).parse(value);
				return pattern;
			} catch (ParseException e) {
				// Do nothing.
			}
		}

		throw new Exception("Unknown date format");
	}

    public static Date getDateRoundedMilliseconds(Date date) {
    	return new Date((date.getTime() + 500) / 1000 * 1000);
    }

	public static boolean equals(Date a, Date b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return a.equals(b);
	}

    /**
     * Parse DateTime strings for SOAP Webservices (ISO 8601)
     * 
     * @param dateValue
     * @return
     * @throws ParseException
     */
    public static Date parseIso8601DateTimeString(String dateValue) throws ParseException {
    	if (StringUtils.isBlank(dateValue)) {
    		return null;
    	}
    	
    	dateValue = dateValue.toUpperCase();
    	
    	if (dateValue.endsWith("Z")) {
    		// Standardize UTC time
    		dateValue = dateValue.replace("Z", "+00:00");
    	}
    	
    	boolean hasTimezone = false;
    	if (dateValue.length() > 6 && dateValue.charAt(dateValue.length() - 3) == ':' && (dateValue.charAt(dateValue.length() - 6) == '+' || dateValue.charAt(dateValue.length() - 6) == '-')) {
    		hasTimezone = true;
    	}
    	
    	if (dateValue.contains("T")) {
    		if (dateValue.contains(".")) {
        		if (hasTimezone) {
        			if (dateValue.substring(dateValue.indexOf(".")).length() > 10 ) {
                		// Date with time and fractals
    	    			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSSXXXXX");
    	    		    LocalDateTime dateTime = LocalDateTime.parse(dateValue, formatter);
    	    		    return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
            		} else {
    	        		// Date with time and milliseconds
	        			SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATETIME_FORMAT_WITH_MILLIS);
	        			dateFormat.setLenient(false);
	        			return dateFormat.parse(dateValue);
            		}
        		} else {
        			if (dateValue.substring(dateValue.indexOf(".")).length() > 4 ) {
                		// Date with time and fractals
    	    			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSS");
    	    		    LocalDateTime dateTime = LocalDateTime.parse(dateValue, formatter);
    	    		    return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
            		} else {
    	        		// Date with time and milliseconds
	        			SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATETIME_FORMAT_WITH_MILLIS_NO_TIMEZONE);
	        			dateFormat.setLenient(false);
	        			return dateFormat.parse(dateValue);
            		}
        		}
    		} else {
	    		// Date with time
	    		if (hasTimezone) {
	    			SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATETIME_FORMAT);
	    			dateFormat.setLenient(false);
	    			return dateFormat.parse(dateValue);
	    		} else {
	    			SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATETIME_FORMAT_NO_TIMEZONE);
	    			dateFormat.setLenient(false);
	    			return dateFormat.parse(dateValue);
	    		}
    		}
    	} else {
    		// Date only
    		if (hasTimezone) {
    			SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATE_FORMAT);
    			dateFormat.setLenient(false);
    			return dateFormat.parse(dateValue);
    		} else {
    			SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATE_FORMAT_NO_TIMEZONE);
    			dateFormat.setLenient(false);
    			return dateFormat.parse(dateValue);
    		}
    	}
    }

	public static int compare(Date d1, Date d2) {
		if (d1 == d2) {
			return 0;
		} else if (d1 == null) {
			return -1;
		} else if (d2 == null) {
			return 1;
		} else {
			return d1.compareTo(d2);
		}
	}

	public static Date max(Date... dates) {
		Date max = null;
		for (Date date : dates) {
			if (max == null || max.before(date)) {
				max = date;
			}
		}
		return max;
	}

	public static Date min(Date... dates) {
		Date min = null;
		for (Date date : dates) {
			if (min == null || min.after(date)) {
				min = date;
			}
		}
		return min;
	}

	public static Date parse(String dateAsString, DateFormat format, Date defaultValue) {
		if (StringUtils.isNotBlank(dateAsString)) {
			try {
				return format.parse(dateAsString);
			} catch (ParseException e) {
				logger.debug("Error occurred when parsing date: " + e.getMessage(), e);
			}
		}
		return defaultValue;
	}

	public static Date parse(String dateAsString, DateFormat format, TimeZone zone, Date defaultValue) {
		format = (DateFormat) format.clone();
		format.setTimeZone(zone);

		return parse(dateAsString, format, defaultValue);
	}

	public static Date parse(String dateAsString, DateFormat format, TimeZone zone) {
		return parse(dateAsString, format, zone, null);
	}

	public static Date parse(String dateAsString, DateFormat format) {
		return parse(dateAsString, format, (Date) null);
	}

	public static LocalDateTime parse(String dateAsString, DateTimeFormatter formatter) {
    	return parse(dateAsString, formatter, null);
	}

	public static LocalDateTime parse(String dateAsString, DateTimeFormatter formatter, LocalDateTime defaultValue) {
		if (StringUtils.isNotBlank(dateAsString)) {
			try {
				return LocalDateTime.parse(dateAsString, formatter);
			} catch (DateTimeParseException e) {
				logger.debug("Error occurred when parsing date/time: " + e.getMessage(), e);
			}
		}
		return defaultValue;
	}

	public static LocalDate parseDate(String dateAsString, DateTimeFormatter formatter) {
    	return parseDate(dateAsString, formatter, null);
	}

	public static LocalDate parseDate(String dateAsString, DateTimeFormatter formatter, LocalDate defaultValue) {
		if (StringUtils.isNotBlank(dateAsString)) {
			try {
				return LocalDate.parse(dateAsString, formatter);
			} catch (DateTimeParseException e) {
				logger.debug("Error occurred when parsing date: " + e.getMessage(), e);
			}
		}
		return defaultValue;
	}

	public static LocalTime parseTime(String timeAsString, DateTimeFormatter formatter) {
    	return parseTime(timeAsString, formatter, null);
	}

	public static LocalTime parseTime(String timeAsString, DateTimeFormatter formatter, LocalTime defaultValue) {
		if (StringUtils.isNotBlank(timeAsString)) {
			try {
				return LocalTime.parse(timeAsString, formatter);
			} catch (DateTimeParseException e) {
				logger.debug("Error occurred when parsing time: " + e.getMessage(), e);
			}
		}
		return defaultValue;
	}

	public static boolean isPast(Date date) {
    	if (date == null) {
    		return false;
		}

		return date.before(new Date());
	}

	public static boolean isFuture(Date date) {
    	if (date == null) {
    		return false;
		}

		return date.after(new Date());
	}

	public static int getMonth(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH);
	}

	public static int getYear(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	/**
	 * Make a calendars "week of year" ISO-8601 compliant
	 */
	public static Calendar makeWeekOfYearISO8601Compliant(Calendar calendar) {
		calendar.setMinimalDaysInFirstWeek(4);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		return calendar;
	}

	/**
	 * Calculate the day between two Dates
	 * Watchout for timezone changes because of DST (e.g.: CEST and CET)
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int getDaysBetween(Date date1, Date date2) {
    	return Math.abs(Math.round((date1.getTime() - date2.getTime()) / (float) A_DAYS_MILLISECONDS));
	}

	public static SimpleDateFormat getFormat(String pattern, TimeZone timezone) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		format.setTimeZone(timezone);
		return format;
	}

	public static SimpleDateFormat getFormat(String pattern) {
		return getFormat(pattern, UTC);
	}

	/**
	 * A shortcut for {@link #toDate(java.time.LocalDate, java.time.ZoneId)} treating given date as UTC.
	 *
	 * @param date a local date object.
	 * @return instant date object.
	 */
	public static Date toDate(LocalDate date) {
		return toDate(date, UTC_ZONE);
	}

	/**
	 * Convert local date belonging to a particular timezone into instant date object.
	 *
	 * @param date a local date object.
	 * @param zoneId a timezone that a local date belongs to.
	 * @return instant date object.
	 */
	public static Date toDate(LocalDate date, ZoneId zoneId) {
		if (date == null) {
			return null;
		}
		return Date.from(date.atStartOfDay(zoneId).toInstant());
	}

	/**
	 * A shortcut for {@link #toDate(java.time.LocalDate, java.time.LocalTime, java.time.ZoneId)} treating given
	 * date and time as UTC.
	 *
	 * @param date a local date object.
	 * @param time a local time object.
	 * @return instant date object.
	 */
	public static Date toDate(LocalDate date, LocalTime time) {
		return toDate(date, time, UTC_ZONE);
	}

	/**
	 * Compose local date and time belonging to a particular timezone and convert into instant date object.
	 * See also {@link LocalDateTime#of(java.time.LocalDate, java.time.LocalTime)}.
	 *
	 * @param date a local date object.
	 * @param time a local time object.
	 * @param zoneId a timezone that a local date/time belong to.
	 * @return instant date object.
	 */
	public static Date toDate(LocalDate date, LocalTime time, ZoneId zoneId) {
		if (date == null || time == null) {
			return null;
		}
		return Date.from(date.atTime(time).atZone(zoneId).toInstant());
	}

	/**
	 * A shortcut for {@link #toDate(java.time.LocalDateTime, java.time.ZoneId)} treating given date/time as UTC.
	 *
	 * @param dateTime a local date/time object.
	 * @return instant date object.
	 */
	public static Date toDate(LocalDateTime dateTime) {
		return toDate(dateTime, UTC_ZONE);
	}

	/**
	 * Convert local date/time belonging to a particular timezone into instant date object.
	 *
	 * @param dateTime a local date/time object.
	 * @param zoneId a timezone that a local date/time belong to.
	 * @return instant date object.
	 */
	public static Date toDate(LocalDateTime dateTime, ZoneId zoneId) {
		if (dateTime == null) {
			return null;
		}
		return Date.from(dateTime.atZone(zoneId).toInstant());
	}

	/**
	 * A shortcut for {@link #toLocalDateTime(java.util.Date, java.time.ZoneId)} using UTC timezone.
	 *
	 * @param date an instant date object to be converted.
	 * @return a local date/time object representing instant date in UTC.
	 */
	public static LocalDateTime toLocalDateTime(Date date) {
		return toLocalDateTime(date, UTC_ZONE);
	}

	/**
	 * Represent instant date as local date/time object using given timezone.
	 *
	 * @param date an instant date object to be converted.
	 * @param zoneId a timezone to be used to convert a local date/time.
	 * @return a local date/time object representing instant date in a particular timezone.
	 */
	public static LocalDateTime toLocalDateTime(Date date, ZoneId zoneId) {
		if (date == null) {
			return null;
		}
		return LocalDateTime.ofInstant(date.toInstant(), zoneId);
	}

	/**
	 * A shortcut for {@link #toLocalDate(java.util.Date, java.time.ZoneId)} using UTC timezone.
	 *
	 * @param date an instant date object.
	 * @return a local date object representing given instant date in UTC.
	 */
	public static LocalDate toLocalDate(Date date) {
		return toLocalDate(date, UTC_ZONE);
	}

	/**
	 * Get local date from an instant date object using given timezone.
	 *
	 * @param date an instant date object.
	 * @param zoneId a timezone to be used to calculate a local date.
	 * @return a local date object representing given instant date in a particular timezone.
	 */
	public static LocalDate toLocalDate(Date date, ZoneId zoneId) {
		LocalDateTime dateTime = toLocalDateTime(date, zoneId);

		if (dateTime == null) {
			return null;
		}

		return dateTime.toLocalDate();
	}

	/**
	 * A shortcut for {@link #toLocalTime(java.util.Date, java.time.ZoneId)} using UTC timezone.
	 *
	 * @param date an instant date object.
	 * @return a local time object representing given instant date in UTC.
	 */
	public static LocalTime toLocalTime(Date date) {
		return toLocalTime(date, UTC_ZONE);
	}

	/**
	 * Get local time from an instant date object using given timezone.
	 *
	 * @param date an instant date object.
	 * @param zoneId a timezone to be used to calculate a local time.
	 * @return a local time object representing given instant date in a particular timezone.
	 */
	public static LocalTime toLocalTime(Date date, ZoneId zoneId) {
		LocalDateTime dateTime = toLocalDateTime(date, zoneId);

		if (dateTime == null) {
			return null;
		}

		return dateTime.toLocalTime();
	}

	/**
	 * Get locale-dependent timezone-aware date/time format using predefined notations (see {@link DateFormat#FULL},
	 * {@link DateFormat#LONG}, {@link DateFormat#MEDIUM}, {@link DateFormat#SHORT} and {@link DateFormat#DEFAULT}).
	 *
	 * @param dateStyle the given date formatting style.
	 * @param timeStyle the given time formatting style.
	 * @param locale a locale to be used to produce locale-dependent date format.
	 * @param timezone a timezone to be assigned to date format object.
	 * @return a locale-dependent timezone-aware date format object.
	 */
	public static SimpleDateFormat getDateTimeFormat(int dateStyle, int timeStyle, Locale locale, TimeZone timezone) {
		SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
		format.applyPattern(format.toPattern().replaceFirst("y+", "yyyy").replaceFirst(", ", " "));
		format.setTimeZone(timezone);
		return format;
	}

	public static SimpleDateFormat getDateTimeFormat(int dateStyle, int timeStyle, Locale locale) {
		SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
		format.applyPattern(format.toPattern().replaceFirst("y+", "yyyy").replaceFirst(", ", " "));
		return format;
	}

	/**
	 * Get locale-dependent date/time format pattern using predefined notations (see {@link DateFormat#FULL},
	 * {@link DateFormat#LONG}, {@link DateFormat#MEDIUM}, {@link DateFormat#SHORT} and {@link DateFormat#DEFAULT}).
	 *
	 * @param dateStyle the given date formatting style.
	 * @param timeStyle the given time formatting style.
	 * @param locale a locale to be used to produce locale-dependent date format.
	 * @return a locale-dependent date format pattern string.
	 */
	public static String getDateTimeFormatPattern(int dateStyle, int timeStyle, Locale locale) {
		SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
		format.applyPattern(format.toPattern().replaceFirst("y+", "yyyy").replaceFirst(", ", " "));
		return format.toPattern();
	}

	/**
	 * Get locale-dependent timezone-aware date format (without time) using predefined notations (see {@link DateFormat#FULL},
	 * {@link DateFormat#LONG}, {@link DateFormat#MEDIUM}, {@link DateFormat#SHORT} and {@link DateFormat#DEFAULT}).
	 *
	 * @param style the given date formatting style.
	 * @param locale a locale to be used to produce locale-dependent date format.
	 * @param timezone a timezone to be assigned to date format object.
	 * @return a locale-dependent timezone-aware date format object.
	 */
	public static SimpleDateFormat getDateFormat(int style, Locale locale, TimeZone timezone) {
		SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getDateInstance(style, locale);
		format.setTimeZone(timezone);
		return format;
	}
	
	public static SimpleDateFormat getDateFormat(int style, Locale locale) {
		SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getDateInstance(style, locale);
		return format;
	}

	/**
	 * Replace "yyyy" -> "%Y", "MM" -> "%m", "dd" -> "%d"
	 */
	public static String toC3DateFormatPattern(String pattern) {
		pattern = MONTH_REGEX.matcher(pattern)
				.replaceFirst("%m");

		pattern = DAY_REGEX.matcher(pattern)
				.replaceFirst("%d");

		pattern = YEAR_REGEX.matcher(pattern)
				.replaceFirst("%Y");

		return pattern;
	}
	
	public static String replaceDatePatternsInFileName(String fileNameWithPatterns, int companyID, ZonedDateTime zonedDateTime) {
    	if (StringUtils.isNotBlank(fileNameWithPatterns)) {
    		DecimalFormat twoDigitsFormat = new DecimalFormat("00");
    		GregorianCalendar now = new GregorianCalendar();
    		String filename = fileNameWithPatterns;
    		filename = filename.replace("[CID]", Integer.toString(companyID));
    		if (zonedDateTime == null) {
	    		filename = filename.replace("[YYYY]", Integer.toString(now.get(Calendar.YEAR)));
	    		filename = filename.replace("[MM]", twoDigitsFormat.format(now.get(Calendar.MONTH) + 1));
	    		filename = filename.replace("[DD]", twoDigitsFormat.format(now.get(Calendar.DAY_OF_MONTH)));
	    		filename = filename.replace("[HH]", twoDigitsFormat.format(now.get(Calendar.HOUR_OF_DAY)));
	    		filename = filename.replace("[MI]", twoDigitsFormat.format(now.get(Calendar.MINUTE)));
	    		filename = filename.replace("[SS]", twoDigitsFormat.format(now.get(Calendar.SECOND)));
    		} else {
	    		filename = filename.replace("[YYYY]", Integer.toString(zonedDateTime.get(ChronoField.YEAR)));
	    		filename = filename.replace("[MM]", twoDigitsFormat.format(zonedDateTime.get(ChronoField.MONTH_OF_YEAR)));
	    		filename = filename.replace("[DD]", twoDigitsFormat.format(zonedDateTime.get(ChronoField.DAY_OF_MONTH)));
	    		filename = filename.replace("[HH]", twoDigitsFormat.format(zonedDateTime.get(ChronoField.HOUR_OF_DAY)));
	    		filename = filename.replace("[MI]", twoDigitsFormat.format(zonedDateTime.get(ChronoField.MINUTE_OF_HOUR)));
	    		filename = filename.replace("[SS]", twoDigitsFormat.format(zonedDateTime.get(ChronoField.SECOND_OF_MINUTE)));
    		}
    		return filename.replace("\\[", "[").replace("\\]", "]");
    	} else {
    		return fileNameWithPatterns;
    	}
    }
	
	public static String getDateTimeString(Date value, ZoneId timezone, DateTimeFormatter dateTimeFormatter) {
		ZoneId dbTimezone = ZoneId.systemDefault();
		ZonedDateTime dbZonedDateTime = ZonedDateTime.ofInstant(value.toInstant(), dbTimezone);
		ZonedDateTime exportZonedDateTime = dbZonedDateTime.withZoneSameInstant(timezone);
		return dateTimeFormatter.format(exportZonedDateTime);
	}

	/**
	 * Get the ordinal of occurence of the given weekdy in its month
	 * @param dayOfMonth
	 * @return
	 */
	public static int getNumberOfWeekdayInMonth(int dayOfMonth) {
		float ordinalFloat = dayOfMonth / 7.0f;
		int ordinalInt = (int) Math.round(Math.ceil(ordinalFloat));
		return ordinalInt;
	}

	public static String format(Date date, DateFormat dateFormat) {
		if (date == null) {
			return null;
		}

		return dateFormat.format(date);
	}

	public static String format(Date date, ZoneId zoneId, DateTimeFormatter formatter) {
		return format(toLocalDate(date, zoneId), formatter);
	}

	public static String format(LocalDate date, DateTimeFormatter formatter) {
	    if (date == null) {
	        return null;
        }

        return formatter.format(date);
    }

    public static Long toLong(Date date) {
        return toLong(date, null);
    }

    public static Long toLong(Date date, Long defaultValue) {
        if (date == null) {
            return defaultValue;
        }

        return date.getTime();
    }
	
	public static List<Date> getDaysForChartPeriod(Date from, Date till) {
		return getDaysForChartPeriod(from, till, AgnUtils.DEFAULT_NUMBER_OF_CHART_BARS);
	}
	
    public static List<Date> getDaysForChartPeriod(Date from, Date till, int numberOfBars) {
		// Watch out for timezone changes because of DST (e.g.: CEST and CET)
    	int daysBetween = DateUtilities.getDaysBetween(till, from);

        if (daysBetween == 0) {
            till = DateUtils.addDays(from, 1);
            daysBetween = 1;
        }
        int numberOfDaysBetweenDates;
        int numberOfDates;
        if (daysBetween <= numberOfBars * 1.5f) {
            numberOfDaysBetweenDates = 1;
            numberOfDates = daysBetween + 1;
        } else {
            numberOfDaysBetweenDates = daysBetween / numberOfBars;
            if (numberOfDaysBetweenDates < 2) {
                numberOfDaysBetweenDates = 2;
            }
            numberOfDates = (int) Math.ceil(((double)daysBetween) / numberOfDaysBetweenDates) + 1;
        }
        List<Date> days = new ArrayList<>(numberOfDates);
        days.add(till);
        for (int i = 1; i < numberOfDates; i++) {
            days.add(DateUtils.addDays(days.get(i - 1), -numberOfDaysBetweenDates));
        }
        Collections.reverse(days);
		return days;
	}
}
