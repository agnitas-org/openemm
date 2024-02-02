<%@ page import="com.agnitas.emm.core.components.service.MailingBlockSizeService" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="mailingSubject" type="java.lang.String"--%>
<%--@elvariable id="targetGroups" type="java.lang.String"--%>
<%--@elvariable id="isAlreadySentToday" type="java.lang.Boolean"--%>

<c:set var="DEFAULT_STEPPING" value="<%= MailingBlockSizeService.DEFAULT_STEPPING %>"/>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <mvc:form servletRelativeAction="/mailing/send/activate-date-based.action" modelAttribute="form">
                <mvc:hidden path="mailingID" />
                <mvc:hidden path="autoImportId" />
                <mvc:hidden path="sendHour" />
                <mvc:hidden path="sendMinute" />

                <mvc:hidden path="stepping" value="${DEFAULT_STEPPING}"/>
                <mvc:hidden path="blocksize"/>
                <mvc:hidden path="generationOptimization" />
                <mvc:hidden path="checkForDuplicateRecords"/>
                <mvc:hidden path="skipWithEmptyTextContent"/>
                <mvc:hidden path="maxRecipients" />
                <mvc:hidden path="securitySettings.enableNotifications" />
                <mvc:hidden path="securitySettings.enableNoSendCheckNotifications" />
                <mvc:hidden path="securitySettings.clearanceEmail" />
                <mvc:hidden path="securitySettings.clearanceThreshold" />

                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span></button>
                    <h4 class="modal-title"><mvc:message code="Mailing"/>:&nbsp;${form.shortname}</h4>
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
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <c:choose>
                            <c:when test="${isAlreadySentToday}">
                                <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal" data-url="<c:url value="/mailing/send/activate-date-based.action?activateAgainToday=true"/>">
                                    <i class="icon icon-check"></i>
                                    <span class="text"><mvc:message code="button.mailing.send.today"/></span>
                                </button>
                                <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                                    <i class="icon icon-check"></i>
                                    <span class="text"><mvc:message code="button.mailing.send.tomorrow"/></span>
                                </button>
                            </c:when>
                            <c:otherwise>
                                <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                                    <i class="icon icon-check"></i>
                                    <span class="text"><mvc:message code="button.Activate"/></span>
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
