<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="isMailingGrid" type="java.lang.Boolean"--%>
<%--@elvariable id="isFullscreenTileSizingDisabled" type="java.lang.Boolean"--%>
<%--@elvariable id="footerItems" type="java.util.List"--%>

<tiles:importAttribute/>

<c:choose>
    <c:when test="${isMailingGrid}">
        <div class="tile" data-sizing="container">
            <div class="tile-header" data-sizing="top">
                <tiles:insertAttribute name="header"/>
            </div>

            <div id="gt-wrapper" class="tile-content"
                 <c:if test="${not isFullscreenTileSizingDisabled}">
                 data-sizing="scroll" data-scroll-retain=""
                 </c:if>
            >
                <tiles:insertAttribute name="content"/>
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
        <tiles:insertAttribute name="content"/>
    </c:otherwise>
</c:choose>
