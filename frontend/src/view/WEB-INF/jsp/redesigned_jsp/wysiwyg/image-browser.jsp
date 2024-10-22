<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag" %>
<%@ taglib prefix="fmt"        uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm"        uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"        uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="ext"        uri="https://emm.agnitas.de/jsp/jstl/extended" %>
<%@ taglib prefix="fn"         uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"          uri="http://java.sun.com/jsp/jstl/core" %>

<div class="tile" style="height: 100vh" data-controller="wysiwyg-image-browser">
    <script data-initializer="wysiwyg-image-browser" type="application/json">
        {
            "rdirDomain": "${rdirDomain}",
            "companyId": "${companyId}"
        }
    </script>

    <div class="tile-header p-2">
        <nav class="navbar navbar-expand-lg">
            <a class="chosen-tab btn btn-primary" href="#"><span class="text text-truncate"></span></a>
            <button class="navbar-toggler btn-icon" type="button" data-bs-toggle="offcanvas" data-bs-target="#wysiwyg-image-browse" aria-controls="wysiwyg-image-browse" aria-expanded="false">
                <i class="icon icon-bars"></i>
            </button>
            <div class="collapse navbar-collapse offcanvas" tabindex="-1" id="wysiwyg-image-browse">
                <ul class="navbar-nav offcanvas-body">
                    <li class="nav-item">
                        <a class="btn btn-outline-primary active" href="#" data-toggle-tab="#other-images-tab" data-bs-dismiss="offcanvas" data-image-tab-name="other">
                            <span class="text-truncate"><mvc:message code="others"/></span>
                        </a>
                    </li>

                    <%@include file="./fragments/mediapool-images-tab-header-button.jspf" %>
                </ul>
            </div>
        </nav>
    </div>

    <%@include file="./fragments/other-images-tab-content.jspf" %>
    <%@include file="./fragments/mediapool-images-tab-content.jspf" %>

    <div class="tile-footer tile-footer--buttons border-top">
        <button type="button" class="btn btn-danger" data-action="close-window">
            <i class="icon icon-times"></i>
            <span class="text"><mvc:message code="button.Cancel" /></span>
        </button>
        <button type="button" class="btn btn-primary" data-action="submit-image">
            <i class="icon icon-save"></i>
            <span class="text"><mvc:message code="button.Apply"/></span>
        </button>
    </div>
</div>
