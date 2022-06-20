<%@ page language="java" contentType="text/html; charset=utf-8" import="com.agnitas.mailing.autooptimization.beans.*" buffer="64kb" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.StrutsActionBase" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://ajaxanywhere.sourceforge.net/" prefix="aa"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" value="<%= StrutsActionBase.ACTION_LIST %>" scope="request"/>
<c:set var="ACTION_VIEW" value="<%= StrutsActionBase.ACTION_VIEW %>" scope="request"/>
<c:set var="STATUS_NOT_STARTED" value="<%=ComOptimization.STATUS_NOT_STARTED%>" scope="request" />
<c:set var="STATUS_TEST_SEND" value="<%=ComOptimization.STATUS_TEST_SEND%>" scope="request" />
<c:set var="STATUS_EVAL_IN_PROGRESS" value="<%=ComOptimization.STATUS_EVAL_IN_PROGRESS%>" scope="request" />
<c:set var="STATUS_FINISHED" value="<%=ComOptimization.STATUS_FINISHED%>" scope="request" />
<c:set var="STATUS_SCHEDULED" value="<%=ComOptimization.STATUS_SCHEDULED%>" scope="page" />

<emm:CheckLogon />
<emm:Permission token="campaign.change" />
	
<c:set var="admin" value="${emm.admin}" scope="request" />
<c:set var="sessionID" value="${pageContext.session.id}" scope="request"/>

<c:set var="agnNavigationKey" 		value="Campaign" 					scope="request" />
<c:set var="agnTitleKey" 			value="mailing.archive" 			scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 					scope="request" />
<c:set var="sidemenu_sub_active" 	value="mailing.archive" 			scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.autooptimization"	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 						scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 					scope="request" />
<c:set var="agnHelpKey" 			value="autooptimization" 			scope="request" />

<c:choose>
	<c:when test="${optimizationForm.optimizationID != 0}">
        <emm:HideByPermission token="campaign.migration">
            <c:set var="agnNavHrefAppend"	value="&campaignID=${optimizationForm.campaignID}&optimizationID=${optimizationForm.optimizationID}"	scope="request" />
        </emm:HideByPermission>
        <emm:ShowByPermission token="campaign.migration">
            <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                <c:set target="${agnNavHrefParams}" property="campaignID" value="${optimizationForm.campaignID}"/>
                <c:set target="${agnNavHrefParams}" property="campaignName" value="${optimizationForm.campaignName}"/>
            </emm:instantiate>
        </emm:ShowByPermission>

		<c:set var="agnSubtitleKey" 	value="mailing.autooptimization" 																		scope="request" />
	</c:when>
	<c:otherwise>
        <emm:HideByPermission token="campaign.migration">
            <c:set var="agnNavHrefAppend"	value="&campaignID=${optimizationForm.campaignID}"	scope="request" />
        </emm:HideByPermission>
        <emm:ShowByPermission token="campaign.migration">
            <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                <c:set target="${agnNavHrefParams}" property="campaignID" value="${optimizationForm.campaignID}"/>
                <c:set target="${agnNavHrefParams}" property="campaignName" value="${optimizationForm.campaignName}"/>
            </emm:instantiate>
        </emm:ShowByPermission>

        <c:set var="agnSubtitleKey" 	value="mailing.autooptimization.new" 				scope="request" />
	</c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.archive"/>

        <emm:HideByPermission token="campaign.migration">
            <c:url var="campaignOverviewLink" value="/campaign.do">
                <c:param name="action" value="${ACTION_LIST}"/>
            </c:url>
        </emm:HideByPermission>
        <emm:ShowByPermission token="campaign.migration">
            <c:url var="campaignOverviewLink" value="/campaign/list.action"/>
        </emm:ShowByPermission>

        <c:set target="${agnBreadcrumb}" property="url" value="${campaignOverviewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>

        <emm:HideByPermission token="campaign.migration">
            <c:set target="${agnBreadcrumb}" property="text" value="${campaignForm.shortname}"/>
            <c:url var="campaignLink" value="/campaign.do">
                <c:param name="action" value="${ACTION_VIEW}"/>
                <c:param name="campaignID" value="${optimizationForm.campaignID}"/>
            </c:url>
        </emm:HideByPermission>
        <emm:ShowByPermission token="campaign.migration">
            <c:set target="${agnBreadcrumb}" property="text" value="${optimizationForm.campaignName}"/>
            <c:url var="campaignLink" value="/campaign/${optimizationForm.campaignID}/view.action"/>
        </emm:ShowByPermission>

        <c:set target="${agnBreadcrumb}" property="url" value="${campaignLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.autooptimization"/>
        <c:url var="campaignOptimizationsLink" value="/optimize.do">
            <c:param name="action" value="${ACTION_LIST}"/>
            <c:param name="campaignID" value="${optimizationForm.campaignID}"/>
        </c:url>
        <c:set target="${agnBreadcrumb}" property="url" value="${campaignOptimizationsLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="3" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${optimizationForm.optimizationID != 0}">
                <c:set target="${agnBreadcrumb}" property="text" value="${optimizationForm.shortname}"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.autooptimization.new"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
    <logic:notEqual name="optimizationForm" property="optimizationID" value="0">
        <emm:ShowByPermission token="mailing.send.world">
            <jsp:useBean id="element2" class="java.util.LinkedHashMap" scope="request">
                <c:set target="${itemActionsSettings}" property="2" value="${element2}"/>
                <c:set target="${element2}" property="btnCls" value="btn btn-regular btn-secondary"/>
                <c:set target="${element2}" property="extraAttributes" value="data-form-set='method: unSchedule' data-form-target='#optimizationForm' data-form-confirm"/>
                <c:set target="${element2}" property="iconBefore" value="icon-tasks"/>
                <c:set target="${element2}" property="name"><bean:message key="btndeactivate" /></c:set>
            </jsp:useBean>
            <jsp:useBean id="element3" class="java.util.LinkedHashMap" scope="request">
                <c:set target="${itemActionsSettings}" property="3" value="${element3}"/>
                <c:set target="${element3}" property="btnCls" value="btn btn-regular btn-secondary"/>
                <c:set target="${element3}" property="extraAttributes" value="data-form-set='method: schedule' data-form-target='#optimizationForm' data-form-confirm"/>
                <c:set target="${element3}" property="iconBefore" value="icon-tasks"/>
                <c:set target="${element3}" property="name" />
                <c:set target="${element3}" property="name"><bean:message key="button.Activate" /></c:set>
            </jsp:useBean>
        </emm:ShowByPermission>
    </logic:notEqual>

    <logic:notEqual name="optimizationForm" property="optimizationID" value="0">
        <emm:ShowByPermission token="campaign.delete">
            <c:if test="${optimizationForm.status == STATUS_NOT_STARTED || optimizationForm.status ==  STATUS_TEST_SEND || optimizationForm.status ==  STATUS_EVAL_IN_PROGRESS }">
                <jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">
                    <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
                    <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-alert"/>
                    <c:set target="${element0}" property="extraAttributes" value="data-form-set='method: confirmDelete' data-form-target='#optimizationForm' data-form-confirm"/>
                    <c:set target="${element0}" property="iconBefore" value="icon-trash-o"/>
                    <c:set target="${element0}" property="name">
                        <bean:message key="button.Delete"/>
                    </c:set>
                </jsp:useBean>
            </c:if>
        </emm:ShowByPermission>
    </logic:notEqual>

    <c:if test="${optimizationForm.status == STATUS_NOT_STARTED }">
        <jsp:useBean id="element1" class="java.util.LinkedHashMap" scope="request">
            <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
            <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element1}" property="extraAttributes" value="data-form-set='method: save' data-form-target='#optimizationForm' data-form-submit"/>
            <c:set target="${element1}" property="iconBefore" value="icon-save"/>
            <c:set target="${element1}" property="name">
                <bean:message key="button.Save"/>
            </c:set>
        </jsp:useBean>
    </c:if>
</jsp:useBean>
