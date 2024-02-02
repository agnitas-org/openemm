<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="dependentsForm" type="com.agnitas.emm.core.target.form.TargetDependentsListForm"--%>
<%--@elvariable id="targetShortname" type="java.lang.String"--%>

<%--@elvariable id="workflowId" type="java.lang.String"--%>
<%--@elvariable id="workflowForwardParams" type="java.lang.String"--%>

<c:set var="isTabsMenuShown" 		value="true" 									scope="request" />

<c:set var="agnNavigationKey" 		value="TargetQBEdit" 							scope="request" />
<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="target-id" value="${dependentsForm.targetId}"/>
</emm:instantiate>

<c:set var="agnTitleKey" 			value="Target" 									scope="request" />
<c:set var="agnSubtitleKey" 		value="Target" 									scope="request" />
<c:set var="sidemenu_active" 		value="Targetgroups" 							scope="request" />
<c:set var="sidemenu_sub_active" 	value="none" 									scope="request" />
<c:set var="agnHighlightKey" 		value="default.usedIn" 							scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 									scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Targetgroups" 							scope="request" />
<c:set var="agnHelpKey" 			value="targetGroupView" 						scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/target/list.action" />
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${targetShortname}"/>
    </emm:instantiate>
</emm:instantiate>

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
    <c:if test="${not empty workflowForwardParams}">
        <jsp:useBean id="element" class="java.util.LinkedHashMap" scope="request">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="btnCls" value="btn btn-secondary btn-regular"/>
            <c:set target="${element}" property="iconBefore" value="icon-angle-left"/>
            <c:set target="${element}" property="type" value="href"/>
            <c:set target="${element}" property="url">
                <c:url value="/workflow/${workflowId}/view.action">
                    <c:param name="forwardParams" value="${workflowForwardParams};elementValue=${dependentsForm.targetId}"/>
                </c:url>
            </c:set>
            <c:set target="${element}" property="name">
                <mvc:message code="button.Back"/>
            </c:set>
        </jsp:useBean>
    </c:if>
</jsp:useBean>
