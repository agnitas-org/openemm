<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="oAuthAccountLinkingAllowed" type="java.lang.Boolean"--%>
<%--@elvariable id="oAuthAccountLinked" type="java.lang.Boolean"--%>

<c:set var="agnTitleKey" 			value="settings.Admin" 				scope="request" />
<c:set var="agnHighlightKey" 		value="settings.admin.edit" 		scope="request" />
<c:set var="agnHelpKey" 			value="user_self-administration" 	scope="request" />
<c:set var="agnEditViewKey" 	    value="self-service" 	            scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="icon" value="cog"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${sessionScope['userName']}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Settings"/>
    </emm:instantiate>
</emm:instantiate>

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

        <c:if test="${oAuthAccountLinkingAllowed}">
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="0" value="${option}"/>
                <c:set target="${option}" property="type" value="href" />
                <c:if test="${oAuthAccountLinked}">
                    <c:set target="${option}" property="extraAttributes" value="data-ajax='POST'"/>
                </c:if>
                <c:set target="${option}" property="url">
                    <c:url value="${oAuthAccountLinked ? '/user/self/oauth/unlink.action' : '/user/self/oauth/link.action'}"/>
                </c:set>
                <c:set target="${option}" property="name">
                    <mvc:message code="${oAuthAccountLinked ? 'GWUA.oauth.unlinkAccount' : 'GWUA.oauth.linkAccount'}" />
                </c:set>
            </emm:instantiate>
        </c:if>
    </emm:instantiate>

    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
        <c:set target="${element}" property="extraAttributes" value="data-form-target='#settings-tile' data-form-submit"/>
        <c:set target="${element}" property="iconBefore" value="icon-save"/>
        <c:set target="${element}" property="name">
            <mvc:message code="button.Save"/>
        </c:set>
    </emm:instantiate>
</emm:instantiate>
