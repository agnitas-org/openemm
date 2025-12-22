<%@ page contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnTitleKey" 			value="statistic.comparison" 	        scope="request" />
<c:set var="sidemenu_active" 		value="Statistics" 				        scope="request" />
<c:set var="sidemenu_sub_active" 	value="statistic.comparison" 	        scope="request" />
<c:set var="agnHighlightKey" 		value="statistic.comparison" 	        scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Statistics" 				        scope="request" />
<c:set var="agnHelpKey" 			value="compareMailings" 		        scope="request" />
<c:set var="agnEditViewKey" 	    value="mailing-comparison-overview"     scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="statistic.comparison"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

        <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
        <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

        <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
            <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
        </emm:instantiate>

        <%-- Items for dropdown --%>
        <c:url var="exportUrl" value="/statistics/mailing/comparison/export.action"/>
        <emm:instantiate var="option" type="java.util.LinkedHashMap">
            <c:set target="${optionList}" property="0" value="${option}"/>
            <c:set target="${option}" property="extraAttributes" value="data-form-target='#table-tile' data-form-set='reportFormat:csv' data-form-submit-static data-form-url='${exportUrl}' data-prevent-load"/>
            <c:set target="${option}" property="name">
                <mvc:message code="user.export.csv"/>
            </c:set>
        </emm:instantiate>
    </emm:instantiate>

    <c:url var="compareUrl" value="/statistics/mailing/comparison/compare.action"/>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

        <c:set target="${element}" property="extraAttributes" value="data-form-url='${compareUrl}' data-form-submit-static data-form-target='#table-tile'"/>
        <c:set target="${element}" property="iconBefore" value="icon icon-search"/>
        <c:set target="${element}" property="name">
            <mvc:message code="statistic.compare" />
        </c:set>
    </emm:instantiate>
</emm:instantiate>
