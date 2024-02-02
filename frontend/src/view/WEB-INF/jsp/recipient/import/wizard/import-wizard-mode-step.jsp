<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="org.agnitas.util.importvalues.ImportMode"%>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page import="org.agnitas.beans.ImportStatus" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="_agnTbl_column_name" type="java.lang.String"--%>
<%--@elvariable id="_agnTbl_shortname" type="java.lang.String"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<c:set var="DOUBLECHECK_FULL" value="<%= ImportStatus.DOUBLECHECK_FULL %>" />
<c:set var="DOUBLECHECK_NONE" value="<%= ImportStatus.DOUBLECHECK_NONE %>" />
<c:set var="TO_BLACKLIST_IMPORT_MODE" value="" />
<c:set var="BLACKLIST_EXCLUSIVE_IMPORT_MODE" value="" />
<c:set var="MARK_SUSPENDED_IMPORT_MODE" value="" />
<c:set var="REACTIVATE_SUSPENDED_IMPORT_MODE" value="" />
<c:set var="MARK_OPT_OUT_IMPORT_MODE" value="" />

<c:set var="customerID_allowed" value="false" scope ="page"/>
<emm:ShowByPermission token="import.customerid">
    <c:set var="customerID_allowed" value="true" scope ="page"/>
</emm:ShowByPermission>

<mvc:form servletRelativeAction="/recipient/import/wizard/step/mode.action" modelAttribute="importWizardSteps" data-form="resource">
    <c:set var="tileContent">
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <mvc:message code="Mode"/>
                        <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_2/Mode.xml"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="helper.mode" size="1" cssClass="form-control js-select">
                        <emm:ShowByPermission token="import.mode.add">
                            <mvc:option value="<%= ImportMode.ADD.getIntValue() %>"><mvc:message code="import.mode.add"/></mvc:option>
                        </emm:ShowByPermission>
                        <emm:ShowByPermission token="import.mode.add_update">
                            <mvc:option value="<%= ImportMode.ADD_AND_UPDATE.getIntValue() %>"><mvc:message code="import.mode.add_update"/></mvc:option>
                        </emm:ShowByPermission>
                        <emm:ShowByPermission token="import.mode.only_update">
                            <mvc:option value="<%= ImportMode.UPDATE.getIntValue() %>"><mvc:message code="import.mode.only_update"/></mvc:option>
                        </emm:ShowByPermission>
                        <emm:ShowByPermission token="import.mode.bounce">
                            <mvc:option value="<%= ImportMode.MARK_BOUNCED.getIntValue() %>"><mvc:message code="import.mode.bounce"/></mvc:option>
                        </emm:ShowByPermission>
                        <emm:ShowByPermission token="import.mode.blacklist">
                            <mvc:option value="<%= ImportMode.TO_BLACKLIST.getIntValue() %>"><mvc:message code="import.mode.blacklist"/></mvc:option>
                        </emm:ShowByPermission>
                        <emm:ShowByPermission token="import.mode.blacklist_exclusive">
                            <mvc:option value="<%= ImportMode.BLACKLIST_EXCLUSIVE.getIntValue() %>"><mvc:message code="import.mode.blacklist_exclusive"/></mvc:option>
                        </emm:ShowByPermission>
                        <emm:ShowByPermission token="import.mode.remove_status">
                            <mvc:option value="<%= ImportMode.MARK_SUSPENDED.getIntValue() %>"><mvc:message code="import.mode.remove_status"/></mvc:option>
                        </emm:ShowByPermission>
                        <emm:ShowByPermission token="import.mode.reactivateSuspended">
                            <mvc:option value="<%= ImportMode.REACTIVATE_SUSPENDED.getIntValue() %>"><mvc:message code="import.mode.reactivateSuspended"/></mvc:option>
                        </emm:ShowByPermission>
                        <emm:ShowByPermission token="import.mode.unsubscribe">
                            <mvc:option value="<%= ImportMode.MARK_OPT_OUT.getIntValue() %>"><mvc:message code="import.mode.unsubscribe"/></mvc:option>
                        </emm:ShowByPermission>
                    </mvc:select>
                </div>
            </div>
    
            <%@include file="fragments/import-wizard-mode-ignore-nulls-select.jspf" %>
    
             <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <mvc:message code="import.keycolumn"/>
                        <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_2/KeyColumn.xml"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="helper.status.keycolumn" size="1" cssClass="form-control js-select">
                        <emm:ShowColumnInfo id="agnTbl" table="<%=AgnUtils.getCompanyID(request)%>">
                            <c:if test="${customerID_allowed || fn:toLowerCase(_agnTbl_column_name) != 'customer_id'}">
                                <mvc:option value='${_agnTbl_column_name}'>${_agnTbl_shortname}</mvc:option>
                            </c:if>
                        </emm:ShowColumnInfo>
                    </mvc:select>
                </div>
            </div>
    
            <emm:ShowByPermission token="import.mode.doublechecking">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label">
                            <mvc:message code="import.doublechecking"/>
                            <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_2/Doublechecking.xml"></button>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:select path="helper.status.doubleCheck" size="1" cssClass="form-control js-select">
                            <mvc:option value="${DOUBLECHECK_FULL}"><mvc:message code="default.Yes"/></mvc:option>
                            <mvc:option value="${DOUBLECHECK_NONE}"><mvc:message code="default.No"/></mvc:option>
                        </mvc:select>
                    </div>
                </div>
            </emm:ShowByPermission>
    
            <emm:ShowByPermission token="import.mode.duplicates">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label checkbox-control-label">
                            <mvc:message code="import.profile.updateAllDuplicates"/>
                            <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_2/Doublechecking.xml"></button>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <label data-form-change class="toggle">
                            <mvc:checkbox path="modeStep.updateAllDuplicates" id="import_duplicates" />
                            <div class="toggle-control"></div>
                        </label>
                    </div>
                </div>
            </emm:ShowByPermission>
        </div>
    </c:set>
    <c:set var="step" value="2"/>
    <c:url var="backUrl" value='/recipient/import/wizard/step/file.action'/>
    
    <%@ include file="fragments/import-wizard-step-template.jspf" %>
    
</mvc:form>

<% out.flush(); %>
