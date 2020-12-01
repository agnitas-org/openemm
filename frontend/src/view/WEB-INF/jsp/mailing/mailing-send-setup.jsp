<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.beans.Mailing" %>
<%@ page import="com.agnitas.emm.core.report.enums.fields.MailingTypes" %>
<%@ page import="org.agnitas.web.MailingBaseAction" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<emm:CheckLogon/>
<emm:Permission token="mailing.send.show"/>

<c:set var="BASE_ACTION_LIST" value="<%= MailingBaseAction.ACTION_LIST %>"/>
<c:set var="BASE_ACTION_VIEW" value="<%= MailingBaseAction.ACTION_VIEW %>" />

<c:set var="TYPE_NORMAL" value="<%= MailingTypes.NORMAL.getCode() %>" scope="request"/>
<c:set var="TYPE_FOLLOWUP" value="<%= MailingTypes.FOLLOW_UP.getCode() %>" scope="request"/>

<c:set var="isMailingGrid" value="${mailingSendForm.isMailingGrid}"/>
<c:set var="mailingId" value="${mailingSendForm.mailingID}" scope="request"/>
<c:set var="gridTemplateId" value="0" scope="page"/>
<c:if test="${templateId gt 0}">
    <c:set var="gridTemplateId" value="${templateId}" scope="page"/>
</c:if>

<c:url var="mailingsOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_LIST}"/>
    <c:param name="isTemplate" value="false"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="templatesOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_LIST}"/>
    <c:param name="isTemplate" value="true"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="mailingViewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_VIEW}"/>
    <c:param name="mailingID" value="${mailingId}"/>
    <c:param name="keepForward" value="true"/>
    <c:param name="init" value="true"/>
</c:url>

<c:url var="templateViewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_VIEW}"/>
    <c:param name="mailingID" value="${mailingId}"/>
</c:url>

<c:set var="sidemenu_active" 		value="Mailings"	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 		scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 	scope="request" />
<c:set var="agnHelpKey" 			value="preview" 	scope="request" />

<logic:equal name="mailingSendForm" property="isTemplate" value="true">
    <c:set var="agnNavigationKey" 		value="templateView" 						scope="request" />
    <c:set var="agnNavHrefAppend" 		value="&mailingID=${mailingId}&init=true"	scope="request" />
    <c:set var="agnTitleKey" 			value="Template" 							scope="request" />
    <c:set var="agnSubtitleKey" 		value="Template" 							scope="request" />
    <c:set var="sidemenu_sub_active"	value="Templates" 							scope="request" />
    <c:set var="agnHighlightKey" 		value="template.testing" 					scope="request" />
</logic:equal>

<logic:equal name="mailingSendForm" property="isTemplate" value="false">
    <c:choose>
        <c:when test="${isMailingGrid}">
            <c:set var="isTabsMenuShown" 		value="false" 															scope="request" />

            <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

            <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                <c:set target="${agnNavHrefParams}" property="templateID" value="${gridTemplateId}"/>
                <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
            </emm:instantiate>

            <c:set var="agnTitleKey" 			value="Mailing" 														scope="request" />
            <c:set var="agnSubtitleKey" 		value="Mailing" 														scope="request" />
            <c:set var="sidemenu_sub_active"	value="none" 															scope="request" />
            <c:set var="agnHighlightKey" 		value="Send_Mailing" 													scope="request" />
            <c:set var="agnHelpKey" 			value="mailingsCheck" 										            scope="request" />
        </c:when>
        <c:otherwise>
            <c:choose>
	            <c:when test="${isPostMailing}">
	                <c:set var="agnNavigationKey" value="mailingView_post" scope="request" />
	            </c:when>
                <c:when test="${limitedRecipientOverview}">
                    <c:set var="agnNavigationKey" 		value="mailingView_DisabledMailinglist"     scope="request" />
                </c:when>
                <c:otherwise>
                    <c:set var="agnNavigationKey" 		value="mailingView"                         scope="request" />
                </c:otherwise>
            </c:choose>
            <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
                <c:set target="${agnNavHrefParams}" property="init" value="true"/>
            </emm:instantiate>
            <c:set var="agnTitleKey" 			value="Mailing" 							scope="request" />
            <c:set var="agnSubtitleKey" 		value="Mailing" 							scope="request" />
            <c:set var="agnHighlightKey" 		value="Send_Mailing" 						scope="request" />
            <c:set var="sidemenu_sub_active"	value="none" 								scope="request" />
            <c:set var="agnHelpKey" 			value="mailingsCheck" 			            scope="request" />
        </c:otherwise>
    </c:choose>
</logic:equal>


<c:choose>
    <c:when test="${mailingSendForm.isTemplate}">
        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="Templates"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templatesOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingSendForm.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templateViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="template.testing"/>
            </emm:instantiate>
        </emm:instantiate>
        <c:set var="agnHelpKey" value="mailingsCheck" scope="request" />
    </c:when>
    <c:otherwise>
        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingSendForm.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="Send_Mailing"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>

    <jsp:include page="actions-dropdown.jsp">
        <jsp:param name="elementIndex" value="0"/>
        <jsp:param name="mailingId" value="${mailingSendForm.mailingID}"/>
        <jsp:param name="isTemplate" value="${mailingSendForm.isTemplate}"/>
        <jsp:param name="workflowId" value="${mailingSendForm.workflowId}"/>
        <jsp:param name="isMailingUndoAvailable" value="${mailingSendForm.isMailingUndoAvailable}"/>
    </jsp:include>

    <c:if test="${not mailingSendForm.isTemplate and not mailingSendForm.isMailingGrid}">
        <c:if test="${mailingSendForm.mailingtype eq TYPE_NORMAL || mailingSendForm.mailingtype eq TYPE_FOLLOWUP}">
            <%-- View dropdown --%>

            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

                <c:set target="${element}" property="btnCls" value="btn btn-secondary btn-regular dropdown-toggle"/>
                <c:set target="${element}" property="extraAttributes" value="data-toggle='dropdown'"/>
                <c:set target="${element}" property="iconBefore" value="icon-eye"/>
                <c:set target="${element}" property="name">
                    <bean:message key="default.View" />
                </c:set>
                <c:set target="${element}" property="iconAfter" value="icon-caret-down"/>

                <emm:instantiate var="optionList01" type="java.util.LinkedHashMap">
                    <c:set target="${element}" property="dropDownItems" value="${optionList01}"/>
                </emm:instantiate>

                <%-- Add dropdown items (view modes) --%>

                <emm:instantiate var="option3" type="java.util.LinkedHashMap">
                    <c:set target="${optionList01}" property="3" value="${option3}"/>

                    <c:set target="${option3}" property="type" value="radio"/>
                    <c:set target="${option3}" property="radioName" value="view-state"/>
                    <c:set target="${option3}" property="radioValue" value="block"/>
                    <c:set target="${option3}" property="extraAttributes" value="data-view='mailingSend'"/>
                    <c:set target="${option3}" property="name">
                        <bean:message key="mailing.content.blockview"/>
                    </c:set>
                </emm:instantiate>

                <emm:instantiate var="option4" type="java.util.LinkedHashMap">
                    <c:set target="${optionList01}" property="4" value="${option4}"/>

                    <c:set target="${option4}" property="type" value="radio"/>
                    <c:set target="${option4}" property="radioName" value="view-state"/>
                    <c:set target="${option4}" property="radioValue" value="split"/>
                    <c:set target="${option4}" property="extraAttributes" value="checked data-view='mailingSend'"/>
                    <c:set target="${option4}" property="name">
                        <bean:message key="mailing.content.splitview"/>
                    </c:set>
                </emm:instantiate>

            </emm:instantiate>
        </c:if>
    </c:if>
</emm:instantiate>
