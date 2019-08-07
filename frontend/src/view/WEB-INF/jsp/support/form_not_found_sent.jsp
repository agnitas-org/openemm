<%@ page language="java"
	contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:url var="editionLogoSrc" value="/assets/core/images/facelift/edition_logo.png"/>
<c:url var="agnitasEmmLogoSvgSrc" value="/assets/core/images/facelift/agnitas-emm-logo.svg"/>
<c:url var="agnitasEmmLogoPngSrc" value="/assets/core/images/facelift/agnitas-emm-logo.png"/>

<!DOCTYPE html>
<html>
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

          <%@include file="/WEB-INF/jsp/messages.jsp"%>
      </div>
    </body>
</html>
