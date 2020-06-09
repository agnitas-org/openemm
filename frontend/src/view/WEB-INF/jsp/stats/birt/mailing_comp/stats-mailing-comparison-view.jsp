<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.do" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="targetGroupList" type="java.util.List"--%>
<%--@elvariable id="birtReportUrl" type="java.lang.String"--%>
<%--@elvariable id="birtExportReportUrl" type="java.lang.String"--%>

<mvc:form servletRelativeAction="/statistics/mailing/comparison/compare.action" modelAttribute="form"
          data-form="resource"
          data-resource-selector="#maings-comparison-content">

    <mvc:hidden path="bulkIds"/>

    <div class="tile">
        <div class="tile-header">
            <a class="headline" href="#" data-toggle-tile="#tile-targetGroup">
                <i class="icon tile-toggle icon-angle-up"></i>
                <mvc:message code="Targets"/>
            </a>

            <ul class="tile-header-actions">
                <li>
                    <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                        <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Refresh"/></span>
                    </button>
                </li>
            </ul>
        </div>

        <div id="tile-targetGroup" class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <mvc:message code="Targetgroups"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:message var="placeholderTargetGroupSelect" code="addTargetGroup"/>
                    <mvc:select path="targetIds" cssClass="form-control js-select" multiple="true"
                                data-placeholder="${placeholderTargetGroupSelect}">
                        <mvc:options items="${targetGroupList}" itemValue="id" itemLabel="targetName"/>
                    </mvc:select>
                </div>
            </div>
        </div>
    </div>

    <div class="tile" id="maings-comparison-content">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="default.Overview"/></h2>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-cloud-download"></i>
                        <span class="text"><mvc:message code="Export"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header">
                            <mvc:message code="statistics.exportFormats"/>
                        </li>
                        <li>
                            <a href="${birtExportReportUrl}" tabindex="-1" data-prevent-load="">
                                <i class="icon icon-file-excel-o"></i>
                                <mvc:message code="user.export.csv"/>
                            </a>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content">
            <div class="content_element_container">
                <iframe src="${birtReportUrl}" border="0" scrolling="auto" frameborder="0" style="width: 100%">
                    Your Browser does not support IFRAMEs, please update!
                </iframe>
            </div>
        </div>
    </div>
</mvc:form>
