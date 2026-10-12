<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailinglistForm" type="com.agnitas.emm.core.mailinglist.form.MailinglistForm"--%>
<%--@elvariable id="isRestrictedForSomeAdmins" type="java.lang.Boolean"--%>
<%--@elvariable id="currentMonth" type="java.lang.Integer"--%>
<%--@elvariable id="currentYear" type="java.lang.Integer"--%>
<%--@elvariable id="monthList" type="java.util.List"--%>
<%--@elvariable id="yearlist" type="java.util.List"--%>

<c:set var="isNew" value="${mailinglistForm.id eq 0}"/>

<mvc:form id="mailinglist-form" cssClass="tiles-container flex-column" servletRelativeAction="/mailinglist/save.action" modelAttribute="mailinglistForm"
          data-form="resource"
          data-form-focus="shortname"
          data-controller="mailinglist-view"
          data-initializer="mailinglist-view"
          data-editable-view="${agnEditViewKey}">

    <mvc:hidden path="id"/>

    <script id="config:mailinglist-view" type="application/json">
        {
          "mailinglistId": ${id}
        }
    </script>

    <div id="settings-tile" class="tile h-auto flex-none" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="settings.EditMailinglist"/></h1>
            <div class="tile-controls">
                <%@include file="fragments/mailinglist-frequency-counter-toggle.jspf"%>

                <c:if test="${isRestrictedForSomeAdmins}">
                    <span class="icon-badge badge--dark-red" data-tooltip="<mvc:message code="mailinglist.limit.access" />">
                        <i class="icon icon-user-lock"></i>
                    </span>
                </c:if>
             </div>
        </div>
        <div class="tile-body row g-3 js-scrollable">
            <div class="col-6">
                <label for="shortname" class="form-label">
                    <mvc:message var="nameMsg" code="default.Name"/>
                    ${nameMsg}*
                </label>
                <mvc:text path="shortname" cssClass="form-control" id="shortname" placeholder="${nameMsg}"/>
            </div>

            <div class="col-6">
                <label for="description" class="form-label">
                    <mvc:message var="descriptionMsg" code="Description"/>
                    ${descriptionMsg}
                </label>
                <mvc:textarea path="description" id="description" cssClass="form-control" rows="1" placeholder="${descriptionMsg}"/>
            </div>

            <%@include file="fragments/domain-addresses-dropdown.jspf" %>

            <div class="col-6">
                <label for="description" class="form-label">
                    <mvc:message var="senderEmailMsg" code="mailing.SenderEmail"/>
                    ${senderEmailMsg}
                </label>
                <c:choose>
                    <c:when test="${domainAddressesDropdown eq null}">
                        <mvc:text path="senderEmail" cssClass="form-control" id="senderEmail" placeholder="${senderEmailMsg}"/>
                    </c:when>
                    <c:otherwise>
                        ${domainAddressesDropdown}
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="col-6">
                <label for="description" class="form-label">
                    <mvc:message var="replyToEmailMsg" code="mailing.ReplyEmail"/>
                    ${replyToEmailMsg}
                </label>
                <mvc:text path="replyEmail" cssClass="form-control" id="replyToEmail" placeholder="${replyToEmailMsg}"/>
            </div>

            <div class="col-6">
                <label for="sender-name" class="form-label">
                    <mvc:message var="senderFullnameMsg" code="mailing.SenderFullname"/>
                    ${senderFullnameMsg}
                </label>
                <mvc:text path="senderName" cssClass="form-control" id="sender-name" placeholder="${senderFullnameMsg}"/>
            </div>

            <div class="col-6">
                <label for="reply-to-name" class="form-label">
                    <mvc:message var="replyFullNameMsg" code="mailing.ReplyFullName"/>
                     ${replyFullNameMsg}
                </label>
                <mvc:text path="replyToName" cssClass="form-control" id="reply-to-name" placeholder="${replyFullNameMsg}"/>
            </div>

            <%@include file="fragments/approval-emails-select.jspf"%>

        </div>
    </div>
    <c:if test="${not isNew}">
        <div id="statistics-tile" class="tile" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="Statistics"/></h1>
            </div>
            <div class="tile-body js-scrollable" style="overflow-y: auto !important;">
                <div class="row g-3">
                    <div class="col-4">
                        <select id="statMoth" class="form-control">
                            <c:forEach var="month" items="${monthList}">
                                <option value="${month[0] + 1}" ${month[0] eq currentMonth - 1 ? 'selected' : ''}>
                                    <mvc:message code="${month[1]}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-4">
                        <select id="statYear" class="form-control">
                            <c:forEach items="${yearlist}" var="year">
                                <option value="${year}" ${year eq currentYear ? 'selected' : ''}>
                                    <c:out value="${year}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-4 d-flex">
                        <button class="btn btn-sm btn-primary flex-grow-1" type="button" data-action="reload-stats">
                            <i class="icon icon-sync"></i>
                            <span class="text"><mvc:message code="button.Refresh"/></span>
                        </button>
                    </div>
                </div>
                <div id="mailinglist-stats">
                    <div style="height: 300px"><canvas id="line-chart"></canvas></div>
                    <div id="period-table" style="height: auto" class="table-wrapper flex-grow-1"></div>
                </div>
            </div>
        </div>
    </c:if>
</mvc:form>
