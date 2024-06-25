<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="sidemenuActive" type="java.lang.String"--%>
<%--@elvariable id="sidemenuSubActive" type="java.lang.String"--%>

<c:set var="sidemenu_active" 		value="${sidemenuActive}"       scope="request" />
<c:set var="sidemenu_sub_active" 	value="${sidemenuSubActive}"    scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 			        scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Forbidden feature"       scope="request" />
