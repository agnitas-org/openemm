<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="baseEndpoint" value="${param.restful ? '/restfulUser' : '/admin'}" />

<mvc:message var="passwordSendMsg" code="password.send" />
<mvc:message var="deleteMsg"       code="settings.admin.delete" />
<mvc:message var="adminBanMsg"     code="settings.admin.ban.tooltip" />

<c:set var="sendAuthDataAllowed" value="${not param.restful and emm:permissionAllowed('admin.sendWelcome', pageContext.request)}" />
<c:set var="deleteAllowed"       value="${emm:permissionAllowed('admin.delete', pageContext.request)}" />

<c:url var="sendAuthDataUrl" value="/admin/welcome.action" />
<c:url var="deleteUrl"       value="/admin/deleteRedesigned.action">
    <c:param name="backToUrl" value="${param.restful ? '/restfulUser/list.action' : ''}" />
</c:url>

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" servletRelativeAction="${baseEndpoint}/list.action" modelAttribute="adminListForm" method="GET" cssClass="tile" data-editable-tile="main">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "admin-overview": {
                    "rows-count": ${adminListForm.numberOfRows}
                }
            }
        </script>

        <div class="tile-body">
            <div class="table-wrapper">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <div class="bulk-actions hidden">
                            <p class="bulk-actions__selected">
                                <span><%-- Updates by JS --%></span>
                                <mvc:message code="default.list.entry.select" />
                            </p>
                            <div class="bulk-actions__controls">
                                <a href="#" class="icon-btn icon-btn--primary" data-tooltip="${passwordSendMsg}" data-form-url="${sendAuthDataUrl}"
                                   data-form-submit data-form-method="POST" data-bulk-action="send-auth-data">
                                    <i class="icon icon-paper-plane"></i>
                                </a>
                                <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${deleteMsg}" data-form-url="${deleteUrl}" data-form-confirm data-bulk-action="delete">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </div>
                        </div>

                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp">
                            <jsp:param name="filteredEntries" value="${adminEntries.fullListSize}"/>
                            <jsp:param name="totalEntries" value="${adminEntries.notFilteredFullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table var="admin" modelAttribute="adminEntries" cssClass="table table-hover table--borderless js-table">

                        <c:if test="${deleteAllowed or sendAuthDataAllowed}">
                            <c:set var="checkboxSelectAll">
                                <input class="form-check-input" type="checkbox" data-bulk-checkboxes />
                            </c:set>

                            <emm:column title="${checkboxSelectAll}" cssClass="mobile-hidden" headerClass="mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${admin.id}" data-bulk-checkbox />
                            </emm:column>
                        </c:if>

                        <emm:column titleKey="logon.username" sortProperty="username" sortable="true">
                            <div class="hstack gap-2 overflow-wrap-anywhere">
                                <c:if test="${admin.passwordExpired}">
                                    <span class="status-badge mailing.status.disable" data-tooltip="${adminBanMsg}"></span>
                                </c:if>
                                <span class="text-truncate-table">${admin.username}</span>
                            </div>

                            <a href="<c:url value="${baseEndpoint}/${admin.id}/view.action"/>" class="hidden" data-view-row="page"></a>
                        </emm:column>

                        <emm:column property="firstname" titleKey="recipient.Firstname" sortable="true" />
                        <emm:column titleKey="recipient.Lastname" sortable="true" property="fullname" />

                        <emm:ShowByPermission token="master.companies.show">
                            <emm:column titleKey="Company" sortable="true" sortProperty="company_name">
                                <span>${admin.companyName} (${admin.companyID})</span>
                            </emm:column>
                        </emm:ShowByPermission>

                        <emm:column titleKey="mailing.MediaType.0" sortable="true" property="email" />

                        <emm:column titleKey="default.creationDate" sortable="true" property="creationDate" sortProperty="creation_date" />

                        <emm:column titleKey="admin.login.last" sortable="true" property="loginDate" sortProperty="last_login" />

                        <c:if test="${(sendAuthDataAllowed or deleteAllowed) and adminEntries.fullListSize gt 0}">
                            <emm:column cssClass="table-actions">
                                <div>
                                    <c:if test="${sendAuthDataAllowed}">
                                        <a href="${sendAuthDataUrl}?bulkIds=${admin.id}" class="icon-btn icon-btn--primary js-row-delete" data-tooltip="${passwordSendMsg}" data-bulk-action="send-auth-data">
                                            <i class="icon icon-paper-plane"></i>
                                        </a>
                                    </c:if>
                                    <c:if test="${deleteAllowed}">
                                        <a href="${deleteUrl}&bulkIds=${admin.id}" class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${deleteMsg}" data-bulk-action="delete">
                                            <i class="icon icon-trash-alt"></i>
                                        </a>
                                    </c:if>
                                </div>
                            </emm:column>
                        </c:if>
                    </emm:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="${baseEndpoint}/search.action" modelAttribute="adminListForm"
              data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="" data-editable-tile="">
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-secondary" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body vstack gap-3 js-scrollable">
            <div>
                <mvc:message var="usernameMsg" code="logon.username" />
                <label class="form-label" for="filter-username">${usernameMsg}</label>
                <mvc:text id="filter-username" path="filterUsername" cssClass="form-control" placeholder="${usernameMsg}"/>
            </div>

            <div>
                <mvc:message var="firstNameMsg" code="Firstname" />
                <label class="form-label" for="filter-firstname">${firstNameMsg}</label>
                <mvc:text id="filter-firstname" path="searchFirstName" cssClass="form-control" placeholder="${firstNameMsg}"/>
            </div>

            <div>
                <mvc:message var="lastNameMsg" code="Lastname" />
                <label class="form-label" for="filter-lastname">${lastNameMsg}</label>
                <mvc:text id="filter-lastname" path="searchLastName" cssClass="form-control" placeholder="${lastNameMsg}"/>
            </div>

            <div>
                <label class="form-label" for="filter-email"><mvc:message code="mailing.MediaType.0" /></label>
                <mvc:text id="filter-email" path="searchEmail" cssClass="form-control" placeholder="${emailPlaceholder}"/>
            </div>

            <emm:ShowByPermission token="master.companies.show">
                <div>
                    <label class="form-label" for="filter-client"><mvc:message code="settings.Company"/></label>
                    <mvc:select id="filter-client" path="filterCompanyId" cssClass="form-control" data-sort="alphabetic">
                        <mvc:option data-no-sort="" value=""><mvc:message code="default.All"/></mvc:option>
                        <c:forEach var="client" items="${adminListForm.companies}">
                            <mvc:option value="${client.id}">${client.shortname} (${client.id})</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </emm:ShowByPermission>

            <div>
                <label class="form-label" for="filter-usergroup"><mvc:message code="settings.Usergroup"/></label>
                <mvc:select id="filter-usergroup" path="filterAdminGroupId" cssClass="form-control" data-sort="alphabetic">
                    <mvc:option data-no-sort="" value=""><mvc:message code="default.All"/></mvc:option>
                    <mvc:options itemValue="groupID" itemLabel="shortname" items="${adminListForm.adminGroups}"/>
                </mvc:select>
            </div>

            <div>
                <label class="form-label" for="filter-language"><mvc:message code="Language"/></label>
                <mvc:select id="filter-language" path="filterLanguage" cssClass="form-control">
                    <mvc:option value=""><mvc:message code="default.All"/></mvc:option>
                    <mvc:option value="de"><mvc:message code="settings.German"/></mvc:option>
                    <mvc:option value="en"><mvc:message code="settings.English"/></mvc:option>
                    <mvc:option value="fr"><mvc:message code="settings.French"/></mvc:option>
                    <mvc:option value="es"><mvc:message code="settings.Spanish"/></mvc:option>
                    <mvc:option value="pt"><mvc:message code="settings.Portuguese"/></mvc:option>
                    <mvc:option value="nl"><mvc:message code="settings.Dutch"/></mvc:option>
                    <mvc:option value="it"><mvc:message code="settings.Italian"/></mvc:option>
                </mvc:select>
            </div>

            <div>
                <label class="form-label" for="filter-creation-from"><mvc:message code="default.creationDate" /></label>
                <mvc:dateRange id="filter-creation" inline="true" path="filterCreationDate" options="maxDate: 0" />
            </div>

            <div>
                <label class="form-label" for="filter-lastLogin-from"><mvc:message code="admin.login.last" /></label>
                <mvc:dateRange id="filter-lastLogin" inline="true" path="filterLastLoginDate" options="maxDate: 0" />
            </div>
        </div>
    </mvc:form>
</div>
