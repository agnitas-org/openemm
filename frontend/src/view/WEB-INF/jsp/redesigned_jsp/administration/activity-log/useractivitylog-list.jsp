<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="filter" type="com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilter"--%>
<%--@elvariable id="actions" type="com.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="admins" type="java.util.List"--%>
<%--@elvariable id="userActions" type="java.util.List"--%>
<%--@elvariable id="logExpire" type="java.lang.Integer"--%>
<%--@elvariable id="useractivitylog" type="com.agnitas.emm.core.useractivitylog.dao.LoggedUserAction"--%>

<c:set var="NEWLINE" value="<%= '\n' %>"/>

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" servletRelativeAction="/administration/useractivitylog/listRedesigned.action" modelAttribute="filter" cssClass="tile" data-editable-tile="main">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "useractivitylog-overview": {
                    "rows-count": ${filter.numberOfRows}
                }
            }
        </script>

        <div class="tile-body">
            <div class="table-wrapper">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp">
                            <jsp:param name="filteredEntries" value="${actions.fullListSize}"/>
                            <jsp:param name="totalEntries" value="${actions.notFilteredFullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table var="useractivitylog" modelAttribute="actions" cssClass="table table--borderless js-table">

                        <emm:column titleKey="Date" sortProperty="logtime" property="date" sortable="true" headerClass="fit-content" />

                        <emm:column property="shownName" sortProperty="username" titleKey="logon.username" sortable="true" headerClass="fit-content"/>

                        <emm:column property="action" titleKey="action.Action" sortable="true" headerClass="fit-content"/>

                        <emm:column titleKey="Description" sortProperty="description" sortable="true">
                            <span>${fn:replace(fn:escapeXml(useractivitylog.description), NEWLINE, '<br>')}</span>
                        </emm:column>
                    </emm:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/administration/useractivitylog/search.action" modelAttribute="filter"
              data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="" data-editable-tile="">
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-secondary" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body vstack gap-3 js-scrollable">
            <div>
                <label class="form-label" for="filter-date-from"><mvc:message code="Date"/></label>
                <mvc:dateRange id="filter-date" path="timestamp" options="minDate: '-${logExpire}', maxDate: 0" />
            </div>

            <div>
                <label class="form-label" for="filter-users"><mvc:message code="logon.username" /></label>
                <mvc:select id="filter-users" path="username" cssClass="form-control" data-sort="alphabetic">
                    <mvc:option value="0" data-no-sort=""><mvc:message code="UserActivitylog.All_Users"/></mvc:option>
                    <mvc:options items="${admins}" itemValue="username" itemLabel="username"/>
                </mvc:select>
            </div>

            <div>
                <label class="form-label" for="filter-action"><mvc:message code="action.Action" /></label>
                <mvc:select id="filter-action" path="action" cssClass="form-control">
                    <c:forEach var="action" items="${userActions}">
                        <mvc:option value="${action.intValue}">
                            <mvc:message code="${action.publicValue}"/>
                        </mvc:option>
                    </c:forEach>
                </mvc:select>
            </div>

            <div>
                <mvc:message var="descriptionMsg" code="Description" />
                <label class="form-label" for="filter-description">${descriptionMsg}</label>
                <mvc:text id="filter-description" path="description" cssClass="form-control" placeholder="${descriptionMsg}"/>
            </div>
        </div>
    </mvc:form>
</div>
