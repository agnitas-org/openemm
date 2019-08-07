<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.do" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="system-tile-header">
    <tiles:insert attribute="header"/>
</div>

<div class="system-tile-content">
    <div class="notification notification-success">
        <div class="notification-header">
            <p class="headline">
                <i class="icon icon-state-success"></i>
                <span class="text"><s:message code="default.Success"/></span>
            </p>
        </div>
        <div class="notification-content">
            <p><b><s:message code="password.changed"/></b></p>
            <p>
                <a href="<c:url value="/start.action"/>" class="btn btn-regular btn-primary">
                    <s:message code="Proceed"/>
                </a>
            </p>
        </div>
    </div>
</div>
