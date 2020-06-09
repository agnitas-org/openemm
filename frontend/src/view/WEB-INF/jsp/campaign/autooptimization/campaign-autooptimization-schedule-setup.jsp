<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.emm.core.commons.util.*" buffer="64kb" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://ajaxanywhere.sourceforge.net/" prefix="aa"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon />

<emm:Permission token="campaign.autoopt" />
<c:set var="agnNavigationKey" 		value="Campaign" 																										scope="request" />
<c:set var="agnNavHrefAppend" 		value="&campaignID=${optimizationScheduleForm.campaignID}&optimizationID=${optimizationScheduleForm.optimizationID}"	scope="request" />
<c:set var="agnTitleKey" 			value="mailing.archive" 																								scope="request" />
<c:set var="agnSubtitleKey" 		value="mailing.autooptimization.new" 																					scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 																										scope="request" />
<c:set var="sidemenu_sub_active"	value="mailing.archive" 																								scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.autooptimization.send" 																					scope="request" />
<c:set var="agnHelpKey" 			value="autooptimization" 																								scope="request"/>
