<%@ page language="java" contentType="text/html; charset=utf-8"%>
<%@ page import="com.agnitas.web.ComMailingBaseAction"%>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_MAILING_IMPORT" value="<%= ComMailingBaseAction.ACTION_MAILING_IMPORT %>"    scope="request"/>

<emm:include page="newresource-dashboard-news.jsp"/>

<emm:ShowByPermission token="stats.mailing">
    <emm:ShowByPermission token="mailing.show">
        <li class="dropdown">
            <button class="btn btn-inverse btn-regular dropdown-toggle" data-toggle="dropdown" type="button">
                <i class="icon icon-eye"></i>
                <span class="text"><bean:message key="default.View"/></span>
                <i class="icon icon-caret-down"></i>
            </button>

            <ul class="dropdown-menu">
                <li>
                    <label class="label">
                        <input type="radio" name="view-state" value="block"  data-view="dashboardView" />
                        <span class="label-text">
                            <bean:message key="mailing.content.blockview"/>
                        </span>
                    </label>
                    <label class="label">
                        <input type="radio" name="view-state" value="split" checked data-view="dashboardView" />
                        <span class="label-text">
                            <bean:message key="mailing.content.splitview"/>
                        </span>
                    </label>
                    <label class="label">
                        <input type="radio" name="view-state" value="hidden" data-view="dashboardView" />
                        <span class="label-text">
                            <bean:message key="mailing.content.hidestatistics"/>
                        </span>
                    </label>
                </li>
            </ul>
        </li>
    </emm:ShowByPermission>
</emm:ShowByPermission>

<li class="dropdown">
    <button class="btn btn-inverse btn-regular dropdown-toggle" data-toggle="dropdown" type="button">
        <i class="icon icon-plus"></i>
        <span class="text">
            <bean:message key="New"/>
        </span>
        <i class="icon icon-caret-down"></i>
    </button>

    <ul class="dropdown-menu">
    	<emm:ShowByPermission token="mailing.change">
            <li>
                <a tabindex="-1" href="<html:rewrite page="/mwStart.do?action=init"/>">
                    <bean:message key="mailing.create"/>
                </a>
            </li>
		</emm:ShowByPermission>
        <emm:ShowByPermission token="mailing.import">
            <li>
                <a tabindex="-1" href="<html:rewrite page="/mailingbase.do?action=${ACTION_MAILING_IMPORT}"/>">
                    <bean:message key="mailing.import"/>
                </a>
            </li>
        </emm:ShowByPermission>

        <emm:ShowByPermission token="template.change">
            <li>
                <a tabindex="-1" href="<html:rewrite page="/mailingbase.do?action=4&mailingID=0&isTemplate=true"/>">
                    <bean:message key="mailing.template.create"/>
                </a>
            </li>
        </emm:ShowByPermission>
        <emm:ShowByPermission token="mailing.import">
            <li>
                <a tabindex="-1" href="<html:rewrite page="/mailingbase.do?action=${ACTION_MAILING_IMPORT}&isTemplate=true"/>">
                    <bean:message key="template.import"/>
                </a>
            </li>
        </emm:ShowByPermission>
        <emm:ShowByPermission token="targets.show">
            <li>
                <a tabindex="-1" href="<c:url value='/target/create.action'/>">
                    <bean:message key="target.create"/>
                </a>
            </li>
        </emm:ShowByPermission>
    </ul>
</li>
