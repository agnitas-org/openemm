<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="webFormsByActionId" type="java.util.List"--%>
<%--@elvariable id="dependentMailings" type="java.util.List<org.agnitas.emm.core.mailing.beans.LightweightMailing>"--%>

<div class="tile">
    <div class="tile-header">
        <h2 class="headline"><mvc:message code="workflow.panel.forms"/></h2>
    </div>
    <div class="tile-content tile-content-forms">
        <c:if test="${empty webFormsByActionId}">
            <div class="empty-list well">
                <i class="icon icon-info-circle"></i><strong><mvc:message code="default.nomatches"/></strong>
            </div>
        </c:if>

        <c:if test="${not empty webFormsByActionId}">
            <ul class="list-group">
                <c:forEach var="webFormTuple" items="${webFormsByActionId}">
                    <li class="list-group-item">

                        <emm:ShowByPermission token="forms.show">
                            <c:url var="formLink" value="/webform/${webFormTuple.first}/view.action"/>
                            <a href="${formLink}"> ${webFormTuple.second} </a>
                        </emm:ShowByPermission>

                        <emm:HideByPermission token="forms.show">
                            ${webFormTuple.second}
                        </emm:HideByPermission>
                    </li>
                </c:forEach>
            </ul>
        </c:if>
    </div>
</div>

<div class="tile">
    <div class="tile-header">
        <h2 class="headline"><mvc:message code="Mailings"/></h2>
    </div>
    <div class="tile-content tile-content-forms">
        <c:if test="${empty dependentMailings}">
            <div class="empty-list well">
                <i class="icon icon-info-circle"></i><strong><mvc:message code="default.nomatches"/></strong>
            </div>
        </c:if>

        <c:if test="${not empty dependentMailings}">
            <ul class="list-group">
                <c:forEach var="mailing" items="${dependentMailings}">
                    <li class="list-group-item">
                        <a href="<c:url value="/mailing/${mailing.mailingID}/settings.action"/>"> ${mailing.shortname} </a>
                    </li>
                </c:forEach>
            </ul>
        </c:if>
    </div>
</div>
