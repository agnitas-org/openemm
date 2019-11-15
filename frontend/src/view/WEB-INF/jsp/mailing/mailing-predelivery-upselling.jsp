<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url var="upsellingImageSrc" value="assets/core/images/facelift/msgs_msg-inbox-preview.svg"/>
<c:set var="on_error_image" value="/assets/core/images/facelift/msgs_msg-inbox-preview.png"/>

<div class="msg-tile msg-tile-success">
    <div class="msg-tile-header">
        <img alt="" src="${upsellingImageSrc}" onerror="this.onerror=null; this.src='assets/core/images/facelift/msgs_msg-inbox-preview.png'">
        <h1><bean:message key="forbidden.feature.inboxPreview.header"/></h1>
    </div>
    <div class="msg-tile-content">
        <h3><bean:message key="forbidden.feature.inboxPreview.body"/></h3>
        <p><bean:message key="default.forbidden.tab.premium.feature"/></p>

        <a href="#" class="btn btn-regular" onclick="window.history.back(); return false;">
            <i class="icon icon-angle-left"></i>
            <span class="text"> <bean:message key="button.Back"/></span>
        </a>

        <a href="mailto:sales@agnitas.de?subject=<bean:message key="mailing.provider.preview"/>" class="btn btn-regular btn-primary" >
            <i class="icon icon-envelope-o"></i>
            <span class="text"><bean:message key="forbidden.feature.inboxPreview.button"/></span>
        </a>
    </div>
</div>
