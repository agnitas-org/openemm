<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.do" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="layoutdir" type="java.lang.String"--%>

<s:message var="title" code="logon.title"/>

<c:url var="editionLogoSrc" value="/layout/0/edition_logo.png"/>
<c:url var="agnitasEmmLogoSvgSrc" value="/layout/0/logo.svg"/>
<c:url var="agnitasEmmLogoPngSrc" value="/layout/0/logo.png"/>

<c:if test="${not empty layoutdir and layoutdir != 'assets/core'}">
    <%-- Use custom title and edition logo --%>
    <s:message var="title" code="logon.title.${fn:substringAfter(layoutdir, 'assets/')}" text="${title}"/>
</c:if>

<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>${title}</title>

        <tiles:insert page="/WEB-INF/jsp/assets.jsp"/>
    </head>
    <body class="systempage">
        <div class="system-tile" role="main">
            <tiles:insert attribute="body">
                <tiles:put name="header" direct="true">
                    <div class="logo">
                        <img class="logo-image" src="${agnitasEmmLogoSvgSrc}" onerror="this.onerror=null; this.src='${agnitasEmmLogoPngSrc}'" alt="Logo">

                        <p class="headline"><mvc:message code="default.EMM"/></p>
                        <p class="version"><mvc:message code="default.version"/></p>
                    </div>
                    <div class="edition-logo">
                        <img class="logo-image" src="${editionLogoSrc}" alt="Edition Logo">
                    </div>
                </tiles:put>
            </tiles:insert>
        </div>

        <div id="notifications-container">
            <script type="text/javascript" data-message>
              <html:messages id="msg" property="org.apache.struts.action.GLOBAL_MESSAGE" message="false">
                AGN.Lib.Messages('<mvc:message code="Error" javaScriptEscape="true"/>', '${emm:escapeJs(msg)}', 'alert');
              </html:messages>
            </script>
        </div>

        <%@include file="/WEB-INF/jsp/additional.jsp"%>
    </body>
</html>
