<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action"%>
<%@ page import="com.agnitas.emm.core.birtstatistics.DateMode" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="statUrl" type="java.lang.String"--%>
<%--@elvariable id="webFormStatFrom" type="com.agnitas.emm.core.userform.form.WebFormStatFrom"--%>
<%--@elvariable id="userForms" type="java.util.List<com.agnitas.userform.bean.UserForm>"--%>
<%--@elvariable id="years" type="java.util.List<java.lang.Integer>"--%>
<%--@elvariable id="months" type="java.util.List<java.lang.Integer>"--%>

<mvc:form cssClass="tiles-container flex-column" id="stat-form" method="GET" servletRelativeAction="/webform/statistic.action"
          data-form="resource" modelAttribute="webFormStatFrom" data-editable-view="${agnEditViewKey}">

    <mvc:hidden path="allowedToChoseForm"/>

    <div id="filter-tile" class="tile h-auto flex-none" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="report.mailing.filter" /></h1>
        </div>

        <div class="tile-body">
            <div class="row g-3">
                <c:choose>
                    <c:when test="${not empty userForms}">
                        <div class="col">
                            <label class="form-label" for="form-select"><mvc:message code="Form"/></label>
                            <mvc:select path="formId" id="form-select" size="1" cssClass="form-control js-select">
                                <mvc:options items="${userForms}" itemValue="id" itemLabel="formName"/>
                            </mvc:select>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <mvc:hidden path="formId"/>
                    </c:otherwise>
                </c:choose>

                <div class="col">
                    <label class="form-label" for="period-mode-select"><mvc:message code="Time"/></label>
                    <mvc:select id="period-mode-select" path="periodMode" cssClass="form-control js-select">
                        <mvc:option value="${DateMode.SELECT_MONTH}"><mvc:message code="Month"/></mvc:option>
                        <mvc:option value="${DateMode.SELECT_YEAR}"><mvc:message code="Year"/></mvc:option>
                        <mvc:option value="${DateMode.SELECT_PERIOD}"><mvc:message code="statistics.dateRange"/></mvc:option>
                    </mvc:select>
                </div>

                <div class="col" data-show-by-select="#period-mode-select" data-show-by-select-values="${DateMode.SELECT_MONTH}">
                    <label class="form-label" for="start-date"><mvc:message code="Month"/></label>
                    <mvc:select path="month" cssClass="form-control js-select">
                        <c:forEach var="month" begin="1" end="12">
                            <mvc:option value="${month}"><mvc:message code="calendar.month.${month}"/></mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <div class="col" data-show-by-select="#period-mode-select" data-show-by-select-values="${DateMode.SELECT_MONTH},${DateMode.SELECT_YEAR}">
                    <label class="form-label" for="start-date"><mvc:message code="Year"/></label>
                    <mvc:select path="year" cssClass="form-control js-select">
                        <mvc:options items="${years}" />
                    </mvc:select>
                </div>

                <div class="col" data-date-range data-show-by-select="#period-mode-select" data-show-by-select-values="${DateMode.SELECT_PERIOD}">
                    <div class="row">
                        <div class="col">
                            <label class="form-label" for="start-date"><mvc:message code="From"/></label>
                            <div class="date-picker-container">
                                <mvc:text path="dateRange.from" id="start-date" cssClass="form-control js-datepicker"
                                          data-field="required" data-field-options="ignoreHidden: true"/>
                            </div>
                        </div>
                        <div class="col">
                            <label class="form-label" for="end-date"><mvc:message code="default.to"/></label>
                            <div class="date-picker-container">
                                <mvc:text path="dateRange.to" id="end-date" cssClass="form-control js-datepicker"
                                          data-field="required" data-field-options="ignoreHidden: true"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="overview-tile" class="tile" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="Statistics"/></h1>
        </div>
        <div class="tile-body js-scrollable" style="overflow-y: auto !important;">
            <iframe src="${statUrl}&__format=html" style="width: 100%; height: 100%">
                Your Browser does not support IFRAMEs, please update!
            </iframe>
        </div>
    </div>
</mvc:form>
