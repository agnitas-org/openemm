<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="approximateMaxDeliverySize" type="java.lang.Long"--%>
<%--@elvariable id="errorSizeThreshold" type="java.lang.Long"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form cssClass="modal-content" servletRelativeAction="/mailing/send/send-world.action" modelAttribute="form">

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

            <mvc:hidden path="generationOptimization" />

            <mvc:hidden path="autoImportId" />

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="Mailing"/>:&nbsp;${form.shortname}</h1>
                <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <%@ include file="fragments/mailing-size-message-initialize.jspf" %>

                <%-- Show the size value at least in KB since the byte number is inaccurate anyway --%>
                <mvc:message code="mailing.send.confirm.ml" arguments="${[form.shortname, mailingSubject, recipientsCount, potentialSendDate, potentialSendTime, sizeMessage, mailinglistShortname]}" />
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary js-confirm-positive">
                    <i class="icon icon-paper-plane"></i>
                    <span class="text"><mvc:message code="button.Send"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
