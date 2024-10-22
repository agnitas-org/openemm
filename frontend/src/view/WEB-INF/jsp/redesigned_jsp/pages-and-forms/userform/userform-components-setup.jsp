<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="formId" type="java.lang.Integer"--%>
<%--@elvariable id="userFormInfo" type="java.lang.String"--%>

<c:set var="agnNavigationKey" 		value="formViewWithLinks" 				scope="request" />
<c:set var="agnTitleKey" 			value="workflow.panel.forms" 			scope="request" />
<c:set var="sidemenu_active" 		value="Forms" 							scope="request" />
<c:set var="sidemenu_sub_active" 	value="Forms" 			                scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.Graphics_Components" 	scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Forms" 							scope="request" />
<c:set var="agnHelpKey" 			value="formImages" 						scope="request" />
<c:set var="agnEditViewKey" 	    value="userform-images" 	            scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="user-form-id" value="${formId}"/>
</emm:instantiate>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${formId eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="New_Form"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${userFormInfo}"/>
                <c:set target="${agnBreadcrumb}" property="url">
                    <c:url value="/webform/${formId}/components/list.action"/>
                </c:set>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

        <c:set target="${element}" property="cls" value="mobile-hidden"/>
        <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
        <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

        <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
            <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
        </emm:instantiate>

        <c:url var="allDownloadUrl" value="/webform/${formId}/components/all/download.action"/>
        <emm:instantiate var="option" type="java.util.LinkedHashMap">
            <c:set target="${optionList}" property="1" value="${option}"/>
            <c:set target="${option}" property="extraAttributes" value="data-form-url='${allDownloadUrl}' data-prevent-load data-form-submit-static data-form-target='#filter-tile'"/>
            <c:set target="${option}" property="name">
                <mvc:message code="mailing.Graphics_Component.bulk.download" />
            </c:set>
        </emm:instantiate>

    </emm:instantiate>
</emm:instantiate>
