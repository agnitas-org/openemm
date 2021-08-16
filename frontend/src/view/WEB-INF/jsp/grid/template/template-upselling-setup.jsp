<%@ page import="com.agnitas.web.ComMailingBaseAction" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>

<c:set var="ACTION_MAILING_IMPORT" value="<%= ComMailingBaseAction.ACTION_MAILING_IMPORT %>" />

<c:set var="isTabsMenuShown"        value="false"               scope="request"/>
<c:set var="sidemenu_active"        value="grid.layout.builder" scope="request" />
<c:set var="agnNavigationKey"       value="none"                scope="request" />
<c:set var="agnTitleKey"            value="default.A_EMM"       scope="request" />
<c:set var="agnSubtitleKey"         value="none"                scope="request" />
<c:set var="sidemenu_sub_active"    value="none"                scope="request" />
<c:set var="agnHelpKey"             value="grid-up-selling"     scope="request" />

<emm:ShowByPermission token="mailing.import">
    <c:set var="createNewItemUrl" scope="request">
        <html:rewrite page="/mailingbase.do?action=${ACTION_MAILING_IMPORT}&isTemplate=true&grid=true"/>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <mvc:message code="template.import"/>
    </c:set>
</emm:ShowByPermission>

<c:set var="agnHighlightKey" value="default.Overview" scope="request" />
