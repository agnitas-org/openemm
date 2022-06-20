<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix= "fn" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="supportMailAddress" type="java.lang.String"--%>
<%--@elvariable id="iframeUrl" type="java.lang.String"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.logon.forms.LogonForm"--%>
<%--@elvariable id="layoutdir" type="java.lang.String"--%>

<s:message var="title" code="logon.title"/>

<c:url var="editionLogoSrc" value="/layout/0/edition_logo.png"/>
<c:url var="agnitasEmmLogoSvgSrc" value="/layout/0/logo.svg"/>
<c:url var="agnitasEmmLogoPngSrc" value="/layout/0/logo.png"/>

<c:if test="${not empty layoutdir and layoutdir != 'assets/core'}">
    <%-- Use custom title and edition logo --%>
    <s:message var="title" code="logon.title.${fn:substringAfter(layoutdir, 'assets/')}" text="${title}"/>
</c:if>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>${title}</title>

        <link rel="shortcut icon" href="<c:url value="/favicon.ico"/>">

        <tiles:insert page="/WEB-INF/jsp/assets.jsp"/>
    </head>
    <body data-initializer="logon" class="hidden">
        <div class="l-logon-mask" id="l-logon-mask">
            <mvc:form id="logon-form" servletRelativeAction="/logon.action" modelAttribute="form"
                      data-form="resource"
                      data-resource-selector="#logon-form"
                      data-form-focus="username"
                      data-disable-controls="input-mask">

                <script id="config:logon" type="application/json">
                    {
                        "SHOW_TAB_HINT": ${SHOW_TAB_HINT}
                    }
                </script>
                
                <div class="l-form form-vertical" role="main">
                    <div class="form-group l-logo-box">
                        <img class="l-left-logo" src="${agnitasEmmLogoSvgSrc}" onerror="this.onerror=null; this.src='${agnitasEmmLogoPngSrc}'" alt="Logo">

                        <div class="l-edition">
                            <div class="l-edition-name">
                                <span><mvc:message code="default.EMM"/></span>
                            </div>
                            <div class="l-edition-version">
                                <span><mvc:message code="default.version"/></span>
                            </div>
                        </div>

                        <img class="l-right-logo" src="${editionLogoSrc}" alt="Edition Logo"/>
                    </div>


                    <html:messages id="msg" property="username" message="false" >
                        <c:set var="usernameErrorMessage" value="${msg}"/>
                    </html:messages>
                    <div class="form-group ${not empty usernameErrorMessage ? 'has-alert has-feedback' : ''}">
                        <div class="col-md-12">
                            <label for="username" class="control-label">
                                <i class="icon icon-user"></i> <mvc:message code="logon.username"/>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <mvc:text path="username" id="username" cssClass="form-control input-lg" maxlength="180" data-controls-group="input-mask" autocomplete="username"/>

                            <c:if test="${not empty usernameErrorMessage}">
                                <span class="icon icon-state-alert form-control-feedback"></span>
                                <div class="form-control-feedback-message" >${fn:escapeXml(usernameErrorMessage)}</div>
                            </c:if>
                        </div>
                    </div>

                    <html:messages id="msg" property="password" message="false" >
                        <c:set var="passwordErrorMessage" value="${msg}"/>
                    </html:messages>
                    <div class="form-group ${not empty passwordErrorMessage ? 'has-alert has-feedback' : ''}">
                        <div class="col-md-12">
                            <label for="password" class="control-label">
                                <i class="icon icon-key"></i> <mvc:message code="logon.password"/>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <mvc:password path="password" id="password" cssClass="form-control input-lg" showPassword="true" data-controls-group="input-mask" autocomplete="current-password"/>

                            <c:if test="${not empty passwordErrorMessage}">
                                <span class="icon icon-state-alert form-control-feedback"></span>
                                <div class="form-control-feedback-message">${fn:escapeXml(passwordErrorMessage)}</div>
                            </c:if>
                        </div>
                    </div>
                    <div class="form-group l-login-button-group">
                        <div class="col-md-12">
                            <button type="submit" class="btn btn-primary btn-large btn-block" data-controls-group="input-mask">
                                <mvc:message code="logon.login"/> <i class="icon icon-angle-right"></i>
                            </button>
                        </div>
                    </div>
                    <div class="form-group l-bottom-links-group">
                        <div class="col-xs-6">
                            <a href="<c:url value="/logon/reset-password.action"/>">
                                <mvc:message code="logon.password_reset"/>
                            </a>
                        </div>
                        <div class="col-xs-6 align-right">
                            <s:message var="legalNoticeTitle" code="logon.hint" htmlEscape="true"/>
                            <s:message var="legalNoticeMessage" code="logon.security" arguments="${supportMailAddress}" htmlEscape="true"/>

                            <a href="#" data-msg="${legalNoticeTitle}" data-msg-content="${legalNoticeMessage}">
                                <mvc:message code="logon.hint"/>
                            </a>
                        </div>
                    </div>
                    <div class="form-group l-bottom-links-group">
                        <div class="col-xs-6"></div>
                        <div class="col-xs-6 align-right">
                            <a href="https://status.agnitas.de" target="_blank">
                                <mvc:message code="logon.show.status"/>
                            </a>
                        </div>
                    </div>
                </div>

                <div id="notifications-container">
                    <script type="text/javascript" data-message="">
                      <html:messages id="msg" property="org.apache.struts.action.GLOBAL_MESSAGE" message="false">
                      AGN.Lib.Messages('<mvc:message code="Error" javaScriptEscape="true"/>', '${emm:escapeJs(msg)}', 'alert');
                      </html:messages>
                    </script>
                </div>
            </mvc:form>
            <div id="logon-copyright">
                <p><mvc:message code="default.Copyright"/></p>
            </div>
        </div>

        <div class="l-frame-wrapper" id="l-frame-wrapper">
            <iframe src="${iframeUrl}" class="l-frame js-fixed-iframe" id="l-frame" name="l-frame" border="0" frameborder="0" scrolling="auto" width="100%" height="100%">
                Your Browser does not support IFRAMEs, please update!
            </iframe>
        </div>

        <%@include file="/WEB-INF/jsp/additional.jsp"%>
        
        <script>
            window.addEventListener('load', function() {
                if ($('#l-frame').attr('src') == '') {
                    $('#l-frame-wrapper').css('display', 'none');
                    $('#l-logon-mask').css('width', '100%');
                }
            });
        </script>
        
    </body>
</html>
