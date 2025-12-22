<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action"%>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingOverviewForm" type="com.agnitas.emm.core.mailing.forms.MailingOverviewForm"--%>

<c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 		scope="request" />
<c:set var="agnEditViewKey" 	    value="mailings-overview" 	    scope="request" />

<c:choose>
    <c:when test="${mailingOverviewForm.forTemplates}">
        <c:set var="agnTitleKey" 			value="Templates" 		scope="request" />
        <c:set var="sidemenu_sub_active"	value="Templates" 		scope="request" />
        <c:set var="agnBreadcrumbsRootKey"	value="Templates" 		scope="request" />
        <c:set var="agnHelpKey" 			value="templateList"	scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnBreadcrumbsRootKey"	value="Mailings" 		  scope="request" />
        <c:set var="agnTitleKey"            value="Mailings"          scope="request" />
        <c:set var="sidemenu_sub_active"    value="default.Overview"  scope="request" />
        <c:set var="agnHelpKey"             value="mailingList"       scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
    </emm:instantiate>
</emm:instantiate>

<c:choose>
    <c:when test="${mailingOverviewForm.forTemplates}">
        <emm:ShowByPermission token="template.change">
            <emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
                <emm:instantiate var="element" type="java.util.LinkedHashMap">
                    <c:set target="${newResourceSettings}" property="0" value="${element}"/>
                    <c:set target="${element}" property="extraAttributes" value="data-confirm" />
                    <c:set target="${element}" property="url">
                        <c:url value="/mailing/template/create.action"/>
                    </c:set>
                </emm:instantiate>
            </emm:instantiate>
        </emm:ShowByPermission>
    </c:when>
    <c:otherwise>
        <emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:set target="${newResourceSettings}" property="0" value="${element}"/>
                <c:set target="${element}" property="extraAttributes" value="data-confirm" />
                <c:set target="${element}" property="url">
                    <c:url value="/mailing/create.action"/>
                </c:set>
            </emm:instantiate>
        </emm:instantiate>
    </c:otherwise>
</c:choose>
