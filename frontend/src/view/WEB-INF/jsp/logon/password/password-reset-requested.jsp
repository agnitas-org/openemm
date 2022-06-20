<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.do" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div class="system-tile-header">
    <tiles:insert attribute="header"/>
</div>

<div class="system-tile-content">
    <div class="notification notification-success">
        <div class="notification-header">
            <p class="headline">
                <i class="icon icon-state-success"></i>
                <span class="text"><mvc:message code="default.Success"/></span>
            </p>
        </div>
        <div class="notification-content">
            <p><mvc:message code="passwordReset.mail.sent"/></p>
        </div>
    </div>
</div>
