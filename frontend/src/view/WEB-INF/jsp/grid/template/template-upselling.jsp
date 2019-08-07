<%@ page language="java" contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>

<div class="tile" data-sizing="container">
    <div class="tile-header" data-sizing="top">
        <h2 class="headline">
            <i class="icon icon-bolt"></i>
            <bean:message key="grid.layout.builder"/>
        </h2>
    </div>

    <div class="tile-content grid-upselling" data-sizing="scroll">
        <div class="grid-upselling--desc">
            <h3><bean:message key="grid.layout.builder"/></h3>
            <h1><bean:message key="grid.layout.builder.teaser.headline"/></h1>

            <p><bean:message key="grid.layout.builder.teaser.text"/></p>

            <a href="mailto:sales@agnitas.de?Subject=<bean:message key="grid.layout.builder"/>" class="btn btn-primary btn-large">
                <i class="icon icon-envelope-o"></i>
                <bean:message key="contact.sales"/>
            </a>
        </div>

    </div>

    <div class="tile-footer" data-sizing="bottom">
        <a href="javascript:void(0);" class="btn btn-large pull-left" onclick="history.back(); return false;">
            <i class="icon icon-angle-left"></i>
            <span class="text"><bean:message key="button.Back" /></span>
        </a>
    </div>
</div>
