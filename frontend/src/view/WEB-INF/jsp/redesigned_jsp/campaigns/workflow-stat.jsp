<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<mvc:form cssClass="tiles-container" id="stat-form" method="GET" servletRelativeAction="/workflow/${id}/statistic.action">
    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="statistic.workflow"/></h1>
        </div>
        
        <div class="tile-body p-2 js-scrollable" style="overflow-y: auto !important;">
            <iframe src="${statisticUrl}" border="0" scrolling="auto" style="width: 100%; height: 100px"
                    frameborder="0">
                Your Browser does not support IFRAMEs, please update!
            </iframe>
        </div>
    </div>
</mvc:form>
