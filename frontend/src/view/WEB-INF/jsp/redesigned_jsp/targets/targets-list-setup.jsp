<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<c:set var="agnNavigationKey" 		value="targets" 			     scope="request" />
<c:set var="agnTitleKey" 			value="Targetgroups" 		     scope="request" />
<c:set var="agnSubtitleKey" 		value="Targets" 			     scope="request" />
<c:set var="sidemenu_active" 		value="Targetgroups" 		     scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview" 	     scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 	     scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true"	 			     scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Targetgroups" 		     scope="request" />
<c:set var="agnHelpKey" 			value="targetGroupView" 	     scope="request" />
<c:set var="agnEditViewKey" 	    value="target-groups-overview"   scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
    </emm:instantiate>
</emm:instantiate>

<emm:ShowByPermission token="targets.change">
    <emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${newResourceSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="url"><c:url value="/target/create.action"/></c:set>
        </emm:instantiate>
    </emm:instantiate>
</emm:ShowByPermission>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
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

            <emm:ShowByPermission token="targets.delete">
                <c:url var="bulkDeleteUrl" value="/target/confirm/bulk/delete.action"/>

                <emm:instantiate var="option" type="java.util.LinkedHashMap">
                    <c:set target="${optionList}" property="2" value="${option}"/>
                    <c:set target="${option}" property="extraAttributes" value="data-form-url='${bulkDeleteUrl}' data-form-confirm='' data-form-target='#table-tile'"/>
                    <c:set target="${option}" property="url">#</c:set>
                    <c:set target="${option}" property="name">
                        <mvc:message code="bulkAction.delete.target" />
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>
        </emm:instantiate>
    </emm:instantiate>
</emm:instantiate>
