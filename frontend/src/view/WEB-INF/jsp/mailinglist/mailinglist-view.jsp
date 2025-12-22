<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailinglistForm" type="com.agnitas.emm.core.mailinglist.form.MailinglistForm"--%>
<%--@elvariable id="birtStatisticUrlWithoutFormat" type="java.lang.String"--%>
<%--@elvariable id="isRestrictedForSomeAdmins" type="java.lang.Boolean"--%>
<%--@elvariable id="monthList" type="java.util.List"--%>
<%--@elvariable id="yearlist" type="java.util.List"--%>

<c:set var="isNew" value="${mailinglistForm.id eq 0}"/>

<mvc:form id="mailinglist-form" cssClass="tiles-container flex-column" servletRelativeAction="/mailinglist/save.action" modelAttribute="mailinglistForm"
          data-form="resource"
          data-form-focus="shortname" data-editable-view="${agnEditViewKey}">

    <mvc:hidden path="id"/>
    <mvc:hidden path="targetId"/>
    <mvc:hidden path="mediatypes"/>

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
        <div class="tile-body js-scrollable">
            <div class="row g-3">
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

                <div class="col-6">
                    <label for="description" class="form-label">
                        <mvc:message var="senderEmailMsg" code="mailing.SenderEmail"/>
                        ${senderEmailMsg}
                    </label>
                    <%@include file="fragments/domain-addresses-dropdown.jspf" %>
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
            </div>
        </div>
    </div>
    <c:if test="${not isNew or not empty mailinglistForm.statistic}">
        <mvc:hidden path="statistic.mailinglistId"/>
        <div id="statistics-tile" class="tile" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="Statistics"/></h1>
            </div>
            <div class="tile-body js-scrollable" style="overflow-y: auto !important;">
                <div class="row g-3">
                    <div class="col-4">
                        <mvc:select path="statistic.startMonth" cssClass="form-control">
                            <c:forEach items="${monthList}" var="month">
                                <mvc:option value="${month[0]}"><mvc:message code="${month[1]}"/></mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                    <div class="col-4">
                        <mvc:select path="statistic.startYear" cssClass="form-control">
                            <c:forEach items="${yearlist}" var="year">
                                <mvc:option value="${year}"><c:out value="${year}"/></mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                    <div class="col-4 d-flex">
                        <button class="btn btn-sm btn-primary flex-grow-1" type="button" data-form-submit>
                            <i class="icon icon-sync"></i>
                            <span class="text"><mvc:message code="button.Refresh"/></span>
                        </button>
                    </div>
                </div>
                <iframe id="birt-frame" src="${birtStatisticUrlWithoutFormat}&__format=html" border="0" scrolling="auto" width="100%" frameborder="0">
                    Your Browser does not support IFRAMEs, please update!
                </iframe>
            </div>
        </div>
    </c:if>
</mvc:form>
