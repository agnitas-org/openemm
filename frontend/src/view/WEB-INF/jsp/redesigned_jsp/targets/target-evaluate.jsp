<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="targetId" type="java.lang.Integer"--%>
<%--@elvariable id="mailinglists" type="java.util.List<com.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="statistics" type="java.util.List<com.agnitas.reporting.birt.external.beans.RecipientStatusRow>"--%>

<mvc:form id="evaluate-tile" servletRelativeAction="/target/${targetId}/evaluate.action" cssClass="tile" method="GET" cssStyle="flex: 1 1 40%"
          data-editable-tile=""
          data-form="resource"
          data-resource-selector="#evaluate-tile"
          data-controller="target-group-stat"
          data-form-resource-selector="#evaluate-tile">

    <script data-initializer="chart" type="application/json">
        {
            "statistics": ${emm:toJson(statistics)}
        }
    </script>
    <div class="tile-header">
        <h1 class="tile-title text-truncate"><mvc:message code="statistic.Recipient" /></h1>
    </div>
    <div class="tile-body vstack gap-2">
        <div class="d-flex gap-2">
            <select name="mailinglistId" size="1" class="form-control js-select">
                <option value="0"><mvc:message code="statistic.All_Mailinglists" /></option>
                <c:forEach var="mailinglist" items="${mailinglists}">
                    <option value="${mailinglist.id}" ${mailinglist.id eq mailinglistId ? 'selected' : ''}>${mailinglist.shortname}</option>
                </c:forEach>
            </select>
            <button type="button" class="btn btn-primary btn-icon" data-tooltip="<mvc:message code="button.save.evaluate" />" data-form-submit data-prevent-load>
                <i class="icon icon-play-circle"></i>
            </button>
        </div>
        <c:choose>
            <c:when test="${empty statistics}">
                <div class="notification-simple">
                    <i class="icon icon-info-circle"></i>
                    <span><mvc:message code="default.NoEntries"/></span>
                </div>
            </c:when>
            <c:otherwise>
                <div class="flex-grow-1 min-h-0">
                    <canvas id="target-statistic-chart" class="h-100">
                        <%-- Loads by JS --%>
                    </canvas>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</mvc:form>
