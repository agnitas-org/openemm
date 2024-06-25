<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="approximateMaxDeliverySize" type="java.lang.Long"--%>
<%--@elvariable id="errorSizeThreshold" type="java.lang.Long"--%>

<c:choose>
    <c:when test="${approximateMaxDeliverySize <= errorSizeThreshold}">
        <div class="modal">
            <div class="modal-dialog">
                <div class="modal-content">
                    <mvc:form servletRelativeAction="/mailing/send/send-world.action" modelAttribute="form">
                        <mvc:hidden path="mailingID"/>
                        <mvc:hidden path="sendDate"/>
                        <mvc:hidden path="sendHour"/>
                        <mvc:hidden path="sendMinute"/>

                        <mvc:hidden path="stepping"/>
                        <mvc:hidden path="blocksize"/>
                        <mvc:hidden path="checkForDuplicateRecords"/>
                        <mvc:hidden path="skipWithEmptyTextContent"/>
                        <mvc:hidden path="cleanupTestsBeforeDelivery"/>

                        <mvc:hidden path="maxRecipients"/>

                        <mvc:hidden path="reportSendAfter24h"/>
                        <mvc:hidden path="reportSendAfter48h"/>
                        <mvc:hidden path="reportSendAfter1Week"/>
                        <mvc:hidden path="reportSendEmail"/>

                        <mvc:hidden path="textEmailsCount"/>
                        <mvc:hidden path="htmlEmailsCount"/>
                        <mvc:hidden path="offlineHtmlEmailsCount"/>

                        <mvc:hidden path="generationOptimization" />

                        <mvc:hidden path="autoImportId" />

                        <div class="modal-header">
                            <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span></button>
                            <h4 class="modal-title"><mvc:message code="Mailing"/>:&nbsp;${form.shortname}</h4>
                        </div>

                        <div class="modal-body">
                            <%@ include file="fragments/mailing-size-message-initialize.jspf" %>

                            <%-- Show the size value at least in KB since the byte number is inaccurate anyway --%>
                            <mvc:message code="mailing.send.confirm.ml" arguments="${[form.shortname, mailingSubject, recipientsCount, potentialSendDate, potentialSendTime, sizeMessage, mailinglistShortname]}" />
                        </div>

                        <div class="modal-footer">
                            <div class="btn-group">
                                <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                                    <i class="icon icon-times"></i>
                                    <span class="text"><mvc:message code="button.Cancel"/></span>
                                </button>
                                <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                                    <i class="icon icon-check"></i>
                                    <span class="text"><mvc:message code="button.Send"/></span>
                                </button>
                            </div>
                        </div>
                    </mvc:form>
                </div>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <div class="modal">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span></button>
                        <h4 class="modal-title text-state-alert">
                            <i class="icon icon-state-alert"></i>
                            <mvc:message code="Error"/>
                        </h4>
                    </div>
                    <div class="modal-body">
                        <c:set var="mailingSizeErrorThreshold" value="${emm:formatBytes(errorSizeThreshold, 1, 'iec', emm:getLocale(pageContext.request))}"/>
                        <p><mvc:message code="error.mailing.size.large" arguments="${mailingSizeErrorThreshold}"/></p>
                    </div>
                    <div class="modal-footer">
                        <div class="btn-group">
                            <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                                <i class="icon icon-times"></i>
                                <span class="text"><mvc:message code="button.OK"/></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </c:otherwise>
</c:choose>
