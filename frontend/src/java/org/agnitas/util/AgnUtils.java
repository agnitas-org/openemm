/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.Cipher;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.web.forms.WorkflowParameters;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.AdminPreferences;
import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.Company;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.commons.encoder.Sha512Encoder;
import com.agnitas.emm.core.commons.validation.AgnitasEmailValidator;
import com.agnitas.util.Version;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.PageContext;

public class AgnUtils {

	// allowed versions should look like 16.10.999 or 16.10.999-hf35 or 16.10.999.999
	public static final String APPLICATION_VERSION_REGEX = "(\\d){2}\\.(\\d){2}\\.(\\d){3}(((-hf)(\\d){1,3})|(\\.(\\d){3}))?";
	public static final Pattern APPLICATION_VERSION_PATTERN = Pattern.compile(APPLICATION_VERSION_REGEX);

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(AgnUtils.class);

	public static final String DEFAULT_MAILING_HTML_DYNNAME = "HTML-Version";
	public static final String DEFAULT_MAILING_TEXT_DYNNAME = "Text";

	public static final String SESSION_CONTEXT_KEYNAME_ADMIN = "emm.admin";
	public static final String SESSION_CONTEXT_KEYNAME_ADMINPREFERENCES = "emm.adminPreferences";

	public static final String[] SUPPORTED_LOCALES = { "de", "es", "fr", "nl", "pt", "it" };
	private static final String DEFAULT_DECIMAL_SEPARATOR = ".";
	private static final String DEFAULT_GROUPING_SEPARATOR = ",";
	
	private static final String COPY_PREFIX = "mailing.CopyOf";

	private static String HOSTNAME = null;

	private static final TimeoutLRUMap<Integer, String> CKEDITOR_PATH_CACHE = new TimeoutLRUMap<>(100, 5);
	private static final TimeoutLRUMap<Integer, String> ACE_EDITOR_PATH_CACHE = new TimeoutLRUMap<>(100, 5);

	private static String BROWSER_CACHE_MARKER = null;

	public static final String[] FILESIZE_UNITS = {"Bytes", "kByte", "MByte", "GByte", "TByte", "PByte", "EByte", "ZByte", "YByte"};
	
	public static final int DEFAULT_NUMBER_OF_CHART_BARS = 12;

	private static final int CHAR_BYTES_MASK_1 = 0xFFFFFF80;
	private static final int CHAR_BYTES_MASK_2 = 0xFFFFF800;
	private static final int CHAR_BYTES_MASK_3 = 0xFFFF0000;
	private static final int CHAR_BYTES_MASK_4 = 0xFFE00000;
	private static final int CHAR_BYTES_MASK_5 = 0xFC000000;
	private static final int CHAR_BYTES_MASK_6 = 0x80000000;

	/**
	 * Creates a String of an Throwable item and its causes. Cause level is
	 * limited by a maximum of 100 to prevent cyclic or excessive causes.
	 *
	 * @param throwable
	 * @param maxSubCauseLevel
	 *            maximum level of causes to show (maximum is 100, -1 equals
	 *            maximum)
	 * @return
	 */
	public static String throwableToString(Throwable throwable, int maxSubCauseLevel) {
		StringBuilder returnBuilder = new StringBuilder(throwable.getClass().getSimpleName()
				+ ":\n"
				+ throwable.getMessage()
				+ "\nStackTrace:\n" + getStackTraceAsString(throwable));
		int level = 0;

		// prevent cyclic direct reference in cause
		Throwable previousSubThrowable = throwable;

		Throwable subThrowable = throwable.getCause();
		while ((maxSubCauseLevel < 0 || level < maxSubCauseLevel)
				&& level <= 100 && subThrowable != null
				&& previousSubThrowable != subThrowable) {
			returnBuilder.append("\n\ncaused by\n"
					+ subThrowable.getClass().getSimpleName() + ":\n"
					+ subThrowable.getMessage() + "\nStackTrace:\n"
					+ getStackTraceAsString(subThrowable));
			level++;
			subThrowable = subThrowable.getCause();
		}
		if (level == maxSubCauseLevel) {
			returnBuilder.append("\n\n... cut after level " + maxSubCauseLevel
					+ " ...");
		}
		return returnBuilder.toString();
	}

	/**
	 * Get stacktrace of Exception or Throwable as String
	 *
	 * @return Value of property stackTrace.
	 */
	private static String getStackTraceAsString(Throwable throwable) {
		return getStackTraceAsString(throwable.getStackTrace());
	}

	/**
	 * Get stacktrace as String
	 *
	 * @return Value of property stackTrace.
	 */
	public static String getStackTraceAsString(StackTraceElement[] stackTraceElements) {
		StringBuilder traceStringBuilder = new StringBuilder();
		if (stackTraceElements != null) {
			for (int i = 0; i < stackTraceElements.length; i++) {
				traceStringBuilder.append(stackTraceElements[i].toString()
						+ "\n");
			}
		}
		return traceStringBuilder.toString();
	}

	/**
	 * Utility method to show stacktrace in db table
	 *
	 * @param t
	 * @return
	 */
	public static String getStackTraceString(Throwable t) {
		StringBuilder traceStringBuilder = new StringBuilder();
		
		if (t != null) {
			StackTraceElement[] stackTraceElements = t.getStackTrace();
			if (stackTraceElements != null && stackTraceElements.length > 0) {
				for (int level = 0; level < stackTraceElements.length && level < 5; level++) {
					traceStringBuilder.append(stackTraceElements[level].toString() + "\n");
				}
			}
		}

		if (traceStringBuilder.length() == 0) {
			return "<empty Stacktrace>";
		} else {
			return traceStringBuilder.toString();
		}
    }

	/**
	 * Reads a file in encoding UTF-8.
	 */
	public static String readFile(String path) {
		try {
			return FileUtils.readFileToString(new File(path), "UTF-8");
		} catch (Exception e) {
			logger.warn("Error reading file " + path, e);
			return null;
		}
	}

	/**
	 * Getter for property parameterMap.
	 *
	 * @return Value of property parameterMap.
	 */
	public static Map<String, String> getRequestParameterMap(ServletRequest req) {
		Map<String, String> parameterMap = new HashMap<>();
		Enumeration<String> e = req.getParameterNames();
		while (e.hasMoreElements()) {
			String parameterName = e.nextElement();
			String parameterValue = req.getParameter(parameterName);
			parameterMap.put(parameterName, parameterValue);
		}
		return parameterMap;
	}

	/**
	 * Get year for statistics overview from which should starts year list
	 *
	 * @param admin
	 * @return
	 */
	public static int getStatStartYearForCompany(ComAdmin admin) {
		return getStatStartYearForCompany(admin, -1);
	}

	/**
	 *
	 * @param admin
	 * @param initialYear
	 * @return
	 */
	public static int getStatStartYearForCompany(ComAdmin admin, int initialYear) {
		GregorianCalendar startDate = new GregorianCalendar();
		Company company = AgnUtils.getCompany(admin);
		assert (company != null);
		Date creationDate = company.getCreationDate();

        if (creationDate == null) {
            creationDate = new Date();
        }
        startDate.setTime(creationDate);

        return Math.max(startDate.get(Calendar.YEAR) - 1, initialYear);
	}

	public static String getRedirectDomain(Company company) {
		if (company == null) {
			return null;
		}

		return company.getRdirDomain();
	}

	public static String getRedirectDomain(HttpServletRequest request) {
		Company company = getCompany(request);

		if (company == null) {
			return null;
		}

		return company.getRdirDomain();
	}

	public static int compare(String a, String b) {
		if (a == null || b == null) {
			if (a != null) {
				return +1;
			}

			if (b != null) {
				return -1;
			}

			return 0;
		}

		return a.compareTo(b);
	}

	/**
	 * Lower case a String and keep null values
	 * Used only by BSH-Interpreter
	 */
	public static String toLowerCase(String source) {
		if (source == null) {
			return null;
		}
		return source.toLowerCase();
	}

	/**
	 * Getter for property reqParameters.
	 *
	 * @return Value of property reqParameters.
	 */
	public static Map<String, String> getReqParameters(HttpServletRequest req) {
		Map<String, String> params = new HashMap<>();
		String parName = null;

		Enumeration<String> aEnum1 = req.getParameterNames();
		while (aEnum1.hasMoreElements()) {
			parName = aEnum1.nextElement();
			if (parName.startsWith("__AGN_DEFAULT_") && parName.length() > 14) {
				parName = parName.substring(14);
				params.put(parName, req.getParameter("__AGN_DEFAULT_" + parName));
			}
		}

		Enumeration<String> aEnum2 = req.getParameterNames();
		while (aEnum2.hasMoreElements()) {
			parName = aEnum2.nextElement();
			params.put(parName, req.getParameter(parName));
		}

		if (req.getQueryString() != null) {
			params.put("agnQueryString", req.getQueryString());
		}

		return params;
	}

	/**
	 * Checks whether a user has any of demanded permissions.
	 */
	public static boolean allowed(HttpServletRequest req, Permission... permissions) {
		ComAdmin admin = getAdmin(req);
		return admin != null && admin.permissionAllowed(permissions);
	}

	/**
	 * Getter for property companyID.
	 *
	 * @return Value of property companyID.
	 */
	public static int getCompanyID(HttpServletRequest request) {
		try {
			Company company = getCompany(request);
			if (company == null) {
				logger.error("AgnUtils: getCompanyID - no companyID found (company is null)");
				return 0;
			} else {
				return company.getId();
			}
		} catch (Exception e) {
			logger.error("AgnUtils: getCompanyID - no companyID found for request", e);
			return 0;
		}
	}

	public static ComAdmin getAdmin(HttpServletRequest request) {
		try {
			return getAdmin(request.getSession(false));
		} catch (Exception e) {
			logger.error("Error while reading admin from request", e);
			return null;
		}
	}

	public static ComAdmin getAdmin(HttpSession session) {
		try {
			if (session == null) {
				logger.debug("no request session found for getAdmin", new Exception());
				return null;
			} else {
				ComAdmin admin = (ComAdmin) session.getAttribute(SESSION_CONTEXT_KEYNAME_ADMIN);
				if (admin == null) {
					logger.debug("no admin found in request session data", new Exception());
					return null;
				} else {
					return admin;
				}
			}
		} catch (Exception e) {
			logger.error("Error while reading admin from session", e);
			return null;
		}
	}

	public static int getAdminId(HttpServletRequest request) {
		try {
			ComAdmin admin = getAdmin(request);
			if (admin == null) {
				logger.error("AgnUtils: getAdminId - no adminID found (admin is null)");
				return 0;
			} else {
				return admin.getAdminID();
			}
		} catch (Exception e) {
			logger.error("AgnUtils: getAdminId - no adminID found for request", e);
			return 0;
		}
	}

	public static void setAdmin(HttpServletRequest request, ComAdmin admin) {
		try {
			HttpSession session = request.getSession();
			if (session != null) {
				session.setAttribute(SESSION_CONTEXT_KEYNAME_ADMIN, admin);
			} else {
				logger.error("no session found for setting admin data");
			}
		} catch (Exception e) {
			logger.error("error while setting admin data in session");
		}
	}

    public static void setAdminPreferences(HttpServletRequest request, AdminPreferences adminPreferences) {
		try {
			HttpSession session = request.getSession();
			if (session != null) {
				session.setAttribute(SESSION_CONTEXT_KEYNAME_ADMINPREFERENCES, adminPreferences);
			} else {
				logger.error("no session found for setting admin preferences data");
			}
		} catch (Exception e) {
			logger.error("error while setting admin preferences data in session");
		}
	}

	public static ComAdmin getAdmin(PageContext pageContext) {
		try {
			HttpSession session = pageContext.getSession();
			if (session == null) {
				logger.error("No pageContext data found for getAdmin");
				return null;
			} else {
				ComAdmin admin = (ComAdmin) session.getAttribute(SESSION_CONTEXT_KEYNAME_ADMIN);
				if (admin == null) {
					logger.debug("No admin found in pageContext data");
					return null;
				} else {
					return admin;
				}
			}
		} catch (Exception e) {
			logger.error("No admin found for pageContext", e);
			return null;
		}
	}

    public static AdminPreferences getAdminPreferences(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                logger.error("no request session found for getAdminPreferences", new Exception());
                return null;
            } else {
                AdminPreferences adminPreferences = (AdminPreferences) session.getAttribute(SESSION_CONTEXT_KEYNAME_ADMINPREFERENCES);
                if (adminPreferences == null) {
                    logger.debug("No admin preferences found in request session data");
                    return null;
                } else {
                    return adminPreferences;
                }
            }
        } catch (Exception e) {
            logger.error("Error while reading admin preferences from request", e);
            return null;
        }
    }
    
    public static void saveWorkflowForwardParams(HttpServletRequest request, WorkflowParameters params) {
		try {
            WorkflowParametersHelper.put(request, params);
        } catch (Exception e) {
            logger.error("Error while saving workflow forward params to request", e);
        }
    }
    
    public static void saveWorkflowForwardParamsToSession(HttpServletRequest request, WorkflowParameters params, boolean override) {
		HttpSession session = request.getSession(false);
		saveWorkflowForwardParamsToSession(session, params, override);
    }

	public static void saveWorkflowForwardParamsToSession(HttpSession session, WorkflowParameters params, boolean override) {
		try {
			if (session == null) {
				logger.error("no request session found for getAdminPreferences", new Exception());
			} else {
				WorkflowParametersHelper.put(session, params, override);
			}
		} catch (Exception e) {
			logger.error("Error while saving workflow forward params to session", e);
		}
	}

	public static boolean isUserLoggedIn(HttpServletRequest request) {
		return getAdmin(request) != null;
	}

	/**
	 * Getter for property timeZone.
	 *
	 * @return Value of property timeZone.
	 */
	public static TimeZone getTimeZone(HttpServletRequest req) {
		return getTimeZone(getAdmin(req));
	}

	public static TimeZone getTimeZone(ComAdmin admin) {
		try {
			if (admin == null) {
				return DateUtilities.UTC;
			} else {
				return TimeZone.getTimeZone(admin.getAdminTimezone());
			}
		} catch (Exception e) {
			logger.error("Error reading timezone information for current admin", e);
			return DateUtilities.UTC;
		}
	}

	public static ZoneId getZoneId(ComAdmin admin) {
		ZoneId zone = DateUtilities.UTC_ZONE;

		try {
			if (admin == null) {
				logger.error("AgnUtils.getZoneId: admin == null");
			} else {
				zone = TimeZone.getTimeZone(admin.getAdminTimezone()).toZoneId();
			}
		} catch (Exception e) {
			logger.error("Error reading timezone information for current admin", e);
		}

		return zone;
	}

	/**
	 * Getter for property company.
	 *
	 * @return Value of property company.
	 */
	public static Company getCompany(HttpServletRequest req) {
		ComAdmin admin = getAdmin(req);
		if (admin == null) {
			logger.error("AgnUtils: getCompany: no admin found in request");
			return null;
		} else {
			return getCompany(admin);
		}
	}

	/**
	 * Getter for property company.
	 *
	 * @return Value of property company.
	 */
	public static Company getCompany(ComAdmin admin) {
		try {
			Company company = admin.getCompany();
			if (company == null) {
				logger.error("AgnUtils: getCompany: no company found for admin "
						+ admin.getAdminID()
						+ "("
						+ admin.getCompanyID()
						+ ")");
				return null;
			} else {
				return company;
			}
		} catch (Exception e) {
			logger.error("Error reading company for current user", e);
			return null;
		}
	}

	public static boolean isMailTrackingAvailable(ComAdmin admin) {
		return isMailTrackingAvailable(getCompany(admin));
	}
	
	public static boolean isMailTrackingAvailable(Company company) {
		return company != null && company.getMailtracking() == 1;
	}

	/**
	 * Used only by BSH-Interpreter
	 *
	 * @param mask
	 * @param target
	 * @return
	 */
	public static boolean match(String mask, String target) {
		// if anything is null, no match
		if (mask == null || target == null) {
			return false;
		}
		mask = mask.toLowerCase();
		target = target.toLowerCase();

		if (mask.compareTo(target) == 0) {
			return true; // match!
		}

		boolean matched = true;
		if (mask.indexOf('%') >= 0 || mask.indexOf('_') >= 0) {
			matched = rmatch(mask, target); // find match incl wildcards
		} else {
			matched = false; // no wildcard - no match
		}
		return matched;
	}

	private static boolean rmatch(String mask, String target) {
		int moreCharacters = mask.indexOf('%');
		int oneCharacter = mask.indexOf('_');
		int pattern = -1;

		if (moreCharacters >= 0) {
			pattern = moreCharacters;
		}
		if (oneCharacter >= 0 && (oneCharacter < pattern || pattern < 0)) {
			pattern = oneCharacter;
		}

		if (pattern == -1) {
			if (mask.compareTo(target) == 0) {
				return true; // match!
			}
			return false;
		}

		if (!mask.regionMatches(0, target, 0, pattern)) {
			return false;
		}

		if (pattern == oneCharacter) {
			// '_' found
			return rmatch(mask.substring(pattern + 1), target.substring(pattern + 1));
		}
		String after = mask.substring(moreCharacters + 1, mask.length());

		for (int c = pattern; c < target.length(); c++) {
			if (rmatch(after, target.substring(c, target.length()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Escapes any HTML sequence in all values in the given map.
	 *
	 * @param htmlMap
	 * @return
	 */
	public static Map<String, Object> escapeHtmlInValues(Map<String, Object> htmlMap) {
		Map<String, Object> result = new CaseInsensitiveMap<>();

		if (htmlMap != null) {
			for(final Map.Entry<String, Object> entry : htmlMap.entrySet()) {
				final Object value = entry.getValue();

				if (value != null) {
					result.put(entry.getKey(), StringEscapeUtils.escapeHtml4(value.toString()));
				} else {
					result.put(entry.getKey(), null);

					if (logger.isDebugEnabled()) {
						logger.debug("value for key '" + entry.getKey() + "' is null");
					}
				}
			}
		}
		return result;
	}

	/**
	 *
	 * @param startYear
	 * @return a list of years from the current year back to the start year
	 */
	public static List<Integer> getYearList(int startYear) {

		List<Integer> yearList = new ArrayList<>();
		GregorianCalendar calendar = new GregorianCalendar();
		int currentYear = calendar.get(Calendar.YEAR);
		for (int year = currentYear; year >= startYear; year--) {
			yearList.add(year);
		}

		return yearList;
	}

	public static List<Integer> getCalendarYearList(int startYear) {
		List<Integer> yearList = new ArrayList<>();
		GregorianCalendar calendar = new GregorianCalendar();
		int currentYear = calendar.get(Calendar.YEAR);
		for (int year = currentYear + 1; year >= startYear; year--) {
			yearList.add(year);
		}
		return yearList;
	}

	public static List<String[]> getMonthList() {
		List<String[]> monthList = new ArrayList<>();
		monthList.add(new String[] { "0", "calendar.month.1" });
		monthList.add(new String[] { "1", "calendar.month.2" });
		monthList.add(new String[] { "2", "calendar.month.3" });
		monthList.add(new String[] { "3", "calendar.month.4" });
		monthList.add(new String[] { "4", "calendar.month.5" });
		monthList.add(new String[] { "5", "calendar.month.6" });
		monthList.add(new String[] { "6", "calendar.month.7" });
		monthList.add(new String[] { "7", "calendar.month.8" });
		monthList.add(new String[] { "8", "calendar.month.9" });
		monthList.add(new String[] { "9", "calendar.month.10" });
		monthList.add(new String[] { "10", "calendar.month.11" });
		monthList.add(new String[] { "11", "calendar.month.12" });
		return monthList;
	}

	public static boolean parameterNotEmpty(HttpServletRequest request, String paramName) {
		return StringUtils.isNotEmpty(request.getParameter(paramName));
	}

	public static boolean parameterNotEmpty(HttpSession session, String paramName) {
		Object value = session.getAttribute(paramName);
		if (value == null) {
			return false;
		}
		if (value instanceof String) {
			return StringUtils.isNotEmpty(value.toString());
		} else {
			return true;
		}
	}

	public static boolean parameterNotBlank(HttpServletRequest request, String paramName) {
		return StringUtils.isNotBlank(request.getParameter(paramName));
	}

	public static boolean parameterNotBlank(HttpSession session, String paramName) {
		Object value = session.getAttribute(paramName);
		if (value == null) {
			return false;
		}
		if (value instanceof String) {
			return StringUtils.isNotBlank(value.toString());
		} else {
			return true;
		}
	}

	public static String bytesToKbStr(int bytes) {
		long kbSize100x = Math.round(bytes / 10.24);
		return (kbSize100x / 100) + "." + (kbSize100x % 100);
	}

	public static int decryptLayoutID(String layout) {
		int layoutID = 0;
		int index = layout.indexOf('.');
		layout = layout.substring(0, index);
		layoutID = Integer.parseInt(layout, 36);
		return layoutID;
	}

	public static int decryptCompanyID(String company) {
		int companyID = 0;
		int index = company.indexOf('.');
		company = company.substring(index + 1);
		companyID = Integer.parseInt(company, 36);
		return companyID;
	}

	public static File createDirectory(String path) {
		File directory = new File(path);
		boolean dirCreated;
		if (!directory.exists()) {
			dirCreated = directory.mkdirs();
		} else {
			dirCreated = true;
		}
		return dirCreated ? directory : null;
	}

	public static <T> T nullValue(T value, T valueOnNull) {
		return value != null ? value : valueOnNull;
	}

	public static String getUserErrorMessage(Exception e) {
		String result = "";
		boolean ff = false;
		if (e instanceof ParseErrorException) {
			result += "Line " + ((ParseErrorException) e).getLineNumber();
			result += ", column " + ((ParseErrorException) e).getColumnNumber()
					+ ": ";
			String error = e.getMessage();
			result += StringEscapeUtils.escapeHtml4(error.split("\n")[0]);
			ff = true;
		}
		if (e instanceof MethodInvocationException) {
			result += "Line " + ((MethodInvocationException) e).getLineNumber();
			result += ", column "
					+ ((MethodInvocationException) e).getColumnNumber() + ": ";
			String error = e.getMessage();
			result += StringEscapeUtils.escapeHtml4(error.split("\n")[0]);
			ff = true;
		}
		if (e instanceof ResourceNotFoundException) {
			result += "Template not found. \n";
			result += StringEscapeUtils.escapeHtml4(e.getMessage().split("\n")[0]);
			ff = true;
		}
		if (e instanceof IOException) {
			result += e.getMessage().split("\n")[0];
			ff = true;
		}
		if (!ff) {
			result += e.getMessage().split("\n")[0];
		}

		return result;
	}

	public static String getAttributeFromParameterString(String params, String attributeName) {
		if (StringUtils.isEmpty(params) || StringUtils.isEmpty(attributeName)) {
			return null;
		}

		String attribute = null;

		// split the parameters
		String[] paramArray = params.split(",");

		// loop over every entry
		for (String item : paramArray) {
			if (item.trim().startsWith(attributeName)) {
				attribute = item.trim();
			}
		}

		// we dont have that attribute in param-staring
		if (attribute == null) {
			return null;
		}
		
		// Remove optional blanks
		attribute = attribute.replaceAll("\\s+=\\s+", "=");

		// now extract the parameter.
		attribute = attribute.replace(attributeName + "=", "");
		attribute = attribute.replace("\"", "").trim();

		return attribute;
	}

	/**
	 * Build a string of x repetitions of another string. An optional separator
	 * is placed between each repetition. 0 repetitions return an empty string.
	 *
	 * @param itemString
	 * @param repeatTimes
	 * @return
	 */
	public static String repeatString(String itemString, int repeatTimes) {
		return repeatString(itemString, repeatTimes, null);
	}

	/**
	 * Build a string of x repetitions of another string. An optional separator
	 * is placed between each repetition. 0 repetitions return an empty string.
	 *
	 * @param itemString
	 * @param separatorString
	 * @param repeatTimes
	 * @return
	 */
	public static String repeatString(String itemString, int repeatTimes, String separatorString) {
		StringBuilder returnStringBuilder = new StringBuilder();
		for (int i = 0; i < repeatTimes; i++) {
			if (returnStringBuilder.length() > 0 && StringUtils.isNotEmpty(separatorString)) {
				returnStringBuilder.append(separatorString);
			}
			returnStringBuilder.append(itemString);
		}
		return returnStringBuilder.toString();
	}

	/**
	 * Sort a map by a Comparator for the keytype
	 *
	 * @param mapToSort
	 * @param comparator
	 * @return
	 */
	public static <KeyType, ValueType> Map<KeyType, ValueType> sortMap(Map<KeyType, ValueType> mapToSort, Comparator<KeyType> comparator) {
        return mapToSort.entrySet().stream()
				.sorted(Entry.comparingByKey(comparator))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e2, TreeMap::new));
	}

	/**
	 * Sorts Lists and Sets into a new List with all items sorted by their comparator
	 *
	 * @param c
	 * @return
	 */
	public static <T extends Comparable<? super T>> List<T> getSortedList(Collection<T> c) {
		List<T> list = new ArrayList<>(c);
		Collections.sort(list);
		return list;
	}

	/**
	 * Splits String into a list of strings separating text values from number
	 * values Example: "abcd 23.56 ueyr76" will be split to "abcd ", "23", ".",
	 * "56", " ueyr", "76"
	 *
	 * @param mixedString
	 *            string to split
	 * @return split-list of strings
	 */
	public static List<String> splitIntoNumbersAndText(String mixedString) {
		List<String> tokens = new ArrayList<>();
		if (StringUtils.isNotEmpty(mixedString)) {
			StringBuilder numberToken = null;
			StringBuilder textToken = null;
			for (char charValue : mixedString.toCharArray()) {
				if (Character.isDigit(charValue)) {
					if (numberToken == null) {
						numberToken = new StringBuilder();
					}
					numberToken.append(charValue);
					if (textToken != null) {
						tokens.add(textToken.toString());
						textToken = null;
					}
				} else {
					if (textToken == null) {
						textToken = new StringBuilder();
					}
					textToken.append(charValue);
					if (numberToken != null) {
						tokens.add(numberToken.toString());
						numberToken = null;
					}
				}
			}
			if (textToken != null) {
				tokens.add(textToken.toString());
				textToken = null;
			}
			if (numberToken != null) {
				tokens.add(numberToken.toString());
				numberToken = null;
			}
		}
		return tokens;
	}

	/**
	 * Check if a string only consists of digits. No signing (+-) or punctuation
	 * is allowed.
	 *
	 * @param value
	 * @return
	 */
	public static boolean isDigit(String value) {
		for (char charValue : value.toCharArray()) {
			if (!Character.isDigit(charValue)) {
				return false;
			}
		}
		return true;
	}

	public static boolean interpretAsBoolean(String value) {
		return value != null
				&& ("true".equalsIgnoreCase(value)
						|| "yes".equalsIgnoreCase(value)
						|| "on".equalsIgnoreCase(value)
						|| "allowed".equalsIgnoreCase(value)
						|| "1".equals(value)
						|| "+".equals(value)
						|| "enabled".equalsIgnoreCase(value)
						|| "active".equalsIgnoreCase(value));
	}

	public static URL addUrlParameter(URL url, String parameterName, String parameterValue) throws UnsupportedEncodingException, MalformedURLException {
		return addUrlParameter(url, parameterName, parameterValue, null);
	}

	public static URL addUrlParameter(URL url, String parameterName, String parameterValue, String encodingCharSet) throws UnsupportedEncodingException, MalformedURLException {
		return new URL(addUrlParameter(url.toString(), parameterName, parameterValue, encodingCharSet));
	}

	public static String addUrlParameter(String url, String parameterName, String parameterValue) throws UnsupportedEncodingException {
		return addUrlParameter(url, parameterName, parameterValue, null);
	}

	public static String addUrlParameter(String url, String parameterName, String parameterValue, String encodingCharSet) throws UnsupportedEncodingException {
		if (parameterName == null) {
			return url;
		} else {
			StringBuilder escapedParameterNameAndValue = new StringBuilder();
	
			if (StringUtils.isEmpty(encodingCharSet)) {
				escapedParameterNameAndValue.append(parameterName);
			} else {
				escapedParameterNameAndValue.append(URLEncoder.encode(parameterName, encodingCharSet));
			}
	
			escapedParameterNameAndValue.append('=');
	
			if (StringUtils.isEmpty(encodingCharSet)) {
				escapedParameterNameAndValue.append(parameterValue);
			} else {
				escapedParameterNameAndValue.append(URLEncoder.encode(parameterValue, encodingCharSet));
			}
			return addUrlParameter(url, escapedParameterNameAndValue.toString());
		}
	}

	public static String addUrlParameter(String url, String escapedParameterNameAndValue) {
		StringBuilder newUrl = new StringBuilder();

		// Find html link anchor but ignore agnHashTags
		int hpos = indexOf(url, "#", "##");
		if (hpos > -1) {
			newUrl.append(url.substring(0, hpos));
		} else {
			newUrl.append(url);
		}

		newUrl.append(url.indexOf('?') <= -1 ? '?' : '&');

		newUrl.append(escapedParameterNameAndValue);

		if (hpos > -1) {
			newUrl.append(url.substring(hpos));
		}
		return newUrl.toString();
	}
	
	public static int indexOf(String text, String searchString, String... ignoreStrings) {
		int startIndex = 0;
		while (startIndex >= 0) {
			startIndex = text.indexOf(searchString, startIndex);
			if (startIndex >= 0) {
				boolean ignore = false;
				if (ignoreStrings != null) {
					for (String ignoreString : ignoreStrings) {
						if (text.substring(startIndex).startsWith(ignoreString)) {
							startIndex = startIndex + ignoreString.length();
							ignore = true;
							break;
						}
					}
				}
				if (!ignore) {
					return startIndex;
				}
			}
		}
		return -1;
	}

	public static int getLineCountOfFile(File file) throws IOException {
		try(final LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(new FileInputStream(file)))) {
			while (lineNumberReader.readLine() != null) {
				// do nothing
			}

			return lineNumberReader.getLineNumber();
		}
	}

	public static int getLineCountOfStream(InputStream inputStream) throws IOException {
		LineNumberReader lineNumberReader = null;
		try {
			lineNumberReader = new LineNumberReader(new InputStreamReader(inputStream));
			while (lineNumberReader.readLine() != null) {
				// do nothing
			}

			return lineNumberReader.getLineNumber();
		} finally {
			IOUtils.closeQuietly(lineNumberReader);
		}
	}

	public static int getLineCountOfString(String data) throws IOException {
		LineNumberReader lineNumberReader = null;
		try {
			lineNumberReader = new LineNumberReader(new InputStreamReader(new ByteArrayInputStream(data.getBytes("UTF-8"))));
			while (lineNumberReader.readLine() != null) {
				// do nothing
			}

			return lineNumberReader.getLineNumber();
		} finally {
			IOUtils.closeQuietly(lineNumberReader);
		}
	}

	public static Set<String> splitAndNormalizeEmails(String emails) {
		if (StringUtils.isBlank(emails)) {
			return Collections.emptySet();
		}

		Set<String> addresses = new LinkedHashSet<>();

		for (String part : emails.split(";|,| ")) {
			String address = AgnUtils.normalizeEmail(part);

			if (StringUtils.isNotEmpty(address)) {
				addresses.add(address);
			}
		}

		return addresses;
	}

	public static InternetAddress[] getEmailAddressesFromList(String listString) throws Exception {
		Set<String> addresses = splitAndNormalizeEmails(listString);

		for (String address : addresses) {
			if (!AgnUtils.isEmailValid(address)) {
				throw new Exception("Invalid Emailaddress found: " + address);
			}
		}

		return addresses.stream().map(AgnUtils::asInternetAddress).toArray(InternetAddress[]::new);
	}
	
	public static boolean isValidEmailAddresses(String listString) {
		Set<String> addresses = splitAndNormalizeEmails(listString);

		for (String address : addresses) {
			if (!AgnUtils.isEmailValid(address)) {
				return false;
			}
		}
		return true;
	}

	private static InternetAddress asInternetAddress(String address) {
		try {
			return new InternetAddress(address);
		} catch (AddressException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the hostname of the current running server.
	 * This can be set explicitly in the file "$HOME/conf/hostname"
	 *
	 * @return
	 */
	public static String getHostName() {
		if (HOSTNAME == null) {
			String confDir = getUserHomeDir() + File.separator + "conf";
			if (!new File(confDir).exists()) {
				confDir = getUserHomeDir() + File.separator + "tomcat" + File.separator + "conf";
			}

			if (StringUtils.isBlank(confDir) || !new File(confDir).exists()) {
				logger.error("Cannot find runtime configuration directory: " + confDir);
			}

			File hostnameConfigFile = new File(confDir + File.separatorChar + "hostname");
			if (hostnameConfigFile.exists()) {
				try {
					HOSTNAME = readFileToString(hostnameConfigFile, "UTF-8").trim();
				} catch (Exception e) {
					logger.error("Cannot read hostname file", e);
				}
			}

			if (HOSTNAME == null) {
				try {
					HOSTNAME = InetAddress.getLocalHost().getHostName();
				} catch (java.net.UnknownHostException uhe) {
					if (logger.isInfoEnabled()) {
						logger.info("Unknown host", uhe);
					}

					try {
						HOSTNAME = getHostNameFallback();
					} catch (Exception e) {
						HOSTNAME = "unknown hostname";
					}
				}
			}
		}

		return HOSTNAME;
	}
	
	/**
	 * Some systems have a defined $HOME variable with a fileseparator character at the end.
	 * This must be removed.
	 */
	public static String getUserHomeDir() {
		String homeDir = System.getProperty("user.home");
		if (homeDir != null && homeDir.endsWith(File.separator)) {
			homeDir = homeDir.substring(0, homeDir.length() - 1);
		}
		return homeDir;
	}

	/**
	 * Get the IP-address of the current running server.
	 * This is the IP which is known to the DNS System for the hostname of this machine
	 *
	 * @return
	 */
	public static String getHostIpAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (java.net.UnknownHostException uhe) {
			if (logger.isInfoEnabled()) {
				logger.info("Unknown ip-address", uhe);
			}

			try {
				return getHostNameFallback();
			} catch (Exception e) {
				return "unknown ip-address";
			}
		}
	}

	private static String getHostNameFallback() throws Exception {
		BufferedInputStream input = null;
		try {
			Runtime run = Runtime.getRuntime();
			Process proc = run.exec("hostname");
			input = new BufferedInputStream(proc.getInputStream());
			String value = new String(IOUtils.toByteArray(input), "UTF-8");
			if (StringUtils.isNotBlank(value)) {
				return value.trim();
			} else {
				throw new Exception("cannot find hostname");
			}
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	/**
	 * Chop a string in pieces with maximum length of chunkSize
	 */
	public static List<String> chopToChunks(String text, int chunkSize) {
		List<String> returnList = new ArrayList<>((text.length() + chunkSize - 1) / chunkSize);

		for (int start = 0; start < text.length(); start += chunkSize) {
			returnList.add(text.substring(start, Math.min(text.length(), start + chunkSize)));
		}
		return returnList;
	}

	/**
	 * Chop a list of items in pieces of lists with a maximum number of items
	 * within each
	 */
	public static <T> List<List<T>> chopToChunks(List<T> originalList, int subListMaxSize) throws IllegalArgumentException {
		if (originalList == null) {
			throw new IllegalArgumentException("Null list not allowed to chopToChunks");
		} else if (subListMaxSize <= 0) {
			throw new IllegalArgumentException("SubListMaxSize <= 0 not allowed for chopToChunks");
		} else if (originalList.size() == 0) {
			return new ArrayList<>();
		} else {
			List<List<T>> returnList = new ArrayList<>((originalList.size() + subListMaxSize - 1) / subListMaxSize);
			List<T> currentListToAdd = new ArrayList<>(subListMaxSize);
			returnList.add(currentListToAdd);

			for (T item : originalList) {
				if (currentListToAdd.size() >= subListMaxSize) {
					currentListToAdd = new ArrayList<>(subListMaxSize);
					returnList.add(currentListToAdd);
				}
				currentListToAdd.add(item);
			}
			return returnList;
		}
	}

	/**
	 * Chop a set of items in pieces of lists with a maximum number of items
	 * within each
	 */
	public static <T> List<Set<T>> chopToChunks(Set<T> originalSet, int subSetMaxSize) throws IllegalArgumentException {
		if (originalSet == null) {
			throw new IllegalArgumentException("Null set not allowed to chopToChunks");
		} else if (subSetMaxSize <= 0) {
			throw new IllegalArgumentException("SubSetMaxSize <= 0 not allowed for chopToChunks");
		} else if (originalSet.size() == 0) {
			return new ArrayList<>();
		} else {
			List<Set<T>> returnList = new ArrayList<>((originalSet.size() + subSetMaxSize - 1) / subSetMaxSize);
			Set<T> currentSetToAdd = new HashSet<>(subSetMaxSize);
			returnList.add(currentSetToAdd);

			for (T item : originalSet) {
				if (currentSetToAdd.size() >= subSetMaxSize) {
					currentSetToAdd = new HashSet<>(subSetMaxSize);
					returnList.add(currentSetToAdd);
				}
				currentSetToAdd.add(item);
			}
			return returnList;
		}
	}

	public static UUID generateNewUUID() {
		return UUID.randomUUID();
	}

	public static String convertToHexString(UUID uuid) {
		return convertToHexString(uuid, false);
	}

	public static String convertToHexString(UUID uuid, boolean removeHyphens) {
		return uuid.toString().toUpperCase().replace("-", "");
	}

	public static String convertToBase64String(UUID uuid) {
		byte[] data = convertToByteArray(uuid);
		return encodeBase64(data);
	}

	public static String encodeBase64(byte[] data) {
		return data != null ? Base64.getEncoder().encodeToString(data) : null;
	}

	public static String encodeZippedBase64(byte[] data) throws IOException {
		return encodeBase64(ZipUtilities.zip(data));
	}

	public static byte[] decodeBase64(String data) {
		return data != null ? Base64.getDecoder().decode(data) : null;
	}

	public static byte[] decodeZippedBase64(String data) throws IOException {
		return ZipUtilities.unzip(decodeBase64(data));
	}

	public static String decodeURL(String encodedData) throws Exception {
		try {
			return URLDecoder.decode(encodedData, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new Exception("Invalid url-encoded data");
		}
	}

	public static byte[] convertToByteArray(UUID uuid) {
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		byte[] buffer = new byte[16];

		for (int i = 0; i < 8; i++) {
			buffer[i] = (byte) (msb >>> 8 * (7 - i));
		}
		for (int i = 8; i < 16; i++) {
			buffer[i] = (byte) (lsb >>> 8 * (7 - i));
		}
		return buffer;
	}

	public static UUID convertToUUID(byte[] byteArray) {
		if (byteArray.length != 16) {
			throw new IllegalArgumentException(
					"Length of bytearray doesn't fit for UUID");
		}

		long msb = 0;
		long lsb = 0;
		for (int i = 0; i < 8; i++) {
			msb = (msb << 8) | (byteArray[i] & 0xFF);
		}
		for (int i = 8; i < 16; i++) {
			lsb = (lsb << 8) | (byteArray[i] & 0xFF);
		}
		return new UUID(msb, lsb);
	}

	private static SecureRandom random = new SecureRandom();
	private static final char[] allCharacters = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

	public static String getRandomString(int length) {
		return getRandomString(allCharacters, length);
	}

	public static String getRandomString(char[] allowedCharacters, int length) {
		char[] buffer = new char[length];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = allowedCharacters[random.nextInt(allowedCharacters.length)];
		}
		return new String(buffer);
	}

	public static boolean dayListIncludes(List<GregorianCalendar> listOfDays, GregorianCalendar day) {
		for (GregorianCalendar listDay : listOfDays) {
			if (listDay.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)) {
				return true;
			}
		}
		return false;
	}

	public static boolean anyCharsAreEqual(char... values) {
		for (int i = 0; i < values.length; i++) {
			for (int j = i + 1; j < values.length; j++) {
				if (values[i] == values[j]) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean containsCharacter(char[] values, char searchChar) {
		for (int i = 0; i < values.length; i++) {
			if (values[i] == searchChar) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNumber(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isDouble(String value) {
		try {
			Double.parseDouble(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static Number parseNumber(String numberString) throws NumberFormatException {
		if (!isDouble(numberString)) {
			throw new NumberFormatException("Not a number: '" + numberString + "'");
		} else if (numberString.contains(".")) {
			if (numberString.length() < 10) {
				return Float.parseFloat(numberString);
			} else {
				BigDecimal value = new BigDecimal(numberString);
				boolean isFloat = BigDecimal.valueOf(Float.MIN_VALUE).compareTo(value) < 0 && value.compareTo(BigDecimal.valueOf(Float.MAX_VALUE)) < 0;
				if (isFloat) {
					return Float.parseFloat(numberString);
				} else {
					boolean isDouble = BigDecimal.valueOf(Double.MIN_VALUE).compareTo(value) < 0 && value.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) < 0;
					if (isDouble) {
						return Double.parseDouble(numberString);
					} else {
						return value;
					}
				}
			}
		} else {
			if (numberString.length() < 10) {
				return Integer.parseInt(numberString);
			} else {
				BigDecimal value = new BigDecimal(numberString);
				boolean isInteger = BigDecimal.valueOf(Integer.MIN_VALUE).compareTo(value) < 0 && value.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) < 0;
				if (isInteger) {
					return Integer.parseInt(numberString);
				} else {
					boolean isLong = BigDecimal.valueOf(Long.MIN_VALUE).compareTo(value) < 0 && value.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) < 0;
					if (isLong) {
						return Long.parseLong(numberString);
					} else {
						return value;
					}
				}
			}
		}
	}

	public static boolean isNumberValid(final String value) {
		try {
			return parseNumber(value) != null;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Call lowercase and trim on email address. Watch out: apostrophe and other
	 * special characters !#$%&'*+-/=?^_`{|}~ are allowed in local parts of
	 * emailaddresses
	 *
	 * @param email
	 * @return
	 */
	public static String normalizeEmail(String email) {
		if (StringUtils.isBlank(email)) {
			return null;
		} else {
			return email.toLowerCase().trim();
		}
	}

	public static String checkAndNormalizeEmail(String email) throws Exception {
		if (StringUtils.isBlank(email)) {
			throw new Exception("Empty email address");
		} else {
			email = normalizeEmail(email);
			if (!isEmailValid(email)) {
				throw new Exception("Invalid email address");
			} else {
				return email;
			}
		}
	}

	public static boolean compareByteArrays(byte[] array1, byte[] array2) {
		if (array1 == array2) {
			return true;
		} else if (array1 == null || array2 == null
				|| array1.length != array2.length) {
			return false;
		} else {
			for (int i = 0; i < array1.length; i++) {
				if (array1[i] != array2[i]) {
					return false;
				}
			}
			return true;
		}
	}

    /**
     * Compare two lists.
     * Equity is checked by ".equals" of all list elements.
     *
     * @param listActually
     * @param listExpected
     * @return
     */
	public static boolean compareLists(List<?> listExpected, List<?> listActually) {
		if (listExpected == null && listActually == null) {
			return true;
		} else if (listExpected == null || listActually == null
				|| listExpected.size() != listActually.size()) {
			return false;
		} else {
			for (int i = 0; i < listExpected.size(); i++) {
				if (listExpected.get(i) == null && listActually.get(i) == null) {
					// ok
				} else if (listExpected.get(i) == null
						|| listActually.get(i) == null
						|| !listExpected.get(i).equals(listActually.get(i))) {
					return false;
				}
			}
			return true;
		}
	}


    /**
     * Compare two lists.
     * Equity is checked by ".equals" of all list elements.
     *
     * @param listActually
     * @param checkedIndexes
     * @return
     */
	public static boolean compareLists(List<?> listExpected, List<?> listActually, int... checkedIndexes) {
		if (listExpected == null && listActually == null) {
			return true;
		} else if (listExpected == null || listActually == null
				|| listExpected.size() != listActually.size()) {
			return false;
		} else {
			for (int checkedIndex : checkedIndexes) {
				if (checkedIndex >= listExpected.size()) {
					throw new IllegalArgumentException("Itemindex to check is out of bounds");
				}

				if (listExpected.get(checkedIndex) == null && listActually.get(checkedIndex) == null) {
					// ok
				} else if (listExpected.get(checkedIndex) == null
						|| listActually.get(checkedIndex) == null
						|| !listExpected.get(checkedIndex).equals(listActually.get(checkedIndex))) {
					return false;
				}
			}
			return true;
		}
	}

	public static int getValidPageNumber(int fullListSize, int page, int rownums) {
		int pageNumber = page;
		double doubleFullListSize = fullListSize;
		double doublePageSize = rownums;
		int lastPagenumber = (int) Math.ceil(doubleFullListSize / doublePageSize);
		if (lastPagenumber < pageNumber) {
			pageNumber = 1;
		}
		return pageNumber;
	}

	public static String getStringIfStringIsNull(String str) {
		if (StringUtils.isBlank(str)) {
			return StringUtils.EMPTY;
		} else {
			return str;
		}
	}

	/**
	 * @deprecated Use "com.agnitas.emm.core.LinkServiceImpl.personalizeLink(ComTrackableLink, String, int, String)" instead
	 *
	 * @param hashTagString
	 * @param replacementMaps
	 * @return
	 */
	@Deprecated
	public static String replaceHashTags(String hashTagString, @SuppressWarnings("unchecked") Map<String, Object>... replacementMaps) {
		if (StringUtils.isBlank(hashTagString)) {
			return hashTagString;
		} else {
			String returnString = hashTagString;
			Pattern pattern = Pattern.compile("##([^#]+)##");
			Matcher matcher = pattern.matcher(hashTagString);
			int currentPosition = 0;

			while (matcher.find(currentPosition)) {
				int matcherStart = matcher.start();
				String tagNameString = matcher.group(1);
				String[] referenceKeys = tagNameString.split("/");
				String replacementValue = null;

				if (replacementMaps != null) {
					for (String referenceKey : referenceKeys) {
						for (Map<String, Object> replacementMap : replacementMaps) {
							if (replacementMap != null) {
								Object replacementData = replacementMap.get(referenceKey);
								if (replacementData != null) {
									String replacementDataString = replacementData.toString();
									if (StringUtils.isNotEmpty(replacementDataString)) {
										replacementValue = replacementData.toString();
										break;
									}
								}
							}
						}
						if (replacementValue != null) {
							break;
						}
					}
				}

				if (replacementValue == null) {
					replacementValue = "";
				}
				returnString = matcher.replaceAll(replacementValue);
				matcher = pattern.matcher(returnString);
				currentPosition += matcherStart + replacementValue.length();
			}
			return returnString;
		}
	}

	public static boolean isPhoneNumberValid(CharSequence value) {
		if (value == null || value.length() == 0) {
			return false;
		}

		PhoneNumberToken previousToken = PhoneNumberToken.BEGIN;
		boolean insideParentheses = false;
		int digitsCount = 0;

		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);

			if (Character.isDigit(c)) {
				digitsCount++;
				previousToken = PhoneNumberToken.DIGIT;
			} else if (c == '.' || c == '-' || c == '/') {
				if (previousToken == PhoneNumberToken.DIGIT || previousToken == PhoneNumberToken.RPAREN) {
					previousToken = PhoneNumberToken.SEPARATOR;
				} else {
					return false;
				}
			} else if (c == '(') {
				if (insideParentheses) {
					return false;
				}

				insideParentheses = true;
				previousToken = PhoneNumberToken.LPAREN;
			} else if (c == ')') {
				if (previousToken == PhoneNumberToken.DIGIT && insideParentheses) {
					insideParentheses = false;
					previousToken = PhoneNumberToken.RPAREN;
				} else {
					return false;
				}
			} else if (c == '+') {
				if (previousToken == PhoneNumberToken.BEGIN) {
					previousToken = PhoneNumberToken.PLUS;
				} else {
					return false;
				}
			} else if (!Character.isWhitespace(c)) {
				return false;
			}
		}

		return !insideParentheses && digitsCount >= 3 && digitsCount < 20;
	}

	/**
	 * Check if a given e-mail address is valid.
	 * Notice that a {@code null} value is invalid address.
	 *
	 * @param email an e-mail address to check.
	 * @return {@code true} if address is valid or {@code false} otherwise.
	 */
	public static boolean isEmailValid(String email) {
		return email != null && AgnitasEmailValidator.getInstance().isValid(email) && email.equals(email.trim());
	}
	
	/**
	 * Checks, if given address is a valid bounce-filter address.
	 * This must be either a valid email address or a domain starting with &quot;@&quot;.
	 * 
	 * @param address filter filter address
	 * 
	 * @return <code>true</code> if address is valid
	 */
	public static final boolean isValidBounceFilterAddress(final String address) {
		if(address == null || !address.equals(address.trim())) {
			return false;
		}
		
		final AgnitasEmailValidator validator = AgnitasEmailValidator.getInstance();
		if(validator.isValid(address)) {
			return true;
		} else if(address.startsWith("@")) {
			final String domain = address.substring(1);
			
			return validator.isValidDomain(domain);
		}
		
		return false;
	}

	public static boolean isEmailsListValid(String emails) {
		return isEmailsListValid(emails, true);
	}

	/**
	 * Validate list of e-mails. {@code null} is invalid e-mail list.
	 *
	 * @param emails a string containing e-mails separated by commas, semicolons or whitespaces.
	 * @param required whether ({@code true}) or not ({@code false}) at least one e-mail required
	 *                 (blank string or string containing only separators is considered invalid).
	 * @return {@code true} if e-mail list is valid or {@code false} otherwise.
	 */
	public static boolean isEmailsListValid(String emails, boolean required) {
		if (StringUtils.isNotBlank(emails)) {
			for (String address : emails.split("[,;\\s\\n\\r]+")) {
				if (StringUtils.isNotBlank(address)) {
					if (isEmailValid(address)) {
						required = false;
					} else {
						return false;
					}
				}
			}
		}

		return !required;
	}

	public static String getPythonVersion() {
		try {
			String version = executeOsCommand("python", "-c", "import sys\nprint(\".\".join(map(str, sys.version_info[:3])))");
			return StringUtils.trimToEmpty(version);
		} catch (Exception e) {
			logger.error("Cannot obtain Python version", e);
			return "";
		}
	}

	public static String getPythonSSL() {
		try {
			String result = executeOsCommand("python", "-c", "import socket\nprint(hasattr(socket, \"ssl\"))");
			return StringUtils.trimToEmpty(result);
		} catch (Exception e) {
			logger.error("Cannot obtain Python SSL", e);
			return "";
		}
	}

	public static String getMysqlClientVersion() {
		try {
			return StringUtils.trimToEmpty(executeOsCommand("mysql", "-V"));
		} catch (Exception e) {
			return "";
		}
	}

	public static String getSqlPlusClientVersion() {
		try {
			return StringUtils.trimToEmpty(executeOsCommand("sqlplus", "-v"));
		} catch (Exception e) {
			return "";
		}
	}

	public static String getMailVersion() {
		String sendmailVersion = getSendMailVersion();
		if (StringUtils.isNotBlank(sendmailVersion)) {
			return "Sendmail " + sendmailVersion.trim();
		} else {
			String postfixVersion = getPostfixVersion();
			if (StringUtils.isNotBlank(postfixVersion)) {
				return "Postfix " + postfixVersion.trim();
			} else {
				return "None";
			}
		}
	}

	public static String getPostfixVersion() {
		String postfixVersion = null;
		try {
			String postfixVersionRaw = executeOsCommand("postconf", "-d", "mail_version");
			if (postfixVersionRaw != null && postfixVersionRaw.contains("mail_version = ")) {
				postfixVersion = postfixVersionRaw.replace("mail_version = ", "");
			}
		} catch (Exception e) {
			logger.error("Cannot obtain postfix version", e);
		}
		return postfixVersion;
	}

	public static String getSendMailVersion() {
		String sendmailVersion = null;
		try {
			String sendmailVersionRaw = executeOsCommand("rpm", "-qa");
			Pattern sendmailVersionPattern = Pattern.compile("sendmail-(\\d+\\.\\d+\\.\\d+)-.*");
			if (sendmailVersionRaw != null) {
				Matcher sendmailVersionMatcher = sendmailVersionPattern.matcher(sendmailVersionRaw);
				if (sendmailVersionMatcher.find()) {
					sendmailVersion = sendmailVersionMatcher.group(1);
				}
			}
		} catch (Exception e) {
			logger.error("Cannot obtain send mail version", e);
		}
		return sendmailVersion;
	}

	public static boolean isJCEUnlimitedKeyStrenght() throws NoSuchAlgorithmException {
		return Cipher.getMaxAllowedKeyLength("RC5") >= 256;
	}

	public static String getTomcatVersion() {
		String version = "";
		try {
			if (new File("/opt/agnitas.com/software/tomcat/lib/catalina.jar").exists()) {
				String result = executeOsCommand("java", "-cp", "/opt/agnitas.com/software/tomcat/lib/catalina.jar", "org.apache.catalina.util.ServerInfo");
	
				Pattern tomcatVersionPattern = Pattern.compile("Server number:[ ]*(\\d+\\.\\d+\\.\\d+\\.\\d+)");
				Matcher tomcatVersionMatcher = tomcatVersionPattern.matcher(result);
				if (tomcatVersionMatcher.find()) {
					version = StringUtils.trimToEmpty(tomcatVersionMatcher.group(1));
				}
			}
		} catch (Exception e) {
			logger.error("Cannot obtain tomcat version", e);
		}
		return version;
	}

	public static String getOSVersion() throws IOException {
		File redhatVersionFile = new File("/etc/redhat-release");
		File ubuntuVersionFile = new File("/etc/os-release");

		if (redhatVersionFile.exists()) {
			return FileUtils.readFileToString(redhatVersionFile, "UTF-8");
		} else if (ubuntuVersionFile.exists()) {
			Properties osProperties = new Properties();
			try (FileInputStream ubuntuVersionFileInputStream = new FileInputStream(ubuntuVersionFile)) {
				osProperties.load(ubuntuVersionFileInputStream);
				return StringUtils.strip((String) osProperties.get("PRETTY_NAME"), "\"");
			}
		}

		return "";
	}

	public static String getWkhtmlVersion(ConfigService configService) {
		try {
			String result = executeOsCommand(configService.getValue(ConfigValue.WkhtmlToImageToolPath), "-V");
			return StringUtils.trimToEmpty(result);
		} catch (Exception e) {
			return "";
		}
	}

	public static String getJavaVersion() {
		StringBuilder javaVersion = new StringBuilder();
		javaVersion.append(System.getProperty("java.version"));
		javaVersion.append(" (");
		javaVersion.append(System.getProperty("java.vendor"));
		javaVersion.append(")");
		return javaVersion.toString();
	}
	
	public static int getCompanyMaxRecipients(HttpServletRequest request) {
		Company company = AgnUtils.getCompany(request);
		if (company != null) {
			return company.getMaxRecipients();
		}
		
		return 0;
	}
	
	public static int getCompanyMaxRecipients(ComAdmin admin) {
		Company company = AgnUtils.getCompany(admin);
		if (company != null) {
			return company.getMaxRecipients();
		}
		
		return 0;
	}

	public static String makeCloneName(Locale locale, String originName) {
		return makeCloneName(locale, originName, 0);
	}

	public static String makeCloneName(Locale locale, String originName, int cloneIndex) {
		String cloneName = SafeString.getLocaleString(COPY_PREFIX, locale) + " " + StringUtils.trimToEmpty(originName);

		if (cloneIndex > 0) {
			return cloneName + " (" + cloneIndex + ")";
		} else {
			return cloneName;
		}
	}
    
    public static String findUniqueCloneName(List<String> nameList, String prefix) {
		if (!nameList.contains(prefix)) {
			return prefix;
		}
		
		Collections.sort(nameList);
		
		int index = 1;
		String uniqueName;
		
		do {
			uniqueName = prefix + " (" + index ++ + ")";
		} while (nameList.contains(uniqueName));

		return uniqueName;
    }

    public static class TimeIgnoringComparator implements Comparator<Calendar> {
		@Override
		public int compare(Calendar c1, Calendar c2) {
			if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)) {
				return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
			} else if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH)) {
				return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
			} else {
				return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
			}
		}
	}

	/**
	 * This method splits a String list at the characters ',' ';' and '|' and
	 * trims the resulting items
	 *
	 * @param stringList
	 * @return
	 */
	public static List<String> splitAndTrimStringlist(String stringList) {
		if (stringList == null) {
			return null;
		} else {
			List<String> returnList = new ArrayList<>();
			String[] parts = stringList.split(",|;|\\|");
			for (String part : parts) {
				returnList.add(part.trim());
			}
			return returnList;
		}
	}

	/**
	 * Removes all empty items from stringlist.
	 *
	 * @param originalList the original list
	 * @return the list
	 */
	public static List<String> removeEmptyFromStringlist(List<String> originalList) {
		if (originalList == null) {
			return null;
		} else {
			List<String> returnList = new ArrayList<>();
			for (String item : originalList) {
				if (StringUtils.isNotBlank(item)) {
					returnList.add(item);
				}
			}
			return returnList;
		}
	}

	/**
	 * This method acts like splitAndTrimStringlist, but also splits at blanks
	 *
	 * @param stringList
	 * @return
	 */
	public static List<String> splitAndTrimList(String stringList) {
		if (stringList == null) {
			return null;
		} else {
			List<String> returnList = new ArrayList<>();
			String[] parts = stringList.split(",|;|\\|| ");
			for (String part : parts) {
				returnList.add(part.trim());
			}
			return returnList;
		}
	}

	public static boolean isDateValid(String date, String datePattern) {
		if (date == null || datePattern == null || datePattern.length() <= 0) {
			return false;
		}
		SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
		formatter.setLenient(false);
		try {
			formatter.parse(date);
		} catch (ParseException e) {
			return false;
		}
		return true;
	}

	public static boolean isDatePeriodValid(String startDate, String endDate, String datePattern) {
		try {
			Calendar start = Calendar.getInstance();
			Calendar end = Calendar.getInstance();
			start.setTime(new SimpleDateFormat(datePattern).parse(startDate));
			end.setTime(new SimpleDateFormat(datePattern).parse(endDate));
			if (start.before(end)) {
				return true;
			} else {
				return false;
			}
		} catch (ParseException e) {
			return false;
		}
	}

	public static String shortenStringToMaxLength(String value, int maxLength) {
		if (value != null && value.length() > maxLength) {
			return value.substring(0, maxLength - 4) + " ...";
		} else {
			return value;
		}
	}

	public static String getTempDir() {
		return System.getProperty("java.io.tmpdir");
	}

	public static Object getValueFromMapByCamelcaseKey(Map<String, Object> map, String key) {
		for (Entry<String, Object> entry : map.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(key)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public static String getActiveIndexesFromBooleanArray(boolean[] data) {
		StringBuilder dataString = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			if (data[i]) {
				if (dataString.length() > 0) {
					dataString.append(", ");
				}
				dataString.append(i);
			}
		}
		return dataString.toString();
	}

	public static WebApplicationContext getSpringContextFromRequest( HttpServletRequest request) {
		return WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext());
	}

	public static void logJspError(String jspFileName, String message, Exception e) {
		logger.error("Error in JSP '" + jspFileName + "': " + message, e);
	}

	public static void logJspInfo(String jspFileName, String message) {
		logger.info("Info from JSP '" + jspFileName + "': " + message);
	}

	public static boolean checkNumberIsWithinInterval(String numberIntervalString, int number) throws Exception {
		try {
			if (StringUtils.isNotBlank(numberIntervalString)) {
				List<String> intervals = splitAndTrimList(numberIntervalString);
				for (String interval : intervals) {
					if (StringUtils.isNotBlank(interval)) {
						if (interval.startsWith("-")) {
							int intervalEnd = Integer.parseInt(interval
									.substring(1));
							if (number <= intervalEnd) {
								return true;
							}
						} else if (interval.endsWith("+")) {
							int intervalStart = Integer.parseInt(interval
									.substring(0, interval.length() - 1));
							if (intervalStart <= number) {
								return true;
							}
						} else if (interval.contains("-")) {
							int intervalStart = Integer.parseInt(interval
									.substring(0, interval.indexOf('-')));
							int intervalEnd = Integer.parseInt(interval
									.substring(interval.indexOf('-') + 1));
							if (intervalStart <= number
									&& number <= intervalEnd) {
								return true;
							}
						} else {
							int item = Integer.parseInt(interval);
							if (item == number) {
								return true;
							}
						}
					}
				}
			}
			return false;
		} catch (Exception e) {
			throw new Exception("Invalid numberIntervalString: " + numberIntervalString);
		}
	}
	
	public static Map<String, String> getParamsMap(String paramsString) {
		return getParamsMap(paramsString, ";", "=");
	}

	public static Map<String, String> getParamsMap(String paramsString, String pairsSplitter, String keyValueSplitter) {
		if (StringUtils.isEmpty(paramsString) ||
				StringUtils.isEmpty(pairsSplitter) ||
				StringUtils.isEmpty(keyValueSplitter)) {
			return new HashMap<>();
		}
		
		return Arrays.stream(paramsString.split(pairsSplitter))
				.map(s -> s.split(keyValueSplitter))
				.filter(param -> param.length == 2)
				.collect(Collectors.toMap(s -> s[0], s -> s[1]));
	}
	
	public static String getParamsString(Map<String, String> paramsMap) {
		return getParamsString(paramsMap, ";", "=");
	}

	public static String getParamsString(Map<String, String> paramsMap, String pairsDelimiter, String keyValueDelimiter) {
		if (MapUtils.isEmpty(paramsMap)) {
			return "";
		}

		Objects.requireNonNull(pairsDelimiter);
		Objects.requireNonNull(keyValueDelimiter);

		return paramsMap.entrySet().stream()
				.map(pair -> pair.getKey() + keyValueDelimiter + pair.getValue())
				.collect(Collectors.joining(pairsDelimiter));
	}

	public static <T> List<T> getEnumerationAsList(Enumeration<T> enumeration) {
		List<T> returnList = new ArrayList<>();
    	while (enumeration.hasMoreElements()) {
    		T item = enumeration.nextElement();
    		returnList.add(item);
    	}
    	return returnList;
	}

	public static boolean check24HourTime(String timeString) {
		return Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]$").matcher(timeString).matches();
	}

	/**
	 * Returns domain of email address
	 *
	 * Examples:
	 *   "abc@xyz" <abc@def.de> => def.de
	 *   abc@def.de => def.de
	 *
	 * @param emailAddress
	 * @return
	 */
	public static String getDomainFromEmail(String emailAddress) {
		int domainTextDelimiterIndex = emailAddress.lastIndexOf("@");
		if (domainTextDelimiterIndex >= 0) {
			String domain = emailAddress.substring(domainTextDelimiterIndex + 1);
			if (domain.endsWith(">")) {
				domain = domain.substring(0, domain.length() - 1);
			}
			return domain;
		} else {
			return null;
		}
	}

	public static List<String> makeListTrimAndLowerCase(List<String> data) {
		List<String> returnList = new ArrayList<>();
		for (String item : data) {
			if (item == null) {
				returnList.add(null);
			} else {
				returnList.add(item.trim().toLowerCase());
			}
		}
		return returnList;
	}

	public static List<String> makeListTrim(List<String> data) {
		List<String> returnList = new ArrayList<>();
		for (String item : data) {
			if (item == null) {
				returnList.add(null);
			} else {
				returnList.add(item.trim());
			}
		}
		return returnList;
	}

	/**
	 * Search for the next index of any of the given search Strings after the startIndex in a data String
	 *
	 * @param dataString
	 * @param startIndex
	 * @param searchStrings
	 * @return
	 */
	public static int searchNext(String dataString, int startIndex, String... searchStrings) {
		int foundIndex = -1;

		for (String searchString : searchStrings) {
			int nextFoundIndex = dataString.indexOf(searchString, startIndex);
			if (nextFoundIndex >= 0 && (foundIndex == -1 || nextFoundIndex < foundIndex)) {
				foundIndex = nextFoundIndex;
			}
		}

		return foundIndex;
	}

	/**
	 * Mac/Apple linebreak character
	 */
	public static String linebreakMac = "\r";

	/**
	 * Unix/Linux linebreak character
	 */
	public static String linebreakUnix = "\n";

	/**
	 * Windows linebreak characters
	 */
	public static String linebreakWindows = "\r\n";

	/**
	 * Get line number at a given text position
	 *
	 * @param dataString
	 * @param textPosition
	 * @return
	 */
	public static int getLineNumberOfTextposition(String dataString, int textPosition) {
		if (dataString == null) {
			return -1;
		} else {
			try {
				String textPart = dataString;
				if (dataString.length() > textPosition) {
					textPart = dataString.substring(0, textPosition);
				}
				int lineNumber = getLineCount(textPart);
				if (textPart.endsWith(linebreakUnix) || textPart.endsWith(linebreakMac)) {
					lineNumber++;
				}
				return lineNumber;
			} catch (Exception e) {
				logger.error("Error when trying to get line number", e);
				return -1;
			}
		}
	}

	/**
	 * Get the number of lines in a text
	 *
	 * @param dataString
	 * @return
	 * @throws IOException
	 */
	public static int getLineCount(String dataString) throws IOException {
		if (dataString == null) {
			return 0;
		} else if ("".equals(dataString)) {
			return 1;
		} else {
			LineNumberReader lineNumberReader = null;
			try {
				lineNumberReader = new LineNumberReader(new StringReader(dataString));
				while (lineNumberReader.readLine() != null) {
					// do nothing
				}

				return lineNumberReader.getLineNumber();
			} finally {
				IOUtils.closeQuietly(lineNumberReader);
			}
		}
	}

	/**
	 * Get the startindex of the line at a given position within the text
	 *
	 * @param dataString
	 * @param index
	 * @return
	 */
	public static int getStartIndexOfLineAtIndex(String dataString, int index) {
		if (dataString == null || index < 0) {
			return -1;
		} else if (index == 0) {
			return 0;
		} else {
			int nextLineBreakMac = dataString.lastIndexOf(linebreakMac, index);
			int nextLineBreakUnix = dataString.lastIndexOf(linebreakUnix, index);
			int nextLineBreakWindows = dataString.lastIndexOf(linebreakWindows, index);

			if (nextLineBreakMac >= 0 && (nextLineBreakUnix < 0 || nextLineBreakMac < nextLineBreakUnix) && (nextLineBreakWindows < 0 || nextLineBreakMac < nextLineBreakWindows)) {
				return nextLineBreakMac + linebreakMac.length();
			} else if (nextLineBreakUnix >= 0 && (nextLineBreakWindows < 0 || nextLineBreakUnix < nextLineBreakWindows)) {
				return nextLineBreakUnix + linebreakUnix.length();
			} else if (nextLineBreakWindows >= 0) {
				return nextLineBreakWindows + linebreakWindows.length();
			} else {
				return 0;
			}
		}
	}

	/**
	 * Make sure that a new line is represented by LF (\n) character only.
	 *
	 * @param text a string to process.
	 * @return a string with replaced new line characters.
	 */
	public static String ensureUnixNewLines(String text) {
		if (StringUtils.isNotEmpty(text)) {
			return text.replace(linebreakWindows, linebreakUnix)
					.replace(linebreakMac, linebreakUnix);
		}
		return text;
	}

	public static boolean startsWith(byte[] data, byte[] prefix) {
		if (data == null || prefix == null) {
			return data == prefix;
		}

		if (data.length < prefix.length) {
			return false;
		}

		for (int i = 0; i < prefix.length; i++) {
			if (data[i] != prefix[i]) {
				return false;
			}
		}

		return true;
	}

	public static String getHumanReadableNumber(Number value, String unitTypeSign, boolean siUnits, HttpServletRequest request) {
		return getHumanReadableNumber(value, unitTypeSign, siUnits, getLocale(request), true);
	}

	public static String getHumanReadableNumber(Number value, String unitTypeSign, boolean siUnits) {
		return getHumanReadableNumber(value, unitTypeSign, siUnits, Locale.ENGLISH, true);
	}

	public static String getHumanReadableNumber(Number value, String unitTypeSign, boolean siUnits, Locale locale) {
		return getHumanReadableNumber(value, unitTypeSign, siUnits, locale, true);
	}

	  /**
	   * Make a number with unitsign human readable
	   *
	   * @param value
	   * @param unitTypeSign
	   * @param siUnits
	   * @return
	   */
	public static String getHumanReadableNumber(Number value, String unitTypeSign, boolean siUnits, Locale locale, boolean keepTrailingZeros) {
		int unit = siUnits ? 1000 : 1024;
		double interimValue = value.doubleValue();
		String unitExtension = "";
		if (interimValue < unit) {
			if (StringUtils.isNotBlank(unitTypeSign)) {
				unitExtension = " " + unitTypeSign;
			}

			if (value instanceof Integer || value instanceof Long) {
				return value + unitExtension;
			}
		} else {
			int exp = (int) (Math.log(interimValue) / Math.log(unit));
			unitExtension = " " + (siUnits ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (siUnits ? "" : "i");
			if (StringUtils.isNotBlank(unitTypeSign)) {
				unitExtension += unitTypeSign;
			}
			interimValue = interimValue / Math.pow(unit, exp);
		}

		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
		DecimalFormat numberFormat;
		if (keepTrailingZeros) {
			if (interimValue >= 1000) {
				numberFormat = new DecimalFormat("#0.0", decimalFormatSymbols);
			} else if (interimValue >= 100) {
				numberFormat = new DecimalFormat("#0.00", decimalFormatSymbols);
			} else if (interimValue >= 10) {
				numberFormat = new DecimalFormat("#0.000", decimalFormatSymbols);
			} else if (interimValue >= 1) {
				numberFormat = new DecimalFormat("#0.0000", decimalFormatSymbols);
			} else {
				numberFormat = new DecimalFormat("#0.00000", decimalFormatSymbols);
			}
		} else {
			if (interimValue >= 1000) {
				numberFormat = new DecimalFormat("#0.0", decimalFormatSymbols);
			} else if (interimValue >= 100) {
				numberFormat = new DecimalFormat("#0.0#", decimalFormatSymbols);
			} else if (interimValue >= 10) {
				numberFormat = new DecimalFormat("#0.0##", decimalFormatSymbols);
			} else if (interimValue >= 1) {
				numberFormat = new DecimalFormat("#0.0###", decimalFormatSymbols);
			} else {
				numberFormat = new DecimalFormat("#0.0####", decimalFormatSymbols);
			}
		}

		return numberFormat.format(interimValue) + unitExtension;
	}

	public static String getHumanReadableDurationFromMillis(long durationMillis) {
		long interimValue = durationMillis;

		int milliseconds = (int) interimValue % 1000;
		interimValue = interimValue / 1000;
		int seconds = (int) interimValue % 60;
		interimValue /= 60;
		int minutes = (int) interimValue % 60;
		interimValue /= 60;
		int hours = (int) interimValue % 24;
		interimValue /= 24;
		int days = (int) interimValue % 12;
		interimValue /= 12;
		int months = (int) interimValue % 365;
		interimValue /= 365;
		int years = (int) interimValue;

		String reaturnValue = "";
		boolean showOthers = false;
		if (years > 0) {
			showOthers = true;
			reaturnValue += years + "Y ";
		}
		if (showOthers || months > 0) {
			showOthers = true;
			reaturnValue += months + "M ";
		}
		if (showOthers || days > 0) {
			showOthers = true;
			reaturnValue += days + "D ";
		}
		if (showOthers || hours > 0) {
			showOthers = true;
			reaturnValue += hours + "h ";
		}
		if (showOthers || minutes > 0) {
			showOthers = true;
			reaturnValue += minutes + "m ";
		}
		if (showOthers || seconds > 0) {
			showOthers = true;
			reaturnValue += seconds + "s ";
		}
		if (milliseconds > 0) {
			reaturnValue += milliseconds + "ms";
		}

		return reaturnValue.trim();
	}

	/**
	 * Make a number human readable
	 *
	 * @param value
	 * @return
	 */
	public static String getHumanReadableNumber(Number value, Locale locale) {
		return NumberFormat.getInstance(locale).format(value);
	}

	/**
	 * Escape a char by adding the same char to it like escaping "\" by "\\"
	 *
	 * @param input
	 * @param charToEscape
	 * @return
	 */
    public static String escapeChars(String input, String charToEscape) {
    	if (StringUtils.isEmpty(input) || StringUtils.isEmpty(charToEscape)) {
    		return input;
    	} else {
    		return input.replace(charToEscape, charToEscape + charToEscape);
    	}
    }

    public static String emptyToNull(String string) {
    	return StringUtils.isEmpty(string) ? null : string;
	}

	public static boolean equalsNullToEmpty(String str1, String str2) {
    	return StringUtils.defaultString(str1).equals(StringUtils.defaultString(str2));
	}

    public static String readFileToString(File file, String encoding) throws Exception {
    	try (FileInputStream fileInputStream = new FileInputStream(file)) {
    		byte[] data = new byte[(int) file.length()];
    		if (fileInputStream.read(data) != -1) {
    			return new String(data, encoding);
    		} else {
    			throw new Exception("Cannot read file: " + file.getAbsolutePath());
    		}
    	}
    }

	public static String getIpAddressForStorage(HttpServletRequest request) {
		String ipAddress = request.getRemoteAddr();
		return ipAddress.substring(0, ipAddress.lastIndexOf(".")) + ".???";
	}

	/**
	 * Gets the lowercase file extension like "jpg", "xml" or "png"
	 *
	 * @param fileName the file name
	 * @return the file extension
	 */
	public static String getFileExtension(String fileName) {
		int index = fileName.lastIndexOf('.');
		if (index == -1) {
			// No "."? Take whole name
			return fileName.toLowerCase().trim();
		} else {
			return fileName.substring(index + 1).toLowerCase().trim();
		}
	}

	/**
	 * make the file extension with dot like ".jpg", ".xml" or ".png"
	 *
	 * @param extension the file extension
	 * @return the file extension with dot
	 */
	public static String makeFileExtension(String extension) {
		if (StringUtils.isEmpty(extension) || StringUtils.startsWith(extension, ".")) {
			return StringUtils.trimToEmpty(extension);
		}

		return "." + extension;
	}

	/**
	 * Convert the wildcard notation to corresponding regex pattern.
	 *
	 * @return a regex pattern.
	 */
	public static String getPatternForWildcard(String wildcard) {
		if (StringUtils.isBlank(wildcard)) {
			return "";
		}
		return ("\\Q" + wildcard + "\\E")
				.replace("*", "\\E.*\\Q")
				.replace("|", "\\E|\\Q");
	}

	/**
	 *  Check for Struts or BeanUtils Bug:
	 *  Sometimes String-value is filled with String[]-value
	 *  (This cannot be checked at compile-time, syntax says it is a String, but infact it is a String[])
	 *  TODO: Check when using newer Strusts version (1.3.10), if Struts-bug still exists
	 *
	 *  This method replaces the String[]-values with their last element
	 * @param formMap
	 */
	public static Map<String, String> repairFormMap(Map<String, String> formMap) {
		Map<String, String> resultMap = new HashMap<>();
		for (Map.Entry<String,String> entry : formMap.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String[]) {
				// Findbug thinks this cast is a bug, but at runtime this really works
				String[] keyData = (String[]) ((Object) entry.getValue());
				resultMap.put(entry.getKey(), keyData[keyData.length - 1]);
			} else {
				resultMap.put(entry.getKey(), entry.getValue());
			}
		}
		return resultMap;
	}

	public static Locale getLocale(HttpServletRequest request) {
        ComAdmin admin = getAdmin(request);
        return (admin == null)
        		? Locale.getDefault() //(Locale)request.getSession().getAttribute(org.apache.struts.Globals.LOCALE_KEY)
        		: admin.getLocale();
	}

	public static boolean isGerman(Locale locale) {
		return Locale.GERMAN.getLanguage().equals(locale.getLanguage());
	}

	public static Object[] extendObjectArray(Object[] array, Object... objects) {
		if (objects != null && objects.length > 0) {
			if (array == null) {
				array = new Object[0];
			}
			Object[] extendedParameters = new Object[array.length + objects.length];
			for (int i = 0; i < array.length; i++) {
				extendedParameters[i] = array[i];
			}
			for (int i = 0; i < objects.length; i++) {
				extendedParameters[array.length + i] = objects[i];
			}
			return extendedParameters;
		} else {
			return array;
		}
	}

	public static String removeJsessionIdFromUrl(String urlString) {
		if (urlString.contains("jsessionid")) {
			int start = urlString.indexOf("jsessionid");
			if (urlString.charAt(start - 1) == ';') {
				start -= 1;
			}
			int end = urlString.indexOf("?", start);
			if (end < 0) {
				end = urlString.indexOf("&", start);
			}
			if (end < 0) {
				urlString = urlString.substring(0, start);
			} else {
				urlString = urlString.substring(0, start) + urlString.substring(end);
			}
		}
		return urlString;
	}

	public static String mapToString(Map<?, ?> map) {
		StringBuilder builder = new StringBuilder();
		if (map != null) {
			for (Entry<?, ?> entry : map.entrySet()) {
				if (builder.length() > 0) {
					builder.append("\n");
				}
				if (entry.getKey() instanceof String) {
					builder.append((String) entry.getKey());
				} else {
					builder.append(entry.getKey());
				}

				builder.append(" = ");

				if (entry.getValue() instanceof String) {
					builder.append((String) entry.getValue());
				} else if (entry.getValue() instanceof String[]) {
					builder.append(StringUtils.join((String[]) entry.getValue(), ", "));
				} else {
					builder.append(entry.getValue());
				}
			}
		}
		return builder.toString();
	}

	public static void checkUrl(String urlString) throws Exception{
		if (StringUtils.isBlank(urlString)) {
			throw new Exception("Url is empty");
		}

		URL url;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			throw new Exception("Url is invalid: " + urlString + " : " + e.getMessage(), e);
		}

		try {
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.connect();
			httpURLConnection.getResponseCode();
		} catch (ProtocolException e) {
			throw new Exception("Url protocol is invalid: " + urlString + " : " + e.getMessage(), e);
		} catch (IOException e) {
			throw new Exception("Cannot connect to Url: " + urlString + " : " + e.getMessage(), e);
		}
	}

	public static void checkHostConnection(String hostname, int port) throws Exception {
		int timeout = 2000;

		InetSocketAddress endPoint = new InetSocketAddress(hostname, port);

		if (endPoint.isUnresolved()) {
			throw new Exception("Cannot resolve hostname: " + hostname);
		} else {
			try (Socket socket = new Socket()) {
				socket.connect(endPoint, timeout);
			} catch (IOException ioe) {
				throw new Exception("Connection Error: " + hostname + ":" + port + " : " + ioe.getMessage(), ioe);
			}
		}
	}

	/**
	 * Searches for the current ckEditor installation with the highest version number.
	 * The result is cached.
	 */
	public static String getCkEditorPath(HttpServletRequest request) throws Exception {
		ComAdmin admin = getAdmin(request);
		int companyID = 0;

		if (admin != null) {
			companyID = admin.getCompanyID();
		}

		String cachedPath = CKEDITOR_PATH_CACHE.get(companyID);
		File ckEditorRootDir = getLibraryRootDir("/../../js/lib/ckeditor");
		boolean shouldUseLatestVersion = ConfigService.getInstance().getBooleanValue(ConfigValue.UseLatestCkEditor, companyID);

		String editorPath = getLibraryPath("CkEditor", cachedPath, "ckeditor-", ckEditorRootDir, shouldUseLatestVersion);

		if (cachedPath == null) {
			editorPath = "js/lib/ckeditor/" + editorPath;
			CKEDITOR_PATH_CACHE.put(companyID, editorPath);
		}

		return editorPath;
	}

	public static String getAceEditorPath(HttpServletRequest request) throws Exception {
		ComAdmin admin = getAdmin(request);
		int companyID = 0;

		if (admin != null) {
			companyID = admin.getCompanyID();
		}

		String cachedPath = ACE_EDITOR_PATH_CACHE.get(companyID);
		File aceEditorRootDir = getLibraryRootDir("/../../js/lib/ace");
		boolean shouldUseLatestVersion = ConfigService.getInstance().getBooleanValue(ConfigValue.UseLatestAceEditor, companyID);

		String editorPath = getLibraryPath("AceEditor", cachedPath, "ace_", aceEditorRootDir, shouldUseLatestVersion);

		if (cachedPath == null) {
			editorPath = "js/lib/ace/" + editorPath;
			ACE_EDITOR_PATH_CACHE.put(companyID, editorPath);
		}

		return editorPath;
	}

	private static String getLibraryPath(String libName, String cachedPath, String dirNamePrefix, File rootDir, boolean useLastVersion) throws Exception {
		String libraryPath = cachedPath;
		if (libraryPath == null) {
			try {
				Version libraryVersion = new Version("0.0.0");
				for (File subFile : rootDir.listFiles()){
					if (subFile.isDirectory() && subFile.getName().startsWith(dirNamePrefix)) {
						String versionString = subFile.getName().substring(dirNamePrefix.length());
						Version nextVersion = new Version(versionString);
						if (libraryPath == null) {
							libraryVersion = nextVersion;
							libraryPath = subFile.getName();
						} else {
							if (useLastVersion) {
								if (nextVersion.compareTo(libraryVersion) > 0) {
									libraryVersion = nextVersion;
									libraryPath = subFile.getName();
								}
							} else {
								if (nextVersion.compareTo(libraryVersion) < 0) {
									libraryVersion = nextVersion;
									libraryPath = subFile.getName();
								}
							}
						}
					}
				}

				if (libraryPath != null) {
					return libraryPath;
				}

				throw new Exception("Cannot find " + libName + " directory");
			} catch (Exception e) {
				throw new Exception("Cannot find " + libName + " directory: " + e.getMessage(), e);
			}
		}

		return libraryPath;
	}

	private static File getLibraryRootDir(String libRelativePath) throws Exception {
		String applicationInstallPath = AgnUtils.class.getClassLoader().getResource("/").getFile();
		if (applicationInstallPath != null && applicationInstallPath.endsWith("/")) {
			applicationInstallPath = applicationInstallPath.substring(0, applicationInstallPath.length() - 1);
		}

		if (StringUtils.isBlank(applicationInstallPath)) {
			throw new Exception("Cannot find application install directory");
		} else {
			applicationInstallPath += libRelativePath;
			try {
				return new File(applicationInstallPath).getCanonicalFile();
			} catch (Exception e) {
				throw new Exception("Cannot find application install directory: " + e.getMessage(), e);
			}
		}
	}

	public static boolean endsWithIgnoreCase(String data, String suffix) {
		if (data == suffix) {
			// both null or same object
			return true;
		} else if (data == null) {
			// data is null but suffix is not
			return false;
		} else if (suffix == null) {
			// suffix is null but data is not
			return true;
		} else if (data.toLowerCase().endsWith(suffix.toLowerCase())) {
			// both are set, so ignore the case for standard endsWith-method
			return true;
		} else {
			// anything else
			return false;
		}
	}

	public static int indexOfIgnoreCase(String data, String part) {
		if (data == part) {
			// both null or same object
			return 0;
		} else if (data == null || part == null) {
			// suffix is null but data is not or vice versa
			return -1;
		} else {
			// anything else
			return data.toLowerCase().indexOf(part.toLowerCase());
		}
	}

	public static List<String> getArrayListOfStrings(String... values) {
		List<String> returnList = new ArrayList<>();
		if (values != null) {
			for (String value : values) {
				returnList.add(value);
			}
		}
		return returnList;
	}

	/**
	 * Get the preceding character at a index position within a text.
	 * Some characters may be are ignored while searching.
	 *
	 * @param text
	 * @param startIndex
	 * @param ignoredCharacters
	 * @return
	 */
	public static char getCharacterBefore(String text, int startIndex, char... ignoredCharacters) {
		try {
			int currentIndex = startIndex - 1;
			char foundChar = text.charAt(currentIndex);
			while (containsCharacter(ignoredCharacters, foundChar)) {
				currentIndex--;
				foundChar = text.charAt(currentIndex);
			}
			return foundChar;
		} catch (Exception e) {
			// Index out of text
			return 0;
		}
	}

	/**
	 * Get the next position of any of the given characters within a text
	 *
	 * @param text
	 * @param startIndex
	 * @param searchedCharacters
	 * @return
	 */
	public static int getNextIndexOf(String text, int startIndex, char... searchedCharacters) {
		try {
			int currentIndex = startIndex;
			char nextChar = text.charAt(currentIndex);
			while (!containsCharacter(searchedCharacters, nextChar)) {
				currentIndex++;
				nextChar = text.charAt(currentIndex);
			}
			return currentIndex;
		} catch (Exception e) {
			// Index out of text
			return -1;
		}
	}

	/**
	 * Check if a searchString is included in a text berore a given index position.
	 * Some characters may be are ignored while comparing.
	 *
	 * @param text
	 * @param startIndex
	 * @param searchString
	 * @param ignoredCharacters
	 * @return
	 */
	public static boolean checkPreviousTextEquals(String text, int startIndex, String searchString, char... ignoredCharacters) {
		try {
			int indexInSearchString = searchString.length() - 1;
			int indexInText = startIndex - 1;
			while (indexInSearchString > 0) {
				char searchStringChar = searchString.charAt(indexInSearchString);
				if (containsCharacter(ignoredCharacters, searchStringChar)) {
					indexInSearchString--;
					continue;
				}
				char textChar = text.charAt(indexInText);
				if (containsCharacter(ignoredCharacters, textChar)) {
					indexInText--;
					continue;
				}
				if (textChar != searchStringChar) {
					return false;
				} else {
					indexInSearchString--;
					indexInText--;
				}
			}
			return true;
		} catch (Exception e) {
			// Index out of text
			return false;
		}
	}

	public static String getUniqueCloneName(String name, int maxLength, java.util.function.Predicate<String> checkInUse) {
		return getUniqueCloneName(name, null, maxLength, checkInUse);
	}

	public static String getUniqueCloneName(String name, String prefix, int maxLength, java.util.function.Predicate<String> checkInUse) {
		String newName;

		// Do not add extra prefix if one is already there
		if (StringUtils.isEmpty(prefix) || name.startsWith(prefix)) {
			newName = name;
		} else {
			newName = StringUtils.abbreviate(prefix + name, maxLength);
		}

		int index = 0;

		// Parse and replace index (suffix) if one is already there
		Pattern trailingIndexPattern = Pattern.compile("(.+)\\s\\((\\d+)\\)");
		Matcher matcher = trailingIndexPattern.matcher(newName);
		if (matcher.matches()) {
			newName = matcher.group(1);
			index = Integer.parseInt(matcher.group(2));
		}

		if (index > 0 || checkInUse.test(newName)) {
			String suffix;
			do {
				index++;
				suffix = " (" + index + ")";
				newName = StringUtils.abbreviate(newName, maxLength - suffix.length());
			} while (checkInUse.test(newName + suffix));
			return newName + suffix;
		} else {
			return newName;
		}
	}

	public static boolean sortingDirectionToBoolean(String direction) {
		// Ascending by default
		return sortingDirectionToBoolean(direction, true);
	}

	public static boolean sortingDirectionToBoolean(String direction, boolean defaultValue) {
		if (StringUtils.isBlank(direction)) {
			return defaultValue;
		}

		direction = direction.toLowerCase();

		if (StringUtils.startsWith("ascending", direction)) {
			return true;
		} else if (StringUtils.startsWith("descending", direction)) {
			return false;
		} else {
			return defaultValue;
		}
	}
	
	/**
	 * Convert a float or double number from the admins locale display into english standard display
	 */
	public static String normalizeNumber(Locale locale, String numberString) {
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
		String decimalSeparator = Character.toString(decimalFormatSymbols.getDecimalSeparator());
		String groupingSeparator = Character.toString(decimalFormatSymbols.getGroupingSeparator());
		return normalizeNumber(decimalSeparator, groupingSeparator, numberString);
	}

	/**
	 * Convert a float or double number with defined decimalSeparator into english standard display
	 */
	public static String normalizeNumber(String decimalSeparator, String numberString) {
		if (",".equals(decimalSeparator)) {
			return normalizeNumber(decimalSeparator, ".", numberString);
		} else {
			return normalizeNumber(decimalSeparator, DEFAULT_GROUPING_SEPARATOR, numberString);
		}
	}

	/**
	 * Convert a float or double number with defined decimalSeparator and groupingSeparator into english standard display
	 */
	public static String normalizeNumber(String decimalSeparator, String groupingSeparator, String numberString) {
		if (StringUtils.isBlank(numberString)) {
			return "";
		} else {
			decimalSeparator = StringUtils.defaultIfEmpty(decimalSeparator, DEFAULT_DECIMAL_SEPARATOR);
			groupingSeparator = StringUtils.defaultIfEmpty(groupingSeparator, DEFAULT_GROUPING_SEPARATOR);

			String result;
			if (StringUtils.equals(decimalSeparator, ".")) {
				result = StringUtils.replaceEach(numberString, new String[] { groupingSeparator, " " }, new String[] { "", "" });
			} else {
				result = StringUtils.replaceEach(numberString, new String[] { decimalSeparator, groupingSeparator, " " }, new String[] { ".", "", "" });
			}
			return StringUtils.trimToEmpty(result);
		}
	}

	/**
	 * This marker text is used to prevent browser caches from caching old versions of css and js files.
	 * This marker is cached and only changed for new builds.
	 * Even reboot of the same version won't change the marker, which is based on emm-jars buildtime.
	 */
	public static String getBrowserCacheMarker() throws UnsupportedEncodingException {
		if (BROWSER_CACHE_MARKER == null) {
			BROWSER_CACHE_MARKER = new Sha512Encoder().encodeToHex(new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS).format(ConfigService.getBuildTime()));
		}

		return BROWSER_CACHE_MARKER;
	}

	/**
	 * "16.07.123-hf777" -> 1607123
	 * @param applicationVersion string like "16.07.123-hf77" or "16.07.123"
	 */
	public static Integer transformApplicationVersionToNumber(String applicationVersion){
		if (isApplicationVersionValid(applicationVersion)){
			int yearPart = Integer.parseInt(applicationVersion.substring(0,2));
			int quarterPart = Integer.parseInt(applicationVersion.substring(3,5));
			int commitPart = Integer.parseInt(applicationVersion.substring(6,9));
			return commitPart + 1000 * quarterPart + 100000 * yearPart;
		}
		return null;
	}

	/**
	 * 1607123 -> "16.07.123"
	 * @param number between 100000 and 9999999
	 */
	public static String transformNumberToApplicationVersion(int number){
		if (number < 100000 || number > 9999999){
			return null;
		}
		int commitPart = number % 1000;
		int quarterPart = ((number - commitPart) % 100000) / 1000;
		int yearPart = (number - commitPart - quarterPart * 1000) / 100000;
		return StringUtils.leftPad(String.valueOf(yearPart), 2, '0') +
				"." + StringUtils.leftPad(String.valueOf(quarterPart), 2, '0') +
				"." + StringUtils.leftPad(String.valueOf(commitPart), 3, '0');
	}

	/**
	 * Is application version looks like "16.07.123-hf77" or "16.07.123"
	 */
	public static boolean isApplicationVersionValid(String applicationVersion){
		if (StringUtils.isEmpty(applicationVersion)){
			return false;
		}
		return APPLICATION_VERSION_PATTERN.matcher(applicationVersion).matches();
	}

	/**
	 * User can choose only initial block size.
	 * This method counts correct blocksize and stepping from the initial blocksize.
	 * @param blocksize chosen by user
	 * @param defaultStepping required if this method is unable to get stepping for blocksize
	 * @return first value - blocksize, second value - stepping
	 */
	public static Tuple<Integer, Integer> makeBlocksizeAndSteppingFromBlocksize(int blocksize, int defaultStepping){
		int stepping = 0;
		switch (blocksize) {
			case 0:
				stepping = 0;
				break;
			case 1000:
				blocksize = 250;
				stepping = 15;
				break;
			case 5000:
				blocksize = 1250;
				stepping = 15;
				break;
			case 10000:
				blocksize = 500;
				stepping = 3;
				break;
			case 25000:
				blocksize = 416;
				stepping = 1;
				break;
			case 50000:
				blocksize = 833;
				stepping = 1;
				break;
			case 100000:
				blocksize = 1666;
				stepping = 1;
				break;
			case 250000:
				blocksize = 4166;
				stepping = 1;
				break;
			case 500000:
				blocksize = 8333;
				stepping = 1;
				break;
			default:
				stepping = defaultStepping;
		}
		return new Tuple<>(blocksize, stepping);
	}

	public static String replaceHomeVariables(String value) {
		if (StringUtils.isNotBlank(value)) {
			String homeDir = AgnUtils.getUserHomeDir();
			return value.replace("~", homeDir).replace("$HOME", homeDir).replace("${HOME}", homeDir).replace("$home", homeDir).replace("${home}", homeDir);
		} else {
			return value;
		}
	}
	
	public static String replaceVersionPlaceholders(String value, Version applicationVersion) throws Exception {
		if (StringUtils.isEmpty(value)) {
			return value;
		}

        try {
        	if (applicationVersion == null) {
				return value.replace("${ApplicationVersion}", "UnknownApplicationVersion")
				        .replace("${ApplicationMajorVersion}", "UnknownMajorVersion")
				        .replace("${ApplicationMinorVersion}", "UnknownMinorVersion")
				        .replace("${ApplicationMicroVersion}", "UnknownMicroVersion")
				        .replace("${ApplicationHotfixVersion}", "UnknownHotfixVersion");
        	} else {
				return value.replace("${ApplicationVersion}", applicationVersion.toString())
				        .replace("${ApplicationMajorVersion}", Integer.toString(applicationVersion.getMajorVersion()))
				        .replace("${ApplicationMinorVersion}", Integer.toString(applicationVersion.getMinorVersion()))
				        .replace("${ApplicationMicroVersion}", Integer.toString(applicationVersion.getMicroVersion()))
				        .replace("${ApplicationHotfixVersion}", Integer.toString(applicationVersion.getHotfixVersion()));
        	}
		} catch (Exception e) {
			logger.error("Error in replacing placeholders of value: '" + value + "'");
			throw e;
		}
    }

	public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> collection) {
		List<T> list = new ArrayList<>(collection);
		Collections.sort(list);
		return list;
	}

	public static String shortenStringToMaxLengthCutRight(String value, int maxLength) {
		if (value != null && value.length() > maxLength) {
			return value.substring(0, maxLength - 4) + " ...";
		} else {
			return value;
		}
	}

	public static String shortenStringToMaxLengthCutMiddle(String value, int maxLength) {
		if (value != null && value.length() > maxLength) {
			int leftLength = (maxLength - 5) / 2;
			return value.substring(0, leftLength) + " ... " + value.substring(value.length() - ((maxLength - leftLength) - 5));
		} else {
			return value;
		}
	}

	public static String shortenStringToMaxLengthCutLeft(String value, int maxLength) {
		if (value != null && value.length() > maxLength) {
			return "... " + value.substring((value.length() - maxLength) + 4);
		} else {
			return value;
		}
	}

	public static void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				// Do nothing
			}
		}
	}

	/**
	 * JSP example:
	 * <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
	 * ...
	 * <fmt:formatDate value="${change_date}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" />
	 */
	public static void setAdminDateTimeFormatPatterns(HttpServletRequest request) {
		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin != null) {
	        request.setAttribute("adminTimeZone", admin.getAdminTimezone());

			SimpleDateFormat dateTimeFormat = admin.getDateTimeFormat();
	        request.setAttribute("adminDateTimeFormat", dateTimeFormat.toPattern());

			SimpleDateFormat dateFormat = admin.getDateFormat();
	        request.setAttribute("adminDateFormat", dateFormat.toPattern());

			SimpleDateFormat timeFormat = admin.getTimeFormat();
	        request.setAttribute("adminTimeFormat", timeFormat.toPattern());

			SimpleDateFormat dateTimeFormatWithSeconds = admin.getDateTimeFormatWithSeconds();
	        request.setAttribute("adminDateTimeFormatWithSeconds", dateTimeFormatWithSeconds.toPattern());
		}
    }

	/**
	 * JSP example:
	 * <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
	 * ...
	 * <fmt:formatDate value="${change_date}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" />
	 */
	public static void setAdminDateTimeFormatPatterns(ComAdmin admin, Model model) {
		if (admin != null) {
	        model.addAttribute("adminTimeZone", admin.getAdminTimezone());

			SimpleDateFormat dateTimeFormat = admin.getDateTimeFormat();
	        model.addAttribute("adminDateTimeFormat", dateTimeFormat.toPattern());

			SimpleDateFormat dateFormat = admin.getDateFormat();
	        model.addAttribute("adminDateFormat", dateFormat.toPattern());

			SimpleDateFormat timeFormat = admin.getTimeFormat();
	        model.addAttribute("adminTimeFormat", timeFormat.toPattern());

			SimpleDateFormat dateTimeFormatWithSeconds = admin.getDateTimeFormatWithSeconds();
	        model.addAttribute("adminDateTimeFormatWithSeconds", dateTimeFormatWithSeconds.toPattern());
		}
    }

	/**
	 * Check for change in customer data
	 * Changes of "null" to "empty String" and vice versa are ignored
	 */
	public static boolean stringValueChanged(String valueOriginal, String valueNew) {
		if (StringUtils.isEmpty(valueOriginal) && StringUtils.isEmpty(valueNew)) {
			return false;
		} else {
			return !StringUtils.equals(valueOriginal, valueNew);
		}
	}

	public static double round(double value, int scale) {
	    return Math.round(value * Math.pow(10, scale)) / Math.pow(10, scale);
	}

	public static float round(float value, int scale) {
	    return (float) (Math.round(value * Math.pow(10, scale)) / Math.pow(10, scale));
	}

	public static <T> T or(T value1, T value2) {
		return value1 == null ? value2 : value1;
	}

	/**
	 * Change all linebreaks ("\r", "\n", "\r\n") into "\r\n",
	 * so that Windows systems can handle them naturally, Linux and MAC systems will use some internal workaround
	 *
	 * @param text
	 * @return
	 */
	public static String normalizeTextLineBreaks(String text) {
		if (text == null) {
			return null;
		} else {
			return text.replace("\r\n", "\n").replace("\r", "\n").replace("\n", "\r\n");
		}
	}

	public static boolean equalsIgnoreLineBreaks(String s1, String s2) {
		if (s1 == null || s2 == null) {
			return s1 == s2;
		}

		return normalizeTextLineBreaks(s1).equals(normalizeTextLineBreaks(s2));
	}

	public static <T extends Enum<T>> T getEnum(Class<T> clz, String name) {
		return getEnum(clz, name, null);
	}

	public static <T extends Enum<T>> T getEnum(Class<T> clz, String name, T defaultValue) {
		try {
			return Enum.valueOf(clz, name);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static Set<String> getAvailableFonts() {
		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Set<String> availableFonts = new HashSet<>();
		for (Font font : e.getAllFonts()) {
			availableFonts.add(font.getFontName());
		}
		return availableFonts;
	}

	public static String stripTrailingZeros(String numberString) {
		if (StringUtils.isBlank(numberString)) {
			return numberString;
		} else {
			DecimalFormat decimalFormat = new DecimalFormat("0.#####");
			return decimalFormat.format(Double.valueOf(numberString));
		}
	}

	public static Set<String> getCaseInsensitiveSet(String... values) {
		return new CaseInsensitiveSet(Arrays.asList(values));
	}

	public static String formatBytes(int bytes, int minUnit, String mode, Locale locale) {
		int scale;

		if (StringUtils.equalsIgnoreCase(mode, "IEC")) {
			// IEC units use 1024 (2^10) as a scale
			scale = 1024;
		} else {
			// SI units use the metric representation based on 10^3 scale
			scale = 1000;
		}

		// Ignore a sign anyway.
		bytes = Math.abs(bytes);

		int unit = Math.max(Math.min(minUnit, FILESIZE_UNITS.length), (int) Math.floor(Math.log10(bytes) / Math.log10(scale)));
		double result = bytes / Math.pow(scale, unit);

		if (unit == 0) {
			// Bytes (an accurate value).
			return Math.round(result) + " " + FILESIZE_UNITS[0];
		} else {
			String value = DecimalFormat.getNumberInstance(locale)
				.format(AgnUtils.round(result, 2));

			return value + " " + FILESIZE_UNITS[unit];
		}
	}

	public static String getMaxLengthString(String value, int maxLength, String cutSign) {
		if (maxLength < 0) {
			maxLength = 0;
		}

		if (value == null || value.length() <= maxLength) {
			return value;
		} else {
			if (cutSign == null) {
				cutSign = "...";
			}

			return value.substring(0, maxLength - cutSign.length()) + cutSign;
		}
	}

	/**
	 * Count how many bytes it would take to represent a given string as a UTF-8.
	 * This implementation is much more effective that using {@link String#getBytes()}.
	 */
	public static int countBytes(String str) {
		if (StringUtils.isEmpty(str)) {
			return 0;
		}

		return countBytes(str, 0, str.length());
	}

	/**
	 * Count how many bytes it would take to represent a substring between {@code begin} (inclusive) and {@code end} (exclusive) as a UTF-8.
	 * This implementation is much more effective that using {@link String#getBytes()}.
	 */
	public static int countBytes(String str, int begin, int end) {
		if (StringUtils.isEmpty(str)) {
			return 0;
		}

		int count = 0;

		while (begin < end) {
			int bytes = countBytes(str.charAt(begin));

			// When UTF-8 representation takes 4 bytes and more the UTF-16 representation needs two chars.
			begin += bytes < 4 ? 1 : 2;
			count += bytes;
		}

		return count;
	}

	private static int countBytes(char c) {
		if ((c & CHAR_BYTES_MASK_1) == 0) {
			return 1;
		}

		if ((c & CHAR_BYTES_MASK_2) == 0) {
			return 2;
		}

		if ((c & CHAR_BYTES_MASK_3) == 0) {
			return 3;
		}

		if ((c & CHAR_BYTES_MASK_4) == 0) {
			return 4;
		}

		if ((c & CHAR_BYTES_MASK_5) == 0) {
			return 5;
		}

		if ((c & CHAR_BYTES_MASK_6) == 0) {
			return 6;
		}

		return -1;
	}

	public static String executeOsCommand(String... commandAndParameters) throws Exception {
		try {
			Process p = Runtime.getRuntime().exec(commandAndParameters);
			String error = IOUtils.toString(new InputStreamReader(p.getErrorStream()));
			if (StringUtils.isNotBlank(error)) {
				throw new Exception(error);
			} else {
				return IOUtils.toString(new InputStreamReader(p.getInputStream()));
			}
		} catch (Exception e) {
			throw new Exception("Cannot execute OS command: " + StringUtils.join(commandAndParameters, ";\n"), e);
		}
	}

	public static String removeObsoleteEnclosingBrackets(String expression) {
		expression = expression.trim();
		boolean removedAnotherBracketLevel;
		do {
			removedAnotherBracketLevel = false;
			if (expression.startsWith("(") && expression.endsWith(")") && isBalancedBrackets(expression.substring(1, expression.length() -1))) {
				expression = expression.substring(1, expression.length() -1);
				removedAnotherBracketLevel = true;
			}
		} while (removedAnotherBracketLevel);
		return expression;
	}

	public static boolean isBalancedBrackets(String expression) {
		int level = 0;
		for (char nextChar : expression.toCharArray()) {
			if (nextChar == '(') {
				level++;
			} else if (nextChar == ')') {
				if (level <= 0) {
					return false;
				} else {
					level--;
				}
			}
		}
		return level == 0;
	}

	public static String insertIndexToFilename(String filename, int index) {
		if (StringUtils.isEmpty(filename)) {
			return Integer.toString(index);
		} else if (filename.contains(".")) {
			return filename.substring(0, filename.lastIndexOf(".")) + "_" + index + filename.substring(filename.lastIndexOf(".") + 1);
		} else {
			return filename + "_" + index;
		}
	}

	public static String boolToString(final boolean checkedValue, final String trueValue, final String falseValue){
		return checkedValue ? trueValue : falseValue;
	}

	public static String boolToString(final boolean checkedValue){
		return boolToString(checkedValue, "checked", "unchecked");
	}

	private enum PhoneNumberToken { BEGIN, PLUS, DIGIT, SEPARATOR, LPAREN, RPAREN }
    
	public static List<String> removeObsoleteItemsFromList(List<String> list) {
		if (list == null) {
			return null;
		} else {
			List<String> returnList = new ArrayList<>();
			for (String item : list) {
				if (item != null) {
					item = item.trim();
					if (StringUtils.isNotBlank(item) && !returnList.contains(item)) {
						returnList.add(item);
					}
				}
			}
			return returnList;
		}
	}

	public static boolean isLatinCharacter(int code) {
		return Character.UnicodeScript.of(code) == Character.UnicodeScript.LATIN;
	}
	
	public static TimeZone getSystemTimeZone() {
		return Calendar.getInstance().getTimeZone();
	}
	
	public static ZoneId getSystemTimeZoneId() {
		return getSystemTimeZone().toZoneId();
	}

	public static String getNormalizedRdirDomain(String rdirDomain) {
        rdirDomain = StringUtils.trim(rdirDomain);

        if (StringUtils.isEmpty(rdirDomain)) {
            return StringUtils.EMPTY;
        }

        if (rdirDomain.endsWith("/")) {
            return rdirDomain;
        } else {
            return rdirDomain + "/";
        }
    }
	
	public static List<String> sortCollectionWithItemsFirst(Collection<String> sourceCollection, String... keepItemsFirst) {
		List<String> list = new ArrayList<>(sourceCollection);
		Collections.sort(list, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (o1 == o2) {
					return 0;
				} else if (o1 == null) {
					return 1;
				} else if (o1.equals(o2)) {
					return 0;
				} else {
					for (String item : keepItemsFirst) {
						if (o1.equalsIgnoreCase(item)) {
							return -1;
						}
					}
					for (String item : keepItemsFirst) {
						if (o2.equalsIgnoreCase(item)) {
							return 1;
						}
					}
					return o1.compareTo(o2);
				}
			}
		});
		return list;
	}
	
    public static String getNormalizedDecimalNumber(String number, Locale locale) {
	    number = number.trim();
	    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
        String groupingSeparator = Character.toString(decimalFormatSymbols.getGroupingSeparator());
        String decimalSeparator = Character.toString(decimalFormatSymbols.getDecimalSeparator());
        if (number.contains(groupingSeparator)
                && !isValidNumberWithGroupingSeparator(number, groupingSeparator, decimalSeparator)) {
            return "";
        }
        return AgnUtils.normalizeNumber(decimalSeparator, groupingSeparator, number);
	}
	
    private static boolean isValidNumberWithGroupingSeparator(String number, String groupingSeparator, String decimalSeparator) {
        return number.matches(String.format("^[+-]?[0-9]{1,3}(%s[0-9]{3})*(\\%s[0-9]+)?$", groupingSeparator, decimalSeparator));
    }
    
    public static boolean isValidNumberWithGroupingSeparator(String number, Locale locale) {
    	DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
        String groupingSeparator = Character.toString(decimalFormatSymbols.getGroupingSeparator());
        String decimalSeparator = Character.toString(decimalFormatSymbols.getDecimalSeparator());
        return isValidNumberWithGroupingSeparator(number, groupingSeparator, decimalSeparator);
    }
    
    public static boolean isZipArchiveFile(File potentialZipFile) throws FileNotFoundException, IOException {
        try (FileInputStream inputStream = new FileInputStream(potentialZipFile)) {
            byte[] magicBytes = new byte[4];
            int readBytes = inputStream.read(magicBytes);
            return readBytes == 4 && magicBytes[0] == 0x50 && magicBytes[1] == 0x4B && magicBytes[2] == 0x03 && magicBytes[3] == 0x04;
        }
    }
}
