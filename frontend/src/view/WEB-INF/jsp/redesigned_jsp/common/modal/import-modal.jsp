<%@ page import="com.agnitas.emm.core.imports.web.ImportController" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="MAILING" value="<%= ImportController.ImportType.MAILING %>" />
<c:set var="TEMPLATE" value="<%= ImportController.ImportType.TEMPLATE %>" />
<c:set var="USER_FORM" value="<%= ImportController.ImportType.USER_FORM %>" />
<c:set var="BUILDING_BLOCK" value="<%= ImportController.ImportType.BUILDING_BLOCK %>" />

<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="type" type="com.agnitas.emm.core.imports.web.ImportController.ImportType"--%>

<c:url var="formActionUrl" value="/import/execute.action" />
<c:set var="isStaticForm" value="false" />

<c:choose>
    <c:when test="${type eq MAILING}">
        <mvc:message var="titleMsg" code="mailing.import" />
    </c:when>
    <c:when test="${type eq TEMPLATE}">
        <mvc:message var="titleMsg" code="template.import" />
    </c:when>
    <c:when test="${type eq USER_FORM}">
        <mvc:message var="titleMsg" code="forms.import" />
        <c:url var="formActionUrl" value="/webform/importUserForm.action" />
        <c:set var="isStaticForm" value="true" />
    </c:when>
    <c:when test="${type eq BUILDING_BLOCK}">
        <mvc:message var="titleMsg" code="grid.component.import" />
        <c:url var="formActionUrl" value="/import/buildingBlocksRedesigned.action" />
    </c:when>
</c:choose>

<c:set var="isWorkflowDriven" value="${workflowId gt 0 or not empty workflowForwardParams}" />

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg modal-fullscreen-lg-down" data-controller="import-modal">
        <mvc:form action="${formActionUrl}" enctype="multipart/form-data" modelAttribute="form" cssClass="modal-content"
                  data-form="${isStaticForm ? 'static' : 'resource'}" data-custom-loader="" data-initializer="upload" data-action="${isWorkflowDriven ? 'import-for-workflow' : ''}">
            <div class="modal-header">
                <h1 class="modal-title">
                    <span class="text-truncate">${titleMsg}</span>

                    <c:if test="${type eq MAILING}">
                        <span class="icon-badge badge--grey" data-tooltip="${titleMsg}">
                            <i class="icon icon-upload"></i>
                        </span>
                    </c:if>
                </h1>

                <div class="modal-controls">
                    <c:choose>
                        <c:when test="${type eq TEMPLATE}">
                            <emm:ShowByPermission token="settings.extended">
                                <div class="form-check form-switch">
                                    <mvc:checkbox cssClass="form-check-input" path="overwriteTemplate" role="switch" id="import-duplicates-switch" />
                                    <label class="form-label form-check-label fw-semibold" for="import-duplicates-switch">
                                        <mvc:message code="import.template.overwrite"/>
                                        <a href="#" class="icon icon-question-circle" data-help="mailing/OverwriteTemplate.xml"></a>
                                    </label>
                                </div>
                            </emm:ShowByPermission>

                            <mvc:hidden path="template" value="true" />
                        </c:when>

                        <c:when test="${type eq BUILDING_BLOCK}">
                            <div class="form-check form-switch">
                                <mvc:checkbox cssClass="form-check-input" path="overwriteTemplate" role="switch" id="import-duplicates-blocks-switch" />
                                <label class="form-label form-check-label fw-semibold" for="import-duplicates-blocks-switch">
                                    <mvc:message code="grid.component.overwrite"/>
                                </label>
                            </div>

                            <mvc:hidden path="template" value="true" />
                        </c:when>
                    </c:choose>

                    <button type="button" class="btn-close" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
            </div>

            <div class="modal-body">
                <%@ include file="../../data-management/import-templates/fragments/import-dropzone.jspf" %>
            </div>

            <c:if test="${type eq MAILING}">
                <div class="modal-footer modal-footer--nav">
                    <a href="<c:url value="/mailing/create.action?keepForward=${isWorkflowDriven}" />" class="btn btn-secondary" data-bs-dismiss="modal" data-confirm>
                        <i class="icon icon-angle-left"></i>
                        <span class="mobile-hidden"><mvc:message code="mailing.create.back" /></span>
                        <span class="mobile-visible"><mvc:message code="button.Back" /></span>
                    </a>
                </div>
            </c:if>
        </mvc:form>
    </div>
</div>
