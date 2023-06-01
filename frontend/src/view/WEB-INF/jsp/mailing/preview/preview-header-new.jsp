<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.preview.ModeType" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.preview.form.PreviewForm"--%>
<%--@elvariable id="components" type="java.util.List<org.agnitas.beans.MailingComponent>"--%>
<%--@elvariable id="isTagFailureInFromAddress" type="java.lang.Boolean"--%>
<%--@elvariable id="isTagFailureInSubject" type="java.lang.Boolean"--%>
<%--@elvariable id="previewRecipients" type="java.util.Map<java.lang.Integer, java.lang.String>"--%>
<%--@elvariable id="availableTargetGroups" type="java.util.List<com.agnitas.beans.TargetLight>"--%>

<c:set var="RECIPIENT_MODE" value="<%= ModeType.RECIPIENT %>"/>
<c:set var="TARGET_GROUP_MODE" value="<%= ModeType.TARGET_GROUP %>"/>

<c:set var="storedFieldsScope" value="${form.mailingId}"/>

<div id="mailing-preview-header" class="mailing-preview-header">
    <table class="table-list" data-controller="preview-header-new-new">
        <tr>
            <c:choose>
                <c:when test="${isTagFailureInFromAddress}">
                    <th style="color: red;" title="<mvc:message code="error.template.dyntags"/>">
                        <mvc:message code="ecs.From"/>
                    </th>
                </c:when>
                <c:otherwise>
                    <th><mvc:message code="ecs.From"/></th>
                </c:otherwise>
            </c:choose>
            <td><b><c:out value="${form.senderEmail}"/></b></td>
        </tr>
        <tr>
            <th><mvc:message code="To"/></th>
            <td><b>
                <c:choose>
                    <c:when test="${form.modeType == TARGET_GROUP_MODE}">
                        <c:if test="${form.targetGroupId eq 0}">
                            <mvc:message code="statistic.all_subscribers"/>
                        </c:if>
                        <c:forEach items="${availableTargetGroups}" var="targetGroup">
                            <c:if test="${form.targetGroupId eq targetGroup.id}">
                                ${targetGroup.targetName}
                            </c:if>
                        </c:forEach>
                    </c:when>

                    <c:otherwise>
                        <c:if test="${form.useCustomerEmail}">
                            ${form.customerEmail}
                        </c:if>
                        <c:if test="${not form.useCustomerEmail}">
                            <c:forEach var="customer" items="${previewRecipients}">
                                <c:if test="${form.customerATID eq customer.key or form.customerID eq customer.key}">
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
                    <th style="color: red;" title="<mvc:message code="error.template.dyntags"/>">
                        <mvc:message code="mailing.Subject"/>
                    </th>
                </c:when>
                <c:otherwise>
                    <th><mvc:message code="mailing.Subject"/></th>
                </c:otherwise>
            </c:choose>
            <td><b>${form.subject}</b></td>
        </tr>

        <c:forEach var="component" items="${components}" varStatus="status">
            <c:set var="isVisibleComponent" value="${false}"/>
            <c:choose>
                <c:when test="${form.modeType == TARGET_GROUP_MODE}">
                    <c:set var="isVisibleComponent" value="${component.targetID eq form.targetGroupId}"/>
                </c:when>
                <c:otherwise>
                    <emm:CustomerMatchTarget customerID="${form.customerID}" targetID="${component.targetID}">
                        <c:set var="isVisibleComponent" value="${true}"/>
                    </emm:CustomerMatchTarget>
                </c:otherwise>
            </c:choose>

            <c:if test="${isVisibleComponent}">
                <tr>
                    <th class="align-top">
                        <c:if test="${status.first}">
                            <mvc:message code="mailing.Attachments"/><br>
                        </c:if>
                    </th>
                    <td class="align-top">
                        <c:url var="downloadLink" value="/sc?compID=${component.id}&mailingID=${form.mailingId}&customerID=${form.customerID}&targetGroupID=${form.targetGroupId}"/>
                        <a href="${downloadLink}" data-prevent-load="">
                            ${component.componentName}
                            <span class="badge"><i class="icon icon-download"></i></span>
                        </a>
                    </td>
                </tr>
            </c:if>
        </c:forEach>
    </table>
</div>
