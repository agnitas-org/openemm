<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="logFileContent" type="java.lang.String"--%>

<div class="tiles-container">
    <div class="tile">
        <div class="tile-body js-scrollable">
            <pre>${fn:escapeXml(logFileContent)}</pre>
        </div>
    </div>
</div>
