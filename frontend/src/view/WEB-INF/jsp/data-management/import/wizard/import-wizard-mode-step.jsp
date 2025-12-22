<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.beans.ImportStatus"%>
<%@ page import="com.agnitas.util.importvalues.ImportMode" %>
<%@ page import="com.agnitas.util.AgnUtils" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="_agnTbl_column_name" type="java.lang.String"--%>
<%--@elvariable id="_agnTbl_shortname" type="java.lang.String"--%>

<c:set var="DOUBLECHECK_FULL" value="<%= ImportStatus.DOUBLECHECK_FULL %>" />
<c:set var="DOUBLECHECK_NONE" value="<%= ImportStatus.DOUBLECHECK_NONE %>" />

<c:set var="customerIdImportAllowed" value="${emm:permissionAllowed('import.customerid', pageContext.request)}" scope="page" />

<c:set var="step" value="2"/>
<c:url var="backUrl" value='/recipient/import/wizard/step/file.action'/>

<mvc:form servletRelativeAction="/recipient/import/wizard/step/mode.action" modelAttribute="importWizardSteps" data-form="resource" cssClass="tiles-container">
    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="import.Wizard" /></h1>
            <div class="tile-controls">
                <%@ include file="fragments/import-wizard-steps-navigation.jspf" %>
            </div>
        </div>
        <div class="tile-body vstack gap-3 js-scrollable">
            <div>
                <label class="form-label" for="import-mode">
                    <mvc:message code="Mode"/>
                    <a href="#" type="button" class="icon icon-question-circle" data-help="importwizard/step_2/Mode.xml"></a>
                </label>

                <mvc:select id="import-mode" path="helper.mode" size="1" cssClass="form-control">
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

            <%@include file="fragments/import-wizard-mode-ignore-nulls-select.jspf" %>

            <div>
                <label class="form-label" for="import-keycolumn">
                    <mvc:message code="import.keycolumn"/>
                    <a href="#" type="button" class="icon icon-question-circle" data-help="importwizard/step_2/KeyColumn.xml"></a>
                </label>

                <mvc:select id="import-keycolumn" path="helper.status.keycolumn" size="1" cssClass="form-control">
                    <emm:ShowColumnInfo id="agnTbl" table="<%=AgnUtils.getCompanyID(request)%>">
                        <c:if test="${customerIdImportAllowed || fn:toLowerCase(_agnTbl_column_name) != 'customer_id'}">
                            <mvc:option value='${_agnTbl_column_name}'>${_agnTbl_shortname}</mvc:option>
                        </c:if>
                    </emm:ShowColumnInfo>
                </mvc:select>
            </div>

            <emm:ShowByPermission token="import.mode.doublechecking">
                <div>
                    <label class="form-label" for="import-doublecheck">
                        <mvc:message code="import.doublechecking"/>
                        <a href="#" type="button" class="icon icon-question-circle" data-help="importwizard/step_2/Doublechecking.xml"></a>
                    </label>
                    <mvc:select id="import-doublecheck" path="helper.status.doubleCheck" size="1" cssClass="form-control">
                        <mvc:option value="${DOUBLECHECK_FULL}"><mvc:message code="default.Yes"/></mvc:option>
                        <mvc:option value="${DOUBLECHECK_NONE}"><mvc:message code="default.No"/></mvc:option>
                    </mvc:select>
                </div>
            </emm:ShowByPermission>

            <emm:ShowByPermission token="import.mode.duplicates">
                <div>
                    <div class="form-check form-switch">
                        <mvc:checkbox id="import_duplicates" path="modeStep.updateAllDuplicates" cssClass="form-check-input" role="switch" />
                        <label class="form-label form-check-label" for="import_duplicates">
                            <mvc:message code="import.profile.updateAllDuplicates"/>
                            <a href="#" type="button" class="icon icon-question-circle" data-help="importwizard/step_2/Doublechecking.xml"></a>
                        </label>
                    </div>
                </div>
            </emm:ShowByPermission>
        </div>
    </div>
</mvc:form>

<% out.flush(); %>
