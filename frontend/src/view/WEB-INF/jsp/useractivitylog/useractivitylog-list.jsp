<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>

<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.useractivitylog.forms.UserActivityLogForm"--%>
<%--@elvariable id="actions" type="org.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="admins" type="java.util.List"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="localeTableFormat" type="java.text.SimpleDateFormat"--%>
<%--@elvariable id="userActions" type="java.util.List"--%>
<%--@elvariable id="defaultDate" type="java.lang.String"--%>

<c:set var="NEWLINE" value="<%= '\n' %>"/>

<mvc:form servletRelativeAction="/administration/useractivitylog/list.action" modelAttribute="form" data-form="resource">

    <input type="hidden" name="page" value="${actions.pageNumber}"/>
    <input type="hidden" name="sort" value="${actions.sortCriterion}"/>
    <input type="hidden" name="dir" value="${actions.sortDirection}"/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "useractivitylog-overview": {
                "rows-count": ${form.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <a class="headline" href="#" data-toggle-tile="#tile-basicSearch">
                <i class="icon tile-toggle icon-angle-up"></i>
                <mvc:message code="Search"/>
            </a>
            <ul class="tile-header-actions">
                <li>
                    <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                        <i class="icon icon-search"></i>
                        <span class="text"><mvc:message code="Search"/></span>
                    </button>
                </li>
            </ul>
        </div>

        <div id="tile-basicSearch" class="tile-content tile-content-forms" data-initializer="user-activity-log-period">
            <div class="row">
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label class="control-label-left" for="search_action">
                                <mvc:message code="action.Action"/>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <mvc:select path="userAction" id="search_action" cssClass="form-control js-select">
                                <c:forEach var="action" items="${userActions}">
                                    <mvc:option value="${action.intValue}">
                                        <mvc:message code="${action.publicValue}"/>
                                    </mvc:option>
                                </c:forEach>
                            </mvc:select>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label class="control-label-left" for="userFilter">
                                <mvc:message code="UserActivitylog.Users"/>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <mvc:select path="username" id="userFilter" cssClass="form-control js-select">
                                <mvc:option value="0">
                                    <mvc:message code="UserActivitylog.All_Users"/>
                                </mvc:option>

                                <mvc:options items="${admins}" itemValue="username" itemLabel="username"/>
                            </mvc:select>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label class="control-label-left" for="search_dateFrom">
                                <mvc:message code="UserActivitylog.FromDate"/>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <mvc:text path="dateFrom.date" id="search_dateFrom"
                                              cssClass="form-control datepicker-input js-datepicker js-datepicker-period-start"
                                              value="${not empty form.dateFrom.date ? form.dateFrom.date : defaultDate}"
                                              datepicker-period-id="0"
                                              data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}', formatSubmit: '${fn:toLowerCase(adminDateFormat)}'"/>
                                </div>
                                <div class="input-group-btn">
                                    <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                        <i class="icon icon-calendar-o"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label class="control-label-left" for="search_dateTo">
                                <mvc:message code="UserActivitylog.ToDate"/>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <mvc:text path="dateTo.date" id="search_dateTo"
                                              cssClass="form-control datepicker-input js-datepicker js-datepicker-period-end"
                                              value="${not empty form.dateTo.date ? form.dateTo.date : defaultDate}"
                                              datepicker-period-id="0"
                                              data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}', formatSubmit: '${fn:toLowerCase(adminDateFormat)}'"/>
                                </div>
                                <div class="input-group-btn">
                                    <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                        <i class="icon icon-calendar-o"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-6">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label class="control-label-left" for="description">
                                <mvc:message code="default.search.description"/>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <mvc:text id="description" path="description" cssClass="form-control"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="default.Overview"/></h2>

            <ul class="tile-header-actions">
                <li>

                    <c:url value="/administration/useractivitylog/download.action" var="userActivityLogDownloadURL"/>
                    <a href="#"
                       data-form-url="${userActivityLogDownloadURL}"
                       data-form-submit-static=""
                       data-tooltip="<mvc:message code='export.message.csv'/>"
                       class="link"
                       data-prevent-load>
                        <i class="icon icon-cloud-download"></i>
                        <mvc:message code="Export"/>
                    </a>
                </li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><mvc:message code="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="listSize"/></li>
                        <li>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change
                                        data-form-submit>
                                    <i class="icon icon-refresh"></i>
                                    <span class="text"><mvc:message code="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content">
            <div class="table-wrapper">
                <display:table id="useractivitylog"
                               name="actions"
                               class="table table-bordered table-striped js-table"
                               sort="external"
                               requestURI="/administration/useractivitylog/list.action"
                               partialList="true"
                               size="${form.numberOfRows}"
                               excludedParams="*">

                    <%--@elvariable id="useractivitylog" type="org.agnitas.emm.core.useractivitylog.LoggedUserAction"--%>

                    <display:setProperty name="basic.empty.showtable" value="false"/>
                    <display:setProperty name="basic.msg.empty_list_row" value=" "/>

                    <display:column titleKey="Date"
                                    sortProperty="logtime"
                                    sortable="true"
                                    headerClass="js-table-sort">
                        <span><emm:formatDate value="${useractivitylog.date}" format="${localeTableFormat}"/></span>
                    </display:column>

                    <display:column property="shownName"
                                    sortProperty="firstname"
                                    titleKey="logon.username"
                                    sortable="true"
                                    headerClass="js-table-sort"/>

                    <display:column property="action"
                                    sortProperty="action"
                                    titleKey="action.Action"
                                    sortable="true"
                                    headerClass="js-table-sort"/>

                    <display:column titleKey="Description"
                                    sortProperty="description"
                                    sortable="true"
                                    headerClass="js-table-sort">
                        <span class="multiline-auto">
                                ${fn:replace(fn:escapeXml(useractivitylog.description), NEWLINE, '<br>')}
                        </span>
                    </display:column>

                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
