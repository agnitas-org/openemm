<%@ page language="java" import="org.agnitas.util.*, org.agnitas.web.*, org.agnitas.web.forms.*" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<% int index=((Integer)request.getAttribute("opIndex")).intValue(); %>
<%
	EmmActionForm form = (EmmActionForm)session.getAttribute("emmActionForm");
	com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters operation = (com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters)form.getActions().get(index);	
	String currentColumnName = operation.getColumnName();
	String colName="";
	String selected="";
	boolean useTr = operation.isUseTrack();
	int companyId = operation.getCompanyId();
	if (companyId == 0) {
		companyId = AgnUtils.getCompanyID(request);
	}
%>

<% if (index == 0) { %>
<script type="text/javascript">
	function toggleTrackingPointElements(idx) {
		if (document.getElementById("useTrack_id_"+idx).checked) {
	        document.getElementById("s2id_tpSelect_id_"+idx).style.display = 'inline-block';
	        document.getElementById("val_id_"+idx).style.display = 'none';
		} else {
	        document.getElementById("s2id_tpSelect_id_"+idx).style.display = 'none';
	        document.getElementById("val_id_"+idx).style.display = 'inline';
		}
	}
</script>
<% } %>

<div class="inline-tile-content">
    <%@ include file="UpdateCustomer-trackpoint.jspf" %>
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><bean:message key="Column_Name"/></label>
        </div>
        <div class="col-sm-8">
            <table class="table table-bordered table-form">
                <tr>
                    <td>
                        <html:select property='<%= "actions[" + index + "].columnName" %>' size="1" styleClass="form-control js-select">
                            <emm:ShowColumnInfo id="agnTbl" table="<%= AgnUtils.getCompanyID(request) %>"
                                                hide="change_date, timestamp, creation_date, datasource_id, bounceload, email, customer_id, gender, mailtype, firstname, lastname, title, cleaned_date, facebook_status, foursquare_status, google_status, twitter_status, xing_status">
                                <%
                                    String columnName = (String) pageContext.getAttribute("_agnTbl_column_name");
                                    String columnShortName = (String) pageContext.getAttribute("_agnTbl_shortname");
                                    String columnType = (String) pageContext.getAttribute("_agnTbl_data_type");
                                    String currentColumnAndType = operation.getColumnName();
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
                    </td>
                    <td>
                        <html:select styleId='<%="oper_id_"+index%>' styleClass="form-control js-select" property='<%= "actions[" + index + "].updateType" %>' size="1">
                            <html:option value="1">+</html:option>
                            <html:option value="2">-</html:option>
                            <html:option value="3">=</html:option>
                        </html:select>
                    </td>
                    <td>
                        <html:text styleId='<%="val_id_"+index%>' styleClass="form-control" property='<%= "actions[" + index + "].updateValue" %>' style='<%= useTr ? "display:none" : "" %>'/>

                        <%@ include file="UpdateCustomer-trackpoint-table.jspf" %>
                    </td>
                </tr>
            </table>
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
