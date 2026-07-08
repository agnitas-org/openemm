<%@ page import="com.agnitas.emm.core.components.service.MailingBlockSizeService" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="mailingSubject" type="java.lang.String"--%>
<%--@elvariable id="targetGroups" type="java.lang.String"--%>
<%--@elvariable id="isAlreadySentToday" type="java.lang.Boolean"--%>
<%--@elvariable id="thumbnailComponentId" type="java.lang.Integer"--%>

<c:set var="DEFAULT_STEPPING" value="<%= MailingBlockSizeService.DEFAULT_STEPPING %>"/>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form cssClass="modal-content" servletRelativeAction="/mailing/send/activate-date-based.action" modelAttribute="form">

            <mvc:hidden path="mailingID" />
            <mvc:hidden path="autoImportId" />
            <mvc:hidden path="sendHour" />
            <mvc:hidden path="sendMinute" />

            <mvc:hidden path="stepping" value="${DEFAULT_STEPPING}"/>
            <mvc:hidden path="blocksize"/>
            <mvc:hidden path="generationOptimization" />
            <mvc:hidden path="checkForDuplicateRecords"/>
            <mvc:hidden path="skipWithEmptyTextContent"/>
            <mvc:hidden path="cleanupTestsBeforeDelivery"/>
            <mvc:hidden path="maxRecipients" />
            <mvc:hidden path="securitySettings.enableNotifications" />
            <mvc:hidden path="securitySettings.enableNoSendCheckNotifications" />
            <mvc:hidden path="securitySettings.clearanceEmail" />
            <mvc:hidden path="securitySettings.clearanceThreshold" />

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="Mailing"/>:&nbsp;${form.shortname}</h1>
                <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <div class="d-flex gap-3 align-items-center">
                    <c:choose>
                        <c:when test="${isAlreadySentToday}">
                            <p><mvc:message code="mailing.activate.confirm.question" arguments="${[form.sendTime]}"/></p>
                        </c:when>
                        <c:otherwise>
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
                                <%@ include file="fragments/mailing-size-message-initialize.jspf" %>
                                <div>
                                    <label class="form-label">
                                        <i class="icon icon-pen"></i>
                                        <mvc:message code="mailing.Subject"/>
                                    </label>

                                    <p class="text-truncate">${mailingSubject}</p>
                                </div>

                                <div>
                                    <label class="form-label">
                                        <i class="icon icon-clock"></i>
                                        <mvc:message code="Time"/>
                                    </label>

                                    <p class="text-truncate">${form.sendTime}</p>
                                </div>

                                <div>
                                    <label class="form-label">
                                        <i class="icon icon-users"></i>
                                        <mvc:message code="Targets"/>
                                    </label>

                                    <p class="text-truncate">${targetGroups}</p>
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
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="modal-footer">
                <c:choose>
                    <c:when test="${isAlreadySentToday}">
                        <button type="button" class="btn btn-primary js-confirm-positive" data-url="<c:url value="/mailing/send/activate-date-based.action?activateAgainToday=true"/>">
                            <i class="icon icon-paper-plane"></i>
                            <span class="text"><mvc:message code="button.mailing.send.today"/></span>
                        </button>
                        <button type="button" class="btn btn-primary js-confirm-positive">
                            <i class="icon icon-paper-plane"></i>
                            <span class="text"><mvc:message code="button.mailing.send.tomorrow"/></span>
                        </button>
                    </c:when>
                    <c:otherwise>
                        <button type="button" class="btn btn-primary js-confirm-positive">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="button.Activate"/></span>
                        </button>
                    </c:otherwise>
                </c:choose>
            </div>
        </mvc:form>
    </div>
</div>
