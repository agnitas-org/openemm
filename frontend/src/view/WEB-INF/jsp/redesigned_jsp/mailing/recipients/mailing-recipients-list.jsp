<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.mailing.enums.MailingRecipientType" %>

<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="tiles"   uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fmt"     uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="fieldsMap" type="java.util.Map"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="deactivatePagination" type="java.lang.Boolean"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.mailing.forms.MailingRecipientsOverviewFilter"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="recipient" type="com.agnitas.emm.core.mailing.bean.MailingRecipientStatRow"--%>
<%--@elvariable id="countOfRecipients" type="java.lang.Integer"--%>
<%--@elvariable id="recipients" type="org.agnitas.beans.impl.PaginatedListImpl<com.agnitas.emm.core.mailing.bean.MailingRecipientStatRow>"--%>

<c:set var="RECIPIENT_TYPES" value="<%= MailingRecipientType.values() %>"/>

<c:set var="recipientsLimitExceeded" value="${recipients.fullListSize > countOfRecipients}" />
<mvc:message var="allMsg" code="default.All" />

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" servletRelativeAction="/mailing/${mailingId}/recipients/list.action" modelAttribute="form" method="GET" cssClass="tile" data-editable-tile="main">
        <input type="hidden" name="loadRecipients" value="true">

        <script type="application/json" data-initializer="web-storage-persist">
            {
                "mailing-recipient-overview": {
                    "rows-count": ${form.numberOfRows},
                    "fields": ${emm:toJson(form.selectedFields)}
                }
            }
        </script>

        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
        </div>

        <c:set var="addAdditionalColumns">
            <div class="dropdown table-header-dropdown">
                <i class="icon icon-plus" role="button" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false"></i>

                <ul class="dropdown-menu">
                    <div class="d-flex flex-column gap-2">
                        <mvc:select path="selectedFields" cssClass="form-control dropdown-select" multiple="multiple">
                            <c:forEach var="field" items="${fieldsMap}">
                                <c:set var="column" value="${field.key}"/>
                                <c:set var="fieldName" value="${field.value}"/>

                                <c:set var="isDefaultField" value="${form.isDefaultColumn(column)}"/>
                                <c:set var="fieldSelected" value="${form.isSelectedColumn(column)}"/>

                                <c:if test="${isDefaultField}">
                                    <option value="${column}" disabled>${fieldName}</option>
                                </c:if>
                                <c:if test="${not isDefaultField}">
                                    <option value="${column}" ${fieldSelected ? 'selected' : ''}>${fieldName}</option>
                                </c:if>
                            </c:forEach>
                        </mvc:select>

                        <button class="btn btn-primary js-dropdown-close" type="button" data-form-change data-form-submit>
                            <i class="icon icon-sync"></i>
                            <span class="text"><mvc:message code="button.Refresh"/></span>
                        </button>
                    </div>
                </ul>
            </div>
        </c:set>

        <div class="tile-body d-flex flex-column gap-3">
            <c:if test="${recipientsLimitExceeded}">
                <div class="notification-simple notification-simple--lg notification-simple--info">
                    <span><mvc:message code="recipient.search.max_recipients" arguments="${countOfRecipients}"/></span>
                </div>
            </c:if>

            <div class="table-box ${deactivatePagination ? 'hide-pagination' : ''}">
                <div class="table-scrollable">
                    <display:table class="table table-rounded js-table" id="recipient" name="${recipients}" partialList="true"
                                   size="${form.numberOfRows}" requestURI="/mailing/${mailingId}/recipients/list.action"
                                   sort="external" excludedParams="*">

                        <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                        <c:if test="${not form.loadRecipients}">
                            <display:setProperty name="basic.msg.empty_list_row" value=""/>
                        </c:if>

                        <display:column titleKey="Firstname" property="firstName" sortProperty="firstname"
                                        sortable="${not recipientsLimitExceeded}" headerClass="js-table-sort"/>

                        <display:column titleKey="Lastname" property="lastName" sortProperty="lastname"
                                        sortable="${not recipientsLimitExceeded}" headerClass="js-table-sort"/>

                        <display:column titleKey="mailing.MediaType.0" property="email" sortProperty="email"
                                        sortable="${not recipientsLimitExceeded}" headerClass="js-table-sort"/>

                        <display:column titleKey="statistic.mailing.recipient.received" sortProperty="receive_time"
                                        sortable="${not recipientsLimitExceeded}" headerClass="js-table-sort">
                            <fmt:formatDate value="${recipient.receiveTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}"/>
                        </display:column>

                        <display:column titleKey="statistic.opened" sortProperty="open_time"
                                        sortable="${not recipientsLimitExceeded}" headerClass="js-table-sort">
                            <fmt:formatDate value="${recipient.openTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}"/>
                        </display:column>

                        <display:column titleKey="statistic.openings" property="openingsCount" sortProperty="openings"
                                        sortable="${not recipientsLimitExceeded}" headerClass="js-table-sort"/>

                        <display:column titleKey="default.clicked" sortProperty="click_time"
                                        sortable="${not recipientsLimitExceeded}" headerClass="js-table-sort">
                            <fmt:formatDate value="${recipient.clickTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}"/>
                        </display:column>

                        <display:column titleKey="statistic.Clicks" property="clicksCount" sortProperty="clicks"
                                        sortable="${not recipientsLimitExceeded}" headerClass="js-table-sort"/>

                        <display:column titleKey="recipient.MailingState2" sortProperty="bounce_time"
                                        sortable="${not recipientsLimitExceeded}" headerClass="js-table-sort">
                            <fmt:formatDate value="${recipient.bounceTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}"/>
                        </display:column>

                        <display:column titleKey="recipient.status.optout" sortProperty="optout_time"
                                        sortable="${not recipientsLimitExceeded}" headerClass="js-table-sort">
                            <fmt:formatDate value="${recipient.unsubscribeTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}"/>
                        </display:column>

                        <c:forEach var="field" items="${form.selectedFields}">
                            <c:choose>
                                <c:when test="${'gender'.equalsIgnoreCase(field)}">
                                    <display:column titleKey="recipient.Salutation" sortProperty="gender" sortable="${not recipientsLimitExceeded}"
                                                    class="recipient_title" headerClass="js-table-sort">
                                        <mvc:message code="recipient.gender.${recipient.getVal(field)}.short"/>
                                    </display:column>
                                </c:when>
                                <c:otherwise>
                                    <display:column title="${fieldsMap.get(field)}" sortProperty="${field}" sortable="${not recipientsLimitExceeded}"
                                                    class="recipient_title" headerClass="js-table-sort">
                                        ${recipient.getVal(field)}
                                    </display:column>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>

                        <display:column class="fit-content" title="${addAdditionalColumns}" headerClass="additional-columns"/>
                    </display:table>
                </div>

                <c:if test="${not form.loadRecipients}">
                    <button class="btn btn-primary absolute-center" data-form-submit>
                        <i class="icon icon-sync"></i>
                        <span class="text"><mvc:message code="button.load.recipients"/></span>
                    </button>
                </c:if>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/mailing/${mailingId}/recipients/search.action" modelAttribute="form"
              data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="mobile" data-editable-tile="">

        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up desktop-hidden"></i><mvc:message code="report.mailing.filter"/>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-icon-sm btn-inverse" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
                <a class="btn btn-icon btn-icon-sm btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <label for="filter-types" class="form-label"><mvc:message code="default.Type" /></label>
                    <mvc:select id="filter-types" path="types" cssClass="form-control" placeholder="${allMsg}">
                        <c:forEach var="recipientType" items="${RECIPIENT_TYPES}">
                            <mvc:option value="${recipientType}">${recipientType}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
                <div class="col-12">
                    <mvc:message var="firstnameMsg" code="Firstname" />
                    <label for="filter-firstname" class="form-label">${firstnameMsg}</label>
                    <mvc:text id="filter-firstname" path="firstname" cssClass="form-control" placeholder="${firstnameMsg}"/>
                </div>
                <div class="col-12">
                    <mvc:message var="lastnameMsg" code="Lastname" />
                    <label for="filter-lastname" class="form-label">${lastnameMsg}</label>
                    <mvc:text id="filter-lastname" path="lastname" cssClass="form-control" placeholder="${lastnameMsg}"/>
                </div>
                <div class="col-12">
                    <label for="filter-email" class="form-label"><mvc:message code="mailing.MediaType.0" /></label>
                    <mvc:text id="filter-email" path="email" cssClass="form-control" placeholder="${emailPlaceholder}"/>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
