<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.ImportProfileAction"  errorPage="/error.do" %>
<%@ page import="org.agnitas.web.ProfileImportAction" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>>

<c:set var="agnNavigationKey" 		value="ImportProfile" 			scope="request" />
<c:set var="agnTitleKey" 			value="import.ImportProfile"	scope="request" />
<c:set var="agnSubtitleKey" 		value="import.ImportProfile" 	scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 			scope="request" />
<c:set var="sidemenu_sub_active"	value="import.csv_upload" 		scope="request" />
<c:set var="agnHelpKey" 			value="manageProfile" 			scope="request" />

<c:choose>
    <c:when test="${fromListPage}">
		<c:set var="agnHighlightKey" value="import.ProfileAdministration" scope="request" />
	</c:when>
    <c:otherwise>
		<c:set var="agnNavHrefAppend"	value="&profileId=${profileId}"	scope="request" />
		<c:set var="agnHighlightKey" 	value="import.ImportProfile" 	scope="request" />
 	</c:otherwise>
</c:choose>
