<%@page import="org.agnitas.emm.core.commons.util.ConfigValue"%>
<%@page import="org.agnitas.emm.core.commons.util.ConfigService"%>
<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="org.agnitas.util.AgnUtils"%>
<%@ page import="com.agnitas.web.ComUserFormEditAction" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" value="<%= ComUserFormEditAction.ACTION_LIST %>"/>
<c:set var="ACTION_VIEW" value="<%= ComUserFormEditAction.ACTION_VIEW %>"/>

<emm:CheckLogon/>

<c:set var="birturl" value='<%= ConfigService.getInstance().getValue(ConfigValue.BirtUrl) %>' scope="request"/>

<c:set var="agnNavigationKey" 		value="formViewWithLinks" 								scope="request" />
<c:set var="agnNavHrefAppend" 		value="&formID=${trackableUserFormLinkStatForm.formID}" scope="request" />
<c:set var="agnTitleKey" 			value="Form" 											scope="request" />
<c:set var="agnSubtitleKey" 		value="Form" 											scope="request" />
<c:set var="sidemenu_active" 		value="Forms" 											scope="request" />
<c:set var="sidemenu_sub_active" 	value="workflow.panel.forms" 							scope="request" />
<c:set var="agnHighlightKey" 		value="Statistics" 										scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 											scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Forms" 											scope="request" />
<c:set var="agnHelpKey" 			value="formStatistic" 									scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="workflow.panel.forms"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/userform.do">
                <c:param name="action" value="${ACTION_LIST}"/>
            </c:url>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${trackableUserFormLinkStatForm.formID eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="New_Form"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${userForm.formName}"/>
                <c:set target="${agnBreadcrumb}" property="url">
                    <c:url value="/userform.do">
                        <c:param name="action" value="${ACTION_VIEW}"/>
                        <c:param name="formID" value="${trackableUserFormLinkStatForm.formID}"/>
                    </c:url>
                </c:set>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Statistics"/>
    </emm:instantiate>
</emm:instantiate>
