<%@ page language="java" contentType="text/html; charset=utf-8" import="com.agnitas.web.ComUserFormEditAction, org.agnitas.web.UserFormEditAction"  errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" value="<%= ComUserFormEditAction.ACTION_LIST %>" scope="request" />

<emm:CheckLogon/>

<emm:Permission token="forms.show"/>

<c:set var="agnNavigationKey" 		value="formDelete" 				scope="request" />
<c:set var="agnTitleKey" 			value="workflow.panel.forms" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="workflow.panel.forms" 	scope="request" />
<c:set var="sidemenu_active" 		value="Forms"		 			scope="request" />
<c:set var="sidemenu_sub_active"	value="workflow.panel.forms" 	scope="request" />
<c:set var="agnHighlightKey" 		value="settings.form.delete"	scope="request" />
<c:set var="agnHelpKey" 			value="formList" 				scope="request" />
