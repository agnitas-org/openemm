<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.common.MailingType" %>
<%@ page import="com.agnitas.beans.Mailing" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="enableLinkCheck" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingListExist" type="java.lang.Boolean"--%>

<c:set var="TYPE_NORMAL" value="<%=MailingType.NORMAL.getCode()%>" scope="request" />
<c:set var="TYPE_FOLLOWUP" value="<%=MailingType.FOLLOW_UP.getCode()%>" scope="request" />
<c:set var="TYPE_ACTIONBASED" value="<%=MailingType.ACTION_BASED.getCode()%>" scope="request" />
<c:set var="TYPE_DATEBASED" value="<%=MailingType.DATE_BASED.getCode()%>" scope="request" />
<c:set var="TYPE_INTERVAL" value="<%=MailingType.INTERVAL.getCode()%>" scope="request" />

<c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, form.workflowId)}" scope="page"/>
<c:set var="isWorkflowDriven" value="${workflowParams.workflowId gt 0}" scope="page"/>
<c:set var="canLoadStatusBox" value="${form.mailingtype eq TYPE_NORMAL or form.mailingtype eq TYPE_FOLLOWUP}" />

<c:set var="isMailingGrid" value="${form.isMailingGrid}" />
<c:set var="tmpMailingID" value="${form.mailingID}" />

<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>

<c:if test="${isWorkflowDriven}">
    <c:url var="WORKFLOW_LINK" value="/workflow/${workflowParams.workflowId}/view.action" scope="page">
        <c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${form.mailingID}"/>
    </c:url>
</c:if>

<tiles:insert page="/WEB-INF/jsp/mailing/template.jsp">
    <c:if test="${isMailingGrid}">
        <tiles:put name="header" type="string">
            <ul class="tile-header-nav">
                <!-- Tabs BEGIN -->
                <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
                <!-- Tabs END -->
            </ul>

            <c:if test="${empty mailingListExist or mailingListExist}">
                <ul class="tile-header-actions">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-eye"></i>
                            <span class="text"><mvc:message code="default.View" /></span>
                            <i class="icon icon-caret-down"></i>
                        </a>
                        <ul class="dropdown-menu">
                            <li>
                                <label class="label">
                                    <input type="radio" value="block" name="view-state" data-view="mailingSend">
                                    <span class="label-text"><mvc:message code="mailing.content.blockview" /></span>
                                </label>
                                <label class="label">
                                    <input type="radio" value="split" checked name="view-state" data-view="mailingSend">
                                    <span class="label-text"><mvc:message code="mailing.content.splitview" /></span>
                                </label>
                            </li>
                        </ul>
                    </li>
                </ul>
            </c:if>
        </tiles:put>
    </c:if>

    <tiles:put name="content" type="string">
        <div class="${isMailingGrid ? "tile-content-padded" : "row"}">

            <c:set var="contentAttr" value=""/>
            <c:if test="${form.mailingtype eq TYPE_NORMAL or form.mailingtype eq TYPE_FOLLOWUP}">
                <c:set var="contentAttr" value="data-view-block='col-xs-12 row-1-1' data-view-split='col-md-6' data-view-hidden='col-xs-12 row-1-1'"/>
            </c:if>
            <div class="col-xs-12 row-1-1" ${contentAttr} data-controller="mailing-send-new">

                <c:if test="${enableLinkCheck}">
                    <%@include file="fragments/mailing-check-links.jspf"%>
                </c:if>

                <%@include file="fragments/mailing-send-test-admin.jspf"%>

                <c:if test="${form.mailingtype eq TYPE_INTERVAL}">
                    <%@include file="configure-interval-delivery.jspf"%>
                </c:if>

                <c:if test="${not form.isTemplate}">
                    <div class="tile">
                        <div class="tile-header">
                            <h2 class="headline">
                                <i class="icon icon-send-o"></i>
                                <mvc:message code="mailing.Delivery" />
                            </h2>
                        </div>
                        <div class="tile-content tile-content-forms">
                            <c:if test="${form.mailingtype eq TYPE_NORMAL}">
                                <%@include file="delivery-normal-settings-new.jspf"%>
                            </c:if>
                            <c:if test="${form.mailingtype eq TYPE_ACTIONBASED}">
                                <%@include file="delivery-actionbased-settings-new.jspf"%>
                            </c:if>
                            <c:if test="${form.mailingtype eq TYPE_DATEBASED}">
                                <%@include file="delivery-datebased-settings-new.jspf"%>
                            </c:if>
                            <c:if test="${form.mailingtype eq TYPE_FOLLOWUP}">
                                <%@include file="delivery-followup-settings-new.jspf"%>
                            </c:if>
                            <c:if test="${form.mailingtype eq TYPE_INTERVAL}">
                                <%@include file="delivery-interval-settings-new.jspf"%>
                            </c:if>
                        </div>
                        <!-- Tile Content END -->
                    </div>
                    <!-- Tile END -->
                </c:if>

                <c:if test="${form.mailingtype eq TYPE_ACTIONBASED}">
                     <%@ include file="mailing-send-dependents-list-new.jsp"%>
                </c:if>
            </div>
            <!-- Col END -->
            <c:if test="${not form.isTemplate}">
                <c:if test="${canLoadStatusBox}">
                    <div data-view-split="col-md-6" data-view-block="col-xs-12" data-view-hidden="hidden">
                        <div class="tile">
                            <div class="tile-header" style="display: flex; justify-content: space-between">
                                <h2 class="headline" style="flex-grow: 1"><mvc:message code="Status" /></h2>

                                <c:set var="workstatus" value=""/>
                                <c:if test="${not empty form.workStatus}">
                                    <c:set var="workstatus">
                                        <mvc:message code="${form.workStatus}"/>
                                    </c:set>
                                </c:if>

                                <strong class="headline" id="workstatus-icon">
                                    <span class="mailing-badge ${form.workStatus}" data-tooltip="${workstatus}" style="padding: 0"></span>
                                </strong>
                            </div>
                            <div class="tile-content">
                                <div class="mailing-preview-wrapper">
                                    <c:url var="deliveryStatisticsBoxLink" value="/mailing/send/${form.mailingID}/delivery-status-box/load.action"/>
                                    <div data-load="${deliveryStatisticsBoxLink}" data-load-interval="50000"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- Col END -->
                </c:if>
            </c:if>
        </div>
        <!-- Row END -->
    </tiles:put>
</tiles:insert>

<script id="recipients-row" type="text/x-mustache-template">
    <tr class="js-recipients-row">
        <td>{{= email }}</td>
        <td class="table-actions">
            <input type="hidden" name="{{= _.uniqueId('statusmailRecipient_') }}" value="{{= email }}" />
            <a href="#" class="btn btn-regular btn-alert" data-action="recipients-row-remove" data-form-target="#statusMailRecipientsForm" data-from-submit data-tooltip="<mvc:message code='button.Delete'/>">
                <i class="icon icon-trash-o"></i>
            </a>
        </td>
    </tr>
</script>
