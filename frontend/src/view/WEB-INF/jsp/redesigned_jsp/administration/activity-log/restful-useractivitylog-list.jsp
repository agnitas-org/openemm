<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="filter" type="com.agnitas.emm.core.useractivitylog.forms.RestfulUserActivityLogFilter"--%>
<%--@elvariable id="actions" type="org.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="admins" type="java.util.List"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="localeTableFormat" type="java.text.SimpleDateFormat"--%>
<%--@elvariable id="defaultDate" type="java.lang.String"--%>

<c:set var="NEWLINE" value="<%= '\n' %>"/>

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" servletRelativeAction="/administration/restful-user/activitylog/listRedesigned.action" modelAttribute="filter" cssClass="tile" data-editable-tile="main">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "restful-useractivitylog-overview": {
                    "rows-count": ${filter.numberOfRows}
                }
            }
        </script>

        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
        </div>
        <div class="tile-body">
            <div class="table-box">
                <div class="table-scrollable">
                    <display:table id="userAction" name="actions" class="table table-rounded js-table" sort="external"
                                   requestURI="/administration/restful-user/activitylog/listRedesigned.action" partialList="true"
                                   size="${filter.numberOfRows}" excludedParams="*">

                        <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                        <display:column titleKey="recipient.Timestamp" sortProperty="timestamp" sortable="true" headerClass="js-table-sort fit-content">
                            <emm:formatDate value="${userAction.timestamp}" format="${localeTableFormat}"/>
                        </display:column>

                        <display:column property="username" sortProperty="username" titleKey="logon.username" sortable="true" headerClass="js-table-sort fit-content"/>

                        <display:column property="endpoint" sortProperty="endpoint" titleKey="webservice.endpoint" sortable="true" headerClass="js-table-sort"/>

                        <display:column property="requestMethod" sortProperty="requestMethod" titleKey="webservice.method" sortable="true" headerClass="js-table-sort fit-content"/>

                        <display:column titleKey="Description" sortProperty="description" sortable="true" headerClass="js-table-sort">
                            ${fn:replace(fn:escapeXml(userAction.description), NEWLINE, '<br>')}
                        </display:column>
                    </display:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/administration/restful-user/activitylog/search.action" modelAttribute="filter"
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
                <div class="col-12" data-date-range>
                    <label class="form-label" for="upload-date-from"><mvc:message code="Date"/></label>
                    <div class="date-picker-container mb-1">
                        <c:set var="fromDate" value="${defaultDate}"/>
                        <c:if test="${not empty filter.timestamp.from}">
                            <emm:formatDate var="fromDate" value="${filter.timestamp.from}" format="${adminDateFormat}" />
                        </c:if>

                        <mvc:message var="fromMsg" code="From" />
                        <mvc:text id="filter-date-from" path="timestamp.from" placeholder="${fromMsg}" value="${fromDate}" cssClass="form-control js-datepicker" />
                    </div>
                    <div class="date-picker-container">
                        <c:set var="toDate" value="${defaultDate}"/>
                        <c:if test="${not empty filter.timestamp.to}">
                            <emm:formatDate var="toDate" value="${filter.timestamp.to}" format="${adminDateFormat}" />
                        </c:if>

                        <mvc:message var="toMsg" code="To" />
                        <mvc:text id="filter-date-to" path="timestamp.to" placeholder="${toMsg}" value="${toDate}" cssClass="form-control js-datepicker"/>
                    </div>
                </div>

                <div class="col-12">
                    <label class="form-label" for="filter-users"><mvc:message code="logon.username" /></label>
                    <mvc:select id="filter-users" path="username" cssClass="form-control" data-sort="alphabetic">
                        <mvc:option value="" data-no-sort=""><mvc:message code="UserActivitylog.All_Users"/></mvc:option>
                        <mvc:options items="${admins}" itemValue="username" itemLabel="username"/>
                    </mvc:select>
                </div>

                <div class="col-12">
                    <mvc:message var="requestUrlMsg" code="webservice.endpoint" />
                    <label class="form-label" for="filter-request-url">${requestUrlMsg}</label>
                    <mvc:text id="filter-request-url" path="requestUrl" placeholder="${requestUrlMsg}" cssClass="form-control"/>
                </div>

                <div class="col-12">
                    <label class="form-label" for="filter-request-method"><mvc:message code="webservice.method" /></label>
                    <mvc:select id="filter-request-method" path="requestMethod" cssClass="form-control" data-sort="alphabetic">
                        <mvc:option value="" data-no-sort=""><mvc:message code="default.All"/></mvc:option>
                        <c:forEach var="httpMethod" items="${httpMethods}">
                            <mvc:option value="${httpMethod.name()}">${httpMethod.name()}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <div class="col-12">
                    <mvc:message var="descriptionMsg" code="Description" />
                    <label class="form-label" for="filter-description">${descriptionMsg}</label>
                    <mvc:text id="filter-description" path="description" cssClass="form-control" placeholder="${descriptionMsg}"/>
                </div>
            </div>
        </div>
    </mvc:form>

</div>
