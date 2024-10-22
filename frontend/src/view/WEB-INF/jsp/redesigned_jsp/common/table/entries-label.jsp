<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<p class="table-wrapper__entries-label">
    <b>
        <c:choose>
            <c:when test="${param.filteredEntries eq null}">
                <span>${param.totalEntries}</span>
            </c:when>
            <c:otherwise>
                <span>${param.filteredEntries}</span>
                <c:if test="${emm:toInt(param.totalEntries, -1) gt -1}">
                    <span> / ${param.totalEntries}</span>
                </c:if>
            </c:otherwise>
        </c:choose>
    </b>
    <span class="text-truncate"><mvc:message code="default.entries" /></span>
</p>
