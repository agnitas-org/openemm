<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="org.agnitas.util.AgnUtils"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN%>" />
<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}" />

<c:set var="aLocale" value="${admin.getLocale()}" />

<%--@elvariable id="upsellingInfoUrl" type="String"--%>
<%--@elvariable id="featureNameKey" type="java.lang.String"--%>
<%--@elvariable id="headlineKey" type="java.lang.String"--%>
<%--@elvariable id="descriptionKey" type="java.lang.String"--%>
<%--@elvariable id="upgradeInfoKey" type="java.lang.String"--%>

<div class="tile" data-sizing="container">
    <div class="tile-header" data-sizing="top">
        <h2 class="headline">
            <i class="icon icon-bolt"></i>
            <mvc:message code="${featureNameKey}"/>
        </h2>
    </div>

    <div class="upselling-content" data-sizing="scroll">
        <c:url var="upsellingImageSrc" value="assets/core/images/facelift/general-upselling.png"/>

        <div class="row">
            <div class="col-sm-12">
                <div class="upselling-headline" style="position: unset; padding: 50px">
                    <h3 class="upselling-title"><mvc:message code="${featureNameKey}"/></h3>
                    <h1 class="upselling-header"><mvc:message code="${headlineKey}"/></h1>
                </div>
            </div>
        </div>
        <div class="row" style="padding: 0 50px">
            <div class="col-sm-3">
                <div class="general-upselling-image"><img src="${upsellingImageSrc}" alt="Upselling"/></div>
            </div>
            <div class="upselling-desc col-sm-9" style="margin-left: 50px; position:unset;">
                <p><mvc:message code="${descriptionKey}"/></p>
                <p><mvc:message code="${upgradeInfoKey}"/></p>

                <c:choose>
                    <c:when test="${aLocale eq 'de_DE'}">
                        <a href="https://www.agnitas.de/e-marketing-manager/premium-features/cross-media/" class="more-info-btn" target="_blank">
                    </c:when>
                    <c:otherwise>
                        <a href="https://www.agnitas.de/en/e-marketing_manager/premium-features/cross-media/" class="more-info-btn" target="_blank">
                    </c:otherwise>
                </c:choose>
                     <mvc:message code="general.upselling.information"/>
                 </a>
            </div>
        </div>

    </div>

    <div class="tile-footer" data-sizing="bottom">
        <a href="javascript:void(0);" class="btn btn-large pull-left" onclick="history.back(); return false;">
            <i class="icon icon-angle-left"></i>
            <span class="text"><mvc:message code="button.Back" /></span>
        </a>
    </div>
</div>
