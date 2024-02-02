<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="downloadFileName" type="java.lang.String"--%>
<%--@elvariable id="exportedLines" type="java.lang.String"--%>
<%--@elvariable id="tmpFileName" type="java.lang.String"--%>
<%--@elvariable id="id" type="java.lang.Integer"--%>

<emm:CheckLogon/>
<emm:Permission token="wizard.export"/>

<div class="tile">
    <div class="tile-header">
        <h2 class="headline">
            <mvc:message code="export"/>
        </h2>
    </div>
    <div class="tile-content tile-content-forms">
        <div class="well block">
            <p><b><mvc:message code="export.data"/></b></p>
            <p>
                ${exportedLines} <mvc:message code="Recipients"/>
            </p>
        </div>

        <div class="vspacer-10"></div>
        <div class="well block">
            <mvc:message code="export.finished"/>
        </div>

        <c:if test="${not empty downloadFileName}">
            <div class="vspacer-10"></div>
            <div class="well block align-center">
                <a href="<c:url value="/export/${id}/download.action?tmpFileName=${tmpFileName}"/>" class="btn btn-regular btn-success" data-prevent-load="">
                    <i class="icon icon-download"></i>
                    <span class="text">
                        <mvc:message code="button.Download"/> ${downloadFileName}
                    </span>
                </a>
            </div>
        </c:if>
    </div>
</div>
