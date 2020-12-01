<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.trackablelinks.form.FormTrackableLinkForm"--%>
<%--@elvariable id="userFormId" type="java.lang.Integer"--%>
<%--@elvariable id="defaultExtensions" type="java.util.List"--%>
<%--@elvariable id="userFormName" type="java.lang.String"--%>

<c:set var="agnNavigationKey" 		value="formViewWithLinks" 							scope="request" />
<c:set var="agnTitleKey" 			value="Form" 										scope="request" />
<c:set var="agnSubtitleKey" 		value="Form" 										scope="request" />
<c:set var="sidemenu_active" 		value="Forms" 										scope="request" />
<c:set var="sidemenu_sub_active" 	value="workflow.panel.forms" 						scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.Trackable_Links" 					scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 										scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Forms" 										scope="request" />
<c:set var="agnHelpKey" 			value="trackableLinkView" 							scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="user-form-id" value="${userFormId}"/>
</emm:instantiate>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/webform/list.action"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${userFormId eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="New_Form"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${userFormName}"/>
                <c:set target="${agnBreadcrumb}" property="url">
                    <c:url value="/webform/${userFormId}/view.action"/>
                </c:set>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Trackable_Links"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/webform/${userFormId}/trackablelink/list.action"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="3" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${not empty form.name}">
                <c:set target="${agnBreadcrumb}" property="text" value="${form.name}"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="textKey" value="Unknown"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>


<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

            <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element}" property="extraAttributes" value="data-form-target='#userFormTrackableLinkForm' data-form-submit-event=''"/>
            <c:set target="${element}" property="iconBefore" value="icon-save"/>
            <c:set target="${element}" property="name">
                <bean:message key="button.Save"/>
            </c:set>
        </emm:instantiate>
</emm:instantiate>
