<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.forms.ExportWizardForm"  errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="export.delete"/>
<c:set var="agnNavigationKey" 		value="subscriber_export" 				scope="request" />
<c:set var="agnTitleKey" 			value="export" 							scope="request" />
<c:set var="agnSubtitleKey" 		value="export" 							scope="request" />
<c:set var="agnSubtitleValue" 		value="${exportWizardForm.shortname}"	scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 					scope="request" />
<c:set var="sidemenu_sub_active"	value="export" 							scope="request" />
<c:set var="agnHighlightKey" 		value="export"		 					scope="request" />
<c:set var="agnHelpKey" 			value="export" 							scope="request" />
