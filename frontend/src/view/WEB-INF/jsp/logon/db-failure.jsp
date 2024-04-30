<%@ page isErrorPage="true" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="supportEmergencyUrl" type="java.lang.String"--%>

<c:url var="errorSvgSrc" value="/assets/core/images/facelift/errors_error-500.svg"/>
<c:url var="errorPngSrc" value="/assets/core/images/facelift/errors_error-500.png"/>

<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title><mvc:message code="error.global.title"/></title>

        <link rel="shortcut icon" href="<c:url value="/favicon.ico"/>">

        <tiles:insert page="/WEB-INF/jsp/assets.jsp"/>
    </head>
    <body class="systempage">
        <div class="msg-tile msg-tile-error">
            <div class="msg-tile-header">
                <img src="${errorSvgSrc}" onerror="this.onerror=null; this.src='${errorPngSrc}'">
                <h1><strong>500</strong> DataBase Failure</h1>
            </div>
            <div class="msg-tile-content">
                <h2><mvc:message code="error.global.headline"/></h2>
                <p><mvc:message code="error.global.message" arguments="${supportEmergencyUrl}"/></p>

                <html:messages id="msg" property="org.apache.struts.action.GLOBAL_MESSAGE" message="false">
                    <p style="color: red;">${msg}</p>
                </html:messages>
            </div>
        </div>
    </body>
</html>
