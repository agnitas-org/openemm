<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="campaign.delete"/>

<c:set var="agnNavigationKey" 		value="campaignDelete" 							scope="request" />
<c:set var="agnNavHrefAppend" 		value="&campaignID=${campaignForm.campaignID}"	scope="request" />
<c:set var="agnTitleKey" 			value="mailing.archive" 						scope="request" />
<c:set var="agnSubtitleKey" 		value="mailing.archive" 						scope="request" />
<c:set var="agnSubtitleValue" 		value="${campaignForm.shortname}" 				scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 								scope="request" />
<c:set var="sidemenu_sub_active"	value="mailing.archive" 						scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.archive" 						scope="request" />
<c:set var="agnHelpKey" 			value="mailingArchive" 							scope="request" />


