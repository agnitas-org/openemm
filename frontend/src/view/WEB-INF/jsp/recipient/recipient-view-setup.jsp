<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComRecipientForm" %>
<%@ page import="org.agnitas.web.RecipientAction" %>
<%@ page import="com.agnitas.web.ComRecipientAction" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="recipient.show"/>

<c:set var="ACTION_LIST" 			value="<%= RecipientAction.ACTION_LIST %>"/>
<c:set var="ACTION_OVERVIEW_START" 	value="<%= ComRecipientAction.ACTION_OVERVIEW_START %>"/>
<c:set var="ACTION_CONFIRM_DELETE" 	value="<%= RecipientAction.ACTION_CONFIRM_DELETE %>"/>

<c:url var="recipientsOverviewLink" value="/recipient.do">
    <c:param name="action" value="${ACTION_OVERVIEW_START}"/>
    <c:param name="trgt_clear" value="1"/>
    <c:param name="overview" value="true"/>
</c:url>

<c:set var="recipientExists" value="${recipientForm.recipientID != 0}"/>

<c:set var="reportTooltipMessage" scope="request">
    <bean:message key="recipient.report.rightOfAccess.mouseover"/>
</c:set>

<c:set var="agnTitleKey" 			value="Recipient" 									scope="request" />
<c:set var="agnSubtitleKey" 		value="Recipients" 									scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 									scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.search" 								scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 										scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Recipients" 									scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="recipientID" value="${recipientForm.recipientID}"/>
</emm:instantiate>

<c:choose>
    <c:when test="${recipientExists}">
        <c:set var="agnHighlightKey" 	value="recipient.RecipientEdit" scope="request" />
		<c:set var="agnHelpKey" 		value="recipientView" 			scope="request" />
		<c:choose>
		    <c:when test="${mailtracking}">
				<c:set var="agnNavigationKey" value="subscriber_editor_mailtracking" scope="request" />
		    </c:when>
		    <c:otherwise>
				<c:set var="agnNavigationKey" value="subscriber_editor_no_mailtracking" scope="request" />
		    </c:otherwise>
		</c:choose>
        
        <c:choose>
            <c:when test="${not empty recipientForm.firstname and not empty recipientForm.lastname}">
                <c:set var="recipientMention" value="${recipientForm.firstname} ${recipientForm.lastname}"/>
            </c:when>
            <c:when test="${not empty recipientForm.firstname}">
                <c:set var="recipientMention" value="${recipientForm.firstname}"/>
            </c:when>
            <c:when test="${not empty recipientForm.lastname}">
                <c:set var="recipientMention" value="${recipientForm.lastname}"/>
            </c:when>
            <c:otherwise>
                <c:set var="recipientMention" value="${recipientForm.email}"/>
            </c:otherwise>
        </c:choose>

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="default.search"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${recipientsOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${recipientMention}"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" 	value="none" 					scope="request" />
        <c:set var="agnHighlightKey" 	value="recipient.NewRecipient" 	scope="request" />
        <c:set var="agnHelpKey" 		value="newRecipient" 			scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="recipient.NewRecipient"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
    <jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
        <c:set target="${element0}" property="btnCls" value="btn btn-secondary btn-regular dropdown-toggle"/>
        <c:set target="${element0}" property="extraAttributes" value="data-toggle='dropdown'"/>
        <c:set target="${element0}" property="iconBefore" value="icon-wrench"/>
        <c:set target="${element0}" property="name"><bean:message key="action.Action"/></c:set>
        <c:set target="${element0}" property="iconAfter" value="icon-caret-down"/>
        <jsp:useBean id="optionList" class="java.util.LinkedHashMap" scope="request">
            <c:set target="${element0}" property="dropDownItems" value="${optionList}"/>
        </jsp:useBean>

        <c:if test="${recipientExists}">
            <jsp:useBean id="option0" class="java.util.LinkedHashMap" scope="request">
                <c:set target="${optionList}" property="0" value="${option0}"/>
                <c:set target="${option0}" property="icon" value="icon-bar-chart-o"/>
                <c:set target="${option0}" property="extraAttributes"
                       value="data-tooltip-help data-tooltip-help-text='${reportTooltipMessage}'"/>
                <c:set target="${option0}" property="url">
                    <%-- todo: change to report url --%>
                    <c:url value="/report/recipients.action">
                        <c:param name="id" value="${recipientForm.recipientID}"/>
                    </c:url>
                </c:set>
                <c:set target="${option0}" property="name">
                    <bean:message key="recipient.report.rightOfAccess"/>
                </c:set>
            </jsp:useBean>
        </c:if>

        <jsp:useBean id="option1" class="java.util.LinkedHashMap" scope="request">
            <c:set target="${optionList}" property="1" value="${option1}"/>

            <c:set target="${option1}" property="icon" value="icon-times"/>
            <c:set target="${option1}" property="url">
                <c:url value="/recipient.do?">
                    <c:param name="action" value="${ACTION_LIST}"/>
                    <c:param name="overview" value="true"/>
                    <c:param name="user_type" value="${recipientForm.user_type}"/>
                    <c:param name="user_status" value="${recipientForm.user_status}"/>
                    <c:param name="listID" value="${recipientForm.listID}"/>
                    <c:param name="targetID" value="${recipientForm.targetID}"/>
                </c:url>
            </c:set>
            <c:set target="${option1}" property="name">
                <bean:message key="button.Cancel"/>
            </c:set>
        </jsp:useBean>

        <c:if test="${recipientExists}">
            <emm:ShowByPermission token="recipient.delete">
                    <jsp:useBean id="option2" class="java.util.LinkedHashMap" scope="request">
                        <c:set target="${optionList}" property="2" value="${option2}"/>
                        <c:set target="${option2}" property="extraAttributes" value="data-form-confirm='${ACTION_CONFIRM_DELETE}' data-form-target='#recipientForm'"/>
                        <c:set target="${option2}" property="url">
                            <html:rewrite page="#"/>
                        </c:set>
                        <c:set target="${option2}" property="icon" value="icon-trash-o"/>
                        <c:set target="${option2}" property="name">
                            <bean:message key="button.Delete"/>
                        </c:set>
                    </jsp:useBean>
            </emm:ShowByPermission>
        </c:if>
    </jsp:useBean>


    <c:if test="${recipientExists}">
        <emm:ShowByPermission token="recipient.change">
            <jsp:useBean id="element1" class="java.util.LinkedHashMap" scope="request">
                <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
                <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-inverse"/>
                <c:set target="${element1}" property="extraAttributes" value="data-form-set='save: save' data-form-target='#recipientForm' data-form-submit-event"/>
                <c:set target="${element1}" property="iconBefore" value="icon-save"/>
                <c:set target="${element1}" property="name">
                    <bean:message key="button.Save"/>
                </c:set>
            </jsp:useBean>
        </emm:ShowByPermission>
    </c:if>
    <c:if test="${not recipientExists}">
        <emm:ShowByPermission token="recipient.create">
            <jsp:useBean id="element2" class="java.util.LinkedHashMap" scope="request">
                <c:set target="${itemActionsSettings}" property="2" value="${element2}"/>
                <c:set target="${element2}" property="btnCls" value="btn btn-regular btn-inverse"/>
                <c:set target="${element2}" property="extraAttributes" value="data-form-set='save: save' data-form-target='#recipientForm' data-form-submit-event"/>
                <c:set target="${element2}" property="iconBefore" value="icon-save"/>
                <c:set target="${element2}" property="name">
                    <bean:message key="button.Save"/>
                </c:set>
            </jsp:useBean>
        </emm:ShowByPermission>
    </c:if>
</jsp:useBean>
