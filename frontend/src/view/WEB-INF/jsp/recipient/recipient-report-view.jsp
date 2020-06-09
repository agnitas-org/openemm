<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="reportId" type="java.lang.Integer"--%>
<%--@elvariable id="reportContentEscaped" type="java.lang.String"--%>

<c:url var="view_url" value="/recipientsreport/${reportId}/view.action"/>
<mvc:form action="${view_url}" method="GET">
    <div class="tile">
        <iframe class="js-simple-iframe" style="width: 100%; height: 750px; border: 0px;" srcdoc="${reportContentEscaped}"></iframe>
    </div>
</mvc:form>

