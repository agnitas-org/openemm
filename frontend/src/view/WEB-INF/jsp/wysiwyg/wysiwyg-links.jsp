<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${emm:isJoditEditorUsageAllowed(pageContext.request)}">
    <c:set var="JODIT_EDITOR_PATH" value="${emm:joditEditorPath(pageContext.request)}" scope="page" />

    <link rel="stylesheet" href="${pageContext.request.contextPath}/${JODIT_EDITOR_PATH}/jodit.min.css"/>
    <script type="text/javascript" src="${pageContext.request.contextPath}/${JODIT_EDITOR_PATH}/jodit.min.js"></script>
</c:if>
