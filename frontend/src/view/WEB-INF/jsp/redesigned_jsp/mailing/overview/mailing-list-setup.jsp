<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action"%>
<%@ page import="com.agnitas.emm.core.imports.web.ImportController" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="MAILING" value="<%= ImportController.ImportType.MAILING %>" />
<c:set var="CLASSIC_TEMPLATE" value="<%= ImportController.ImportType.CLASSIC_TEMPLATE %>" />

<%--@elvariable id="mailingOverviewForm" type="com.agnitas.emm.core.mailing.forms.MailingOverviewForm"--%>

<c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 				scope="request" />
<c:set var="agnEditViewKey" 	    value="mailings-overview" 	    scope="request" />

<c:choose>
    <c:when test="${mailingOverviewForm.forTemplates}">
        <c:set var="agnNavigationKey" 		value="templates" 		scope="request" />
        <c:set var="agnTitleKey" 			value="Templates" 		scope="request" />
        <c:set var="agnSubtitleKey" 		value="Templates" 		scope="request" />
        <c:set var="sidemenu_sub_active"	value="Templates" 		scope="request" />
        <c:set var="agnHelpKey" 			value="templateList"	scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="Templates"/>
            </emm:instantiate>
        </emm:instantiate>

		<emm:ShowByPermission token="mailing.import">
			<c:url var="importItemUrl" value="/import/file.action?type=${CLASSIC_TEMPLATE}"    scope="request"/>
			<c:set var="importItemLabelKey" value="template.import"    scope="request"/>
        </emm:ShowByPermission>
    </c:when>
    <c:otherwise>
        <c:set var="agnTitleKey" value="Mailings"                                              scope="request"/>
        <c:set var="agnSubtitleKey" value="Mailings"                                           scope="request"/>
        <c:set var="sidemenu_sub_active" value="default.Overview"                              scope="request"/>
        <c:set var="agnHelpKey" value="mailingList"                                            scope="request"/>
        <emm:ShowByPermission token="mailing.import">
            <c:url var="importItemUrl" value="/import/file.action?type=${MAILING}"             scope="request"/>
            <c:set var="importItemLabelKey" value="mailing.import"                             scope="request"/>
        </emm:ShowByPermission>
    </c:otherwise>
</c:choose>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

        <c:set target="${element}" property="btnCls" value="btn dropdown-toggle"/>
        <c:set target="${element}" property="cls" value="mobile-hidden"/>
        <c:set target="${element}" property="extraAttributes" value="data-bs-toggle='dropdown'"/>
        <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
        <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

        <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
            <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
        </emm:instantiate>

        <%-- buld delete or restore action option--%>
        <emm:ShowByPermission token="mailing.delete|template.delete">
            <c:choose>
                <c:when test="${mailingOverviewForm.useRecycleBin}">
                    <emm:instantiate var="option" type="java.util.LinkedHashMap">
                        <c:set target="${optionList}" property="0" value="${option}"/>
                        <c:set target="${option}" property="extraAttributes" value="data-action='bulk-restore' data-form-target='#mailing-overview-form'"/>
                        <c:set target="${option}" property="url">#</c:set>
                        <c:set target="${option}" property="name"><mvc:message code="bulk.mailing.restore"/></c:set>
                    </emm:instantiate>
                </c:when>
                <c:otherwise>
                    <c:url var="bulkDeleteUrl" value="/mailing/${mailingOverviewForm.forTemplates ? 'deleteTemplates' : 'deleteMailings'}.action"/>
                    <emm:instantiate var="option" type="java.util.LinkedHashMap">
                        <c:set target="${optionList}" property="0" value="${option}"/>
                        <c:set target="${option}" property="extraAttributes" value="data-form-url='${bulkDeleteUrl}' data-form-confirm data-form-target='#mailing-overview-form'"/>
                        <c:set target="${option}" property="url">#</c:set>
                        <c:set target="${option}" property="name">
                            <c:choose>
                                <c:when test="${mailingOverviewForm.forTemplates}">
                                    <emm:ShowByPermission token="template.delete">
                                        <mvc:message code="bulkAction.delete.template"/>
                                    </emm:ShowByPermission>
                                </c:when>
                                <c:otherwise>
                                    <emm:ShowByPermission token="mailing.delete">
                                        <mvc:message code="bulkAction.delete.mailing"/>
                                    </emm:ShowByPermission>
                                </c:otherwise>
                            </c:choose>
                        </c:set>
                    </emm:instantiate>
                </c:otherwise>
            </c:choose>
        </emm:ShowByPermission>

        <%-- import action option--%>
        <emm:ShowByPermission token="mailing.import">
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="1" value="${option}"/>
                <c:set target="${option}" property="extraAttributes" value="data-confirm"/>
                <c:set target="${option}" property="url">${importItemUrl}</c:set>
                <c:set target="${option}" property="name">
                    <mvc:message code="${importItemLabelKey}" />
                </c:set>
            </emm:instantiate>
        </emm:ShowByPermission>
    </emm:instantiate>

    <c:if test="${not mailingOverviewForm.forTemplates}">
        <emm:HideByPermission token="mailing.content.readonly">
            <%-- "New" dropdown --%>
            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

                <c:set target="${element}" property="btnCls" value="btn dropdown-toggle"/>
                <c:set target="${element}" property="cls" value="mobile-hidden"/>
                <c:set target="${element}" property="extraAttributes" value="data-bs-toggle='dropdown'"/>
                <c:set target="${element}" property="iconBefore" value="icon-plus"/>
                <c:set target="${element}" property="name"><mvc:message code="button.New"/></c:set>

                <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
                    <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
                </emm:instantiate>

                <%-- "E-Mail Creator" option--%>
                <%@include file="fragments/mailing-create-grid.jspf" %>

                <%-- "Standard mailing" option--%>
                <emm:ShowByPermission token="mailing.classic">
                    <emm:instantiate var="option" type="java.util.LinkedHashMap">
                        <c:set target="${optionList}" property="1" value="${option}"/>
                        <c:set target="${option}" property="extraAttributes" value="data-confirm"/>
                        <c:set target="${option}" property="url">
                            <%-- TODO: check workflow id!!!! --%>
                            <c:url value="/mailing/templates.action?keepForward=${workflowId > 0}" />
                        </c:set>
                        <c:set target="${option}" property="name">
                            <mvc:message code="mailing.wizard.Normal" />
                        </c:set>
                    </emm:instantiate>
                </emm:ShowByPermission>
            </emm:instantiate>
        </emm:HideByPermission>
    </c:if>
</emm:instantiate>

<%-- 'New' button for template overview --%>
<c:if test="${mailingOverviewForm.forTemplates}">
    <emm:ShowByPermission token="template.change">
        <emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:set target="${newResourceSettings}" property="0" value="${element}"/>
                <c:set target="${element}" property="url"><c:url value="/mailing/new.action?isTemplate=true"/></c:set>
            </emm:instantiate>
        </emm:instantiate>
    </emm:ShowByPermission>
</c:if>
