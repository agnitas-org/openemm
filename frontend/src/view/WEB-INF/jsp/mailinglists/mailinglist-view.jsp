<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailinglistForm" type="com.agnitas.emm.core.mailinglist.form.MailinglistForm"--%>
<%--@elvariable id="birtStatisticUrlWithoutFormat" type="java.lang.String"--%>
<%--@elvariable id="monthList" type="java.util.List"--%>
<%--@elvariable id="yearlist" type="java.util.List"--%>

<c:set var="isNew" value="${mailinglistForm.id eq 0}"/>

<mvc:form servletRelativeAction="/mailinglist/save.action"
          modelAttribute="mailinglistForm"
          data-form="resource"
          data-form-focus="shortname">

    <mvc:hidden path="id"/>
    <mvc:hidden path="targetId"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="settings.EditMailinglist"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="shortname" class="control-label">
                        <mvc:message code="Name"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <c:set var="namePlaceholder">
                        <mvc:message code="Name"/>
                    </c:set>
                    <mvc:text path="shortname" cssClass="form-control" id="shortname" placeholder="${namePlaceholder}"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="description" class="control-label">
                        <mvc:message code="default.description" />
                    </label>
                </div>
                <div class="col-sm-8">
                    <c:set var="descriptionPlaceholder">
                        <mvc:message code="default.description"/>
                    </c:set>
                    <mvc:textarea path="description" id="description" cssClass="form-control" rows="5" placeholder="${descriptionPlaceholder}"/>
                </div>
            </div>
        </div>
    </div>
    <c:if test="${not isNew or not empty mailinglistForm.statistic}">
        <mvc:hidden path="statistic.mailinglistId"/>
        <div class="tile">
            <div class="tile-header">
                <label class="headline">
                    <mvc:message code="Statistics"/>
                </label>
                <ul class="tile-header-actions">
                    <li class="dropdown">
                        <a class="dropdown-toggle" href="#" data-toggle="dropdown">
                            <i class="icon icon-cloud-download"></i>
                            <span class="text"><mvc:message code="Export"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>
                        <ul class="dropdown-menu">
                            <li class="dropdown-header"><mvc:message code="statistics.exportFormats"/></li>
                            <li>
                                <a href="${birtStatisticUrlWithoutFormat}&__format=csv" tabindex="-1" data-prevent-load="">
                                    <i class="icon icon-file-excel-o"></i>
                                    <mvc:message code='export.message.csv'/>
                                </a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-controls">
                <div class="controls controls-left">
                    <div class="control">
                        <mvc:select path="statistic.startMonth" cssClass="form-control select2-offscreen">
                            <c:forEach items="${monthList}" var="month">
                                <mvc:option value="${month[0]}"><mvc:message code="${month[1]}"/></mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                    <div class="control">
                        <mvc:select path="statistic.startYear" cssClass="form-control select2-offscreen">
                            <c:forEach items="${yearlist}" var="year">
                                <mvc:option value="${year}"><c:out value="${year}"/></mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </div>

                <div class="controls controls-right">
                    <div class="control">
                        <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                            <i class="icon icon-refresh"></i>
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
