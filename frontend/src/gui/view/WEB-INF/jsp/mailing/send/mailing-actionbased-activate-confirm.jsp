<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="mailingSubject" type="java.lang.String"--%>
<%--@elvariable id="thumbnailComponentId" type="java.lang.Integer"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form cssClass="modal-content" servletRelativeAction="/mailing/send/activate-action-based.action">

            <input type="hidden" name="mailingID" value="${mailingId}">

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="Mailing"/>:&nbsp;${shortname}</h1>
                <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <%@ include file="fragments/mailing-size-message-initialize.jspf" %>

                <div class="d-flex gap-3 align-items-center">
                    <c:choose>
                        <c:when test="${not empty thumbnailComponentId and thumbnailComponentId gt 0}">
                            <c:url var="previewUrl" value="/sc">
                                <c:param name="compID" value="${thumbnailComponentId}"/>
                                <c:param name="cacheKiller" value="${emm:milliseconds()}"/>
                            </c:url>
                        </c:when>
                        <c:otherwise>
                            <c:url var="previewUrl" value="assets/core/images/facelift/no_preview.svg"/>
                        </c:otherwise>
                    </c:choose>

                    <div class="bordered-box-xs min-w-0" style="flex: 210">
                        <img src="${previewUrl}" alt="Mailing thumbnail" class="img-fluid">
                    </div>

                    <div class="d-flex flex-column gap-3 min-w-0" style="flex: 360">
                        <div>
                            <label class="form-label">
                                <i class="icon icon-pen"></i>
                                <mvc:message code="mailing.Subject"/>
                            </label>

                            <p class="text-truncate">${mailingSubject}</p>
                        </div>

                        <div>
                            <label class="form-label">
                                <i class="icon icon-weight-hanging"></i>
                                <mvc:message code="default.Size"/>
                            </label>

                            <%-- Show the size value at least in KB since the byte number is inaccurate anyway --%>
                            <p class="text-truncate">${sizeMessage}</p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary js-confirm-positive">
                    <i class="icon icon-check"></i>
                    <span class="text"><mvc:message code="button.Activate"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
