/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web.forms;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.util.AgnUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.emm.util.html.xssprevention.AbstractTagError;
import com.agnitas.emm.util.html.xssprevention.ForbiddenTagAttributeError;
import com.agnitas.emm.util.html.xssprevention.ForbiddenTagError;
import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import com.agnitas.emm.util.html.xssprevention.HtmlXSSPreventer;
import com.agnitas.emm.util.html.xssprevention.UnclosedTagError;
import com.agnitas.emm.util.html.xssprevention.UnopenedTagError;
import com.agnitas.emm.util.html.xssprevention.XSSHtmlException;
import com.agnitas.emm.util.html.xssprevention.http.RequestParameterXssPreventerHelper;
import com.agnitas.emm.util.streams.struts.ActionMessageCollector;

/**
 * Implementation of <strong>Action</strong> that validates a user logon.
 */
public class StrutsFormBase extends ActionForm {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(StrutsFormBase.class);
	
	/** Serial version UID: */
	private static final long serialVersionUID = -517998059502119608L;
	
	public static final int DEFAULT_NUMBER_OF_ROWS = 50;
	
    public static final int DEFAULT_REFRESH_MILLIS = 250;
    public static final int REFRESH_MILLIS_STEP = 250;
    public static final int REFRESH_MILLIS_MAXIMUM = 2000;

	public static final String STRUTS_CHECKBOX = "__STRUTS_CHECKBOX_";
	public static final String STRUTS_MULTIPLE = "__STRUTS_MULTIPLE_";
    
    /**
     *  holds the preferred number of rows a user wants to see in a list
     */
    private int numberOfRows = -1; // -1 -> not initialized
    /**
     * flag which show's that the number of rows a user wants to see has been changed
     */
    private boolean numberOfRowsChanged = false;
    
 // keep sort, order , page , columnwidth
	private String sort = "";
	private String order = "";
	private String page = "1";
	private int pageNumber = 1;
    protected List<String> columnwidthsList = new ArrayList<>();

    private int refreshMillis = DEFAULT_REFRESH_MILLIS ;
    private boolean error = false;

    /**
     * Resets parameters.
     */
    @Override
    public void reset(ActionMapping map, HttpServletRequest request) {
		setUnselectedCheckboxProperties(request);
    }

	protected void setUnselectedCheckboxProperties(HttpServletRequest request) {
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();

			try {
				if (name.startsWith(STRUTS_CHECKBOX) && name.length() > STRUTS_CHECKBOX.length()) {
					String propertyName = name.substring(STRUTS_CHECKBOX.length());

					String value = request.getParameter(name);
					if (value != null) {
						BeanUtils.setProperty(this, propertyName, value);
					}
				} else if (name.startsWith(STRUTS_MULTIPLE) && name.length() > STRUTS_MULTIPLE.length()) {
					String propertyName = name.substring(STRUTS_MULTIPLE.length());

					String[] values = request.getParameterValues(name);
					if (values != null && values.length == 1 && StringUtils.isEmpty(values[0])) {
						values = new String[0];
					}
					BeanUtils.setProperty(this, propertyName, values);
				}
			} catch (Exception e) {
				logger.error("reset: " + e.getMessage());
			}
		}
	}

	/**
	 * Reset unchecked html form checkboxes while working in a strutsaction.
	 * Somehow "setUnselectedCheckboxProperties" didn't work because request parameters were empty back there.
	 */
	public void setUnselectedCheckboxPropertiesInAction(HttpServletRequest request) {
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();

			try {
				if (name.startsWith(STRUTS_CHECKBOX) && name.length() > STRUTS_CHECKBOX.length()) {
					String propertyName = name.substring(STRUTS_CHECKBOX.length());

					String value = request.getParameter(name);
					if (request.getParameter(propertyName) == null && value != null) {
						BeanUtils.setProperty(this, propertyName, value);
					}
				}
			} catch (Exception e) {
				logger.error("reset: " + e.getMessage());
			}
		}
	}

	/**
     * Getter for property webApplicationContext.
     *
     * @return Value of property webApplicationContext.
     */
    public ApplicationContext getWebApplicationContext() {
        return WebApplicationContextUtils.getWebApplicationContext(this.getServlet().getServletContext());
    }

	public int getNumberOfRows() {
		return numberOfRows;
	}

	public void setNumberOfRows(int numberOfRows) {
		this.numberOfRows = numberOfRows;
	}

	public boolean isNumberOfRowsChanged() {
		return numberOfRowsChanged;
	}

	public void setNumberOfRowsChanged(boolean numberOfRowsChanged) {
		this.numberOfRowsChanged = numberOfRowsChanged;
	}
	
    public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getPage() {
		return page;
	}

	public void setDir(String dir) {
		setOrder(dir);
	}

	public String getDir() {
		return getOrder();
	}

	public void setPage(String page) {
		pageNumber = NumberUtils.toInt(page, 1);
		this.page = page;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
		this.page = Integer.toString(pageNumber);
	}

	public int getRefreshMillis() {
		return refreshMillis;
	}

	public void setRefreshMillis(int refreshMillis) {
		this.refreshMillis = refreshMillis;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public List<String> getColumnwidthsList() {
		return columnwidthsList;
	}

	public void setColumnwidthsList(List<String> columnwidthsList) {
		this.columnwidthsList = columnwidthsList;
	}

	/**
	 * Original validate() called by Struts.
	 * This method is made "final" to force calling method checkForUnsafeHtmlTags().
	 * If you want to implement your own validate() use formSpecificValidate()!
	 * 
	 * @see #formSpecificValidate(ActionMapping, HttpServletRequest)
	 */
	@Override
	public final ActionErrors validate(final ActionMapping mapping, final HttpServletRequest request) {
		// Prevent deadlock of html check, which causes a stack overflow
		if (!AgnUtils.getEnumerationAsList(request.getParameterNames()).contains("init")) {
			final ActionErrors errors = new ActionErrors();

			
			// First, check if we can find HTML tags in at least one request parameter.
			final ActionMessages htmlCheckErrors = checkForHtmlTags(request);
			if(htmlCheckErrors != null) {
				errors.add(htmlCheckErrors);
			}
			
			// The do user defined (and form specific) validation
			errors.add(formSpecificValidate(mapping, request));

			errors.add(super.validate(mapping, request));

			if (!errors.isEmpty()) {
				loadNonFormDataForErrorView(mapping, request);
			}
			
			return errors;
		} else {
			return new ActionErrors();
		}
	}
	
	/**
	 * Load additional Data which should be stored in request attributes etc.
	 * 
	 * @param mapping
	 * @param request
	 */
	protected void loadNonFormDataForErrorView(ActionMapping mapping, HttpServletRequest request) {
		// in default do nothing
	}

	public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {
		return null;
	}

	protected ActionMessages checkForHtmlTags(final HttpServletRequest request) {
		final Set<HtmlCheckError> htmlErrors = getHtmlCheckErrors(request);

		if(!htmlErrors.isEmpty()) {
			final ActionMessages errors = htmlErrors.stream()
					.map(this::mapHtmlErrorToActionMessage)
					.collect(new ActionMessageCollector(ActionMessages.GLOBAL_MESSAGE));
			
			return errors;
		} else {
			return null;
		}
	}
	
	protected ActionMessage mapHtmlErrorToActionMessage(final HtmlCheckError htmlCheckError) {
		if (htmlCheckError instanceof AbstractTagError) {
			if(htmlCheckError instanceof ForbiddenTagError) {
				return new ActionMessage("error.html.forbiddenTag", ((ForbiddenTagError) htmlCheckError).getTagName());
			} else if(htmlCheckError instanceof UnopenedTagError) {
				return new ActionMessage("error.html.missingStartTag", ((UnopenedTagError) htmlCheckError).getTagName());
			} else if(htmlCheckError instanceof UnclosedTagError) {
				return new ActionMessage("error.html.missingEndTag", ((UnclosedTagError) htmlCheckError).getTagName());
			} else if(htmlCheckError instanceof ForbiddenTagAttributeError) {
				return new ActionMessage("error.html.forbiddenAttribute", ((ForbiddenTagAttributeError) htmlCheckError).getTagName(), ((ForbiddenTagAttributeError) htmlCheckError).getAttributeName());
			} else {
				return new ActionMessage("error.html.genericTagError", ((ForbiddenTagAttributeError) htmlCheckError).getTagName(), ((ForbiddenTagAttributeError) htmlCheckError).getAttributeName());
			}
		} else {
			return new ActionMessage("error.html.genericError");
		}
	}
	
	/**
	 * Checks, if parameter is excluded from checking for unsafe HTML tags. If method returns false,
	 * method checkForHtmlTags() is called.
	 * 
	 * If method is not overwritten, false is returned for every parameter name.
	 * 
	 * @param parameterName parameter name
	 * @param request
	 * @return true, if parameter is excluded from check for unsafe HTML tags
	 * 
	 * @see RequestParameterXssPreventerHelper
	 */
	@Deprecated
	protected boolean isParameterExcludedForUnsafeHtmlTagCheck( String parameterName, HttpServletRequest request) {
		return false;
	}

	/**
	 * 
	 * @see RequestParameterXssPreventerHelper
	 */
	@Deprecated
	protected Set<HtmlCheckError> getHtmlCheckErrors(final HttpServletRequest request) {
		final Set<HtmlCheckError> errors = new HashSet<>();
		
		final Enumeration<String> parameterNames = request.getParameterNames();

		while (parameterNames.hasMoreElements()) {
			final String paramName = parameterNames.nextElement();

			if (!isParameterExcludedForUnsafeHtmlTagCheck(paramName, request)) {
				getHtmlCheckErrors(paramName, request.getParameterValues(paramName), errors);
			} else {			
				getHtmlUnsupportedTagsErrors(paramName, request.getParameterValues(paramName), errors);
			}
		}

		return errors;
	}
	
	/**
	 * 
	 * @see RequestParameterXssPreventerHelper
	 */
	@Deprecated
	protected void getHtmlCheckErrors(final String paramName, final String[] textArray, final Set<HtmlCheckError> errors) {
		for(final String text : textArray) {
			getHtmlCheckErrors(paramName, text, errors);
		}
	}
	
	/**
	 * 
	 * @see RequestParameterXssPreventerHelper
	 */
	@Deprecated
	protected void getHtmlCheckErrors(final String paramName, final String text, final Set<HtmlCheckError> errors) {
		try {
			/** Class to check strings for possible XSS code. */
			HtmlXSSPreventer.checkString(text);
		} catch(final XSSHtmlException e) {
			errors.addAll(e.getErrors());
		}
	}
	
	/**
	 * 
	 * @see RequestParameterXssPreventerHelper
	 */
	@Deprecated
	protected void getHtmlUnsupportedTagsErrors(final String paramName, final String[] textArray, final Set<HtmlCheckError> errors) {
		for(final String text : textArray) {
			getHtmlUnsupportedTagsErrors(paramName, text, errors);
		}
	}
	
	/**
	 * 
	 * @see RequestParameterXssPreventerHelper
	 */
	@Deprecated
	protected void getHtmlUnsupportedTagsErrors(final String paramName, final String text, final Set<HtmlCheckError> errors) {
		try {
			/** Class to check strings for possible XSS code. */
			HtmlXSSPreventer.checkUnsupportedTags(text);
		} catch(final XSSHtmlException e) {
			errors.addAll(e.getErrors());
		}
	}

    protected ConfigService getConfigService() {
    	return (ConfigService) getWebApplicationContext().getBean("ConfigService");
    }
}
