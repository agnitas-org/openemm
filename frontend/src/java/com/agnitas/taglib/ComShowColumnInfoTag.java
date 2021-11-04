/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTag;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.agnitas.util.AgnUtils;
import org.apache.log4j.Logger;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.ProfileField;
import com.agnitas.service.ComColumnInfoService;
 
/**
 * Prepares a list of userdefined fields for use in web-templates.
 */
public class ComShowColumnInfoTag extends TagSupport implements BodyTag {
	private static final long serialVersionUID = -1235292192519826728L;
	
	private static final transient Logger logger = Logger.getLogger(ComShowColumnInfoTag.class);
	
	private BodyContent bodyContent = null;

    @Override
    public void	doInitBody() {
    	// do nothing
    }
    
    @Override
    public void	setBodyContent(BodyContent bodyContent) {
    	this.bodyContent = bodyContent;
    }
	
	private static ComColumnInfoService COLUMNINFOSERVICE;

	protected String iteratorID = null;
	protected int table = 0;
	protected Set<String> sys_columns = null;
    private boolean useCustomSorting = false;

	/**
	 * Set the id to use for session variables.
	 *
	 * @param id
	 *            the id to use for global variables.
	 */
	@Override
	public void setId(String id) {
		this.iteratorID = id;
	}
	
	/**
	 * Set the id of the table to show info for.
	 *
	 * @param table
	 *            companyID to show the table for.
	 */
	public void setTable(int table) {
		this.table = table;
	}

	/**
	 * Set the hidden columns for this query.
	 *
	 * @param hide
	 *            a comma sepereated list of columnnames which should not be
	 *            shown.
	 */
	public void setHide(String hide) {
		sys_columns = new HashSet<>();
		for (String columnName : hide.split(",")) {
			sys_columns.add(columnName.trim().toLowerCase());
		}
	}

	public void setUseCustomSorting(boolean useCustomSorting) {
        this.useCustomSorting = useCustomSorting;
    }
    
	@Override
	public void release() {
		iteratorID = null;
		table = 0;
		sys_columns = null;
		useCustomSorting = false;
		
		super.release();
	}

	private ComColumnInfoService getColumnInfoService() {
		if (COLUMNINFOSERVICE == null) {
			COLUMNINFOSERVICE = (ComColumnInfoService) WebApplicationContextUtils.getWebApplicationContext(this.pageContext.getServletContext()).getBean("ColumnInfoService");
		}
		return COLUMNINFOSERVICE;
	}

	/**
	 * Shows column information.
	 */
	@Override
	public int doStartTag() throws JspTagException {
		try {
			if (iteratorID == null) {
				iteratorID = "";
			}
	
			if (table == 0) {
				table = AgnUtils.getAdmin(pageContext).getCompany().getId();
			}
	
			List<ProfileField> comProfileFieldMap = getColumnInfoService().getComColumnInfos(table, AgnUtils.getAdmin(pageContext).getAdminID(), useCustomSorting);
	
			if (comProfileFieldMap.size() <= 0) {
				return SKIP_BODY;
			} else {
				pageContext.setAttribute("__" + iteratorID + "_data", comProfileFieldMap.iterator());
				pageContext.setAttribute("__" + iteratorID + "_map", comProfileFieldMap);
				pageContext.setAttribute("__" + iteratorID + "_colmap", comProfileFieldMap);
				return doAfterBody();
			}
		} catch (Exception e) {
			logger.error("Error in ComShowColumnInfoTag.doStartTag", e);
			throw new JspTagException("Error: " + e);
		}
	}

	@Override
	public int doAfterBody() throws JspException {
		@SuppressWarnings("unchecked")
		Iterator<ProfileField> profileFieldIterator = (Iterator<ProfileField>) pageContext.getAttribute("__" + iteratorID + "_data");
		try {
			while (profileFieldIterator.hasNext()) {
				ProfileField fieldMap = profileFieldIterator.next();
				if (sys_columns == null || !sys_columns.contains(fieldMap.getColumn().toLowerCase())) {
	 				if (fieldMap.getDataType() != null) {
						pageContext.setAttribute("_" + iteratorID + "_data_type", fieldMap.getDataType());
						pageContext.setAttribute("_" + iteratorID + "_column_name",	fieldMap.getColumn().toUpperCase());
						if (fieldMap.getDataType().equalsIgnoreCase("VARCHAR")) {
							pageContext.setAttribute("_" + iteratorID + "_data_length",	Integer.toString(fieldMap.getDataTypeLength()));
						}
						pageContext.setAttribute("_" + iteratorID + "_shortname", fieldMap.getShortname());
						if (fieldMap.getDefaultValue() != null) {
							pageContext.setAttribute("_" + iteratorID + "_data_default", fieldMap.getDefaultValue());
						} else {
							pageContext.setAttribute("_" + iteratorID + "_data_default", "");
						}
						pageContext.setAttribute("_" + iteratorID + "_editable", fieldMap.getModeEdit());
						pageContext.setAttribute("_" + iteratorID + "_insertable", fieldMap.getModeInsert());
						pageContext.setAttribute("_" + iteratorID + "_line", fieldMap.getLine());
						pageContext.setAttribute("_" + iteratorID + "_nullable", fieldMap.getNullable() ? 1 : 0);
						pageContext.setAttribute("_" + iteratorID + "_sort", fieldMap.getSort());
						pageContext.setAttribute("_" + iteratorID + "_data_isinterest", fieldMap.getInterest());
						pageContext.setAttribute("_" + iteratorID + "_allowed_values", fieldMap.getAllowedValues());
					}
					return EVAL_BODY_BUFFERED;
				}
			}
		} catch (Exception e) {
			logger.error("Error in ComShowColumnInfoTag.doAfterBody", e);
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws JspException {
		iteratorID = null;
		table = 0;
		sys_columns = null;
		useCustomSorting = false;
		
		try {
			if (bodyContent != null) {
				pageContext.getOut().print(bodyContent.getString());
			}
		} catch (IOException e) {
			throw new JspException(e.getMessage());
		}

		return super.doEndTag();
	}
}
