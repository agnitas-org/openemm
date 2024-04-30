<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.preview.form.PreviewForm"--%>

<html>
<head><title><mvc:message code="default.Preview"/></title></head>
<body>
<pre>${form.previewContent}</pre>
</body>
</html>
