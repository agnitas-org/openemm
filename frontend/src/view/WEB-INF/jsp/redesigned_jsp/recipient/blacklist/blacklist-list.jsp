<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="blacklists" type="java.util.List"--%>
<%--@elvariable id="blacklistDto" type="com.agnitas.emm.core.globalblacklist.beans.BlacklistDto"--%>
<%--@elvariable id="blacklistListForm" type="com.agnitas.emm.core.globalblacklist.forms.BlacklistOverviewFilter"--%>
<%--@elvariable id="dateTimeFormat" type="java.text.SimpleDateFormat"--%>

<mvc:message var="blacklistDeleteMessage" code="blacklist.BlacklistDelete" />
<c:url var="saveActionUrl" value="/recipients/blacklist/save.action" />

<mvc:form id="blacklist-overview" cssClass="tiles-container d-flex hidden" servletRelativeAction="/recipients/blacklist/list.action"
     modelAttribute="blacklistListForm" data-controller="blacklist-list" data-form="resource" data-editable-view="${agnEditViewKey}">

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "blacklist-overview": {
                "rows-count": ${blacklistListForm.numberOfRows}
            }
        }
    </script>

    <div class="tiles-block flex-column" style="flex: 3">
        <div id="creation-tile" class="tile h-auto flex-shrink-0" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title"><mvc:message code="recipient.Blacklist"/></h1>
            </div>

            <div class="tile-body">
                <div class="row">
                    <div class="col">
                        <label for="new-entry-email" class="form-label"><mvc:message code="mailing.MediaType.0" /></label>
                        <input id="new-entry-email" class="form-control" placeholder="${emailPlaceholder}" />
                    </div>

                    <div class="col">
                        <label for="new-entry-reason" class="form-label"><mvc:message code="blacklist.reason" /></label>
                        <input id="new-entry-reason" class="form-control" />
                    </div>

                    <div class="col-auto d-flex align-items-end">
                        <button class="btn btn-primary" type="button" data-action="save" data-url="${saveActionUrl}">
                            <i class="icon icon-plus"></i>
                            <mvc:message code="button.Add" />
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div id="table-tile" class="tile" data-editable-tile="main">
            <div class="tile-header">
                <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
            </div>

            <div class="tile-body">
                <div class="table-box">
                    <div class="table-scrollable">
                        <display:table class="table table-hover table-rounded js-table" id="blacklistDto" requestURI="/recipients/blacklist/list.action"
                                       list="${blacklists}" pagesize="${blacklistListForm.numberOfRows gt 0 ? blacklistListForm.numberOfRows : 0}"
                                       excludedParams="*">

                            <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                            <display:column property="email" titleKey="mailing.MediaType.0" sortable="true"
                                            headerClass="js-table-sort" sortProperty="email"/>

                            <display:column property="reason" titleKey="blacklist.reason" sortable="true"
                                            headerClass="js-table-sort" sortProperty="reason" escapeXml="true"/>

                            <display:column property="date" headerClass="js-table-sort" sortProperty="timestamp" titleKey="CreationDate" sortable="true">
                                <emm:formatDate value="${blacklistDto.date}" format="${dateTimeFormat}"/>
                            </display:column>

                            <emm:ShowByPermission token="recipient.change|recipient.delete">
                                <display:column headerClass="fit-content">
                                    <emm:ShowByPermission token="recipient.change">
                                        <a href="#"
                                           data-table-modal="modal-edit-blacklisted-recipient"
                                           data-table-modal-options="email: '${blacklistDto.email}', reason: '${blacklistDto.reason}'" data-view-row></a>
                                    </emm:ShowByPermission>

                                    <emm:ShowByPermission token="recipient.delete">
                                        <c:url var="deleteUrl" value="/recipients/blacklist/confirmDelete.action">
                                            <c:param name="email" value="${blacklistDto.email}"/>
                                        </c:url>

                                        <a href="${deleteUrl}" class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="${blacklistDeleteMessage}">
                                            <i class="icon icon-trash-alt"></i>
                                        </a>
                                    </emm:ShowByPermission>
                                </display:column>
                            </emm:ShowByPermission>
                        </display:table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="filter-tile" class="tile" data-editable-tile style="flex: 1">
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up desktop-hidden"></i><mvc:message code="report.mailing.filter"/>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-icon-sm btn-inverse"  data-form-clear="#filter-tile" data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
                <a class="btn btn-icon btn-icon-sm btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>

        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <label class="form-label" for="filter-email"><mvc:message code="mailing.MediaType.0" /></label>
                    <mvc:text id="filter-email" path="email" cssClass="form-control" placeholder="${emailPlaceholder}"/>
                </div>

                <div class="col-12">
                    <label class="form-label" for="filter-reason"><mvc:message code="blacklist.reason" /></label>
                    <mvc:text id="filter-reason" path="reason" cssClass="form-control"/>
                </div>

                <div class="col-12" data-date-range>
                    <label class="form-label" for="creation-date-from"><mvc:message code="CreationDate"/></label>
                    <div class="date-picker-container mb-1">
                        <mvc:message var="fromMsg" code="From" />
                        <mvc:text id="creation-date-from" path="creationDate.from" placeholder="${fromMsg}" cssClass="form-control js-datepicker"/>
                    </div>
                    <div class="date-picker-container">
                        <mvc:message var="toMsg" code="To" />
                        <mvc:text path="creationDate.to" placeholder="${toMsg}" cssClass="form-control js-datepicker"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
</mvc:form>

<emm:ShowByPermission token="recipient.change">
    <%@ include file="fragments/modal-edit-blacklisted-recipient.jspf" %>
</emm:ShowByPermission>
