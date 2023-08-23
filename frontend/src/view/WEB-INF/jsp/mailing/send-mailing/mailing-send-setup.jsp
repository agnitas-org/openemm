<%@page import="com.agnitas.emm.common.MailingType"%>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="http://www.springframework.org/tags" %>

<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="templateId" type="java.lang.Integer"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<c:set var="TYPE_NORMAL" value="<%= MailingType.NORMAL.getCode() %>" scope="request"/>
<c:set var="TYPE_FOLLOWUP" value="<%= MailingType.FOLLOW_UP.getCode() %>" scope="request"/>
<c:set var="TYPE_INTERVAL" value="<%= MailingType.INTERVAL.getCode() %>" scope="request"/>

<c:set var="isMailingGrid" value="${form.isMailingGrid}"/>
<c:set var="mailingId" value="${form.mailingID}" scope="request"/>
<c:set var="gridTemplateId" value="0" scope="page"/>
<c:if test="${templateId gt 0}">
    <c:set var="gridTemplateId" value="${templateId}" scope="page"/>
</c:if>

<c:url var="mailingsOverviewLink" value="/mailing/list.action"/>

<c:url var="templatesOverviewLink" value="/mailing/list.action">
    <c:param name="forTemplates" value="true"/>
</c:url>

<c:url var="mailingViewLink" value="/mailing/${mailingId}/settings.action">
    <c:param name="keepForward" value="true"/>
</c:url>

<c:url var="templateViewLink" value="/mailing/${mailingId}/settings.action"/>

<c:set var="sidemenu_active" 		value="Mailings"	            scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 		            scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 	            scope="request" />
<c:set var="agnBreadcrumbsRootUrl"	value="${mailingsOverviewLink}"	scope="request" />
<c:set var="agnHelpKey" 			value="preview" 	            scope="request" />

<c:choose>
    <c:when test="${form.isTemplate}">
        <c:set var="agnNavigationKey" 		value="templateView" 	   scope="request" />
        <c:set var="agnTitleKey" 			value="Template" 		   scope="request" />
        <c:set var="agnSubtitleKey" 		value="Template" 		   scope="request" />
        <c:set var="sidemenu_sub_active"	value="Templates" 		   scope="request" />
        <c:set var="agnHighlightKey" 		value="template.testing"   scope="request" />

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${form.mailingID}"/>
            <c:set target="${agnNavHrefParams}" property="init" value="true"/>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${isMailingGrid}">
                <c:set var="isTabsMenuShown" value="false" scope="request" />

                <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

                <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                    <c:set target="${agnNavHrefParams}" property="templateID" value="${gridTemplateId}"/>
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
                </emm:instantiate>

            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${isPostMailing}">
                        <c:set var="agnNavigationKey" value="mailingView_post" scope="request" />
                    </c:when>
                    <c:when test="${limitedRecipientOverview}">
                        <c:set var="agnNavigationKey" value="mailingView_DisabledMailinglist" scope="request" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="agnNavigationKey" value="mailingView" scope="request" />
                    </c:otherwise>
                </c:choose>

                <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
                    <c:set target="${agnNavHrefParams}" property="init" value="true"/>
                </emm:instantiate>
            </c:otherwise>
        </c:choose>

        <c:set var="agnTitleKey" 		  value="Mailing" 	      scope="request" />
        <c:set var="agnSubtitleKey" 	  value="Mailing" 	      scope="request" />
        <c:set var="agnHighlightKey" 	  value="Send_Mailing"    scope="request" />
        <c:set var="sidemenu_sub_active"  value="none" 		      scope="request" />
        <c:set var="agnHelpKey" 		  value="mailingsCheck"   scope="request" />
    </c:otherwise>
</c:choose>


<c:choose>
    <c:when test="${form.isTemplate}">
        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="Templates"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templatesOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${form.shortname}"/>
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
                <c:set target="${agnBreadcrumb}" property="text" value="${form.shortname}"/>
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

    <jsp:include page="/WEB-INF/jsp/mailing/mailing-actions-dropdown.jsp">
        <jsp:param name="elementIndex" value="0"/>
        <jsp:param name="mailingId" value="${form.mailingID}"/>
        <jsp:param name="isTemplate" value="${form.isTemplate}"/>
        <jsp:param name="workflowId" value="${form.workflowId}"/>
        <jsp:param name="isMailingUndoAvailable" value="${form.isMailingUndoAvailable}"/>
    </jsp:include>

    <c:if test="${not form.isTemplate and not form.isMailingGrid}">
        <c:if test="${form.mailingtype eq TYPE_NORMAL || form.mailingtype eq TYPE_FOLLOWUP}">
            <%-- View dropdown --%>

            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

                <c:set target="${element}" property="btnCls" value="btn btn-secondary btn-regular dropdown-toggle"/>
                <c:set target="${element}" property="extraAttributes" value="data-toggle='dropdown'"/>
                <c:set target="${element}" property="iconBefore" value="icon-eye"/>
                <c:set target="${element}" property="name">
                    <mvc:message code="default.View" />
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
                        <mvc:message code="mailing.content.blockview"/>
                    </c:set>
                </emm:instantiate>

                <emm:instantiate var="option4" type="java.util.LinkedHashMap">
                    <c:set target="${optionList01}" property="4" value="${option4}"/>

                    <c:set target="${option4}" property="type" value="radio"/>
                    <c:set target="${option4}" property="radioName" value="view-state"/>
                    <c:set target="${option4}" property="radioValue" value="split"/>
                    <c:set target="${option4}" property="extraAttributes" value="checked data-view='mailingSend'"/>
                    <c:set target="${option4}" property="name">
                        <mvc:message code="mailing.content.splitview"/>
                    </c:set>
                </emm:instantiate>

            </emm:instantiate>
        </c:if>
    </c:if>
</emm:instantiate>
