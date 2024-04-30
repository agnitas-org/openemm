<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="recipient.show"/>

<c:set var="agnNavigationKey" 		value="blacklist" 			scope="request" />
<c:set var="agnTitleKey" 			value="recipient.Blacklist" scope="request" />
<c:set var="agnSubtitleKey" 		value="recipient.Blacklist" scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="none"				scope="request" />
<c:set var="agnHighlightKey" 		value="recipient.Blacklist" scope="request" />
<c:set var="agnHelpKey" 			value="blacklist" 			scope="request" />
