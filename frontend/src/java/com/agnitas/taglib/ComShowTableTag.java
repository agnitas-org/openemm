/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

import org.agnitas.util.AgnUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.dao.ShowTableTagDao;

public class ComShowTableTag extends BodyTagSupport {
	private static final transient Logger logger = LogManager.getLogger(ComShowTableTag.class);

	private static final long serialVersionUID = 8384664178598213588L;
	
	private ShowTableTagDao showTableTagDao = null;

	protected String sqlStatement;
	protected int startOffset = 0;
	protected int maxRows = 10000;
	protected boolean grabAll = false;
	protected boolean encodeHtml = true;

	/**
	 * Setter for property startOffset.
	 * 
	 * @param startOffset
	 *            New value of property startOffset.
	 */
	public void setStartOffset(String startOffset) {
		try {
			this.startOffset = Integer.parseInt(startOffset);
		} catch (Exception e) {
			this.startOffset = 0;
		}
	}

	/**
	 * Setter for property sqlStatement.
	 * 
	 * @param sqlStatement
	 *            New value of property sqlStatement.
	 */
	public void setSqlStatement(String sqlStatement) {
		this.sqlStatement = sqlStatement;
	}

	/**
	 * Setter for property maxRows.
	 * 
	 * @param maxRows
	 *            New value of property maxRows.
	 */
	public void setMaxRows(String maxRows) {
		try {
			this.maxRows = Integer.parseInt(maxRows);
		} catch (Exception e) {
			this.maxRows = 0;
		}
	}

	/**
	 * Setter for property encodeHtml.
	 * 
	 * @param encodeHtml
	 *            New value of property encodeHtml.
	 */
	public void setEncodeHtml(String encodeHtml) {
		try {
			this.encodeHtml = AgnUtils.interpretAsBoolean(encodeHtml);
		} catch (Exception e) {
			this.encodeHtml = true;
		}
	}

	/**
	 * Sets attribute for the pagecontext.
	 */
	@Override
	public int doAfterBody() throws JspException {
		BodyContent currentBodyContent = getBodyContent();
		if (currentBodyContent != null) {
			try {
				currentBodyContent.getEnclosingWriter().write(currentBodyContent.getString());
			} catch (IOException e) {
				logger.error("Error writing body content", e);
			}
			currentBodyContent.clearBody();
		}

		Map<String, Object> result = getNextRecord();

		if (result != null) {
			setResultInPageContext(result);

			return EVAL_BODY_BUFFERED;
		} else {
			return SKIP_BODY;
		}
	}

	private Map<String, Object> getNextRecord() {
		@SuppressWarnings("unchecked")
		ListIterator<Map<String, Object>> it = (ListIterator<Map<String, Object>>) pageContext.getAttribute("__" + id + "_data");
		if (it.hasNext() && (grabAll || (maxRows--) != 0)) {
			return it.next();
		} else {
			return null;
		}
	}

	@Override
	public void doInitBody() throws JspException {
		setResultInPageContext(getNextRecord());
	}

	private void setResultInPageContext(Map<String, Object> map) {
		for (Entry<String, Object> entry : map.entrySet()) {
			String colDataStr;
			if (entry.getValue() != null) {
				colDataStr = entry.getValue().toString();
			} else {
				colDataStr = "";
			}
			if (encodeHtml && String.class.isInstance(entry.getValue())) {
				pageContext.setAttribute("_" + id + "_" + entry.getKey().toLowerCase(), StringEscapeUtils.escapeHtml4(colDataStr));
			} else {
				pageContext.setAttribute("_" + id + "_" + entry.getKey().toLowerCase(), colDataStr);
			}
		}
	}

	@Override
	public int doEndTag() {
		BodyContent currentBodyContent = getBodyContent();

		if (currentBodyContent != null) {
			currentBodyContent.clearBody();
		}

		return EVAL_PAGE;
	}

	/**
	 * Sets attribute for the pagecontext.
	 */
	@Override
	public int doStartTag() throws JspTagException {
		if (id == null) {
			id = "";
		}

		pageContext.setAttribute("__" + id + "_MaxRows", maxRows);

		try {
			grabAll = (maxRows == 0);
			
			List<Map<String, Object>> resultMap = getShowTableTagDao().select(sqlStatement, maxRows, startOffset);

			if (resultMap != null && resultMap.size() > 0) {
				ListIterator<Map<String, Object>> aIt = resultMap.listIterator(startOffset);

				pageContext.setAttribute("__" + id + "_data", aIt);
				pageContext.setAttribute("__" + id + "_ShowTableRownum", resultMap.size());
				return EVAL_BODY_BUFFERED;
			}
		} catch (Exception e) {
			logger.error("doStartTag: " + e);
			logger.error("SQL: " + sqlStatement);
			throw new JspTagException("Error: " + e);
		}
		return SKIP_BODY;
	}
	
	private ShowTableTagDao getShowTableTagDao() {
		if (showTableTagDao == null) { 
			ApplicationContext aContext = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
			showTableTagDao = (ShowTableTagDao) aContext.getBean("ShowTableTagDao");
		}
		return showTableTagDao;
	}
}
