<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.SalutationForm"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<emm:Permission token="salutation.delete"/>

<c:set var="agnNavigationKey" 		value="Salutation" 										scope="request" />
<c:set var="agnNavHrefAppend" 		value="&salutationID=${salutationForm.salutationID}"	scope="request" />
<c:set var="agnTitleKey" 			value="settings.FormsOfAddress" 						scope="request" />
<c:set var="agnSubtitleKey" 		value="settings.FormsOfAddress" 						scope="request" />
<c:set var="agnSubtitleValue" 		value="${salutationForm.shortname}" 					scope="request" />
<c:set var="sidemenu_active" 		value="Mailings"						 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.FormsOfAddress" 						scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 								scope="request" />
<c:set var="agnHelpKey" 			value="salutationForms" 								scope="request" />
