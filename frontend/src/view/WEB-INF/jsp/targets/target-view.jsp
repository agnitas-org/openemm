<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action"%>
<%@page import="org.agnitas.target.ConditionalOperator" %>
<%@page import="org.agnitas.util.AgnUtils"%>
<%@ page import="com.agnitas.emm.core.target.beans.TargetComplexityGrade" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="targetEditForm" type="com.agnitas.emm.core.target.form.TargetEditForm"--%>
<%--@elvariable id="mailTrackingAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="isLocked" type="java.lang.Boolean"--%>
<%--@elvariable id="hidden" type="java.lang.Boolean"--%>
<%--@elvariable id="isValid" type="java.lang.Boolean"--%>
<%--@elvariable id="complexityGrade" type="com.agnitas.emm.core.target.beans.TargetComplexityGrade"--%>
<%--@elvariable id="mailinglists" type="java.util.List<org.agnitas.beans.Mailinglist>"--%>

<c:set var="OPERATOR_IS" value="<%= ConditionalOperator.IS.getOperatorCode() %>" scope="page" />
<c:set var="OPERATOR_MOD" value="<%= ConditionalOperator.MOD.getOperatorCode() %>" scope="page" />

<c:set var="COMPLEXITY_RED" value="<%= TargetComplexityGrade.RED %>" scope="page"/>
<c:set var="COMPLEXITY_YELLOW" value="<%= TargetComplexityGrade.YELLOW %>" scope="page"/>
<c:set var="COMPLEXITY_GREEN" value="<%= TargetComplexityGrade.GREEN %>" scope="page"/>

<c:set var="DISABLED" value="${isLocked or hidden}"/>

<c:set var="ACE_EDITOR_PATH" value="${emm:aceEditorPath(pageContext.request)}" scope="page"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/${ACE_EDITOR_PATH}/emm/ace.min.js"></script>

<emm:ShowColumnInfo id="colsel" table="<%= AgnUtils.getCompanyID(request) %>" />

<%-- Determine active editor tab --%>
<c:choose>
    <c:when test="${targetEditForm.viewFormat == 'QUERY_BUILDER'}">
        <c:set var="QB_EDITOR_TAB_ACTIVE_CLASS" value="active" scope="page" />
        <c:set var="EQL_EDITOR_TAB_ACTIVE_CLASS" value="" scope="page" />
        <c:set var="QB_EDITOR_DIV_SHOW_STATE" value="data-tab-show='true'" scope="page" />
        <c:set var="EQL_EDITOR_DIV_SHOW_STATE" value="data-tab-hide='true'" scope="page" />
    </c:when>
    <c:when test="${targetEditForm.viewFormat == 'EQL'}">
        <c:set var="QB_EDITOR_TAB_ACTIVE_CLASS" value="" scope="page" />
        <c:set var="EQL_EDITOR_TAB_ACTIVE_CLASS" value="active" scope="page" />
        <c:set var="QB_EDITOR_DIV_SHOW_STATE" value="data-tab-hide='true'" scope="page" />
        <c:set var="EQL_EDITOR_DIV_SHOW_STATE" value="data-tab-show='true'" scope="page" />
    </c:when>
    <c:otherwise>
        <%-- This should never happen... --%>
        <c:set var="QB_EDITOR_TAB_ACTIVE_CLASS" value="" scope="page" />
        <c:set var="EQL_EDITOR_TAB_ACTIVE_CLASS" value="" scope="page" />
    </c:otherwise>
</c:choose>

<c:url var="saveUrl" value="/target/${targetEditForm.targetId}/save.action"/>

<mvc:form servletRelativeAction="/target/${targetEditForm.targetId}/view.action" id="targetViewForm" data-form="resource"
             modelAttribute="targetEditForm"
             data-controller="target-group-view"
             data-initializer="target-group-view"
             data-validator-options="skip_empty: false"
             data-action="save-target"
             data-submit-type="${not empty workflowForwardParams ? 'static' : ''}">
    <mvc:hidden path="targetId" />
    <mvc:hidden path="viewFormat" />
    <mvc:hidden path="previousViewFormat" value="${targetEditForm.viewFormat}"/>

    <c:if test="${not empty workflowForwardParams}">
        <input type="hidden" name="workflowForwardParams" value="${workflowForwardParams}"/>
        <input type="hidden" name="workflowId" value="${workflowId}" />
    </c:if>


    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="target.Edit" />
            </h2>
            <mvc:checkbox path="favorite" id="favorite" cssClass="pull-right star-checkbox icon-fa5 far" cssStyle="margin-right: 20px; line-height: 22px; margin-top: 14px; font-size: 18px;" disabled="${DISABLED}"/>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="shortname">
                        <mvc:message var="nameMsg" code="Name"/>
                        ${nameMsg}*
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="shortname" id="shortname" cssClass="form-control" maxlength="99"
                              readonly="${DISABLED}" placeholder="${nameMsg}" />
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="description">
                        <mvc:message var="descriptionMsg" code="default.description" />
                        ${descriptionMsg}
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:textarea path="description" id="description" cssClass="form-control" rows="5" cols="32"
                                  readonly="${DISABLED}" placeholder="${descriptionMsg}"/>
                </div>
            </div>
            <emm:ShowByPermission token="settings.extended">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label checkbox-control-label" for="admin_and_test_delivery"><mvc:message code="target.adminAndTestDelivery"/></label>
                    </div>
                    <div class="col-sm-8">
                        <label class="toggle">
                            <mvc:checkbox path="useForAdminAndTestDelivery" id="admin_and_test_delivery" disabled="${DISABLED}"/>
                            <div class="toggle-control"></div>
                        </label>
                    </div>
                </div>
            </emm:ShowByPermission>
            <emm:HideByPermission token="settings.extended">
                <mvc:hidden path="useForAdminAndTestDelivery"/>
            </emm:HideByPermission>
            <%@include file="fragments/view-altg-toggle.jspf" %>
        </div>
    </div>
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="target.TargetDefinition"/>*
            </h2>

            <ul class="tile-header-nav">
                <li class="${QB_EDITOR_TAB_ACTIVE_CLASS}">
                    <a href="#" data-toggle-tab="#tab-targetgroupQueryBuilderEditor" data-action="switch-tab-viewQB">
                        <mvc:message code="default.basic"/>
                    </a>
                </li>
                <emm:ShowByPermission token="settings.extended">
                <li class="${EQL_EDITOR_TAB_ACTIVE_CLASS}">
                    <a id="eql-editor-tab-trigger" href="#" data-toggle-tab="#tab-targetgroupEqlEditor" data-action="switch-tab-viewEQL">
                        <mvc:message code="default.advanced"/>
                    </a>
                </li>
                </emm:ShowByPermission>
            </ul>

            <ul class="tile-header-actions">
                <li class="status">
                    <label><mvc:message code="target.group.complexity"/>:</label>
                    <c:choose>
                        <c:when test="${complexityGrade eq COMPLEXITY_GREEN}">
                            <div class="form-badge complexity-green bold">
                                <mvc:message code="target.group.complexity.low"/>
                            </div>
                        </c:when>
                        <c:when test="${complexityGrade eq COMPLEXITY_YELLOW}">
                            <div class="form-badge complexity-yellow bold" data-tooltip="<mvc:message code="warning.target.group.performance.yellow"/>">
                                <mvc:message code="target.group.complexity.medium"/>
                            </div>
                        </c:when>
                        <c:when test="${complexityGrade eq COMPLEXITY_RED}">
                            <div class="form-badge complexity-red bold" data-tooltip="<mvc:message code="warning.target.group.performance.red"/>">
                                <mvc:message code="target.group.complexity.high"/>
                            </div>
                        </c:when>
                    </c:choose>
                </li>
            </ul>
        </div>
        
        <c:if test="${not isValid}">
            <div class="alert-line well">
                <i class="icon icon-exclamation-circle"></i><strong><mvc:message code="target.group.invalid"/></strong>
            </div>
        </c:if>
        
        <div class="tile-content tile-content-forms">
            <c:if test="${targetEditForm.viewFormat == 'QUERY_BUILDER'}">
                <div id="tab-targetgroupQueryBuilderEditor" ${QB_EDITOR_DIV_SHOW_STATE} data-initializer="target-group-query-builder">
                    <script id="config:target-group-query-builder" type="application/json">
                        {
                            "mailTrackingAvailable": ${not empty mailTrackingAvailable ? mailTrackingAvailable : false},
                            "isTargetGroupLocked": ${DISABLED},
                            "helpLanguage": "${helplanguage}",
                            "queryBuilderRules": ${emm:toJson(targetEditForm.queryBuilderRules)},
                            "queryBuilderFilters": ${targetEditForm.queryBuilderFilters}
                        }
                    </script>
                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group">
                                <div class="col-md-12">
                                    <label class="control-label"></label>
                                </div>
                                <div class="col-md-12">
                                    <div id="targetgroup-querybuilder">
                                        <mvc:hidden path="queryBuilderRules" id="queryBuilderRules" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>
            <c:if test="${targetEditForm.viewFormat == 'EQL'}">
                <emm:ShowByPermission token="settings.extended">
                    <div id="tab-targetgroupEqlEditor" ${EQL_EDITOR_DIV_SHOW_STATE}>
                        <div class="row">
                            <div class="col-md-12">
                                <div class="form-group">
                                    <mvc:textarea path="eql" id="eql" rows="14" cols="${TEXTAREA_WIDTH}" cssClass="form-control js-editor-eql" readonly="${DISABLED}" />
                                </div>
                            </div>
                        </div>
                    </div>
                </emm:ShowByPermission>
            </c:if>
        </div>
    </div>

    <emm:ShowByPermission token="targets.change">
        <c:if test="${not empty targetEditForm.targetId and targetEditForm.targetId gt 0 and not DISABLED}">
            <div class="tile">
                <div class="tile-header">
                    <div class="headline">
                        <mvc:select path="mailinglistId" size="1" cssClass="js-select">
                            <mvc:option value="0"><mvc:message code="statistic.All_Mailinglists" /></mvc:option>
                            <mvc:options items="${mailinglists}" itemValue="id" itemLabel="shortname" />
                        </mvc:select>
                    </div>
                    <ul class="tile-header-actions">
                        <li>
                            <button type="button" class="btn btn-regular btn-primary" data-form-url="${saveUrl}" data-form-set="showStatistic: true" data-form-submit="">
      						    <i class="icon icon-save"></i>
                                <span class="text"><mvc:message code="button.save.evaluate" /></span>
                            </button>
                        </li>
                    </ul>
                </div>
                <div class="tile-content">
                    <c:if test="${showStatistic and not empty statisticUrl}">
                        <iframe src="${statisticUrl}" border="0" scrolling="auto" width="100%" height="500px" frameborder="0"> Your
                            Browser does not support IFRAMEs, please update! </iframe>
                    </c:if>
                </div>
            </div>
        </c:if>
    </emm:ShowByPermission>
</mvc:form>
