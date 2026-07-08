<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.imports.web.ImportController" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<c:set var="agnTitleKey" 			value="Targetgroups" 		     scope="request" />
<c:set var="sidemenu_active" 		value="Targetgroups" 		     scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview" 	     scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 	     scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Targetgroups" 		     scope="request" />
<c:set var="agnHelpKey" 			value="targetGroupView" 	     scope="request" />
<c:set var="agnEditViewKey" 	    value="target-groups-overview"   scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

        <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
        <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

        <emm:instantiate var="dropDownItems" type="java.util.LinkedHashMap">
            <c:set target="${element}" property="dropDownItems" value="${dropDownItems}"/>
        </emm:instantiate>

        <c:set var="TARGET_GROUP" value="<%= ImportController.ImportType.TARGET_GROUP %>" />
        <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
            <c:set target="${dropDownItems}" property="0" value="${dropDownItem}"/>
            <c:set target="${dropDownItem}" property="url"><c:url value="/import/file.action?type=${TARGET_GROUP}"/></c:set>
            <c:set target="${dropDownItem}" property="extraAttributes" value="data-confirm"/>
            <c:set target="${dropDownItem}" property="name"><mvc:message code="target.import"/></c:set>
        </emm:instantiate>
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
