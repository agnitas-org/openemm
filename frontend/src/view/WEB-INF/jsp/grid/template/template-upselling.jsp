<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div class="tile" data-sizing="container">
    <div class="tile-header" data-sizing="top">
        <h2 class="headline">
            <i class="icon icon-bolt"></i>
            <mvc:message code="grid.layout.builder"/>
        </h2>
    </div>

    <div class="upselling-content grid-upselling" data-sizing="scroll">

        <div class="upselling-headline">
            <h3 class="upselling-title"><mvc:message code="grid.layout.builder"/></h3>
            <h1 class="upselling-header"><mvc:message code="grid.layout.builder.teaser.headline"/></h1>
        </div>
        <div class="upselling-desc">
            <p><mvc:message code="grid.layout.builder.teaser.text"/></p>

            <a href="#" class="more-info-btn">
                <mvc:message code="general.upselling.information"/>
            </a>
        </div>

    </div>

    <div class="tile-footer" data-sizing="bottom">
        <a href="javascript:void(0);" class="btn btn-large pull-left" onclick="history.back(); return false;">
            <i class="icon icon-angle-left"></i>
            <span class="text"><mvc:message code="button.Back" /></span>
        </a>
    </div>
</div>
