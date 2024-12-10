<%@ page import="com.agnitas.emm.core.mediatypes.common.MediaTypes" %>
<%@ page import="com.agnitas.emm.core.imports.web.ImportController" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="MAILING_IMPORT_TYPE" value="<%= ImportController.ImportType.MAILING %>" />

<c:set var="isContentReadonly" value="${emm:permissionAllowed('mailing.content.readonly', pageContext.request)}" scope="request" />

<%--@elvariable id="workflowId" type="java.lang.Integer"--%>

<div id="new-mailing-modal" class="modal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="mailing.type.select"/></h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <div class="row g-3">
                    <div class="col">
                        <a id="standard-mailing-card" href="<c:url value="/mailing/templates.action?keepForward=${workflowId gt 0}&mediaType=${MediaTypes.EMAIL.mediaCode}" />" data-confirm data-bs-dismiss="modal"
                           class="horizontal-card h-100 ${isContentReadonly or not emm:permissionAllowed('mailing.classic', pageContext.request) ? 'disabled' : ''}">
                            <div class="horizontal-card__header">
                                <span class="status-badge status-badge--xl mailing.mediatype.email"></span>
                            </div>
                            <div class="horizontal-card__body">
                                <p class="horizontal-card__title"><mvc:message code="mailing.standard" /></p>
                                <p class="horizontal-card__subtitle"><mvc:message code="mailing.standard.description" /></p>
                            </div>
                        </a>
                    </div>

                    <%@include file="fragments/mailing-extended-creation-options.jspf" %>

                    <div class="col-12">
                        <a id="import-mailing-card" href="<c:url value="/import/file.action?type=${MAILING_IMPORT_TYPE}" />" data-confirm data-bs-dismiss="modal"
                           class="horizontal-card h-100 ${not emm:permissionAllowed('mailing.import', pageContext.request) ? 'disabled' : ''}">
                            <div class="horizontal-card__header">
                                <span class="horizontal-card__icon icon-badge"><i class="icon icon-upload text-white"></i></span>
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
</div>
