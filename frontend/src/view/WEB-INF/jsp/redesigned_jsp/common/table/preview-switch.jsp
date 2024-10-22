<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="selector" value="${param.selector}" />

<label class="icon-switch">
    <input type="checkbox" data-preview-table="${selector}" checked>
    <i class="icon icon-image"></i>
    <i class="icon icon-th-list"></i>
</label>
