<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="isBreadcrumbsShown" value="false" scope="request"/>

<tiles:insertAttribute name="page-setup"/>

<%--TODO: add lang attr--%>
<html>
<tiles:insertAttribute name="head-tag"/>

<body>
    <div id="page-wrapper">
        <aside class="sidebar">
            <%@include file="fragments/sidebar/sidebar.jspf" %>
        </aside>

        <div id="content-wrapper">
            <header class="header">
                <nav id="breadcrumbs-wrapper" aria-label="breadcrumb">
                    <%@include file="fragments/breadcrumbs/breadcrumbs.jspf" %>
                </nav>

                <ul class="header__actions">
                    <li class="loader hidden header__action">
                        <i class="icon icon-spinner icon-pulse"></i>
                        <span><mvc:message code="default.loading" /></span>
                    </li>
                    <tiles:insertAttribute name="itemactions"/>
                    <tiles:insertAttribute name="newresource"/>
                </ul>
            </header>

            <main class="main-view">
                <tiles:insertAttribute name="body"/>
            </main>
        </div>
    </div>

    <tiles:insertAttribute name="footer_matomo"/>
    <tiles:insertAttribute name="messages"/>

</body>
</html>
