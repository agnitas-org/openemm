<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<li id="new-resource" class="dropdown">
    <button class="btn btn-primary rounded-1 dropdown-toggle header__action" type="button" data-bs-toggle="dropdown" aria-expanded="false">
        <i class="icon icon-plus"></i>
        <span><mvc:message code="New"/></span>
    </button>

    <ul class="dropdown-menu">
        <div class="dropdown__items-container">
            <emm:ShowByPermission token="mailing.change">
                <c:url var="mailingCreateLink" value="/mailing/create.action"/>
                <li>
                    <a class="dropdown-item" href="${mailingCreateLink}">
                        <mvc:message code="mailing.create"/>
                    </a>
                </li>
            </emm:ShowByPermission>
            <emm:ShowByPermission token="mailing.import">
                <c:url var="mailingImportLink" value="/import/mailing.action"/>
                <li>
                    <a class="dropdown-item" href="${mailingImportLink}">
                        <mvc:message code="mailing.import"/>
                    </a>
                </li>
            </emm:ShowByPermission>
    
            <emm:ShowByPermission token="template.change">
                <c:url var="templateCreateLink" value="/mailing/new.action?isTemplate=true"/>
                <li>
                    <a class="dropdown-item" href="${templateCreateLink}">
                        <mvc:message code="mailing.template.create"/>
                    </a>
                </li>
            </emm:ShowByPermission>
            <emm:ShowByPermission token="mailing.import">
                <c:url var="importTemplateLink" value="/import/template.action"/>
                <li>
                    <a class="dropdown-item" href="${importTemplateLink}">
                        <mvc:message code="template.import"/>
                    </a>
                </li>
            </emm:ShowByPermission>
            <emm:ShowByPermission token="targets.show">
                <c:url var="targetCreateLink" value="/target/create.action"/>
                <li>
                    <a class="dropdown-item" href="${targetCreateLink}">
                        <mvc:message code="target.create"/>
                    </a>
                </li>
            </emm:ShowByPermission>
        </div>         
    </ul>
</li>
