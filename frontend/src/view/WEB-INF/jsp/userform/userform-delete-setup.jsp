<%@ page language="java" import="org.agnitas.util.*, org.agnitas.web.*, java.util.*" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<emm:Permission token="forms.delete"/>

<c:set var="agnNavigationKey" 		value="formDelete" 						scope="request" />
<c:set var="agnTitleKey" 			value="Form" 							scope="request" />
<c:set var="agnSubtitleKey" 		value="Form" 							scope="request" />
<c:set var="agnSubtitleValue" 		value="${userFormEditForm.formName}"	scope="request" />
<c:set var="sidemenu_active" 		value="Forms" 							scope="request" />
<c:set var="sidemenu_sub_active"	value="workflow.panel.forms" 			scope="request" />
<c:set var="agnHighlightKey" 		value="settings.form.delete"			scope="request" />
<c:set var="agnHelpKey" 			value="formView" 						scope="request" />
