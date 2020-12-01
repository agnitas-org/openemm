<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url var="editionLogoSrc" value="/layout/0/edition_logo.png"/>
<c:url var="agnitasEmmLogoSvgSrc" value="/layout/0/logo.svg"/>
<c:url var="agnitasEmmLogoPngSrc" value="/layout/0/logo.png"/>

<!DOCTYPE html>
<html:html>
    <head>
        <meta http-equiv="cache-control" content="no-cache">
        <meta http-equiv="pragma" content="no-cache">
        <meta http-equiv="expires" content="0">

        <title><bean:message key="FormNotFoundTitle" /></title>
        <link rel="shortcut icon" href="favicon.ico">

        <tiles:insert page="/WEB-INF/jsp/assets.jsp"/>

    </head>
    <body class="systempage">

    <div class="system-tile" role="main">

        <div class="system-tile-header">
            <div class="logo">
                <img class="logo-image" src="${agnitasEmmLogoSvgSrc}" onerror="this.onerror=null; this.src='${agnitasEmmLogoPngSrc}'">

                <p class="headline"><bean:message key="default.EMM" /></p>
                <p class="version"><bean:message key="default.version" /></p>
            </div>
            <div class="edition-logo">
                <img class="logo-image" src="${editionLogoSrc}">
            </div>
        </div>

        <div class="system-tile-content">
            <div class="align-center">
                <bean:message key="FormNotFoundMessage" />
            </div>

	        <c:if test="${SHOW_SUPPORT_BUTTON}">
	            <div class="form-group vspace-top-20">
	                <div class="col-sm-6 col-sm-push-3">
	                    <agn:agnForm action="/support" >
	                        <html:hidden property="method" value="formSupport" />
	                        <logic:iterate name="supportForm" property="indices" id="index">
	                            <html:hidden property='parameterName[${index}]' />
	                            <html:hidden property='parameterValue[${index}]' />
	                        </logic:iterate>
	                        <html:hidden property="url" />
	                        <button type="button" class="btn btn-large btn-block btn-primary" data-form-submit>
	                            <span><bean:message key="button.Send" /></span>
	                        </button>
	                    </agn:agnForm>
	                </div>
	            </div>
		   	</c:if>
	    </div>

    </div>
    </body>
</html:html>
