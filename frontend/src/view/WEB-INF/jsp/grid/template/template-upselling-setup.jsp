<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="isTabsMenuShown"        value="false"               scope="request"/>
<c:set var="sidemenu_active"        value="grid.layout.builder" scope="request" />
<c:set var="agnNavigationKey"       value="none"                scope="request" />
<c:set var="agnTitleKey"            value="default.A_EMM"       scope="request" />
<c:set var="agnSubtitleKey"         value="none"                scope="request" />
<c:set var="sidemenu_sub_active"    value="none"                scope="request" />
<c:set var="agnHelpKey"             value="grid-up-selling"     scope="request" />

<emm:ShowByPermission token="mailing.import">
    <c:url var="createNewItemUrl" value="/import/template.action?grid=true" scope="request" />
    <mvc:message var="createNewItemLabel" code="template.import" scope="request"/>
</emm:ShowByPermission>

<c:set var="agnHighlightKey" value="default.Overview" scope="request" />
