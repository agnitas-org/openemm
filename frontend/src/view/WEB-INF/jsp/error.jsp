<%@page import="org.agnitas.emm.core.commons.util.ConfigValue"%>
<%@page import="java.text.MessageFormat"%>
<%@page import="java.util.Enumeration"%>
<%@page import="com.agnitas.messages.I18nString, org.agnitas.emm.core.commons.util.ConfigService, org.agnitas.util.AgnUtils"%>
<%@page import="org.apache.logging.log4j.Logger"%>
<%@page import="org.apache.logging.log4j.LogManager"%>
<%@ page isErrorPage="true" language="java" pageEncoding="UTF-8" import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%
    Logger logger = LogManager.getLogger("error.jsp");

	ConfigService configService = (ConfigService) WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext()).getBean("ConfigService");

    StringBuilder errorBuilder = new StringBuilder();

    if (exception != null) {
        errorBuilder.append(exception.getMessage() + "\n" + AgnUtils.throwableToString(exception, -1));
    }

    errorBuilder.append("\nRequest Parameters:\n");
    Enumeration parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
        String currentParamName = (String) parameterNames.nextElement();
        errorBuilder.append(currentParamName).append(": ").append(request.getParameter(currentParamName)).append("\n");
    }

    errorBuilder.append("\nRequest Attributes:\n");
    errorBuilder.append("IP: ").append(request.getRemoteAddr()).append("\n");
    Enumeration attrNames = request.getAttributeNames();
    while (attrNames.hasMoreElements()) {
        String currentAttrName = (String) attrNames.nextElement();
        errorBuilder.append(currentAttrName).append(": ").append(request.getAttribute(currentAttrName)).append("\n");
    }

    logger.error(errorBuilder.toString(), exception);
%>

<html>

<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <sec:csrfMetaTags />

    <title><mvc:message code="error.global.title"/></title>
    <jsp:include page="/WEB-INF/jsp/assets.jsp"/>

</head>
<body class="systempage">

    <c:url var="errorSvgSrc" value="/assets/core/images/facelift/errors_error-500.svg"/>
    <c:url var="errorPngSrc" value="/assets/core/images/facelift/errors_error-500.png"/>

    <div class="msg-tile msg-tile-error">
        <div class="msg-tile-header">
            <img alt="" src="${errorSvgSrc}" onerror="this.onerror=null; this.src='${errorPngSrc}'">
            <h1><strong>500</strong> <mvc:message code="error.global.title"/></h1>
        </div>
        <div class="msg-tile-content">
            <h2><mvc:message code="error.global.headline"/></h2>
            <p><%= MessageFormat.format(I18nString.getLocaleString("error.global.message", request.getLocale()), configService.getValue(ConfigValue.SupportEmergencyUrl)) %></p>

            <div class="btn-group">
                <a href="#" class="btn btn-regular btn-primary" onclick="window.history.back(); return false;">
                    <i class="icon icon-angle-left"></i>
                    <span class="text"> <mvc:message code="button.Back"/></span>
                </a>
            </div>
        </div>
    </div>

    <script id="error-message" type="text/x-mustache-template">
        <div class="backdrop backdrop-error js-close-error" style="position: fixed; top: 0; left: 0; bottom: 0; right:0; z-index: 1100; background-color: rgba(0,0,0,0.5)">

            <div class="notification notification-alert" style="position: fixed; top: 50%; left: 50%; width: 420px; margin: -80px 0 0 -210px; z-index: 1101;">
                <div class="notification-header">
                    <p class="headline">
                        <i class="icon icon-state-alert"></i>
                        <span class="text"><mvc:message code="error.global.headline"/></span>
                        <i class="icon icon-times-circle close-icon js-close-error"></i>
                    </p>
                </div>

                <div class="notification-content">
					<p><mvc:message code="error.global.headline" arguments="<%= configService.getValue(ConfigValue.SupportEmergencyUrl) %>"/></p>

                    <a href="#" class="btn btn-regular btn-primary vspace-top-10" onclick="location.reload();">
                        <i class="icon icon-repeat"></i>
                        <span class="text"><mvc:message code="error.reload"/></span>
                    </a>
                </div>
            </div>
        </div>
    </script>

</body>
</html>
