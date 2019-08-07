<%@ page language="java" import="org.agnitas.util.*, org.agnitas.web.EmmActionAction" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id=org.agnitas.emm.core.autoimport.web._TestAutoImportAction type="com.agnitas.emm.core.action.operations.ActionOperationSubscribeCustomer"--%>
<%--@elvariable id="opIndex" type="java.lang.Integer"--%>
<c:set var="index" value="${opIndex}"/>

<div class="inline-tile-content">
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><bean:message key="import.doublechecking"/></label>
        </div>
        <div class="col-sm-8">
            <label class="toggle">
                <c:set var="doubleCheck" value="actions[${index}].doubleCheck"/>
                <html:hidden property="__STRUTS_CHECKBOX_${doubleCheck}" value="false"/>
                <html:checkbox styleId="doubleCheck" property="${doubleCheck}"/>
                <div class="toggle-control"></div>
            </label>
        </div>
    </div>

    <div class="form-group hidden" data-show-by-checkbox="#doubleCheck">
        <div class="col-sm-4">
            <label class="control-label"><bean:message key="import.keycolumn"/></label>
        </div>
        <div class="col-sm-8">
            <html:select styleClass="form-control js-select" property='actions[${index}].keyColumn' size="1">
                <emm:ShowColumnInfo id="agnTbl" table="<%= AgnUtils.getCompanyID(request) %>" hide="change_date, timestamp, creation_date, datasource_id, bounceload, sys_tracking_veto, cleaned_date, facebook_status, foursquare_status, google_status, twitter_status, xing_status">
                    <%--@elvariable id="_agnTbl_column_name" type="java.lang.String"--%>
                    <%--@elvariable id="_agnTbl_shortname" type="java.lang.String"--%>
                    <c:set var="columnNameToSelect" value="${empty op.keyColumn ? '' : op.keyColumn}"/>
                    <c:if test='${columnNameToSelect.contains("#")}'>
                        <c:set var="columnNameToSelect" value='${columnNameToSelect.substring(0, columnNameToSelect.indexOf("#"))}'/>
                    </c:if>
                    <c:set var="selectedMark" value='selected="selected"'/>
                    <c:set var="selectedSign" value='${(not empty _agnTbl_column_name) and _agnTbl_column_name.equalsIgnoreCase(columnNameToSelect) ? selectedMark : ""}'/>

                    <option value="${_agnTbl_column_name}" ${selectedSign}>${_agnTbl_shortname}</option>
                </emm:ShowColumnInfo>
            </html:select>
        </div>
    </div>

    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><bean:message key="action.UseDblOptIn"/></label>
        </div>
        <div class="col-sm-8">
            <label class="toggle">
                <c:set var="doubleOptIn" value="actions[${index}].doubleOptIn"/>
                <html:hidden property="__STRUTS_CHECKBOX_${doubleOptIn}" value="false"/>
                <html:checkbox property="${doubleOptIn}"/>
                <div class="toggle-control"></div>
            </label>
        </div>
    </div>
</div>
<div class="inline-tile-footer">
<emm:ShowByPermission token="actions.change">
    <a class="btn btn-regular" href="#" data-form-set="action: <%= EmmActionAction.ACTION_REMOVE_MODULE %>, deleteModule: ${index}" data-form-submit>
        <i class="icon icon-trash-o"></i>
        <span class="text"><bean:message key="button.Delete"/></span>
    </a>
</emm:ShowByPermission>
</div>
