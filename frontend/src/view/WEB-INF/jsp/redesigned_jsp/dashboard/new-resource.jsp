<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.agnitas.emm.core.imports.web.ImportController" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="MAILING" value="<%= ImportController.ImportType.MAILING %>" />
<c:set var="LB_TEMPLATE" value="<%= ImportController.ImportType.LB_TEMPLATE %>" />

<c:set var="isRedesignedMailingImportAllowed" value="false"/>
<emm:ShowByPermission token="import.ui.migration">
    <emm:ShowByPermission token="mailing.ui.migration">
        <c:set var="isRedesignedMailingImportAllowed" value="true"/>
    </emm:ShowByPermission>
</emm:ShowByPermission>

<li id="new-resource" class="dropdown">
    <button class="btn dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
        <i class="icon icon-plus"></i>
        <span><mvc:message code="New"/></span>
    </button>

    <ul class="dropdown-menu">
        <div class="dropdown__items-container">
            <emm:ShowByPermission token="mailing.change">
                <emm:HideByPermission token="mailing.content.readonly">
                    
                    <%@ include file="fragments/new-resource-emc-mailing-option.jspf" %>
                    
                    <emm:ShowByPermission token="mailing.classic">
                        <li>
                            <a class="dropdown-item" href='<c:url value="/mailing/templates.action"/>' data-confirm>
                                <mvc:message code="UserRight.mailing.classic"/>
                            </a>
                        </li>
                    </emm:ShowByPermission>
                </emm:HideByPermission>
            </emm:ShowByPermission>
            <emm:ShowByPermission token="mailing.import">
                <li>
                    <c:choose>
                        <c:when test="${isRedesignedMailingImportAllowed}">
                            <a class="dropdown-item" href='<c:url value="/import/file.action?type=${MAILING}"/>' data-confirm>
                                <mvc:message code="mailing.import"/>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a class="dropdown-item" href='<c:url value="/import/mailing.action"/>'>
                                <mvc:message code="mailing.import"/>
                            </a>
                        </c:otherwise>
                    </c:choose>                     
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
                <li>
                    <c:choose>
                        <c:when test="${isRedesignedMailingImportAllowed}">
                            <a class="dropdown-item" href="<c:url value="/import/file.action?type=${LB_TEMPLATE}"/>" data-confirm>
                                <mvc:message code="template.import"/>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a class="dropdown-item" href="<c:url value="/import/template.action"/>">
                                <mvc:message code="template.import"/>
                            </a>
                        </c:otherwise>
                    </c:choose>
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
