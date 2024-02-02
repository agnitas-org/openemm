<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="SHOW_SUPPORT_BUTTON" type="java.lang.Boolean"--%>
<%--@elvariable id="userFormSupportForm" type="com.agnitas.web.UserFormSupportForm"--%>

<c:url var="editionLogoSrc" value="/layout/0/edition_logo.png"/>
<c:url var="agnitasEmmLogoSvgSrc" value="/layout/0/logo.svg"/>
<c:url var="agnitasEmmLogoPngSrc" value="/layout/0/logo.png"/>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="cache-control" content="no-cache">
        <meta http-equiv="pragma" content="no-cache">
        <meta http-equiv="expires" content="0">

        <title><mvc:message code="FormNotFoundTitle" /></title>
        <link rel="shortcut icon" href="favicon.ico">

        <tiles:insertTemplate template="/WEB-INF/jsp/assets.jsp"/>

    </head>
    <body class="systempage">

    <div class="system-tile" role="main">

        <div class="system-tile-header">
            <div class="logo">
                <img class="logo-image" src="${agnitasEmmLogoSvgSrc}" onerror="this.onerror=null; this.src='${agnitasEmmLogoPngSrc}'">

                <p class="headline"><mvc:message code="default.EMM" /></p>
                <p class="version"><mvc:message code="default.version" /></p>
            </div>
            <div class="edition-logo">
                <img class="logo-image" src="${editionLogoSrc}">
            </div>
        </div>

        <div class="system-tile-content">
            <div class="align-center">
                <mvc:message code="FormNotFoundMessage" />
            </div>

	        <c:if test="${SHOW_SUPPORT_BUTTON}">
	            <div class="form-group vspace-top-20">
	                <div class="col-sm-6 col-sm-push-3">
	                    <mvc:form servletRelativeAction="/support/sendFormReport.action" modelAttribute="userFormSupportForm">
	                        <c:forEach var="parameter" items="${userFormSupportForm.params}">
	                            <c:forEach var="parameterValue" items="${parameter.value}">
                                    <mvc:hidden path="params['${parameter.key}']" value="${parameterValue}"/>
	                            </c:forEach>
                            </c:forEach>
                            <mvc:hidden path="url" />
	                        <button type="button" class="btn btn-large btn-block btn-primary" data-form-submit>
	                            <span><mvc:message code="button.Send" /></span>
	                        </button>
	                    </mvc:form>
	                </div>
	            </div>
		   	</c:if>
	    </div>

    </div>
    </body>
</html>
