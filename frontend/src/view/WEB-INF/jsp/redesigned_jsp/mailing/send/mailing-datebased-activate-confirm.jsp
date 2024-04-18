<%@ page import="com.agnitas.emm.core.components.service.MailingBlockSizeService" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="mailingSubject" type="java.lang.String"--%>
<%--@elvariable id="targetGroups" type="java.lang.String"--%>
<%--@elvariable id="isAlreadySentToday" type="java.lang.Boolean"--%>

<c:set var="DEFAULT_STEPPING" value="<%= MailingBlockSizeService.DEFAULT_STEPPING %>"/>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
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
                <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <c:choose>
                    <c:when test="${isAlreadySentToday}">
                        <mvc:message code="mailing.activate.confirm.question" arguments="${[form.sendTime]}"/>
                    </c:when>
                    <c:otherwise>
                        <%@ include file="fragments/mailing-size-message-initialize.jspf" %>
                        <%-- Show the size value at least in KB since the byte number is inaccurate anyway --%>
                        <mvc:message code="mailing.date.confirm.activation" arguments="${[form.shortname, mailingSubject, form.sendTime, targetGroups, sizeMessage]}"/>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="modal-footer">
                <c:choose>
                    <c:when test="${isAlreadySentToday}">
                        <button type="button" class="btn btn-primary flex-grow-1 js-confirm-positive" data-url="<c:url value="/mailing/send/activate-date-based.action?activateAgainToday=true"/>">
                            <i class="icon icon-paper-plane"></i>
                            <span class="text"><mvc:message code="button.mailing.send.today"/></span>
                        </button>
                        <button type="button" class="btn btn-primary flex-grow-1 js-confirm-positive">
                            <i class="icon icon-paper-plane"></i>
                            <span class="text"><mvc:message code="button.mailing.send.tomorrow"/></span>
                        </button>
                    </c:when>
                    <c:otherwise>
                        <button type="button" class="btn btn-primary flex-grow-1 js-confirm-positive">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="button.Activate"/></span>
                        </button>
                    </c:otherwise>
                </c:choose>
            </div>
        </mvc:form>
    </div>
</div>
