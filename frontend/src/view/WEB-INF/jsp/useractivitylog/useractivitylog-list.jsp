<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>

<%@ taglib prefix="agn"     uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html"    uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="userActivityLogForm" type="com.agnitas.emm.core.useractivitylog.forms.UserActivityLogForm"--%>
<%--@elvariable id="userActivityLogKey" type="java.util.List"--%>
<%--@elvariable id="admins" type="java.util.List"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="localeTableFormat" type="java.text.SimpleDateFormat"--%>
<%--@elvariable id="userActions" type="java.util.List"--%>
<%--@elvariable id="defaultDate" type="java.lang.String"--%>

<c:set var="NEWLINE" value="<%= '\n' %>"/>

<mvc:form servletRelativeAction="/administration/useractivitylog/list.action"
          id="userActivityLogListForm"
          modelAttribute="userActivityLogForm"
          data-form="search">

    <mvc:hidden path="page"/>
    <mvc:hidden path="dir"/>
    <mvc:hidden path="order"/>
    <mvc:hidden path="sort"/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "useractivitylog-overview": {
                "rows-count": ${userActivityLogForm.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <a class="headline" href="#" data-toggle-tile="#tile-basicSearch">
                <i class="icon tile-toggle icon-angle-up"></i>
                <bean:message key="Search"/>
            </a>
            <ul class="tile-header-actions">
                <li>
                    <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                        <i class="icon icon-search"></i>
                        <span class="text"><bean:message key="Search"/></span>
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
                                <bean:message key="action.Action"/>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <mvc:select path="userAction" id="search_action" cssClass="form-control js-select">
                                <c:forEach var="action" items="${userActions}">
                                    <mvc:option value="${action.intValue}">
                                        <bean:message key="${action.publicValue}"/>
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
                                <bean:message key="UserActivitylog.Users"/>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <mvc:select path="username" id="userFilter" cssClass="form-control js-select">
                                <mvc:option value="0">
                                    <bean:message key="UserActivitylog.All_Users"/>
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
                                <bean:message key="UserActivitylog.FromDate"/>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <mvc:text path="dateFrom.date" id="search_dateFrom"
                                              cssClass="form-control datepicker-input js-datepicker js-datepicker-period-start"
                                              value="${not empty userActivityLogForm.dateFrom.date ? userActivityLogForm.dateFrom.date : defaultDate}"
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
                                <bean:message key="UserActivitylog.ToDate"/>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <mvc:text path="dateTo.date" id="search_dateTo"
                                              cssClass="form-control datepicker-input js-datepicker js-datepicker-period-end"
                                              value="${not empty userActivityLogForm.dateTo.date ? userActivityLogForm.dateTo.date : defaultDate}"
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
                                <bean:message key="default.search.description"/>
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
            <h2 class="headline"><bean:message key="default.Overview"/></h2>

            <ul class="tile-header-actions">
                <li>

                    <c:url value="/administration/useractivitylog/download.action" var="userActivityLogDownloadURL"/>
                    <a href="#"
                       data-form-url="${userActivityLogDownloadURL}"
                       data-form-submit-static=""
                       data-tooltip="<bean:message key='export.message.csv'/>"
                       class="link"
                       data-prevent-load>
                        <i class="icon icon-cloud-download"></i>
                        <bean:message key="Export"/>
                    </a>
                </li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><bean:message key="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><bean:message key="listSize"/></li>
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
                                    <span class="text"><bean:message key="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content" data-form-content>
            <div class="table-wrapper">
                <display:table id="useractivitylog"
                               class="table table-bordered table-striped js-table"
                               sort="external"
                               requestURI="/administration/useractivitylog/list.action"
                               list="${userActivityLogKey}"
                               pagesize="${userActivityLogForm.numberOfRows gt 0 ? userActivityLogForm.numberOfRows : 0}"
                               excludedParams="*">
                    <display:setProperty name="basic.empty.showtable" value="false"/>
                    <display:setProperty name="basic.msg.empty_list_row" value=" "/>

                    <display:column titleKey="Date"
                                    sortProperty="logtime"
                                    sortable="true">
                        <span><emm:formatDate value="${useractivitylog.date}" format="${localeTableFormat}"/></span>
                    </display:column>

                    <display:column property="shownName"
                                    sortProperty="firstname"
                                    titleKey="logon.username"
                                    sortable="true"/>

                    <display:column property="action"
                                    sortProperty="action"
                                    titleKey="action.Action"
                                    sortable="true"/>

                    <display:column titleKey="Description"
                                    sortProperty="description"
                                    sortable="true">
                        <span class="multiline-auto">
                                ${fn:replace(fn:escapeXml(useractivitylog.description), NEWLINE, '<br>')}
                        </span>
                    </display:column>

                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
