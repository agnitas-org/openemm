<%@ page import="com.agnitas.web.ComUserFormEditAction" %>
<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" value="<%= ComUserFormEditAction.ACTION_LIST %>"/>
<c:set var="ACTION_VIEW" value="<%= ComUserFormEditAction.ACTION_VIEW %>"/>

<emm:CheckLogon/>
<emm:Permission token="forms.show"/>

<c:set var="agnNavigationKey" 		value="formViewWithLinks" 							scope="request" />
<c:set var="agnNavHrefAppend" 		value="&formID=${trackableUserFormLinkForm.formID}" scope="request" />
<c:set var="agnTitleKey" 			value="Form" 										scope="request" />
<c:set var="agnSubtitleKey" 		value="Form" 										scope="request" />
<c:set var="sidemenu_active" 		value="SiteActions" 								scope="request" />
<c:set var="sidemenu_sub_active" 	value="workflow.panel.forms" 										scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.Trackable_Links" 					scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 										scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="SiteActions" 								scope="request" />
<c:set var="agnHelpKey" 			value="trackableLinkView" 							scope="request" />

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
            <c:when test="${trackableUserFormLinkForm.formID eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="New_Form"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${trackableUserFormLinkForm.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url">
                    <c:url value="/userform.do">
                        <c:param name="action" value="${ACTION_VIEW}"/>
                        <c:param name="formID" value="${trackableUserFormLinkForm.formID}"/>
                    </c:url>
                </c:set>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Trackable_Links"/>
    </emm:instantiate>
</emm:instantiate>
