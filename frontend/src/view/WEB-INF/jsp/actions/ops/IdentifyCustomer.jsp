<%@ page language="java" import="org.agnitas.util.*, com.agnitas.emm.core.action.operations.*, org.agnitas.web.EmmActionAction" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<% int index=((Integer)request.getAttribute("opIndex")).intValue(); %>

<div class="inline-tile-content">
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><bean:message key="action.identifycust.usercol"/></label>
        </div>
        <div class="col-sm-8">
            <html:select styleClass="form-control js-select" property='<%= "actions[" + index + "].keyColumn" %>' size="1">
                <emm:ShowColumnInfo id="agnTbl" table="<%= AgnUtils.getCompanyID(request) %>" hide="change_date, timestamp, creation_date, datasource_id, bounceload, sys_tracking_veto, cleaned_date, facebook_status, foursquare_status, google_status, twitter_status, xing_status">
                    <%
                    	String columnName = (String) pageContext.getAttribute("_agnTbl_column_name");
                                            String columnShortName = (String) pageContext.getAttribute("_agnTbl_shortname");
                                            String currentColumnAndType = ((ActionOperationIdentifyCustomerParameters) request.getAttribute("op")).getKeyColumn();
                                            String columnNameToSelect = "";
                                            if (currentColumnAndType != null) {
                                                columnNameToSelect = (String) currentColumnAndType;
                                            }
                                            if (columnNameToSelect.contains("#")) {
                                                columnNameToSelect = columnNameToSelect.substring(0, columnNameToSelect.indexOf("#"));
                                            }
                                            String selectedSign = "";
                                            if (columnName != null && columnName.equalsIgnoreCase(columnNameToSelect)) {
                                                selectedSign = " selected=\"selected\"";
                                            }
                    %>

                    <option value="<%=columnName%>"<%=selectedSign%>><%=columnShortName%></option>
                </emm:ShowColumnInfo>
            </html:select>
        </div>
    </div>
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><bean:message key="action.identifycust.passcolumn"/></label>
        </div>
        <div class="col-sm-8">
            <html:select styleClass="form-control js-select" property='<%="actions[" + index + "].passColumn"%>' size="1">
                <html:option value="none"><bean:message key="action.identifycust.nopass"/></html:option>
                <emm:ShowColumnInfo id="agnTbl" table="<%=AgnUtils.getCompanyID(request)%>" hide="change_date, timestamp, creation_date, datasource_id, bounceload, sys_tracking_veto, cleaned_date, facebook_status, foursquare_status, google_status, twitter_status, xing_status">
                    <%
                    	String columnName = (String) pageContext.getAttribute("_agnTbl_column_name");
                                            String columnShortName = (String) pageContext.getAttribute("_agnTbl_shortname");
                                            String currentColumnAndType = ((ActionOperationIdentifyCustomerParameters) request.getAttribute("op")).getPassColumn();
                                            String columnNameToSelect = "";
                                            if (currentColumnAndType != null) {
                                                columnNameToSelect = (String) currentColumnAndType;
                                            }
                                            if (columnNameToSelect.contains("#")) {
                                                columnNameToSelect = columnNameToSelect.substring(0, columnNameToSelect.indexOf("#"));
                                            }
                                            String selectedSign = "";
                                            if (columnName != null && columnName.equalsIgnoreCase(columnNameToSelect)) {
                                                selectedSign = " selected=\"selected\"";
                                            }
                    %>
                    <option value="<%= columnName %>"<%= selectedSign %>><%= columnShortName %></option>
                </emm:ShowColumnInfo>
            </html:select>
        </div>
    </div>
</div>
<div class="inline-tile-footer">
<emm:ShowByPermission token="actions.change">
    <a class="btn btn-regular" href="#" data-form-set="action: <%= EmmActionAction.ACTION_REMOVE_MODULE %>, deleteModule: <%= index %>" data-form-submit>
        <i class="icon icon-trash-o"></i>
        <span class="text"><bean:message key="button.Delete"/></span>
    </a>
</emm:ShowByPermission>
</div>
