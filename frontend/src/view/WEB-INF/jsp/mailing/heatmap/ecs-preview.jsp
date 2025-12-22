<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="isEmptyParamsError" type="java.lang.Boolean"--%>
<%--@elvariable id="isEmptyRecipientError" type="java.lang.Boolean"--%>
<%--@elvariable id="previewAsString" type="java.lang.String"--%>
<%--@elvariable id="heatmapInfo" type="com.agnitas.emm.ecs.web.HeatmapStatInfo"--%>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/jquery/jquery-3.5.1.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/grid/jquery.imagesloaded.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/lodash/lodash-4.17.21.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/ecs/statLabelAdjuster.js"></script>

<c:choose>
    <c:when test="${isEmptyParamsError}">
        <mvc:message code="ecs.Error.NoParams"/>
    </c:when>

    <c:when test="${isEmptyRecipientError}">
        <mvc:message code="ecs.Error.NoTestRecipients"/>
    </c:when>

    <c:otherwise>
        ${previewAsString}

        <c:if test="${not empty heatmapInfo}">
            <c:forEach items="${heatmapInfo.statEntries}" var="entry">
                <input id="info-${entry.id}" type="hidden" name="${entry.color}" value="${entry.value}">
            </c:forEach>
            <input id="info-null-color" type="hidden" value="${heatmapInfo.nullColor}">
        </c:if>
    </c:otherwise>
</c:choose>
