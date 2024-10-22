<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:forEach var="item" items="${itemActionsSettings}">
    <c:set var="item" value="${item.value}" />
    <c:set var="isDropdownBtn" value="${item['dropDownItems'] ne null}" />

    <c:if test="${not isDropdownBtn or fn:length(item['dropDownItems']) gt 0}">
        <li class="${isDropdownBtn ? 'dropdown' : ''} ${item['cls']}" ${item['parentExtraAttributes']}>
            <c:set var="content">
                <c:if test="${not empty item['iconBefore']}">
                    <i class="icon ${item['iconBefore']}"></i>
                </c:if>
                <span class="text">${item['name']}</span>
            </c:set>

            <c:set var="btnCls" value="btn ${isDropdownBtn ? 'dropdown-toggle' : ''} ${item['btnCls']}" />
            <c:set var="btnExtraAttrs" value="${isDropdownBtn ? 'data-bs-toggle=&quot;dropdown&quot;' : ''} ${item['extraAttributes']}" />

            <c:choose>
                <c:when test="${item['type'] eq 'href'}">
                    <a href="${item['url']}" class="${btnCls}" ${btnExtraAttrs}>${content}</a>
                </c:when>
                <c:otherwise>
                    <button class="${btnCls}" type="button" ${btnExtraAttrs}>${content}</button>
                </c:otherwise>
            </c:choose>

            <c:if test="${isDropdownBtn}">
                <ul class="dropdown-menu">
                    <c:forEach items="${item['dropDownItems']}" var="itemListOption">
                        <c:set var="itemOption" value="${itemListOption.value}" />

                        <li ${not empty itemOption['tooltip'] ? "data-tooltip='".concat(itemOption['tooltip']).concat("'") : ""}>
                            <a tabindex="-1" href="${itemOption['url'] ne null ? itemOption['url'] : '#'}" ${itemOption['extraAttributes']} class="dropdown-item ${itemOption['disabled'] ? 'disabled' : ''} ${itemOption['cls']}">
                                ${itemOption['name']}
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </c:if>
        </li>
    </c:if>
</c:forEach>
