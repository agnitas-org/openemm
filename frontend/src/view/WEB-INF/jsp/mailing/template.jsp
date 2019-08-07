<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tiles:importAttribute/>

<c:choose>
    <c:when test="${isMailingGrid}">
        <div class="tile" data-sizing="container">
            <div class="tile-header" data-sizing="top">
                <tiles:insert attribute="header"/>
            </div>

            <div id="gt-wrapper" class="tile-content"
                 <c:if test="${not isFullscreenTileSizingDisabled}">
                 data-sizing="scroll" data-action="scroll-to" data-scroll-retain=""
                 </c:if>
                    >
                <tiles:insert attribute="content"/>
            </div>

            <c:if test="${not empty footerItems}">
                <div class="tile-footer" data-sizing="bottom">
                    <!-- Footer Buttons BEGIN -->
                    <c:forEach var="footerItem" items="${footerItems}">
                        ${footerItem}
                    </c:forEach>
                    <!-- Footer Buttons END -->
                </div>
            </c:if>
        </div>
    </c:when>

    <c:otherwise>
        <tiles:insert attribute="content"/>
    </c:otherwise>
</c:choose>
