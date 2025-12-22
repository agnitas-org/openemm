<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="formName" type="java.lang.String"--%>
<%--@elvariable id="statUrl" type="java.lang.String"--%>
<%--@elvariable id="webFormStatFrom" type="com.agnitas.emm.core.userform.form.WebFormStatFrom"--%>

<c:set var="isStatisticPage" value="${webFormStatFrom.allowedToChoseForm}"/>

<c:if test="${not isStatisticPage}">
    <c:set var="agnNavigationKey"      value="formViewWithLinks" 		                   scope="request" />
</c:if>
<c:set var="agnTitleKey" 			   value="${isStatisticPage ? 'Statistics' : 'Form'}"  scope="request" />
<c:set var="agnHighlightKey" 		   value="${isStatisticPage ? 'Forms' : 'Statistics'}" 				                   scope="request" />
<c:set var="sidemenu_active" 		   value="${isStatisticPage ? 'Statistics' : 'Forms'}" scope="request" />
<c:set var="sidemenu_sub_active" 	   value="Forms" 	                                   scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	   value="${isStatisticPage ? 'Statistics' : 'Forms'}" scope="request" />
<c:if test="${not isStatisticPage}">
    <c:url var="agnBreadcrumbsRootUrl" value="/webform/list.action" 	                   scope="request" />
</c:if>
<c:set var="agnHelpKey" 			   value="formStatistic" 			                   scope="request" />
<c:set var="agnEditViewKey" 	       value="userForm-statistic"  	                       scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="user-form-id" value="${webFormStatFrom.formId}"/>
</emm:instantiate>

<mvc:message var="formsMsg" code="Forms"/>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${isStatisticPage ? formsMsg : formName}"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
        <c:set target="${element}" property="iconBefore" value="icon-cloud-download-alt"/>
        <c:set target="${element}" property="name"><mvc:message code="button.Download"/></c:set>

        <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
            <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
        </emm:instantiate>

        <%-- Items for dropdown --%>
        <emm:instantiate var="option" type="java.util.LinkedHashMap">
            <c:set target="${optionList}" property="0" value="${option}"/>
            <c:set target="${option}" property="extraAttributes" value="data-prevent-load"/>
            <c:set target="${option}" property="url" value="${statUrl}&__format=csv"/>
            <c:set target="${option}" property="name"><mvc:message code="export.message.csv"/></c:set>
        </emm:instantiate>
    </emm:instantiate>

    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
        <c:set target="${element}" property="extraAttributes" value="data-form-target='#stat-form' data-form-submit"/>
        <c:set target="${element}" property="iconBefore" value="icon icon-sync"/>
        <c:set target="${element}" property="name"><mvc:message code="button.Refresh" /></c:set>
    </emm:instantiate>
</emm:instantiate>
