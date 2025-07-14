<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<tiles:insertAttribute name="head-tag"/>
<body data-bs-theme="${emmLayoutBase.themeType.name}">
    <tiles:insertAttribute name="body"/>
</body>
</html>
