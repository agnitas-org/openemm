<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<c:set var="agnTitleKey" value="default.EMM" scope="request"/>
<c:set var="agnHighlightKey" value="permission.denied.title" scope="request"/>
<c:set var="agnSubtitleValue" scope="request">
    <i class="icon icon-ban"></i>
    <s:message code="permission.denied.title"/>
</c:set>
