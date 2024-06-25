<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:url var="upsellingImageSrc" value="assets/core/images/facelift/msgs_msg-inbox-preview.svg"/>
<c:set var="on_error_image" value="/assets/core/images/facelift/msgs_msg-inbox-preview.png"/>

<div class="msg-tile msg-tile-success">
    <div class="msg-tile-header">
        <img alt="" src="${upsellingImageSrc}" onerror="this.onerror=null; this.src='assets/core/images/facelift/msgs_msg-inbox-preview.png'">
        <h1><mvc:message code="forbidden.feature.inboxPreview.header"/></h1>
    </div>
    <div class="msg-tile-content">
        <h3><mvc:message code="forbidden.feature.inboxPreview.body"/></h3>
        <p><mvc:message code="default.forbidden.tab.premium.feature"/></p>

        <a href="#" class="btn btn-regular" onclick="window.history.back(); return false;">
            <i class="icon icon-angle-left"></i>
            <span class="text"> <mvc:message code="button.Back"/></span>
        </a>

        <a href="mailto:sales@agnitas.de?subject=<mvc:message code="mailing.provider.preview"/>" class="btn btn-regular btn-primary" >
            <i class="icon icon-envelope-o"></i>
            <span class="text"><mvc:message code="forbidden.feature.inboxPreview.button"/></span>
        </a>
    </div>
</div>
