<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.preview.ModeType" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>

<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>
<%--@elvariable id="components" type="java.util.List<org.agnitas.beans.MailingComponent>"--%>
<%--@elvariable id="isTagFailureInFromAddress" type="java.lang.Boolean"--%>
<%--@elvariable id="isTagFailureInSubject" type="java.lang.Boolean"--%>
<%--@elvariable id="previewRecipients" type="java.util.Map<java.lang.Integer, java.lang.String>"--%>
<%--@elvariable id="availableTargetGroups" type="java.util.List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="previewHeaderAvailable" type="java.lang.Boolean"--%>
<c:set var="RECIPIENT_MODE" value="<%= ModeType.RECIPIENT %>"/>
<c:set var="TARGET_GROUP_MODE" value="<%= ModeType.TARGET_GROUP %>"/>

<c:set var="storedFieldsScope" value="${mailingSendForm.mailingID}"/>

<div id="mailing-preview-header" class="mailing-preview-header">
    <table class="table-list" data-controller="preview-header-new">
        <tr>
            <c:choose>
                <c:when test="${isTagFailureInFromAddress}">
                    <th style="color: red;" title="<bean:message key="error.template.dyntags"/>">
                        <bean:message key="ecs.From"/>
                    </th>
                </c:when>
                <c:otherwise>
                    <th><bean:message key="ecs.From"/></th>
                </c:otherwise>
            </c:choose>
            <td><b><bean:write name="mailingSendForm" property="previewForm.senderEmail"/></b></td>
        </tr>
        <tr>
            <th><bean:message key="To"/></th>
            <td><b>
                <c:choose>
                    <c:when test="${mailingSendForm.previewForm.modeType == TARGET_GROUP_MODE}">
                        <c:if test="${mailingSendForm.previewForm.targetGroupId eq 0}">
                            <bean:message key="statistic.all_subscribers"/>
                        </c:if>
                        <c:forEach items="${availableTargetGroups}" var="targetGroup">
                            <c:if test="${mailingSendForm.previewForm.targetGroupId eq targetGroup.id}">
                                ${targetGroup.targetName}
                            </c:if>
                        </c:forEach>
                    </c:when>

                    <c:otherwise>
                        <c:if test="${mailingSendForm.previewForm.useCustomerEmail}">
                            ${mailingSendForm.previewForm.customerEmail}
                        </c:if>
                        <c:if test="${not mailingSendForm.previewForm.useCustomerEmail}">
                            <c:forEach var="customer" items="${previewRecipients}">
                                <c:if test="${mailingSendForm.previewForm.customerATID eq customer.key
                                                            or mailingSendForm.previewForm.customerID eq customer.key}">
                                    ${customer.value}
                                </c:if>
                            </c:forEach>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </b></td>
        </tr>
        <tr>
            <c:choose>
                <c:when test="${isTagFailureInSubject}">
                    <th style="color: red;" title="<bean:message key="error.template.dyntags"/>">
                        <bean:message key="mailing.Subject"/>
                    </th>
                </c:when>
                <c:otherwise>
                    <th><bean:message key="mailing.Subject"/></th>
                </c:otherwise>
            </c:choose>
            <td><b><bean:write name="mailingSendForm" property="previewForm.subject"/></b></td>
        </tr>

        <c:forEach var="component" items="${components}" varStatus="status">
            <c:set var="isVisibleComponent" value="${false}"/>
            <c:choose>
                <c:when test="${mailingSendForm.previewForm.modeType == TARGET_GROUP_MODE}">
                    <c:set var="isVisibleComponent" value="${component.targetID eq mailingSendForm.previewForm.targetGroupId}"/>
                </c:when>
                <c:otherwise>
                    <emm:CustomerMatchTarget customerID="${mailingSendForm.previewForm.customerID}" targetID="${component.targetID}">
                        <c:set var="isVisibleComponent" value="${true}"/>
                    </emm:CustomerMatchTarget>
                </c:otherwise>
            </c:choose>

            <c:if test="${isVisibleComponent}">
                <tr>
                    <th class="align-top">
                        <c:if test="${status.first}">
                            <bean:message key="mailing.Attachments"/><br>
                        </c:if>
                    </th>
                    <td class="align-top">
                        <agn:agnLink page="/sc?compID=${component.id}&mailingID=${mailingSendForm.mailingID}&customerID=${mailingSendForm.previewForm.customerID}&targetGroupID=${mailingSendForm.previewForm.targetGroupId}" data-prevent-load="">
                            ${component.componentName}
                            <span class="badge"><i class="icon icon-download"></i></span>
                        </agn:agnLink>
                    </td>
                </tr>
            </c:if>
        </c:forEach>
    </table>
</div>
