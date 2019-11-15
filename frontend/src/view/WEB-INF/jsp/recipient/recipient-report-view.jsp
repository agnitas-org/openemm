<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="DATE_PATTERN" value="yyyy-MM-dd"/>
<c:set var="NO_RESULTS_FOUND_MSG"><bean:message key="noResultsFound"/></c:set>

<%--@elvariable id="recipientsReportForm" type="com.agnitas.emm.core.recipientsreport.web.RecipientsReportForm"--%>

<html:form action="/recipientsreport">
    <div class="tile">
    	<iframe class="js-simple-iframe" style="width: 100%; height: 750px; border: 0px;" srcdoc="${reportContentEscaped}"></iframe>
    </div>
</html:form>
