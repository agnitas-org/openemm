<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<emm:CheckLogon/>

<emm:Permission token="workflow.show"/>

<c:set var="agnTitleKey" 			value="Workflow" 			   scope="request" />
<c:set var="agnSubtitleKey" 		value="default.Overview" 	   scope="request" />
<c:set var="sidemenu_active" 		value="Workflow" 			   scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview" 	   scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 	   scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				   scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Workflow" 			   scope="request" />
<c:set var="agnHelpKey" 			value="workflow" 			   scope="request" />
<c:set var="agnEditViewKey" 	    value="workflow-overview"      scope="request" />

<emm:ShowByPermission token="workflow.delete">
<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
        <%-- Actions dropdown --%>
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

            <c:set target="${element}" property="btnCls" value="btn dropdown-toggle"/>
            <c:set target="${element}" property="extraAttributes" value="data-bs-toggle='dropdown'"/>
            <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
            <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

            <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
                <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
            </emm:instantiate>

            <%-- Items for dropdown --%>
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="0" value="${option}"/>
                <c:set target="${option}" property="extraAttributes" value="data-action='bulk-delete'"/>
                <c:set target="${option}" property="url">#</c:set>
                <c:set target="${option}" property="name">
                    <mvc:message code="bulkAction.delete.workflow"/>
                </c:set>
            </emm:instantiate>

            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="1" value="${option}"/>
                <c:set target="${option}" property="extraAttributes" value="data-action='bulk-deactivate'"/>
                <c:set target="${option}" property="url">#</c:set>
                <c:set target="${option}" property="name">
                    <mvc:message code="bulkAction.deactivate.workflow"/>
                </c:set>
            </emm:instantiate>
        </emm:instantiate>
    </emm:instantiate>
</emm:ShowByPermission>

<emm:ShowByPermission token="workflow.change">
    <emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${newResourceSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="url"><c:url value="/workflow/create.action"/></c:set>
        </emm:instantiate>
    </emm:instantiate>
</emm:ShowByPermission>
