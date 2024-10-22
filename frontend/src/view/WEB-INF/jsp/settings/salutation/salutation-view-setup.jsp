<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.salutation.form.SalutationForm"--%>
<%--@elvariable id="salutationCompanyId" type="java.lang.Integer"--%>
<%--@elvariable id="id" type="java.lang.Integer"--%>

<emm:CheckLogon/>
<emm:Permission token="salutation.show"/>

<c:url var="switchDesignUrl" value="/salutation/listRedesigned.action" scope="request" />

<c:set var="isTabsMenuShown" 		value="false"                               scope="request" />
<c:set var="agnNavHrefAppend" 		value="&salutationID=${id}"                 scope="request" />
<c:set var="agnTitleKey" 			value="settings.FormsOfAddress"             scope="request" />
<c:set var="sidemenu_active" 		value="Mailings"                            scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.FormsOfAddress"             scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true"                                scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Mailings"                            scope="request" />
<c:set var="agnHelpKey" 			value="salutationForms"                     scope="request" />

<c:choose>
    <c:when test="${id eq 0}">
        <c:set var="agnNavigationKey" 	value="SalutationsNew" 					scope="request" />
        <c:set var="agnSubtitleKey" 	value="default.salutation.shortname" 	scope="request" />
        <c:set var="agnHighlightKey" 	value="default.salutation.shortname" 	scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" 	value="SalutationsEdit" 			scope="request" />
        <c:set var="agnSubtitleKey" 	value="settings.FormOfAddress" 		scope="request" />
        <c:set var="agnHighlightKey" 	value="settings.EditFormOfAddress" 	scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.FormsOfAddress"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/salutation/list.action?restoreSort=true"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${id eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="default.salutation.shortname"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${form.description}"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:if test="${not empty id and id ne 0}">
    	<c:if test="${salutationCompanyId ne 0}">
        	<emm:ShowByPermission token="salutation.delete">
            	<emm:instantiate var="element" type="java.util.LinkedHashMap">
                	<c:set target="${itemActionsSettings}" property="0" value="${element}"/>
                    <c:set target="${element}" property="btnCls" value="btn btn-regular btn-alert"/>
                    <c:set target="${element}" property="type" value="href"/>
                    <c:set target="${element}" property="url">
                        <c:url value="/salutation/${id}/confirmDelete.action"/>
                    </c:set>
                    <c:set target="${element}" property="extraAttributes" value="data-confirm=''"/>
                	<c:set target="${element}" property="iconBefore" value="icon-trash-o"/>
                	<c:set target="${element}" property="name">
                    	<mvc:message code="button.Delete"/>
                	</c:set>
            	</emm:instantiate>
        	</emm:ShowByPermission>
        </c:if>
    </c:if>

	<c:if test="${salutationCompanyId ne 0}">
    	<emm:ShowByPermission token="salutation.change">
        	<emm:instantiate var="element" type="java.util.LinkedHashMap">
            	<c:set target="${itemActionsSettings}" property="1" value="${element}"/>
            	<c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
            	<c:set target="${element}" property="extraAttributes" value="data-form-target='#form' data-form-submit"/>
            	<c:set target="${element}" property="iconBefore" value="icon-save"/>
            	<c:set target="${element}" property="name">
                	<mvc:message code="button.Save"/>
            	</c:set>
        	</emm:instantiate>
    	</emm:ShowByPermission>
    </c:if>
</emm:instantiate>
