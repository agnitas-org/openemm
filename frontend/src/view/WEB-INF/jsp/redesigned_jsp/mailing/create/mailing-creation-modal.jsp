<%@ page import="com.agnitas.emm.core.mediatypes.common.MediaTypes" %>
<%@ page import="com.agnitas.emm.core.imports.web.ImportController" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="MAILING_IMPORT_TYPE" value="<%= ImportController.ImportType.MAILING %>" />

<c:set var="isContentReadonly" value="${emm:permissionAllowed('mailing.content.readonly', pageContext.request)}" scope="request" />

<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<c:set var="isWorkflowDriven" value="${workflowId gt 0 or not empty workflowForwardParams}" scope="request" />

<div id="new-mailing-modal" class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg modal-fullscreen-lg-down">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="mailing.create.select"/></h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body js-scrollable">
                <div>
                    <a href="<c:url value="/mailing/templates.action?keepForward=${isWorkflowDriven}&mediaType=${MediaTypes.EMAIL.mediaCode}" />" data-confirm data-bs-dismiss="modal"
                       class="horizontal-card ${isContentReadonly or not emm:permissionAllowed('mailing.classic', pageContext.request) ? 'disabled' : ''}">
                        <div class="horizontal-card__header">
                            <span class="status-badge status-badge--xl mailing.mediatype.email badge--blue"></span>
                        </div>
                        <div class="horizontal-card__body">
                            <p class="horizontal-card__title"><mvc:message code="mailing.standard" /></p>
                            <p class="horizontal-card__subtitle"><mvc:message code="mailing.standard.description" /></p>
                        </div>
                    </a>

                    <%@include file="fragments/mailing-extended-creation-options.jspf" %>

                    <a href="<c:url value="/import/file.action?type=${MAILING_IMPORT_TYPE}" />" data-confirm data-bs-dismiss="modal"
                       class="horizontal-card ${not emm:permissionAllowed('mailing.import', pageContext.request) ? 'disabled' : ''}">
                        <div class="horizontal-card__header">
                            <span class="horizontal-card__icon icon-badge badge--grey"><i class="icon icon-upload"></i></span>
                        </div>
                        <div class="horizontal-card__body">
                            <p class="horizontal-card__title"><mvc:message code="mailing.import" /></p>
                            <p class="horizontal-card__subtitle"><mvc:message code="mailing.import.description" /></p>
                        </div>
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>
