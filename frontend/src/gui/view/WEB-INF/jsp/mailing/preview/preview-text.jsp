<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="previewContent" type="java.lang.String"--%>

<html>
<head><title><mvc:message code="default.Preview"/></title></head>
<body>
<pre>${previewContent}</pre>
</body>
</html>
