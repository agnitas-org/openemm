<%@ page language="java" pageEncoding="UTF-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%--@elvariable id="supportMailAddress" type="java.lang.String"--%>
<%--@elvariable id="adminMailAddress" type="java.lang.String"--%>
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
        <sec:csrfMetaTags />

        <title>${title}</title>

        <link rel="shortcut icon" href="<c:url value="/favicon.ico"/>">

        <tiles:insert page="/WEB-INF/jsp/assets.jsp"/>
    </head>
    <body class="systempage">
        <div class="system-tile" role="main">
            <div class="system-tile-header">
                <div class="logo">
                    <img class="logo-image" src="${agnitasEmmLogoSvgSrc}" onerror="this.onerror=null; this.src='${agnitasEmmLogoPngSrc}'" alt="Logo">

                    <p class="headline"><mvc:message code="default.EMM"/></p>
                    <p class="version"><mvc:message code="default.version"/></p>
                </div>
                <div class="edition-logo">
                    <img class="logo-image" src="${editionLogoSrc}" alt="Edition Logo">
                </div>
            </div>
            <div class="system-tile-content">
                <mvc:form servletRelativeAction="/logon/authenticate-host.action" data-form-focus="authenticationCode" modelAttribute="form">
                    <div class="form-group <logic:messagesPresent property="authenticationCode">has-alert has-feedback</logic:messagesPresent>">
                        <div class="col-sm-4">
                            <label class="control-label"><i class="icon icon-unlock"></i> <mvc:message code="logon.hostauth.code"/></label>
                        </div>
                        <div class="col-sm-8">
                            <mvc:text path="authenticationCode" cssClass="form-control" maxlength="20"/>

                            <logic:messagesPresent property="authenticationCode">
                                <html:messages id="msg" property="authenticationCode">
                                    <span class="icon icon-state-alert form-control-feedback"></span>
                                    <div class="form-control-feedback-message">${msg}</div>
                                </html:messages>
                            </logic:messagesPresent>
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="trustedDevice"><mvc:message code="logon.hostauth.trustDevice"/></label>
                        </div>
                         <div class="col-sm-8">
                         	<label class="toggle">
                         		<mvc:checkbox path="trustedDevice" id="trustedDevice" value="true" />
                         		<div class="toggle-control"></div>
                         	</label>
                         </div>
                    </div>
                    
                    <div class="form-group">
                        <div class="col-sm-offset-4 col-sm-8">
                            <button type="submit" class="btn btn-primary btn-large btn-block">
                                <mvc:message code="logon.hostauth.authenticate"/> <i class="icon icon-angle-right"></i>
                            </button>
                        </div>
                    </div>
                </mvc:form>
            </div>
            <div class="system-tile-footer">
                <div class="pull-left">
                    <a href="<c:url value="/logon/reset-password.action"/>">
                        <mvc:message code="logon.password_reset"/>
                    </a>
                </div>
                <div class="pull-right">
                    <s:message var="logonHint" code="logon.hint" htmlEscape="true"/>
                    <s:message var="logonHintMessage" code="logon.security" arguments="${supportMailAddress}" htmlEscape="true"/>

                    <a href="#" data-msg-system="system" data-msg="${logonHint}" data-msg-content="${logonHintMessage}">
                        <mvc:message code="logon.hint"/>
                    </a>
                </div>
            </div>
        </div>

        <div id="notifications-container">
            <script type="text/javascript" data-message="">
                <html:messages id="msg" property="org.apache.struts.action.GLOBAL_MESSAGE" message="false">
                    AGN.Lib.Messages('<mvc:message code="Error"/>', '${emm:escapeJs(msg)}', 'alert');
                </html:messages>
                <html:messages id="msg" property="de.agnitas.GLOBAL_WARNING">
                    AGN.Lib.Messages('<mvc:message code="warning"/>', '${emm:escapeJs(msg)}', 'warning');
                </html:messages>
            </script>
        </div>

        <%@include file="/WEB-INF/jsp/additional.jsp"%>
    </body>
</html>
