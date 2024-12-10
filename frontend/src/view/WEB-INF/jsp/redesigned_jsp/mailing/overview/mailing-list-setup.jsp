<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action"%>
<%@ page import="com.agnitas.emm.core.imports.web.ImportController" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="TEMPLATE_IMPORT_TYPE" value="<%= ImportController.ImportType.TEMPLATE %>" />

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

<c:if test="${mailingOverviewForm.forTemplates}">
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

            <%-- import action option--%>
            <emm:ShowByPermission token="mailing.import">
                <emm:instantiate var="option" type="java.util.LinkedHashMap">
                    <c:set target="${optionList}" property="0" value="${option}"/>
                    <c:set target="${option}" property="extraAttributes" value="data-confirm"/>
                    <c:set target="${option}" property="url">
                        <c:url value="/import/file.action?type=${TEMPLATE_IMPORT_TYPE}" />
                    </c:set>
                    <c:set target="${option}" property="name">
                        <mvc:message code="template.import" />
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>
        </emm:instantiate>
    </emm:instantiate>
</c:if>

<c:choose>
    <c:when test="${mailingOverviewForm.forTemplates}">
        <emm:ShowByPermission token="template.change">
            <emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
                <emm:instantiate var="element" type="java.util.LinkedHashMap">
                    <c:set target="${newResourceSettings}" property="0" value="${element}"/>
                    <c:set target="${element}" property="url"><c:url value="/mailing/new.action?isTemplate=true"/></c:set>
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
