<%@ page import="com.agnitas.emm.core.mailing.forms.ComMailingParameterForm"  errorPage="/error.do" %>
<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<c:set var="agnNavigationKey" 		value="MailingParameter" 	scope="request" />
<c:set var="agnTitleKey" 			value="MailingParameter" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="MailingParameter" 	scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 			scope="request" />
<c:set var="sidemenu_sub_active"	value="MailingParameter" 	scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 	scope="request" />
<c:set var="agnHelpKey" 			value="mailingParameter" 	scope="request" />
