<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="filter" type="com.agnitas.emm.core.useractivitylog.forms.McpUserActivityLogFilter"--%>
<%--@elvariable id="actions" type="com.agnitas.beans.PaginatedList"--%>
<%--@elvariable id="usernames" type="java.util.Set<java.lang.String>"--%>
<%--@elvariable id="logExpire" type="java.lang.Integer"--%>
<%--@elvariable id="mcpSources" type="java.util.List<java.lang.String>"--%>

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" servletRelativeAction="/administration/mcp/activitylog/list.action" modelAttribute="filter" cssClass="tile" data-editable-tile="main">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "mcp-useractivitylog-overview": {
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
                    <emm:table var="userAction" modelAttribute="actions" cssClass="table table--borderless js-table">
                        <emm:column titleKey="recipient.Timestamp" property="timestamp" sortable="true" headerClass="fit-content" />
                        <emm:column property="username" titleKey="logon.username" sortable="true" />
                        <emm:column property="toolName" sortProperty="tool_name" titleKey="GWUA.mcpTool" sortable="true" />
                        <emm:column property="source" titleKey="GWUA.mcpSource" sortable="true" />
                    </emm:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/administration/mcp/activitylog/search.action" modelAttribute="filter"
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

            <c:if test="${not empty usernames}">
                <div>
                    <label class="form-label" for="filter-users"><mvc:message code="logon.username" /></label>
                    <mvc:select id="filter-users" path="username" cssClass="form-control" data-sort="alphabetic">
                        <mvc:option value="" data-no-sort=""><mvc:message code="UserActivitylog.All_Users"/></mvc:option>
                        <c:forEach var="username" items="${usernames}">
                            <mvc:option value="${username}">${username}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </c:if>

            <div>
                <mvc:message var="mcpTool" code="GWUA.mcpTool" />
                <label class="form-label" for="filter-tool-name">${mcpTool}</label>
                <mvc:text id="filter-tool-name" path="toolName" placeholder="${mcpTool}" cssClass="form-control"/>
            </div>

            <div>
                <label class="form-label" for="filter-source"><mvc:message code="GWUA.mcpSource" /></label>
                <mvc:select id="filter-source" path="source" cssClass="form-control" data-sort="alphabetic">
                    <mvc:option value="" data-no-sort=""><mvc:message code="default.All"/></mvc:option>
                    <c:forEach var="mcpSource" items="${mcpSources}">
                        <mvc:option value="${mcpSource}">${mcpSource}</mvc:option>
                    </c:forEach>
                </mvc:select>
            </div>
        </div>
    </mvc:form>
</div>
