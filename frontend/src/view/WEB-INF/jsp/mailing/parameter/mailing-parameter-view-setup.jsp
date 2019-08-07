<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.StrutsActionBase" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_VIEW" value="<%= StrutsActionBase.ACTION_VIEW %>"/>

<emm:CheckLogon/>
<emm:Permission token="mailing.show"/>

<c:set var="agnNavigationKey" 		value="none" 				scope="request" />
<c:set var="agnTitleKey" 			value="MailingParameter"	scope="request" />
<c:set var="agnSubtitleKey" 		value="MailingParameter" 	scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="MailingParameter" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 			scope="request" />
<c:set var="agnHelpKey" 			value="mailingParameter" 	scope="request" />

<c:choose>
    <c:when test="${mailingParameterForm.mailingInfoID != 0}">
        <c:set var="agnNavHrefAppend" 	value="&mailingInfoID=${mailingParameterForm.mailingInfoID}"	scope="request" />
        <c:set var="agnHighlightKey"	value="MailingParameter.edit" 									scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnHighlightKey" value="MailingParameter.new" scope="request"/>
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="MailingParameter"/>
        <c:url var="parametersOverviewLink" value="/mailingParameter.do">
            <c:param name="action" value="list"/>
        </c:url>
        <c:set target="${agnBreadcrumb}" property="url" value="${parametersOverviewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${mailingParameterForm.mailingInfoID != 0}">
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingParameterForm.parameterName}"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="textKey" value="MailingParameter.new"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
    <emm:ShowByPermission token="mailing.parameter.change">
        <c:if test="${mailingParameterForm.mailingInfoID != 0}">
            <jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">
                <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
                <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-alert"/>
                <c:set target="${element0}" property="extraAttributes" value="data-form-set='previousAction: ${ACTION_VIEW}' data-form-confirm='deleteConfirm' data-form-target='#mailingParameterForm'"/>
                <c:set target="${element0}" property="iconBefore" value="icon-trash-o"/>
                <c:set target="${element0}" property="name">
                    <bean:message key="button.Delete"/>
                </c:set>
            </jsp:useBean>
        </c:if>
    </emm:ShowByPermission>

    <emm:ShowByPermission token="mailing.parameter.change">
        <jsp:useBean id="element1" class="java.util.LinkedHashMap" scope="request">
            <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
            <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element1}" property="extraAttributes" value="data-form-action='save' data-form-target='#mailingParameterForm'"/>
            <c:set target="${element1}" property="iconBefore" value="icon-save"/>
            <c:set target="${element1}" property="name">
                <bean:message key="button.Save"/>
            </c:set>
        </jsp:useBean>
    </emm:ShowByPermission>
</jsp:useBean>
