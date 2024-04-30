<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<emm:CheckLogon/>

<emm:Permission token="wizard.import"/>

<c:set var="agnNavigationKey" 		value="none" 						scope="request" />
<c:set var="agnTitleKey" 			value="import.UploadSubscribers" 	scope="request" />
<c:set var="agnSubtitleKey"	 		value="import.UploadSubscribers"	scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 				scope="request" />
<c:set var="sidemenu_sub_active"	value="import.csv_upload" 			scope="request" />
<c:set var="agnHighlightKey" 		value="import.Wizard" 				scope="request" />
