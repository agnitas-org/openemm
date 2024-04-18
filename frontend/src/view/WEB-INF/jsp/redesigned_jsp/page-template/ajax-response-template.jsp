<%@ page errorPage="/errorRedesigned.action" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="emailPlaceholder" value="info@yourdomain.com" scope="request"/>

<tiles:insertAttribute name="body"/>
<tiles:insertAttribute name="messages"/>
