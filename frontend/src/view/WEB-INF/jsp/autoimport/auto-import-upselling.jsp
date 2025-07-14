<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.agnitas.util.AgnUtils"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN%>" />
<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}" />

<c:set var="aLocale" value="${admin.getLocale()}" />

<%--@elvariable id="upsellingInfoUrl" type="String"--%>

<div class="tile" data-sizing="container">
    <div class="tile-header" data-sizing="top">
        <h2 class="headline">
            <i class="icon icon-bolt"></i>
            <mvc:message code="autoImport.autoImport"/>
        </h2>
    </div>

    <div class="upselling-content auto-import-upselling" data-sizing="scroll">

        <div class="upselling-headline">
            <h3 class="upselling-title"><mvc:message code="autoImport.autoImport"/></h3>
            <h1 class="upselling-header"><mvc:message code="automation.teaser.header"/></h1>
        </div>
        <div class="upselling-desc">
            <p><mvc:message code="autoImport.teaser.text"/></p>
			
			
			<c:choose>
				<c:when test="${aLocale eq 'de_DE'}">
					<a href="https://www.agnitas.de/e-marketing-manager/premium-features/smart-data/" class="more-info-btn" target="_blank">
				</c:when>
				<c:otherwise>
					<a href="https://www.agnitas.de/en/e-marketing_manager/premium-features/smart-data/" class="more-info-btn" target="_blank">
				</c:otherwise>
			</c:choose>
                <mvc:message code="general.upselling.information"/>
            </a>
        </div>

    </div>

    <div class="tile-footer" data-sizing="bottom">
        <a href="javascript:void(0);" class="btn btn-large pull-left" onclick="history.back(); return false;">
            <i class="icon icon-angle-left"></i>
            <span class="text"><mvc:message code="button.Back" /></span>
        </a>
    </div>
</div>
