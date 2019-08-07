<%@ page import="com.agnitas.web.forms.ComBirtStatForm"  errorPage="/error.do" %>
<%@ page language="java"
         contentType="text/html; charset=utf-8" buffer="32kb" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:choose>
	<c:when test="${(empty plugins) || (fn:length(plugins) == 0) }">
		<bean:message key="statistic.NoIndividual" />
	</c:when>
	<c:otherwise>
		<ul>
			<c:forEach items="${plugins}" var="plugin">
				<li><a href="${plugin.url}">${plugin.title}</a></li>
			</c:forEach>
		</ul>
	</c:otherwise> 
</c:choose>
