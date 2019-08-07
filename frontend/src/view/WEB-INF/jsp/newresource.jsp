<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:if test="${createNewItemUrl ne null and createNewItemLabel ne null}">
    <li>
        <a href="${createNewItemUrl}" class="btn btn-inverse btn-regular">
            <i class="icon icon-plus"></i>
            <span class="text">${createNewItemLabel}</span>
        </a>
    </li>
</c:if>

<c:if test="${createNewItemUrl2 ne null and createNewItemLabel2 ne null}">
    <li>
        <a href="${createNewItemUrl2}" class="btn btn-inverse btn-regular">
            <i class="icon icon-plus"></i>
            <span class="text">${createNewItemLabel2}</span>
        </a>
    </li>
</c:if>
