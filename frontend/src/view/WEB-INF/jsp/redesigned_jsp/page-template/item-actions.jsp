<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:forEach var="item" items="${itemActionsSettings}">
    <c:set var="item" value="${item.value}" />

    <c:if test="${item['dropDownItems'] eq null || (item['dropDownItems'] ne null && fn:length(item['dropDownItems']) gt 0)}">
        <li class="<c:if test="${item['dropDownItems'] ne null}">dropdown</c:if><c:if test="${item['cls'] ne null}">${item['cls']}</c:if>">
            <c:set var="content">
                <c:if test="${not empty item['iconBefore']}">
                    <i class="icon ${item['iconBefore']}"></i>
                </c:if>
                <span class="text">${item['name']}</span>
                <c:if test="${not empty item['iconAfter']}">
                    <i class="icon ${item['iconAfter']}"></i>
                </c:if>
            </c:set>

            <!-- element might be button or link START -->
            <c:choose>
                <c:when test="${item['type'] eq 'href'}">
                    <a href="${item['url']}" class="${item['btnCls']}" ${item['extraAttributes']}>${content}</a>
                </c:when>
                <c:otherwise>
                    <c:choose>
                        <c:when test="${not empty item['tooltip']}">
                            <div data-tooltip="${item['tooltip']}" data-tooltip-options="placement: 'bottom'" >
                                <button class="${item['btnCls']}" type="button" ${item['extraAttributes']}>${content}</button>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <button class="${item['btnCls']}" type="button" ${item['extraAttributes']}>${content}</button>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
            <!-- element might be button or link END -->

            <c:if test="${item['dropDownItems'] ne null}">
                <ul class="dropdown-menu">
                    <div class="dropdown__items-container">
                        <c:set var="isTypeWithLabelsStarted" value="false"/>

                        <c:forEach items="${item['dropDownItems']}" var="itemListOption">
                            <c:set var="itemOption" value="${itemListOption.value}" />

                            <c:choose>
                                <c:when test="${itemOption['type'] eq 'radio'}">
                                    <c:if test="${isTypeWithLabelsStarted eq false}">
                                        <c:set var="isTypeWithLabelsStarted" value="true"/>
                                        <li>
                                    </c:if>
                                    <label class="label">
                                        <input type="radio" name="${itemOption['radioName']}" value="${itemOption['radioValue']}" ${itemOption['extraAttributes']}/>
                                        <span class="label-text">
                                                ${itemOption['name']}
                                        </span>
                                    </label>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${isTypeWithLabelsStarted eq true}">
                                        <c:set var="isTypeWithLabelsStarted" value="false"/>
                                        </li>
                                    </c:if>
                                    <c:if test="${itemOption['url'] ne null}">
                                        <li ${itemOption['disabled'] ? "class='disabled'" : ""} ${not empty itemOption['tooltip'] ? "data-tooltip='".concat(itemOption['tooltip']).concat("'") : ""}>
                                            <a tabindex="-1" href="${itemOption['url']}" ${itemOption['extraAttributes']} class="dropdown-item ${itemOption['selected'] ? 'selected' : '?'}">
                                                <c:if test="${itemOption['icon'] ne null}">
                                                    <i class="icon ${itemOption['icon']}"></i>
                                                </c:if>
                                                ${itemOption['name']}
                                            </a>
                                        </li>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                        <c:if test="${isTypeWithLabelsStarted eq true}">
                            <c:set var="isTypeWithLabelsStarted" value="false"/>
                            </li>
                        </c:if>
                    </div>
                </ul>
            </c:if>
        </li>
    </c:if>
</c:forEach>
