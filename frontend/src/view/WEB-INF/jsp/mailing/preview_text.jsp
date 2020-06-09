<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.util.*" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<html>
<head><title><bean:message key="default.Preview"/></title></head>
<body>
<%@include file="/WEB-INF/jsp/messages.jsp" %>

<pre><bean:write name="mailingSendForm" property="preview" filter="false"/></pre>
</body>
</html>
