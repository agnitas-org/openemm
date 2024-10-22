<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.mailing.enums.MailingRecipientType" %>
<%@ page import="org.agnitas.web.MailingRecipientsAdditionalColumn" %>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag" %>
<%@ taglib prefix="emm"        uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"        uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fmt"        uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c"          uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"         uri="http://java.sun.com/jsp/jstl/functions" %>

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
<%--@elvariable id="additionalColumns" type="java.util.Set<java.lang.String>"--%>

<c:set var="RECIPIENT_TYPES" value="<%= MailingRecipientType.values() %>"/>
<c:set var="OPT_OUT_TIME_FIELD" value="<%= MailingRecipientsAdditionalColumn.OPT_OUT_TIME %>"/>
<c:set var="BOUNCE_TIME_FIELD" value="<%= MailingRecipientsAdditionalColumn.BOUNCE_TIME %>"/>

<c:set var="recipientsLimitExceeded" value="${recipients.fullListSize > countOfRecipients}" />
<c:set var="tableSortable" value="${not recipientsLimitExceeded}" />
<mvc:message var="allMsg" code="default.All" />

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" servletRelativeAction="/mailing/${mailingId}/recipients/list.action" modelAttribute="form" method="GET" cssClass="tile"
              data-editable-tile="main" data-controller="mailing-recipients">
        <input type="hidden" name="loadRecipients" value="true">

        <c:if test="${not form.inEditColumnsMode}">
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "mailing-recipient-overview": {
                        "rows-count": ${form.numberOfRows},
                        "fields": ${emm:toJson(form.selectedFields)}
                    }
                }
            </script>
        </c:if>

        <div class="tile-body d-flex flex-column gap-3">
            <c:if test="${recipientsLimitExceeded}">
                <div class="notification-simple notification-simple--lg notification-simple--info">
                    <span><mvc:message code="recipient.search.max_recipients" arguments="${countOfRecipients}"/></span>
                </div>
            </c:if>

            <div class="table-wrapper ${deactivatePagination ? 'table-wrapper--no-pagination' : ''}" data-table-column-manager data-action="update-columns">
                <c:if test="${form.loadRecipients}">
                    <script type="application/json" data-table-column-manager-config>
                        {
                            "columns": [
                                <c:forEach var="field" items="${fieldsMap}" varStatus="loop_status">
                                    {
                                        "name": ${emm:toJson(field.key)},
                                        "text": ${emm:toJson(field.value)},
                                        "selected": ${form.isSelectedColumn(field.key)}
                                    }${loop_status.index + 1 lt fn:length(fieldsMap) ? ',' : ''}
                                </c:forEach>
                            ],
                            "editMode": ${form.inEditColumnsMode}
                        }
                    </script>
                </c:if>
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <c:if test="${form.loadRecipients}">
                        <div class="table-wrapper__controls">
                            <c:if test="${recipients.fullListSize gt 0}">
                                <%@include file="../../common/table/edit-columns-btn.jspf" %>
                                <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                            </c:if>
                            <jsp:include page="../../common/table/entries-label.jsp">
                                <jsp:param name="filteredEntries" value="${recipients.fullListSize}"/>
                                <jsp:param name="totalEntries" value="${recipients.notFilteredFullListSize}"/>
                            </jsp:include>
                        </div>
                    </c:if>
                </div>

                <div class="table-wrapper__body">
                    <agnDisplay:table class="table table--borderless js-table" id="recipient" name="${recipients}" partialList="true"
                                   size="${form.numberOfRows}" requestURI="/mailing/${mailingId}/recipients/list.action" sort="external" excludedParams="*">

                        <%@ include file="../../common/displaytag/displaytag-properties.jspf" %>

                        <c:if test="${not form.loadRecipients}">
                            <agnDisplay:setProperty name="basic.msg.empty_list_row" value=""/>
                        </c:if>

                        <agnDisplay:column titleKey="Firstname" sortProperty="firstname" sortable="${tableSortable}"
                                           headerClass="js-table-sort ${form.isSelectedColumn('firstname') ? '' : 'hidden'}" class="${form.isSelectedColumn('firstname') ? '' : 'hidden'}"
                                           data-table-column="firstname">
                            <span>${recipient.firstName}</span>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="Lastname" sortProperty="lastname" sortable="${tableSortable}"
                                           headerClass="js-table-sort ${form.isSelectedColumn('lastname') ? '' : 'hidden'}" class="${form.isSelectedColumn('lastname') ? '' : 'hidden'}"
                                           data-table-column="lastname">
                            <span>${recipient.lastName}</span>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="mailing.MediaType.0" sortProperty="email" sortable="${tableSortable}" headerClass="js-table-sort" data-table-column="">
                            <span>${recipient.email}</span>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="statistic.mailing.recipient.received" sortProperty="receive_time"
                                        sortable="${tableSortable}" headerClass="js-table-sort" data-table-column="">
                            <span><fmt:formatDate value="${recipient.receiveTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}"/></span>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="statistic.opened" sortProperty="open_time" sortable="${tableSortable}" headerClass="js-table-sort" data-table-column="">
                            <span><fmt:formatDate value="${recipient.openTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}"/></span>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="statistic.openings" sortProperty="openings" sortable="${tableSortable}" headerClass="js-table-sort" data-table-column="">
                            <span>${recipient.openingsCount}</span>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="default.clicked" sortProperty="click_time" sortable="${tableSortable}" headerClass="js-table-sort" data-table-column="">
                            <span><fmt:formatDate value="${recipient.clickTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}"/></span>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="statistic.Clicks" sortProperty="clicks" sortable="${tableSortable}" headerClass="js-table-sort" data-table-column="">
                            <span>${recipient.clicksCount}</span>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="recipient.MailingState2" sortProperty="bounce_time" sortable="${tableSortable}"
                                           headerClass="js-table-sort ${form.isSelectedColumn(BOUNCE_TIME_FIELD.name()) ? '' : 'hidden'}"
                                           class="${form.isSelectedColumn(BOUNCE_TIME_FIELD.name()) ? '' : 'hidden'}"
                                           data-table-column="${BOUNCE_TIME_FIELD.name()}">
                            <span><fmt:formatDate value="${recipient.bounceTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}"/></span>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="recipient.status.optout" sortProperty="optout_time" sortable="${tableSortable}"
                                           headerClass="js-table-sort ${form.isSelectedColumn(OPT_OUT_TIME_FIELD.name()) ? '' : 'hidden'}"
                                           class="${form.isSelectedColumn(OPT_OUT_TIME_FIELD.name()) ? '' : 'hidden'}"
                                           data-table-column="${OPT_OUT_TIME_FIELD.name()}">
                            <span><fmt:formatDate value="${recipient.unsubscribeTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}"/></span>
                        </agnDisplay:column>

                        <c:forEach var="field" items="${form.selectedFields}">
                            <c:if test="${not form.isDefaultColumn(field) and not fn:contains(additionalColumns, field)}">
                                <c:choose>
                                    <c:when test="${'gender'.equalsIgnoreCase(field)}">
                                        <agnDisplay:column titleKey="recipient.Salutation" sortProperty="gender" sortable="${tableSortable}" headerClass="js-table-sort" data-table-column="${field}">
                                            <span><mvc:message code="recipient.gender.${recipient.getVal(field)}.short"/></span>
                                        </agnDisplay:column>
                                    </c:when>

                                    <c:otherwise>
                                        <agnDisplay:column title="${fieldsMap.get(field)}" sortProperty="${field}" sortable="${tableSortable}" headerClass="js-table-sort" data-table-column="${field}">
                                            <span>${recipient.getVal(field)}</span>
                                        </agnDisplay:column>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </c:forEach>

                        <agnDisplay:column class="fit-content" headerClass="columns-picker fit-content"/>
                    </agnDisplay:table>
                </div>

                <c:if test="${not form.loadRecipients}">
                    <button class="btn btn-primary absolute-center" data-form-submit>
                        <i class="icon icon-sync"></i>
                        <span class="text"><mvc:message code="button.load.recipients"/></span>
                    </button>
                </c:if>
            </div>
            <c:if test="${not form.loadRecipients}">
                <div class="table-wrapper__footer"></div>
            </c:if>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/mailing/${mailingId}/recipients/search.action" modelAttribute="form"
              data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="mobile" data-editable-tile="">

        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-inverse" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
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
