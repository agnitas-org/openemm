<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="selector" value="${param.selector}" />
<c:set var="inputPrefix" value="table-${selector}" />
<c:set var="tableTypeInputName" value="${inputPrefix}-type" />

<div class="switch" data-preview-table="${selector}">
    <input type="radio" id="${inputPrefix}-preview" name="${tableTypeInputName}" value="preview" />
    <label for="${inputPrefix}-preview" class="icon icon-image"></label>
    <c:if test="${param.visualList}">
        <input type="radio" id="${inputPrefix}-visual-list" name="${tableTypeInputName}" value="visual-list" />
        <label for="${inputPrefix}-visual-list" class="icon icon-th-list"></label>
    </c:if>
    <input type="radio" id="${inputPrefix}-list" name="${tableTypeInputName}" value="list" checked />
    <label for="${inputPrefix}-list" class="icon icon-align-justify"></label>
</div>
