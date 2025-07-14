<%@ page import="com.agnitas.emm.core.imports.web.ImportController" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="dashboardForm" type="com.agnitas.emm.core.dashboard.form.DashboardForm"--%>

<c:set var="agnTitleKey" 		 value="default.A_EMM" 	   scope="request" />
<c:set var="sidemenu_active" 	 value="Dashboard" 		   scope="request" />
<c:set var="sidemenu_sub_active" value="default.Overview"  scope="request" />
<c:set var="agnHelpKey" 		 value="dashboard" 		   scope="request" />

<c:set var="TEMPLATE" value="<%= ImportController.ImportType.TEMPLATE %>" />
<c:set var="isCalendarMode" value="${dashboardForm.mode eq 'CALENDAR'}"/>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="icon" value="home"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Dashboard"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:if test="${not isCalendarMode}">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="cls" value="mobile-hidden"/>
            <c:set target="${element}" property="iconBefore" value="icon icon-edit"/>
            <c:set target="${element}" property="extraAttributes" value="id='dashboard-stop-edit-btn' data-action='stop-editing'" />
            <c:set target="${element}" property="name"><mvc:message code="default.view.save" /></c:set>
        </emm:instantiate>

        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
            <c:set target="${element}" property="cls" value="mobile-hidden"/>
            <c:set target="${element}" property="iconBefore" value="icon icon-grip-horizontal"/>
            <c:set target="${element}" property="extraAttributes" value="id='dashboard-select-layout-btn' data-action='select-layout'" />
            <c:set target="${element}" property="name"><mvc:message code="dashboard.layout.change" /></c:set>
        </emm:instantiate>

        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="2" value="${element}"/>
            <c:set target="${element}" property="cls" value="mobile-hidden"/>
            <c:set target="${element}" property="iconBefore" value="icon icon-edit"/>
            <c:set target="${element}" property="extraAttributes" value="id='dashboard-start-edit-btn' data-action='edit-dashboard'" />
            <c:set target="${element}" property="name"><mvc:message code="default.view.edit" /></c:set>
        </emm:instantiate>
    </c:if>

    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="3" value="${element}"/>

        <emm:instantiate var="dropDownItems" type="java.util.LinkedHashMap"/>

        <c:set target="${element}" property="iconBefore" value="icon-plus"/>
        <c:set target="${element}" property="name"><mvc:message code="New"/></c:set>
        <c:set target="${element}" property="dropDownItems" value="${dropDownItems}"/>

        <emm:instantiate var="option" type="java.util.LinkedHashMap">
            <c:set target="${dropDownItems}" property="0" value="${option}"/>
            <c:set target="${option}" property="extraAttributes" value="data-confirm"/>
            <c:set target="${option}" property="url">
                <c:url value="/mailing/create.action"/>
            </c:set>
            <c:set target="${option}" property="name">
                <mvc:message code="dashboard.mailing.new"/>
            </c:set>
        </emm:instantiate>

        <emm:ShowByPermission token="template.change">
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${dropDownItems}" property="1" value="${option}"/>
                <c:set target="${option}" property="url">
                    <c:url value="/mailing/new.action?isTemplate=true"/>
                </c:set>
                <c:set target="${option}" property="name">
                    <mvc:message code="mailing.template.create"/>
                </c:set>
            </emm:instantiate>
        </emm:ShowByPermission>

        <emm:ShowByPermission token="mailing.import">
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${dropDownItems}" property="2" value="${option}"/>
                <c:set target="${option}" property="extraAttributes" value="data-confirm"/>
                <c:set target="${option}" property="url">
                    <c:url value="/import/file.action?type=${TEMPLATE}"/>
                </c:set>
                <c:set target="${option}" property="name">
                    <mvc:message code="template.import"/>
                </c:set>
            </emm:instantiate>
        </emm:ShowByPermission>

        <emm:ShowByPermission token="targets.show">
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${dropDownItems}" property="3" value="${option}"/>
                <c:set target="${option}" property="url">
                    <c:url value="/target/create.action"/>
                </c:set>
                <c:set target="${option}" property="name">
                    <mvc:message code="target.create"/>
                </c:set>
            </emm:instantiate>
        </emm:ShowByPermission>
    </emm:instantiate>
</emm:instantiate>

<%@ include file="fragments/news-sidebar-btn.jspf" %>

<c:set var="headerActionsHtml" scope="request">
    <mvc:form servletRelativeAction="/dashboard.action" modelAttribute="dashboardForm" data-form="resource">

        <script type="application/json" data-initializer="web-storage-persist">
            {
                "dashboard": {
                    "mode": "${dashboardForm.mode}"
                }
            }
        </script>

        <label class="switch">
            <input type="checkbox" ${isCalendarMode  ? 'checked' : ''} data-form-set="mode: ${isCalendarMode ? 'GRID' : 'CALENDAR'}" data-form-submit>
            <i class="icon icon-grip-horizontal"></i>
            <i class="icon icon-calendar-alt"></i>
        </label>
    </mvc:form>
</c:set>
