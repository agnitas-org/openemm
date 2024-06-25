<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.action" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div class="system-tile-header">
    <tiles:insertAttribute name="header"/>
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
            <p><b><mvc:message code="password.changed"/></b></p>
            <p>
                <a href="<c:url value="/start.action"/>" class="btn btn-regular btn-primary">
                    <mvc:message code="Proceed"/>
                </a>
            </p>
        </div>
    </div>
</div>
