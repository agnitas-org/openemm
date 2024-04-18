<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="http://www.springframework.org/tags" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="adminTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="copyCancelledMailingEnabled" type="java.lang.Boolean"--%>
<%--@elvariable id="isTransmissionRunning" type="java.lang.Boolean"--%>
<%--@elvariable id="targetGroupsNames" type="java.util.List<java.lang.String>"--%>
<%--@elvariable id="deliveryStat" type="com.agnitas.beans.DeliveryStat"--%>

<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>
<c:set var="deliveryStatus" value="${deliveryStat.deliveryStatus}"/>

<div class="row g-3" data-initializer="delivery-status-view">
    <script id="config:delivery-status-view" type="application/json">
        {
          "isTransmissionRunning":${isTransmissionRunning},
          "workStatus":"${form.workStatus}",
          "workStatusTooltip":"<mvc:message code="${form.workStatus}"/>",
          "deliveryStat": ${emm:toJson(deliveryStat)},
          "deliveryStatExtraInfo": {
            "copyCancelledMailingEnabled": ${copyCancelledMailingEnabled}
          }
        }
    </script>

    <div class="col-12">
        <div class="notification-simple notification-simple--lg">
            <span><strong><mvc:message code="mailing.DistribStatus"/>:</strong> <mvc:message code="statistic.DeliveryStatus.${deliveryStatus}"/></span>
        </div>
    </div>

    <c:if test="${deliveryStat.lastType ne 'NO' and deliveryStat.sendEndTime ne null}">
        <div class="col-12">
            <div class="tile tile--sm tile--highlighted">
                <div class="tile-header">
                    <h3><mvc:message code="mailing.LastDelivery"/>: <mvc:message code="mailing.send.delivery.status.${deliveryStat.lastType}"/></h3>
                </div>

                <div class="tile-body">
                    <div class="grid gap-2" style="--bs-columns: 2;">
                        <p><mvc:message code="Date"/></p>
                        <p><fmt:formatDate value="${deliveryStat.sendEndTime}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}" /></p>

                        <p><mvc:message code="default.Time"/></p>
                        <p><fmt:formatDate value="${deliveryStat.sendEndTime}" pattern="${adminTimeFormat}" timeZone="${adminTimeZone}" /></p>

                        <p><mvc:message code="Targets"/></p>
                        <div>
                            <c:if test="${not empty targetGroupsNames}">
                                <c:forEach var="targetName" items="${targetGroupsNames}">
                                    <p>${targetName}</p>
                                </c:forEach>
                            </c:if>
                        </div>

                        <p><mvc:message code="statistic.TotalMails"/></p>
                        <p><fmt:formatNumber type="number" pattern="###,###,###,###,##0" value="${deliveryStat.lastTotal}"/></p>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <c:if test="${deliveryStatus gt 0}">
        <div class="col-12">
            <div class="tile tile--sm tile--highlighted">
                <div class="tile-header">
                    <h3><mvc:message code="mailing.Generation"/></h3>
                </div>

                <div class="tile-body">
                    <div class="grid gap-2" style="--bs-columns: 2;">
                        <c:if test="${deliveryStatus gt 1 && deliveryStat.generateStartTime ne null}">
                            <p><mvc:message code="mailing.sendStatus.started"/></p>
                            <p><fmt:formatDate value="${deliveryStat.generateStartTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></p>
                        </c:if>

                        <c:if test="${deliveryStatus gt 2 && deliveryStat.generateEndTime ne null}">
                            <p><mvc:message code="mailing.sendStatus.ended"/></p>
                            <p><fmt:formatDate value="${deliveryStat.generateEndTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></p>
                        </c:if>

                        <c:if test="${deliveryStatus le 2}">
                            <p><mvc:message code="statistic.ScheduledGenerateTime"/></p>
                            <p><fmt:formatDate value="${deliveryStat.scheduledGenerateTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></p>
                        </c:if>

                        <c:if test="${deliveryStatus gt 1}">
                            <p><mvc:message code="mailing.GeneratedMails"/></p>
                            <p><fmt:formatNumber type="number" pattern="###,###,###,###,##0" value="${deliveryStat.generatedMails}"/></p>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <c:if test="${deliveryStatus gt 0}">
        <div class="col-12">
            <div class="tile tile--sm tile--highlighted">
                <div class="tile-header">
                    <h3><mvc:message code="mailing.Delivery"/></h3>
                </div>

                <div class="tile-body">
                    <div class="grid gap-2" style="--bs-columns: 2;">
                        <c:if test="${deliveryStatus gt 3 && deliveryStat.sendStartTime ne null}">
                            <p><mvc:message code="mailing.sendStatus.started"/></p>
                            <p><fmt:formatDate value="${deliveryStat.sendStartTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></p>
                        </c:if>

                        <c:if test="${deliveryStatus gt 4 && deliveryStat.sendEndTime ne null}">
                            <p><mvc:message code="mailing.sendStatus.ended"/></p>
                            <p><fmt:formatDate value="${deliveryStat.sendEndTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></p>
                        </c:if>

                        <c:if test="${deliveryStatus le 4}">
                            <p><mvc:message code="statistic.ScheduledSendTime"/></p>
                            <p><fmt:formatDate value="${deliveryStat.scheduledSendTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></p>
                        </c:if>

                        <c:if test="${deliveryStatus gt 3}">
                            <p><mvc:message code="mailing.SentMails"/></p>
                            <p><fmt:formatNumber type="number" pattern="###,###,###,###,##0" value="${deliveryStat.sentMails}"/></p>
                        </c:if>

                        <c:if test="${not empty deliveryStat.optimizeMailGeneration }">
                            <p><mvc:message code="mailing.optimizeMailGeneration"/></p>
                            <p>
                                <c:choose>
                                    <c:when test="${deliveryStat.optimizeMailGeneration eq '24h'}">
                                        <mvc:message code="mailing.optimizeMailGeneration.next24h"/>
                                    </c:when>
                                    <c:otherwise>
                                        <mvc:message code="mailing.optimizeMailGeneration.${deliveryStat.optimizeMailGeneration}"/>
                                    </c:otherwise>
                                </c:choose>
                            </p>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <emm:ShowByPermission token="mailing.resume.world">
        <c:if test="${not empty deliveryStat and deliveryStat.stopped}">
            <c:if test="${copyCancelledMailingEnabled}">
                <c:set var="SHOW_DELIVERY_INFO" value="${deliveryStat.resumable and (deliveryStatus == 3 or deliveryStatus == 4 or deliveryStatus == 8)}" />
                <c:set var="SHOW_DELIVERY_INFO" value="${SHOW_DELIVERY_INFO or (deliveryStatus == 3 or deliveryStatus == 4)}" />
            </c:if>

            <c:if test="${SHOW_DELIVERY_INFO}">
                <div class="col-12">
                    <div class="notification-simple notification-simple--lg notification-simple--info">
                        <span><mvc:message code="mailing.ResumeDelivery.info" /></span>
                    </div>
                </div>
            </c:if>
        </c:if>
    </emm:ShowByPermission>

    <c:if test="${isPostMailing}">
        <div class="col-12">
            <div class="notification-simple notification-simple--lg notification-simple--info">
                <span><mvc:message code="mailing.send.post.hint" /></span>
            </div>
        </div>
    </c:if>
</div>

<script id="mailing-workstatus-icon" type="text/x-mustache-template">
    <span id="workstatus-icon" class="status-badge status-badge--lg {{- workstatus }}" data-tooltip="{{- tooltip }}"></span>
</script>
