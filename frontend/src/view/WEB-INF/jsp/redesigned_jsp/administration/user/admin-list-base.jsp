<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://displaytag.sf.net"               prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm"%>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<c:set var="baseEndpoint" value="${param.restful ? '/restfulUser' : '/admin'}" />

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" servletRelativeAction="${baseEndpoint}/list.action" modelAttribute="adminListForm" cssClass="tile" data-editable-tile="main">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "admin-overview": {
                    "rows-count": ${adminListForm.numberOfRows}
                }
            }
        </script>

        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
        </div>
        <div class="tile-body">
            <div class="table-box">
                <div class="table-scrollable">
                    <display:table class="table table-rounded js-table table-hover" pagesize="${adminListForm.numberOfRows}"
                                   id="admin" name="adminEntries"
                                   requestURI="${baseEndpoint}/list.action?numberOfRows=${adminListForm.numberOfRows}&__fromdisplaytag=true"
                                   excludedParams="*" size="${adminEntries.fullListSize}" partialList="true">

                        <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                        <display:column titleKey="logon.username" sortProperty="username" sortable="true"
                                        headerClass="js-table-sort" value="${admin.username}"/>

                        <display:column sortProperty="firstname" titleKey="recipient.Firstname" sortable="true"
                                        headerClass="js-table-sort" value="${admin.firstname}"/>

                        <display:column titleKey="recipient.Lastname" sortable="true" sortProperty="fullname"
                                        headerClass="js-table-sort" value="${admin.fullname}"/>

                        <emm:ShowByPermission token="master.companies.show">
                            <display:column titleKey="Company" sortable="true" sortProperty="company_name"
                                            headerClass="js-table-sort" value="${admin.companyName} (${admin.companyID})"/>
                        </emm:ShowByPermission>

                        <display:column titleKey="mailing.MediaType.0" sortable="true" property="email" sortProperty="email"
                                        headerClass="js-table-sort" value="${admin.email}"/>

                        <display:column titleKey="default.creationDate" sortable="true" property="creationDate" sortProperty="creation_date"
                                        headerClass="js-table-sort" format="{0, date, ${adminDateFormat}}" value="${admin.creationDate}"/>

                        <display:column titleKey="admin.login.last" sortable="true" property="loginDate" sortProperty="last_login"
                                        headerClass="js-table-sort" format="{0, date, ${adminDateFormat}}" value="${admin.loginDate}"/>

                        <display:column class="table-actions" headerClass="fit-content">
                            <c:url var="adminDeleteUrl" value="${baseEndpoint}/${admin.id}/confirmDelete.action"/>
                            <c:url var="adminViewUrl" value="${baseEndpoint}/${admin.id}/view.action"/>
                            <c:url var="adminSendUrl" value="${baseEndpoint}/${admin.id}/welcome.action"/>

                            <div>
                                <a href="${adminViewUrl}" class="hidden" data-view-row="page" title="<mvc:message code='settings.admin.edit'/> "></a>
                                <emm:ShowByPermission token="admin.delete">
                                    <c:choose>
                                        <c:when test="${admin.passwordExpired}">
                                            <mvc:message var="adminBanMessage" code="settings.admin.ban.tooltip" />
                                            <a href="#" class="btn btn-icon-sm btn-secondary" data-tooltip="${adminBanMessage}">
                                                <i class="icon icon-ban"></i>
                                            </a>
                                        </c:when>
                                    </c:choose>
                                </emm:ShowByPermission>
                                <c:if test="${not param.restful}">
                                    <emm:ShowByPermission token="admin.sendWelcome">
                                        <mvc:message var="passwordSendMessage" code="password.send" />
                                        <a href="${adminSendUrl}" class="btn btn-icon-sm btn-primary js-row-delete" data-tooltip="${passwordSendMessage}">
                                            <i class="icon icon-paper-plane"></i>
                                        </a>
                                    </emm:ShowByPermission>
                                </c:if>
                                <emm:ShowByPermission token="admin.delete">
                                    <mvc:message var="adminDeleteMessage" code="settings.admin.delete" />
                                    <a href="${adminDeleteUrl}" class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="${adminDeleteMessage}">
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </emm:ShowByPermission>
                            </div>
                        </display:column>
                    </display:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="${baseEndpoint}/search.action" modelAttribute="adminListForm"
              data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="mobile" data-editable-tile="">
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up desktop-hidden"></i><mvc:message code="report.mailing.filter"/>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-icon-sm btn-inverse" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
                <a class="btn btn-icon btn-icon-sm btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <mvc:message var="usernameMsg" code="logon.username" />
                    <label class="form-label" for="filter-username">${usernameMsg}</label>
                    <mvc:text id="filter-username" path="filterUsername" cssClass="form-control" placeholder="${usernameMsg}"/>
                </div>

                <div class="col-12">
                    <mvc:message var="firstNameMsg" code="Firstname" />
                    <label class="form-label" for="filter-firstname">${firstNameMsg}</label>
                    <mvc:text id="filter-firstname" path="searchFirstName" cssClass="form-control" placeholder="${firstNameMsg}"/>
                </div>

                <div class="col-12">
                    <mvc:message var="lastNameMsg" code="Lastname" />
                    <label class="form-label" for="filter-lastname">${lastNameMsg}</label>
                    <mvc:text id="filter-lastname" path="searchLastName" cssClass="form-control" placeholder="${lastNameMsg}"/>
                </div>

                <div class="col-12">
                    <label class="form-label" for="filter-email"><mvc:message code="mailing.MediaType.0" /></label>
                    <mvc:text id="filter-email" path="searchEmail" cssClass="form-control" placeholder="${emailPlaceholder}"/>
                </div>

                <emm:ShowByPermission token="master.companies.show">
                    <div class="col-12">
                        <label class="form-label" for="filter-client"><mvc:message code="settings.Company"/></label>
                        <mvc:select id="filter-client" path="filterCompanyId" cssClass="form-control" data-sort="alphabetic">
                            <mvc:option data-no-sort="" value=""><mvc:message code="default.All"/></mvc:option>
                            <c:forEach var="client" items="${adminListForm.companies}">
                                <mvc:option value="${client.id}">${client.shortname} (${client.id})</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </emm:ShowByPermission>

                <div class="col-12">
                    <label class="form-label" for="filter-usergroup"><mvc:message code="settings.Usergroup"/></label>
                    <mvc:select id="filter-usergroup" path="filterAdminGroupId" cssClass="form-control" data-sort="alphabetic">
                        <mvc:option data-no-sort="" value=""><mvc:message code="default.All"/></mvc:option>
                        <mvc:options itemValue="groupID" itemLabel="shortname" items="${adminListForm.adminGroups}"/>
                    </mvc:select>
                </div>

                <div class="col-12">
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

                <div class="col-12">
                    <label class="form-label" for="filter-creation-from"><mvc:message code="default.creationDate" /></label>
                    <div class="inline-input-range" data-date-range>
                        <div class="date-picker-container">
                            <mvc:message var="fromMsg" code="From" />
                            <mvc:text id="filter-creation-from" path="filterCreationDate.from" placeholder="${fromMsg}" cssClass="form-control js-datepicker" />
                        </div>

                        <div class="date-picker-container">
                            <mvc:message var="toMsg" code="To" />
                            <mvc:text id="filter-creation-to" path="filterCreationDate.to" placeholder="${toMsg}" cssClass="form-control js-datepicker" />
                        </div>
                    </div>
                </div>

                <div class="col-12">
                    <label class="form-label" for="filter-lastLogin-from"><mvc:message code="admin.login.last" /></label>
                    <div class="inline-input-range" data-date-range>
                        <div class="date-picker-container">
                            <mvc:text id="filter-lastLogin-from" path="filterLastLoginDate.from" placeholder="${fromMsg}" cssClass="form-control js-datepicker" />
                        </div>

                        <div class="date-picker-container">
                            <mvc:text id="filter-lastLogin-to" path="filterLastLoginDate.to" placeholder="${toMsg}" cssClass="form-control js-datepicker" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
