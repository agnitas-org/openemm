<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action"%>
<%@page import="com.agnitas.emm.core.target.beans.TargetComplexityGrade" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="targetEditForm" type="com.agnitas.emm.core.target.form.TargetEditForm"--%>
<%--@elvariable id="mailTrackingAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="isLocked" type="java.lang.Boolean"--%>
<%--@elvariable id="hidden" type="java.lang.Boolean"--%>
<%--@elvariable id="isValid" type="java.lang.Boolean"--%>
<%--@elvariable id="complexityGrade" type="com.agnitas.emm.core.target.beans.TargetComplexityGrade"--%>
<%--@elvariable id="mailinglists" type="java.util.List<org.agnitas.beans.Mailinglist>"--%>

<c:set var="COMPLEXITY_RED" value="<%= TargetComplexityGrade.RED %>" scope="page"/>
<c:set var="COMPLEXITY_YELLOW" value="<%= TargetComplexityGrade.YELLOW %>" scope="page"/>
<c:set var="COMPLEXITY_GREEN" value="<%= TargetComplexityGrade.GREEN %>" scope="page"/>

<c:set var="DISABLED" value="${isLocked or hidden}"/>
<c:url var="saveUrl" value="/target/${targetEditForm.targetId}/save.action"/>

<c:set var="ACE_EDITOR_PATH" value="${emm:aceEditorPath(pageContext.request)}" scope="page"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/${ACE_EDITOR_PATH}/emm/ace.min.js"></script>

<div id="target-view" class="tiles-container d-flex hidden" data-controller="target-group-view" data-initializer="target-group-view" data-editable-view="${agnEditViewKey}">
    <script id="config:target-group-view" type="application/json">
        {
            "errorPositionDetails": ${emm:toJson(errorPositionDetails)}
        }
    </script>

    <div class="tiles-block flex-column" style="flex: 600">
        <mvc:form id="settings-tile" cssClass="tile" servletRelativeAction="/target/${targetEditForm.targetId}/view.action" modelAttribute="targetEditForm" data-editable-tile=""
                  data-form="resource" data-validator-options="skip_empty: false" data-action="save-target"
                  data-submit-type="${not empty workflowForwardParams ? 'static' : ''}">
            <mvc:hidden path="targetId" />
            <mvc:hidden path="viewFormat" />
            <mvc:hidden path="previousViewFormat" value="${targetEditForm.viewFormat}"/>

            <c:if test="${not empty workflowForwardParams}">
                <input type="hidden" name="workflowForwardParams" value="${workflowForwardParams}"/>
                <input type="hidden" name="workflowId" value="${workflowId}" />
            </c:if>

            <div class="tile-header">
                <h1 class="tile-title"><mvc:message code="Settings" /></h1>

                <div class="tile-controls">
                    <%@include file="fragments/altg-badge.jspf" %>

                    <label class="icon-checkbox">
                        <mvc:checkbox id="favorite" path="favorite" autocomplete="off" disabled="${DISABLED}"/>

                        <span class="icon-badge ${DISABLED ? 'icon-badge--disabled' : 'icon-badge--unfavorite'}" data-icon-off>
                            <i class="icon icon-star far"></i>
                        </span>
                        <span class="icon-badge ${DISABLED ? 'icon-badge--disabled' : ''}" data-icon-on>
                            <i class="icon icon-star"></i>
                        </span>
                    </label>
                </div>
            </div>

            <div class="tile-body">
                <div class="row g-3">
                    <div class="col-12">
                        <label class="form-label" for="shortname">
                            <mvc:message var="nameMsg" code="Name"/>
                            ${nameMsg} *
                        </label>

                        <mvc:text path="shortname" id="shortname" cssClass="form-control" maxlength="99" readonly="${DISABLED}" placeholder="${nameMsg}" />
                    </div>

                    <div class="col-12">
                        <label class="form-label" for="description">
                            <mvc:message var="descriptionMsg" code="Description" />
                            ${descriptionMsg}
                        </label>

                        <mvc:textarea path="description" id="description" cssClass="form-control v-resizable" readonly="${DISABLED}" placeholder="${descriptionMsg}"/>
                    </div>

                    <emm:ShowByPermission token="settings.extended">
                        <div class="col-12">
                            <div class="form-check form-switch">
                                <mvc:checkbox path="useForAdminAndTestDelivery" id="admin_and_test_delivery" disabled="${DISABLED}" cssClass="form-check-input" role="switch"/>

                                <label class="form-label form-check-label" for="admin_and_test_delivery">
                                    <mvc:message code="target.adminAndTestDelivery"/>
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
        </mvc:form>

        <c:if test="${targetEditForm.targetId gt 0}">
            <c:if test="${not DISABLED and emm:permissionAllowed('targets.change', pageContext.request)}">
                <div id="evaluation-tile" class="tile" data-editable-tile>
                    <div class="tile-header">
                        <h1 class="tile-title"><mvc:message code="GWUA.evaluation" /></h1>

                        <div class="tile-controls min-w-0">
                            <mvc:select path="targetEditForm.mailinglistId" size="1" cssClass="form-control js-select" data-select-options="dropdownAutoWidth: true, width: 'auto'" form="settings-tile">
                                <mvc:option value="0"><mvc:message code="statistic.All_Mailinglists" /></mvc:option>
                                <mvc:options items="${mailinglists}" itemValue="id" itemLabel="shortname" />
                            </mvc:select>

                            <button type="button" class="btn btn-primary btn-icon-sm" data-tooltip="<mvc:message code="button.save.evaluate" />"
                                    data-form-target="#settings-tile" data-form-url="${saveUrl}" data-form-set="showStatistic: true" data-form-submit>
                                <i class="icon icon-play-circle"></i>
                            </button>
                        </div>
                    </div>

                    <div class="tile-body js-scrollable" style="overflow-y: auto !important;">
                        <c:choose>
                            <c:when test="${showStatistic and not empty statisticUrl}">
                                <iframe src="${statisticUrl}" border="0" scrolling="auto" frameborder="0" style="width: 100%; height: 100px"> Your
                                    Browser does not support IFRAMEs, please update! </iframe>
                            </c:when>
                            <c:otherwise>
                                <div class="notification-simple notification-simple--lg">
                                    <i class="icon icon-info-circle"></i>
                                    <span><mvc:message code="GWUA.targetGroup.notEvaluated" /></span>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:if>

            <div id="used-in-tile" class="tile" data-editable-tile>
                <div class="tile-header">
                    <h1 class="tile-title"><mvc:message code="default.usedIn" /></h1>
                </div>

                <div class="tile-body" data-load="<c:url value='/target/${targetEditForm.targetId}/dependents.action'/>"></div>
            </div>
        </c:if>
    </div>

    <div id="definition-tile" class="tile" style="flex: 1230" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="TargetDefinition" /></h1>

            <div class="tile-title-controls">
                <emm:ShowByPermission token="settings.extended">
                    <input id="use-advanced-tab" type="checkbox" class="icon-switch" data-form-target="#settings-tile" data-action="toggle-editor-tab" ${targetEditForm.viewFormat == 'QUERY_BUILDER' ? '' : 'checked'}>
                    <label for="use-advanced-tab" class="text-switch__label">
                        <span><mvc:message code="default.basic" /></span>
                        <span><mvc:message code="default.advanced" /></span>
                    </label>
                </emm:ShowByPermission>
            </div>

            <div class="tile-controls">
                <c:choose>
                    <c:when test="${complexityGrade eq COMPLEXITY_GREEN}">
                        <span class="status-badge complexity.status.green" data-tooltip="<mvc:message code="target.group.complexity.low"/>"></span>
                    </c:when>
                    <c:when test="${complexityGrade eq COMPLEXITY_YELLOW}">
                        <span class="status-badge complexity.status.yellow" data-tooltip="<mvc:message code="warning.target.group.performance.yellow"/>"></span>
                    </c:when>
                    <c:when test="${complexityGrade eq COMPLEXITY_RED}">
                        <span class="status-badge complexity.status.red" data-tooltip="<mvc:message code="warning.target.group.performance.red"/>"></span>
                    </c:when>
                </c:choose>
            </div>
        </div>

        <div class="tile-body js-scrollable">
            <div class="d-flex flex-column gap-2 h-100">
                <c:if test="${not isValid}">
                    <div class="notification-simple notification-simple--lg notification-simple--alert">
                        <i class="icon icon-state-alert"></i>
                        <span><b><mvc:message code="target.group.invalid" /></b></span>
                    </div>
                </c:if>

                <c:if test="${targetEditForm.viewFormat == 'QUERY_BUILDER'}">
                    <div data-initializer="target-group-query-builder" data-form-target="#settings-tile">
                        <div id="targetgroup-querybuilder">
                            <mvc:hidden id="queryBuilderRules" path="targetEditForm.queryBuilderRules" form="settings-tile" />
                        </div>

                        <script id="config:target-group-query-builder" type="application/json">
                            {
                                "mailTrackingAvailable": ${not empty mailTrackingAvailable ? mailTrackingAvailable : false},
                                "isTargetGroupLocked": ${DISABLED},
                                "helpLanguage": "${helplanguage}",
                                "queryBuilderRules": ${emm:toJson(targetEditForm.queryBuilderRules)},
                                "queryBuilderFilters": ${targetEditForm.queryBuilderFilters}
                            }
                        </script>
                    </div>
                </c:if>

                <c:if test="${targetEditForm.viewFormat == 'EQL'}">
                    <emm:ShowByPermission token="settings.extended">
                        <div class="border rounded h-100 overflow-hidden">
                            <mvc:textarea id="eql" path="targetEditForm.eql" rows="14" cols="${TEXTAREA_WIDTH}" cssClass="form-control js-editor-eql" readonly="${DISABLED}" form="settings-tile" />
                        </div>
                    </emm:ShowByPermission>
                </c:if>
            </div>
        </div>
    </div>
</div>
