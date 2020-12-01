/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.agnitas.util.SafeString;

/**
 * Use this class to handle standard formats
 * 
 *
 */
public class DateUtil {
	
	/**
	 * @param date
	 * @return the date formatted with the Constants.DATE_PATTERN_FULL
	 */
	public static String formatDateFull(Date date ) {
		SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_PATTERN_FULL);
		return format.format(date);
	}
	
	/**
	 * @param dateAsString - date which matches the Constants.DATE_PATTERN_FULL
	 * @return
	 * @throws ParseException
	 */
	
	public static Date parseFullDate(String dateAsString) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_PATTERN_FULL);
		Date date;
		date = format.parse(dateAsString);
		return date;
	}

	public static String getTimespanString(long timespanInMillis, Locale locale) {
		// TODO: Are the types of the new variables (int) correct? Longs are used in computation...
		int days = (int) (timespanInMillis / Constants.MILLISECONDS_PER_DAY);
		int leftover = (int) (timespanInMillis % Constants.MILLISECONDS_PER_DAY);
		int hours = leftover / Constants.MILLISECONDS_PER_HOUR;
		leftover = leftover % Constants.MILLISECONDS_PER_HOUR;
		int minutes = leftover / Constants.MILLISECONDS_PER_MINUTE;
		leftover = leftover % Constants.MILLISECONDS_PER_MINUTE;
		int seconds = leftover / 1000;
		
		if (days > 0) {
			if (hours == 0 && minutes == 0 && seconds == 0) {
				return days + " " + SafeString.getLocaleString("days", locale);
			} else {
				return days + " " + SafeString.getLocaleString("days", locale) + " " + hours + " " + SafeString.getLocaleString("hours", locale);
			}
		} else if (hours > 0) {
			if (minutes == 0 && seconds == 0) {
				return hours + " " + SafeString.getLocaleString("hours", locale) + " " + minutes + " " + SafeString.getLocaleString("minutes", locale);
			} else {
				return hours + " " + SafeString.getLocaleString("hours", locale) + " " + minutes + " " + SafeString.getLocaleString("minutes", locale);
			}
		} else if (minutes > 0) {
			return minutes + " " + SafeString.getLocaleString("minutes", locale) + " " + seconds + " " + SafeString.getLocaleString("seconds", locale);
		} else {
			return seconds + " " + SafeString.getLocaleString("seconds", locale);
		}
	}
	
    /**
     * Checks, if the send date is good for immediate delivery. For immediate mailing
     * delivery, the send date can be up to five minutes in the future.
     *
     * @param sendDate date to check.
     * 
     * @return true if send date is good for immediate delivery
     */
    public static boolean isSendDateForImmediateDelivery( Date sendDate) {
    	return isDateForImmediateAction(sendDate);
    }
    
    /**
     * Checks, if the send date is good for immediate generation. For immediate mailing
     * generation, the generation date can be up to five minutes in the future.
     *
     * @param generationDate date to check.
     *
     * @return true if date is good for immediate generation
     */
    public static boolean isDateForImmediateGeneration( Date generationDate) {
    	return isDateForImmediateAction(generationDate);
    }
    
    private static boolean isDateForImmediateAction(Date actionDate) {
    	// Create the calendar object for comparison
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar actionDateCalendar = new GregorianCalendar();

        // Set the time of the test-calendar
        actionDateCalendar.setTime(actionDate);

        // Move "current time" 5 minutes into future, so we get a 5 minute fairness period
        now.add(Calendar.MINUTE, 5);
        
        // Do the hard work!
        return !now.before(actionDateCalendar);
	}
    
    public static boolean isValidSendDate( Date sendDate) {
    	// Create the calendar object for comparison
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar sendDateCalendar = new GregorianCalendar();

        // Set the time of the test-calendar
        sendDateCalendar.setTime( sendDate);

        // Move "current time" 5 minutes into future, so we get a 5 minute fairness period
        now.add( Calendar.MINUTE, -5);
        
        // Do the hard work!
        return now.before( sendDateCalendar);
    }
}
