<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="agnRefresh" type="java.lang.String"--%>
<%--@elvariable id="agnTitleKey" type="java.lang.String"--%>
<%--@elvariable id="agnHighlightKey" type="java.lang.String"--%>

<head data-origin-uri="${emm:originUri(pageContext.request)}">
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <c:if test="${not empty agnRefresh}">
        <meta http-equiv="refresh" content="${agnRefresh}">
        <meta http-equiv="Page-Exit" content="RevealTrans(Duration=1,Transition=1)">
    </c:if>

    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="0">

    <tiles:insertAttribute name="head-extra-meta-tags"/>


    <c:set var="title" scope="page"><mvc:message code="default.A_EMM"/></c:set>
    <c:set var="subtitle" value="" scope="page"/>

    <c:if test="${not empty agnTitleKey}">
        <c:set var="title"><mvc:message code="${agnTitleKey}"/></c:set>
    </c:if>
    <c:if test="${not empty agnHighlightKey}">
        <c:set var="subtitle"><mvc:message code="${agnHighlightKey}"/></c:set>
    </c:if>

    <c:choose>
        <c:when test="${not empty subtitle}">
            <title>${title} - ${subtitle}</title>
        </c:when>
        <c:otherwise>
            <title>${title}</title>
        </c:otherwise>
    </c:choose>

    <link rel="shortcut icon" href="<c:url value="/favicon.ico"/>">
    <!-- Polyfills for wkhtmltopdf -->
    <script src="${pageContext.request.contextPath}/js/lib/workflow/hacks-wkhtmltopdf-qt.js"></script>
    <script src="${pageContext.request.contextPath}/js/lib/workflow/es5-shim-4.5.15.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/lib/workflow/polyfill-7.12.1.min.js"></script>

    <tiles:insertTemplate template="/WEB-INF/jsp/assets.jsp"/>

    <%--here you should put extra css/js links needed for your page--%>
    <tiles:insertAttribute name="head-extra-links"/>

    <script>
        jQuery.isFunction = function(obj) {
          return typeof obj === 'function';
        }
    </script>
</head>
