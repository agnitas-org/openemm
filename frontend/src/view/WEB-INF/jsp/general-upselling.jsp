<%@ page language="java" contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="featureNameKey" type="java.lang.String"--%>

<c:url var="upsellingImageSrc" value="assets/core/images/facelift/general-upselling_old.png"/>

<div class="tile" data-sizing="container">
    <div class="tile-header" data-sizing="top">
        <h2 class="headline">
            <i class="icon icon-bolt"></i>
            <mvc:message code="${featureNameKey}"/>
        </h2>
    </div>

    <div class="tile-content general-upselling" data-sizing="scroll">
        <div class="general-upselling-desc">
            <h3><mvc:message code="${featureNameKey}"/></h3>
            <h1><mvc:message code="general.upselling.headline"/></h1>

            <p><mvc:message code="general.upselling.text"/></p>

            <div class="general-upselling-bottom">
                <div class="general-upselling-contact">
                    <a href="mailto:sales@agnitas.de?Subject=<mvc:message code="${featureNameKey}"/>" class="btn btn-primary btn-large btn-inverse">
                        <i class="icon icon-envelope-o"></i>
                        <mvc:message code="contact.sales"/>
                    </a>
                </div>
                <div class="general-upselling-image">
                    <img src="${upsellingImageSrc}" alt="Upselling"/>
                </div>
            </div>
        </div>
    </div>

    <div class="tile-footer" data-sizing="bottom">
        <a href="javascript:void(0);" class="btn btn-large pull-left" onclick="history.back(); return false;">
            <i class="icon icon-angle-left"></i>
            <span class="text"><mvc:message code="button.Back"/></span>
        </a>
    </div>
</div>
