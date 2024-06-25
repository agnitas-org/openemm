<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="nvc" uri="http://java.sun.com/jsp/jstl/fmt" %>

<emm:CheckLogon/>
<emm:Permission token="mailing.components.show"/>

<div class="tile" data-sizing="container" data-controller="wysiwyg-image-browser">
    <script data-initializer="wysiwyg-image-browser" type="application/json">
        {
            "rdirDomain": "${rdirDomain}",
            "companyId": "${companyId}"
        }
    </script>
    <div class="tile-header" data-sizing="top">
        <ul class="tile-header-nav">
            <li image-tab-name="other" class="active">
                <a href="#" data-toggle-tab="#other-images-tab">
                    <mvc:message code="others" />
                </a>
            </li>

            <%@include file="./fragments/mediapool-images-tab-header-button.jspf" %>
        </ul>
    </div>

    <div class="tile-content tile-content-forms" data-sizing="scroll">
        <%@include file="./fragments/other-images-tab-content.jspf" %>
        <%@include file="./fragments/mediapool-images-tab-content.jspf" %>
    </div>

    <div class="tile-footer" data-sizing="bottom">
        <button type="button" class="btn btn-default btn-large" data-action="close-window">
            <i class="icon icon-times"></i>
            <span class="text">Cancel</span>
        </button>
        <button type="button" class="btn btn-primary btn-large pull-right" data-action="submit-image">
            <i class="icon icon-check"></i>
            <span class="text"><mvc:message code="button.Select"/></span>
        </button>
    </div>

</div>
