<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:forEach var="item" items="${newResourceSettings}">
    <c:set var="item" value="${item.value}" />
    <c:if test="${item ne null}">
        <li>
            <a href="${empty item['url'] ? '#' : item['url']}" class="btn ${item['showOnMobile'] ? '' : 'mobile-hidden'}" ${item['extraAttributes']}>
                <i class="icon icon-plus"></i>
                <span class="text"><mvc:message code="button.New"/></span>
            </a>
        </li>
    </c:if>
</c:forEach>
