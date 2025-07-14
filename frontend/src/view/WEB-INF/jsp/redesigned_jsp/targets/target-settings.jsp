<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action"%>
<%@page import="com.agnitas.emm.core.target.beans.TargetComplexityGrade" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="targetEditForm" type="com.agnitas.emm.core.target.form.TargetEditForm"--%>
<%--@elvariable id="mailTrackingAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="isLocked" type="java.lang.Boolean"--%>
<%--@elvariable id="hidden" type="java.lang.Boolean"--%>
<%--@elvariable id="isValid" type="java.lang.Boolean"--%>
<%--@elvariable id="complexityGrade" type="com.agnitas.emm.core.target.beans.TargetComplexityGrade"--%>

<c:set var="DISABLED" value="${isLocked or hidden}"/>
<c:url var="saveUrl" value="/target/${targetEditForm.targetId}/save.action"/>

<c:set var="ACE_EDITOR_PATH" value="${emm:aceEditorPath(pageContext.request)}" scope="page"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/${ACE_EDITOR_PATH}/emm/ace.min.js"></script>

<div id="target-view" class="tiles-container flex-column" data-controller="target-group-view" data-initializer="target-group-view" data-editable-view="${agnEditViewKey}">
    <script id="config:target-group-view" type="application/json">
        {
            "errorPositionDetails": ${emm:toJson(errorPositionDetails)}
        }
    </script>

    <mvc:form id="settings-tile" servletRelativeAction="/target/${targetEditForm.targetId}/view.action" modelAttribute="targetEditForm"
              cssClass="tile h-auto flex-none"
              data-editable-tile=""
              data-form="resource"
              data-validator-options="skip_empty: false"
              data-action="save-target"
              data-submit-type="${not empty workflowForwardParams ? 'static' : ''}">

        <mvc:hidden path="targetId" />
        <mvc:hidden path="targetId" />
        <mvc:hidden path="viewFormat" />
        <mvc:hidden path="previousViewFormat" value="${targetEditForm.viewFormat}"/>

        <c:if test="${not empty workflowForwardParams}">
            <input type="hidden" name="workflowForwardParams" value="${workflowForwardParams}"/>
            <input type="hidden" name="workflowId" value="${workflowId}" />
        </c:if>

        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="Settings" /></h1>

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

        <div class="tile-body vstack gap-3 js-scrollable">
            <div>
                <label class="form-label" for="shortname">
                    <mvc:message var="nameMsg" code="Name"/>
                    ${nameMsg} *
                </label>

                <mvc:text path="shortname" id="shortname" cssClass="form-control" maxlength="99" readonly="${DISABLED}" placeholder="${nameMsg}" />
            </div>

            <div>
                <label class="form-label" for="description">
                    <mvc:message var="descriptionMsg" code="Description" />
                    ${descriptionMsg}
                </label>

                <mvc:textarea path="description" id="description" cssClass="form-control" readonly="${DISABLED}" placeholder="${descriptionMsg}" rows="1" />
            </div>

            <emm:ShowByPermission token="settings.extended">
                <div>
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
            <%@include file="fragments/target-schedule-deletion.jspf" %>
        </div>
    </mvc:form>

    <div id="definition-tile" class="tile" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="TargetDefinition" /></h1>

            <div class="tile-title-controls">
                <emm:ShowByPermission token="settings.extended">
                    <label class="switch">
                        <input type="checkbox" data-form-target="#settings-tile" data-action="toggle-editor-tab" ${targetEditForm.viewFormat == 'QUERY_BUILDER' ? '' : 'checked'}>
                        <span><mvc:message code="default.basic" /></span>
                        <span><mvc:message code="default.advanced" /></span>
                    </label>
                </emm:ShowByPermission>
            </div>

            <div class="tile-controls">
                <c:choose>
                    <c:when test="${complexityGrade eq TargetComplexityGrade.GREEN}">
                        <span class="status-badge complexity.status.green" data-tooltip="<mvc:message code="target.group.complexity.low"/>"></span>
                    </c:when>
                    <c:when test="${complexityGrade eq TargetComplexityGrade.YELLOW}">
                        <span class="status-badge complexity.status.yellow" data-tooltip="<mvc:message code="warning.target.group.performance.yellow"/>"></span>
                    </c:when>
                    <c:when test="${complexityGrade eq TargetComplexityGrade.RED}">
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
