<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="approximateMaxDeliverySize" type="java.lang.Long"--%>
<%--@elvariable id="errorSizeThreshold" type="java.lang.Long"--%>
<%--@elvariable id="thumbnailComponentId" type="java.lang.Integer"--%>

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
                <button type="button" class="btn-close" data-confirm-negative>
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body js-scrollable">
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
                                <i class="icon icon-user-friends"></i>
                                <mvc:message code="Recipients"/>
                            </label>

                            <p class="text-truncate">${recipientsCount}</p>
                        </div>

                        <div>
                            <label class="form-label">
                                <i class="icon icon-list"></i>
                                <mvc:message code="Mailinglist"/>
                            </label>

                            <p class="text-truncate">${mailinglistShortname}</p>
                        </div>

                        <div>
                            <label class="form-label">
                                <i class="icon icon-calendar-day"></i>
                                <mvc:message code="mailing.senddate"/>
                            </label>

                            <p class="text-truncate">${potentialSendDate}</p>
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
                <button type="button" class="btn btn-danger" data-confirm-negative>
                    <i class="icon icon-times"></i>
                    <span><mvc:message code="button.Cancel"/></span>
                </button>

                <button type="button" class="btn btn-primary" data-confirm-positive>
                    <i class="icon icon-paper-plane"></i>
                    <span><mvc:message code="MailingSend"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
