<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page import="com.agnitas.emm.core.components.service.MailingBlockSizeService" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="enableLinkCheck" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingListExist" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailtrackExtended" type="java.lang.Boolean"--%>

<c:set var="DEFAULT_STEPPING" value="<%= MailingBlockSizeService.DEFAULT_STEPPING %>"/>

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN %>" />
<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}" scope="request" />

<fmt:setLocale value="${admin.locale}"/>

<c:set var="deliveryInfoBlock"><%@include file="fragments/delivery-info-block.jspf" %></c:set>
<c:set var="displayStatusTile" value="${not isTemplate and (canLoadStatusBox or not empty deliveryInfoBlock)}" />

<div class="tiles-container" data-controller="mailing-send" data-initializer="send-mailing" data-editable-view="${agnEditViewKey}">
    <script type="application/json" id="config:send-mailing">
        {
            "approximateMaxDeliverySize": ${emm:toJson(approximateMaxDeliverySize)},
            "errorSizeThreshold": ${emm:toJson(errorSizeThreshold)},
            "warningSizeThreshold": ${emm:toJson(warningSizeThreshold)},
            "urls": {
              "CHECK_LINKS": "<c:url value='/mailing/${tmpMailingID}/trackablelink/check.action' />"
            }
        }
    </script>

    <c:set var="usedInTile">
        <c:if test="${isActionBasedMailing}">
            <%@ include file="fragments/tiles/mailing-send-dependents-tile.jspf" %>
        </c:if>
    </c:set>

    <div class="tiles-block flex-column">
        <c:if test="${not isTemplate}">
            <%@ include file="fragments/tiles/delivery-info-tile.jspf" %>
        </c:if>
        <%@ include file="fragments/tiles/send-test-mailing-tile.jspf" %>
    </div>
    <c:if test="${not isTemplate}">
        <mvc:form id="delivery-settings-form" data-form="resource" modelAttribute="form" cssClass="tiles-block flex-column">
            <mvc:hidden path="mailingID" />
            <mvc:hidden path="stepping" value="${DEFAULT_STEPPING}" />

            <c:if test="${not isActionBasedMailing}">
                <%@ include file="fragments/tiles/delivery-settings-tile.jspf" %>
            </c:if>

            <c:if test="${not isIntervalMailing}">
                <%@ include file="fragments/tiles/extended-delivery-settings-tile.jspf" %>
            </c:if>
        </mvc:form>
    </c:if>

    <c:if test="${displayStatusTile or not empty usedInTile}">
        <div class="tiles-block flex-column">
            <c:if test="${displayStatusTile}">
                <%@ include file="fragments/tiles/status-tile.jspf" %>
            </c:if>
            <c:if test="${not empty usedInTile}">
                ${usedInTile}
            </c:if>
        </div>
    </c:if>

    <c:if test="${isTemplate and isIntervalMailing}">
        <div style="flex: 1">
            <%@include file="fragments/tiles/interval-tile.jspf"%>
        </div>
    </c:if>
</div>

<c:if test="${not isTemplate}">
    <script id="delivery-size-error-msg" type="text/x-mustache-template">
        <c:set var="mailingSizeErrorThreshold" value="${emm:formatBytes(errorSizeThreshold, 1, 'iec', emm:getLocale(pageContext.request))}" />
        <mvc:message code="error.mailing.size.large" arguments="${mailingSizeErrorThreshold}" />
    </script>

    <script id="warning-mailing-size-modal" type="text/x-mustache-template">
        <div class="modal modal-warning" tabindex="-1">
            <div class="modal-dialog modal-fullscreen-lg-down modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h1 class="modal-title"><mvc:message code="warning"/></h1>
                        <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                            <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                        </button>
                    </div>

                    <div class="modal-body">
                        <c:set var="mailingSizeWarningThreshold" value="${emm:formatBytes(warningSizeThreshold, 1, 'iec', emm:getLocale(pageContext.request))}" />
                        <p><mvc:message code="warning.mailing.size.large" arguments="${mailingSizeWarningThreshold}" /></p>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-danger js-confirm-negative" data-bs-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>

                        <button type="button" class="btn btn-primary js-confirm-positive" data-bs-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="button.Proceed"/></span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </script>
</c:if>
