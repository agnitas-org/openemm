<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
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

<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>
<c:set var="deliveryStatus" value="${form.deliveryStat.deliveryStatus}"/>

<div data-initializer="delivery-status-view">
    <div class="well block">
        <b><mvc:message code="mailing.DistribStatus"/>:</b> <mvc:message code="statistic.DeliveryStatus.${deliveryStatus}"/>
    </div>
    <c:if test="${form.deliveryStat.lastType ne 'NO'}">
        <div class="table-responsive vspace-top-10">
            <table class="table table-bordered table-striped table-equal-col-width">
                <thead>
                <tr>
                    <th><mvc:message code="mailing.LastDelivery"/></th>
                    <th><mvc:message code="mailing.send.delivery.status.${form.deliveryStat.lastType}"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><mvc:message code="Date"/></td>
                    <td><fmt:formatDate value="${form.deliveryStat.lastDate}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}" /></td>
                </tr>
                <tr>
                    <td><mvc:message code="default.Time"/></td>
                    <td><fmt:formatDate value="${form.deliveryStat.lastDate}" pattern="${adminTimeFormat}" timeZone="${adminTimeZone}" /></td>
                </tr>
                <tr>
                    <td><mvc:message code="Targets"/></td>
                    <td>
                        <c:if test="${not empty targetGroupsNames}">
                            <c:forEach var="targetName" items="${targetGroupsNames}" varStatus="vs">
                                <p>${targetName}</p>
                            </c:forEach>
                        </c:if>
                    </td>
                </tr>
                <tr>
                    <td><mvc:message code="statistic.TotalMails"/></td>
                    <td><fmt:formatNumber type="number" pattern="###,###,###,###,##0" value="${form.deliveryStat.lastTotal}"/></td>
                </tr>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${deliveryStatus gt 0}">
        <div class="table-responsive vspace-top-10">
            <table class="table table-bordered table-striped table-equal-col-width">
                <thead>
                <tr>
                    <th colspan="2"><mvc:message code="mailing.Generation"/></th>
                </tr>
                </thead>
                <tbody>
                <c:if test="${deliveryStatus gt 1 && form.deliveryStat.generateStartTime ne null}">
                    <tr>
                        <td><mvc:message code="mailing.sendStatus.started"/></td>
                        <td><fmt:formatDate value="${form.deliveryStat.generateStartTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></td>
                    </tr>
                </c:if>
                <c:if test="${deliveryStatus gt 2 && form.deliveryStat.generateEndTime ne null}">
                    <tr>
                        <td><mvc:message code="mailing.sendStatus.ended"/></td>
                        <td><fmt:formatDate value="${form.deliveryStat.generateEndTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></td>
                    </tr>
                </c:if>
                <c:if test="${deliveryStatus le 2}">
                    <tr>
                        <td><mvc:message code="statistic.ScheduledGenerateTime"/></td>
                        <td><fmt:formatDate value="${form.deliveryStat.scheduledGenerateTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></td>
                    </tr>
                </c:if>
                <c:if test="${deliveryStatus gt 1}">
                    <tr>
                        <td><mvc:message code="mailing.GeneratedMails"/></td>
                        <td><fmt:formatNumber type="number" pattern="###,###,###,###,##0" value="${form.deliveryStat.generatedMails}"/></td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${deliveryStatus gt 0}">
        <div class="table-responsive vspace-top-10">
            <table class="table table-bordered table-striped table-equal-col-width">
                <thead>
                <th colspan="2"><mvc:message code="mailing.Delivery"/></th>
                </thead>
                <tbody>
                <c:if test="${deliveryStatus gt 3 && form.deliveryStat.sendStartTime ne null}">
                    <tr>
                        <td><mvc:message code="mailing.sendStatus.started"/></td>
                        <td><fmt:formatDate value="${form.deliveryStat.sendStartTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></td>
                    </tr>
                </c:if>
                <c:if test="${deliveryStatus gt 4 && form.deliveryStat.sendEndTime ne null}">
                    <tr>
                        <td><mvc:message code="mailing.sendStatus.ended"/></td>
                        <td><fmt:formatDate value="${form.deliveryStat.sendEndTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></td>
                    </tr>
                </c:if>
                <c:if test="${deliveryStatus le 4}">
                    <tr>
                        <td><mvc:message code="statistic.ScheduledSendTime"/></td>
                        <td><fmt:formatDate value="${form.deliveryStat.scheduledSendTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></td>
                    </tr>
                </c:if>
                <c:if test="${deliveryStatus gt 3}">
                    <tr>
                        <td><mvc:message code="mailing.SentMails"/></td>
                        <td class="send_status_second_column">
                            <fmt:formatNumber type="number" pattern="###,###,###,###,##0" value="${form.deliveryStat.sentMails}"/>
                        </td>
                    </tr>
                </c:if>

                <c:if test="${not empty form.deliveryStat.optimizeMailGeneration }">
                    <tr>
                        <td><mvc:message code="mailing.optimizeMailGeneration"/></td>
                        <c:choose>
                            <c:when test="${form.deliveryStat.optimizeMailGeneration eq '24h'}">
                                <td><mvc:message code="mailing.optimizeMailGeneration.next24h"/></td>
                            </c:when>
                            <c:otherwise>
                                <td><mvc:message code="mailing.optimizeMailGeneration.${form.deliveryStat.optimizeMailGeneration}"/></td>
                            </c:otherwise>
                        </c:choose>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${form.deliveryStat ne null && form.deliveryStat.cancelable}">
        <c:url var="confirmCancelLink" value="/mailing/send/${form.mailingID}/confirm-cancel.action"/>
        <a href="${confirmCancelLink}" class="btn btn-regular btn-warning vspace-top-10" data-confirm="">
            <i class="icon icon-ban"></i>
            <c:choose>
                <c:when test="${deliveryStatus == 3 or deliveryStatus == 4}">
                    <span class="text"><mvc:message code="mailing.PauseDelivery"/></span>
                </c:when>
                <c:otherwise>
                    <span class="text"><mvc:message code="mailing.CancelGeneration"/></span>
                </c:otherwise>
            </c:choose>
        </a>
    </c:if>

    <emm:ShowByPermission token="mailing.resume.world">
        <c:if test="${not empty form.deliveryStat and form.deliveryStat.stopped}">
            <c:choose>
                <c:when test="${form.deliveryStat.resumable}">
                    <c:url var="confirmResumeLink" value="/mailing/send/${form.mailingID}/confirm-resume.action"/>
                    <a href="${confirmResumeLink}" class="btn btn-regular btn-primary vspace-top-10" data-confirm="">
                        <i class="icon icon-paper-plane"></i>
                        <c:choose>
                            <c:when test="${deliveryStatus == 3 or deliveryStatus == 4 or deliveryStatus == 8}">
                                <span class="text"><mvc:message code="mailing.ResumeDelivery"/></span>
                                <c:set var="SHOW_DELIVERY_INFO" value="true" />
                            </c:when>
                            <c:otherwise>
                                <span class="text"><mvc:message code="mailing.ResumeGeneration"/></span>
                            </c:otherwise>
                        </c:choose>
                    </a>
                </c:when>
                <c:otherwise>
                    <span data-tooltip="<mvc:message code="error.mailing.delivery.resuming.impossible" arguments="48"/>">
                        <a href="#" class="btn btn-regular btn-primary vspace-top-10 disabled">
                        <i class="icon icon-paper-plane"></i>
                        <span class="text"><mvc:message code="mailing.ResumeDelivery"/></span>
                        </a>
                    </span>
                </c:otherwise>
            </c:choose>

            <c:if test="${copyCancelledMailingEnabled}">
                <c:url var="resumeByCopyLink" value="/mailing/send/${form.mailingID}/confirm-resume-by-copy.action"/>
                <a href="${resumeByCopyLink}" class="btn btn-regular btn-warning vspace-top-10" data-confirm="">
                    <i class="icon icon-copy"></i>
                    <c:choose>
                        <c:when test="${deliveryStatus == 3 or deliveryStatus == 4}">
                            <span class="text"><mvc:message code="mailing.ResumeDeliveryByCopy"/></span>
                            <c:set var="SHOW_DELIVERY_INFO" value="true" />
                        </c:when>
                        <c:otherwise>
                            <span class="text"><mvc:message code="mailing.ResumeGenerationByCopy"/></span>
                        </c:otherwise>
                    </c:choose>
                </a>
            </c:if>

            <c:if test="${SHOW_DELIVERY_INFO and copyCancelledMailingEnabled}">
                <div class="tile-content-forms form-vertical">
                    <div class="form-group">
                        <div class="notification notification-info">
                            <div class="notification-header">
                                <p class="headline">
                                    <i class="icon icon-state-info"></i>
                                    <span class="text"><mvc:message code="Info" /></span>
                                </p>
                            </div>
                            <div class="notification-content">
                                <p>
                                    <mvc:message code="mailing.ResumeDelivery.info" />
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>
        </c:if>
    </emm:ShowByPermission>

    <c:if test="${not empty isPostMailing and isPostMailing eq 'true'}">
        <div class="tile-content-forms form-vertical">
            <div class="form-group">
                <div class="notification notification-info">
                    <div class="notification-header">
                        <p class="headline">
                            <i class="icon icon-state-info"></i>
                            <span class="text"><mvc:message code="Info" /></span>
                        </p>
                    </div>
                    <div class="notification-content">
                        <p>
                            <mvc:message code="mailing.send.post.hint" />
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <script id="config:delivery-status-view" type="application/json">
        {
          "isTransmissionRunning":${isTransmissionRunning},
          "workStatus":"${form.workStatus}",
          "workStatusTooltip":"<mvc:message code="${form.workStatus}"/>"
        }
    </script>
</div>

<script id="mailing-workstatus-icon" type="text/x-mustache-template">
    <strong class="headline" id="workstatus-icon">
        <span class="mailing-badge {{- workstatus }}" data-tooltip="{{- tooltip }}" style="padding: 0"></span>
    </strong>
</script>
