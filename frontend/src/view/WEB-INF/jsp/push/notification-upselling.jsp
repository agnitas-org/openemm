<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>

<div class="tile" data-sizing="container">
    <div class="tile-header" data-sizing="top">
        <h2 class="headline">
            <i class="icon icon-bolt"></i>
            <bean:message key="PushNotifications"/>
        </h2>
    </div>

    <div class="tile-content push-upselling">
        <div class="push-upselling-background" data-sizing="scroll">
            <div class="push-upselling-description">
                <h3><bean:message key="PushNotifications"/></h3>
                <h1><bean:message key="push.teaser.headline"/></h1>

                <p><bean:message key="push.teaser.text"/></p>

                <a href="mailto:sales@agnitas.de?Subject=<bean:message key="PushNotifications"/>" class="btn btn-primary btn-large">
                    <i class="icon icon-envelope-o"></i>
                    <bean:message key="push.teaser.contact.sales"/>
                </a>
            </div>
        </div>
    </div>

    <div class="tile-footer" data-sizing="bottom">
        <a href="javascript:void(0);" class="btn btn-large pull-left" onclick="history.back(); return false;">
            <i class="icon icon-angle-left"></i>
            <span class="text"><bean:message key="button.Back"/></span>
        </a>
    </div>
</div>
