<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="birtReportUrl" type="java.lang.String"--%>
<%--@elvariable id="birtExportReportUrl" type="java.lang.String"--%>

<div class="tiles-container">
    <mvc:form cssClass="tile" servletRelativeAction="/statistics/mailing/comparison/compare.action" modelAttribute="form"
              data-form="resource"
              data-resource-selector="#maings-comparison-content">
        <mvc:hidden path="bulkIds"/>

        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview"/></h1>
            <div class="tile-controls">
                <button class="btn btn-sm btn-primary" type="button" data-form-submit>
                    <i class="icon icon-sync"></i><span class="text"><mvc:message code="button.Refresh"/></span>
                </button>
            </div>
        </div>
        <div class="tile-body overflow-auto p-0 js-scrollable">
            <iframe src="${birtReportUrl}" border="0" scrolling="auto" frameborder="0" style="width: 100%">
                Your Browser does not support IFRAMEs, please update!
            </iframe>
        </div>
    </mvc:form>
</div>
