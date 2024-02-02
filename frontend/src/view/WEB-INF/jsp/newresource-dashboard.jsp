<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%@include file="newresource-dashboard-news.jspf" %>

<emm:ShowByPermission token="stats.mailing">
    <emm:ShowByPermission token="mailing.show">
        <li class="dropdown">
            <button class="btn btn-inverse btn-regular dropdown-toggle" data-toggle="dropdown" type="button">
                <i class="icon icon-eye"></i>
                <span class="text"><mvc:message code="default.View"/></span>
                <i class="icon icon-caret-down"></i>
            </button>

            <ul class="dropdown-menu">
                <li>
                    <label class="label">
                        <input type="radio" name="view-state" value="block"  data-view="dashboardView" />
                        <span class="label-text">
                            <mvc:message code="mailing.content.blockview"/>
                        </span>
                    </label>
                    <label class="label">
                        <input type="radio" name="view-state" value="split" checked data-view="dashboardView" />
                        <span class="label-text">
                            <mvc:message code="mailing.content.splitview"/>
                        </span>
                    </label>
                    <label class="label">
                        <input type="radio" name="view-state" value="hidden" data-view="dashboardView" />
                        <span class="label-text">
                            <mvc:message code="mailing.content.hidestatistics"/>
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
            <mvc:message code="New"/>
        </span>
        <i class="icon icon-caret-down"></i>
    </button>

    <ul class="dropdown-menu">
    	<emm:ShowByPermission token="mailing.change">
                <c:url var="mailingCreateLink" value="/mailing/create.action"/>
            <li>
                <a tabindex="-1" href="${mailingCreateLink}">
                    <mvc:message code="mailing.create"/>
                </a>
            </li>
		</emm:ShowByPermission>
        <emm:ShowByPermission token="mailing.import">
            <li>
                <a tabindex="-1" href="<c:url value="/import/mailing.action"/>">
                    <mvc:message code="mailing.import"/>
                </a>
            </li>
        </emm:ShowByPermission>

        <emm:ShowByPermission token="template.change">
            <li>
                <a tabindex="-1" href="<c:url value="/mailing/new.action?isTemplate=true"/>">
                    <mvc:message code="mailing.template.create"/>
                </a>
            </li>
        </emm:ShowByPermission>
        <emm:ShowByPermission token="mailing.import">
            <li>
                <a tabindex="-1" href="<c:url value="/import/template.action"/>">
                    <mvc:message code="template.import"/>
                </a>
            </li>
        </emm:ShowByPermission>
        <emm:ShowByPermission token="targets.show">
            <li>
                <a tabindex="-1" href="<c:url value='/target/create.action'/>">
                    <mvc:message code="target.create"/>
                </a>
            </li>
        </emm:ShowByPermission>
    </ul>
</li>
