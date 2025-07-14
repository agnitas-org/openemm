<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.service.MailingRecipientExportWorker" %>

<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="tiles"   uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fmt"     uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="fieldsMap" type="java.util.Map"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="deactivatePagination" type="java.lang.Boolean"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.mailing.forms.MailingRecipientsForm"--%>

<c:set var="RECIPIENTS_SHOW_ALL" value="<%= MailingRecipientExportWorker.MAILING_RECIPIENTS_ALL %>"/>
<c:set var="RECIPIENTS_SHOW_OPENED" value="<%= MailingRecipientExportWorker.MAILING_RECIPIENTS_OPENED %>"/>
<c:set var="RECIPIENTS_SHOW_CLICKED" value="<%= MailingRecipientExportWorker.MAILING_RECIPIENTS_CLICKED %>"/>
<c:set var="RECIPIENTS_SHOW_BOUNCED" value="<%= MailingRecipientExportWorker.MAILING_RECIPIENTS_BOUNCED %>"/>
<c:set var="RECIPIENTS_SHOW_UNSUBSCRIBED" value="<%= MailingRecipientExportWorker.MAILING_RECIPIENTS_UNSUBSCRIBED %>"/>

<c:set var="isMailingGrid" value="${not empty gridTemplateId and gridTemplateId gt 0}" scope="request"/>

<mvc:form servletRelativeAction="/mailing/${mailingId}/recipients/list.action" id="form" modelAttribute="form" data-form="resource" method="GET">
    <input type="hidden" name="loadRecipients" value="true">

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "mailing-recipient-overview": {
                "rows-count": ${form.numberOfRows},
                "fields": ${emm:toJson(form.selectedFields)}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <ul class="tile-header-nav">
                <c:choose>
                    <c:when test="${isMailingGrid eq true}">
                        <tiles:insertTemplate template="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
                    </c:when>
                    <c:otherwise>
                        <h2 class="headline">
                            <mvc:message code="default.Overview"/>
                        </h2>
                    </c:otherwise>
                </c:choose>
            </ul>

            <ul class="tile-header-actions">
                <li>
                    <a href="#" class="link" data-form-url="<c:url value='/mailing/${mailingId}/recipients/export.action'/>"
                       data-tooltip="<mvc:message code='export.message.csv'/>" data-form-submit-static data-prevent-load>
                        <i class="icon icon-cloud-download"></i>
                        <mvc:message code="Export"/>
                    </a>
                </li>

                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><mvc:message code="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>

                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="listSize"/></li>
                        <li>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="200"/>
                                <span class="label-text">200</span>
                            </label>
                        </li>

                        <li class="divider"></li>

                        <li class="dropdown-header"><mvc:message code="default.View"/></li>
                        <li>
                            <label class="label">
                                <mvc:radiobutton path="recipientsFilter" value="${RECIPIENTS_SHOW_ALL}"/>
                                <span class="label-text"><mvc:message code="default.All"/></span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="recipientsFilter" value="${RECIPIENTS_SHOW_OPENED}"/>
                                <span class="label-text"><mvc:message code="statistic.opened"/></span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="recipientsFilter" value="${RECIPIENTS_SHOW_CLICKED}"/>
                                <span class="label-text"><mvc:message code="default.clicked"/></span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="recipientsFilter" value="${RECIPIENTS_SHOW_BOUNCED}"/>
                                <span class="label-text"><mvc:message code="recipient.MailingState2"/></span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="recipientsFilter" value="${RECIPIENTS_SHOW_UNSUBSCRIBED}"/>
                                <span class="label-text"><mvc:message code="recipient.status.optout"/></span>
                            </label>
                        </li>

                        <li class="divider"></li>

                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" data-form-change data-form-submit type="button">
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-columns"></i>
                        <span class="text"><mvc:message code="settings.fields"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="settings.fields"/> </li>
                        <li>
                            <p>
                                <mvc:select path="selectedFields" cssClass="form-control js-select" multiple="multiple">
                                    <c:forEach var="field" items="${fieldsMap}">
                                        <c:set var="column" value="${field.key}"/>
                                        <c:set var="fieldName" value="${field.value}"/>

                                        <c:set var="isDefaultField" value="${form.isDefaultColumn(column)}"/>
                                        <c:set var="fieldSelected" value="${form.isSelectedColumn(column)}"/>

                                        <c:if test="${isDefaultField}">
                                            <option title="${column}" value="${column}" disabled>${fieldName}</option>
                                        </c:if>
                                        <c:if test="${not isDefaultField}">
                                            <option title="${column}" value="${column}" ${fieldSelected ? 'selected' : ''}>${fieldName}</option>
                                        </c:if>
                                    </c:forEach>
                                </mvc:select>
                            </p>
                        </li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Refresh"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content">
            <div class="${deactivatePagination ? 'table-wrapper hide-pagination' : 'table-wrapper'}">
                <c:choose>
                    <c:when test="${not form.loadRecipients}">
                        <div class="table-controls">
                            <div class="table-control" style="text-align: center; padding: 10px;">
                                <a href="#" class="btn btn-regular btn-primary" data-form-submit style="display:inline-block;">
                                    <i class="icon icon-refresh"></i>
                                    <span class="text"><mvc:message code="button.load.recipients"/></span>
                                </a>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <%@include file="mailing-recipients-table.jspf" %>
                    </c:otherwise>
                </c:choose>
            <div>
        </div>
    </div>
</mvc:form>
