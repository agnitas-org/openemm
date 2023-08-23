<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.agnitas.util.AgnUtils" %>


<c:url var="translationLink" value="/assets/translation.js">
    <c:param name="ts" value="<%= AgnUtils.getBrowserCacheMarker() %>"/>
</c:url>
<script src="${translationLink}"></script>

<c:url var="applicationCssLink" value="/application.min.css">
    <c:param name="ts" value="<%= AgnUtils.getBrowserCacheMarker() %>"/>
</c:url>
<link type="text/css" rel="stylesheet" href="${applicationCssLink}">

<c:url var="configJsLink" value="/assets/config.js">
    <c:param name="ts" value="<%= AgnUtils.getBrowserCacheMarker() %>"/>
</c:url>
<script src="${configJsLink}"></script>

<c:url var="applicationJsLink" value="/assets/application.min.js">
    <c:param name="ts" value="<%= AgnUtils.getBrowserCacheMarker() %>"/>
</c:url>
<script src="${applicationJsLink}"></script>
