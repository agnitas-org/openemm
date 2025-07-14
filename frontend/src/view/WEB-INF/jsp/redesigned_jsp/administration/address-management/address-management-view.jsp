<%@ page import="com.agnitas.emm.core.address_management.enums.AddressManagementCategory" %>
<%@ page contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="searchForm" type="com.agnitas.emm.core.address_management.form.AddressManagementSearchForm"--%>
<%--@elvariable id="categories" type="java.util.List<com.agnitas.emm.core.address_management.enums.AddressManagementCategory>"--%>
<%--@elvariable id="availableClients" type="java.util.List<com.agnitas.beans.Company>"--%>
<%--@elvariable id="entries" type="java.util.Map<com.agnitas.emm.core.address_management.enums.AddressManagementCategory, java.util.List>"--%>

<mvc:form id="address-management-form" servletRelativeAction="/address-management.action" method="GET" modelAttribute="searchForm"
          cssClass="tiles-container" data-form="resource" data-controller="address-management" data-editable-view="${agnEditViewKey}">

    <script data-initializer="address-management" type="application/json">
        {
            "email": ${emm:toJson(searchForm.email)}
        }
    </script>

    <div class="tiles-block flex-column" style="flex: 604">
        <div id="search-tile" class="tile flex-none h-auto" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="default.search" /></h1>
            </div>
            <div class="tile-body">
                <label for="email-address" class="form-label"><mvc:message code="settings.RestfulUser.email" /> *</label>
                <div class="row g-1">
                    <div class="col">
                        <mvc:text id="email-address" path="email" cssClass="form-control" placeholder="${emailPlaceholder}" data-field="required" />
                    </div>
                    <div class="col-auto">
                        <button type="button" class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code="Search" />">
                            <i class="icon icon-search"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>
        <div id="categories-tile" class="tile" style="flex: 1" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="grid.mediapool.categories" /></h1>
            </div>
            <div class="tile-body js-scrollable">
                <ul class="vstack gap-2 h-100">
                    <c:forEach var="category" items="${categories}">
                        <c:set var="categoryEntries" value="${entries.get(category)}" />
                        <c:set var="entriesCount" value="${fn:length(categoryEntries)}" />

                        <li class="flex-grow-1">
                            <button type="button" class="btn btn-category w-100 h-100" ${entriesCount gt 0 ? '' : 'disabled'} data-toggle-tab="#${category}-tab"
                                    data-store-tab-state="false" data-entry-category-tab="${category}">
                                <b><mvc:message code="${category.messageKey}" /></b>
                                <span>${entriesCount}</span>
                            </button>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </div>
    <div id="category-content-tile" class="tile" style="flex: 1226" data-editable-tile>
        <c:choose>
            <c:when test="${entriesExists}">
                <c:forEach var="category" items="${categories}">
                    <c:if test="${fn:length(entries.get(category)) gt 0}">
                        <div id="${category}-tab" class="tile-body" data-entry-category="${category}">
                            <div class="table-wrapper" data-web-storage="address-management-view" data-js-table="address-management-${category}">
                                <div class="table-wrapper__header">
                                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                                    <div class="table-wrapper__controls">
                                        <div class="bulk-actions hidden">
                                            <p class="bulk-actions__selected">
                                                <span><%-- Updates by JS --%></span>
                                                <mvc:message code="default.list.entry.select" />
                                            </p>
                                            <div class="bulk-actions__controls">
                                                <a href="#" class="icon-btn icon-btn--primary" data-tooltip="<mvc:message code="button.Replace" />" data-action="bulk-replace">
                                                    <i class="icon icon-pen"></i>
                                                </a>
                                                <a href="#" class="icon-btn icon-btn--danger" data-tooltip="<mvc:message code="button.Delete" />" data-action="bulk-delete">
                                                    <i class="icon icon-trash-alt"></i>
                                                </a>
                                            </div>
                                        </div>

                                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                                        <jsp:include page="../../common/table/entries-label.jsp" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="tile-body">
                    <div class="notification-simple notification-simple--lg">
                        <i class="icon icon-info-circle"></i>
                        <span><mvc:message code="noResultsFound" /></span>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <c:if test="${entriesExists}">
        <script id="address-management-${AddressManagementCategory.RECIPIENTS}" type="application/json">
            {
                "columns": [
                  {
                    "headerName": "<mvc:message code="recipient.Salutation" />",
                    "editable": false,
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "address-management-gender"},
                    "field": "gender"
                  },
                  {
                    "headerName": "<mvc:message code="Firstname" />",
                    "editable": false,
                    "cellRenderer": "StringCellRenderer",
                    "field": "firstname"
                  },
                  {
                    "headerName": "<mvc:message code="Lastname" />",
                    "editable": false,
                    "cellRenderer": "StringCellRenderer",
                    "field": "lastname"
                  },
                  {
                    "headerName": "<mvc:message code='default.creationDate'/>",
                    "editable": false,
                    "field": "creationTimeMs",
                    "type": "dateTimeColumn",
                    "suppressSizeToFit": true
                  },
                  {
                    "type": "tableActionsColumn",
                    "buttons": [{"name": "replace", "template": "email-replace-btn"}, {"name": "delete", "template": "delete-entry-btn"}]
                  }
                ],
                "data": ${emm:toJson(entries.get(AddressManagementCategory.RECIPIENTS))},
                "options": {
                    "viewLinkTemplate": "/recipient/{{- id }}/view.action"
                }
            }
        </script>

        <script id="address-management-${AddressManagementCategory.IMPORT_PROFILES}" type="application/json">
            {
                "columns": [
                  {
                    "headerName": "<mvc:message code="default.Name" />",
                    "editable": false,
                    "cellRenderer": "StringCellRenderer",
                    "field": "name"
                  },
                  {
                    "type": "tableActionsColumn",
                    "buttons": [{"name": "replace", "template": "email-replace-btn"}, {"name": "delete", "template": "delete-entry-btn"}]
                  }
                ],
                "data": ${emm:toJson(entries.get(AddressManagementCategory.IMPORT_PROFILES))},
                "options": {
                    "viewLinkTemplate": "/import-profile/{{- id }}/view.action"
                }
            }
        </script>

        <script id="address-management-${AddressManagementCategory.REPORTS}" type="application/json">
            {
                "columns": [
                  {
                    "headerName": "<mvc:message code="default.Name" />",
                    "editable": false,
                    "cellRenderer": "StringCellRenderer",
                    "field": "name"
                  },
                  {
                    "headerName": "<mvc:message code="Description" />",
                    "editable": false,
                    "cellRenderer": "StringCellRenderer",
                    "field": "description"
                  },
                  {
                    "headerName": "<mvc:message code='default.changeDate'/>",
                    "editable": false,
                    "field": "changeTimeMs",
                    "type": "dateTimeColumn",
                    "suppressSizeToFit": true
                  },
                  {
                    "type": "tableActionsColumn",
                    "buttons": [{"name": "replace", "template": "email-replace-btn"}, {"name": "delete", "template": "delete-entry-btn"}]
                  }
                ],
                "data": ${emm:toJson(entries.get(AddressManagementCategory.REPORTS))},
                "options": {
                    "viewLinkTemplate": "/statistics/report/{{- id }}/view.action"
                }
            }
        </script>

        <script id="address-management-${AddressManagementCategory.USERS}" type="application/json">
            {
                "columns": [
                  {
                    "headerName": "<mvc:message code="logon.username" />",
                    "editable": false,
                    "cellRenderer": "StringCellRenderer",
                    "field": "username"
                  },
                  {
                    "headerName": "<mvc:message code="Firstname" />",
                    "editable": false,
                    "cellRenderer": "StringCellRenderer",
                    "field": "firstname"
                  },
                  {
                    "headerName": "<mvc:message code="Lastname" />",
                    "editable": false,
                    "cellRenderer": "StringCellRenderer",
                    "field": "lastname"
                  },
                  {
                    "headerName": "<mvc:message code="settings.Company" />",
                    "editable": false,
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "address-management-user-client"},
                    "field": "companyId"
                  },
                  {
                    "headerName": "<mvc:message code='admin.login.last'/>",
                    "editable": false,
                    "field": "lastLoginTimeMs",
                    "type": "dateTimeColumn",
                    "suppressSizeToFit": true
                  },
                  {
                    "type": "tableActionsColumn",
                    "buttons": [{"name": "replace", "template": "email-replace-btn"}, {"name": "delete", "template": "delete-entry-btn"}]
                  }
                ],
                "data": ${emm:toJson(entries.get(AddressManagementCategory.USERS))},
                "options": {
                    "viewLinkTemplate": "/admin/{{- id }}/view.action"
                }
            }
        </script>

        <script id="address-management-${AddressManagementCategory.TECHNICAL_CONTACTS}" type="application/json">
            {
                "columns": [
                  {
                    "headerName": "<mvc:message code="settings.Company" />",
                    "editable": false,
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "address-management-client"},
                    "field": "name"
                  },
                  {
                    "type": "tableActionsColumn",
                    "buttons": [{"name": "replace", "template": "email-replace-btn"}, {"name": "delete", "template": "delete-entry-btn"}]
                  }
                ],
                "data": ${emm:toJson(entries.get(AddressManagementCategory.TECHNICAL_CONTACTS))},
                "options": {
                    "viewLinkTemplate": "/administration/company/{{- id }}/view.action"
                }
            }
        </script>

        <%@include file="fragments/extended-tables-config.jspf" %>
    </c:if>
</mvc:form>

<script id="address-management-gender" type="text/x-mustache-template">
    <span class="text-truncate-table">{{- t('import.gender.short.' + value) }}</span>
</script>

<script id="address-management-client" type="text/x-mustache-template">
    <span class="text-truncate-table">{{- value }} ({{- entry.id }})</span>
</script>

<script id="address-management-user-client" type="text/x-mustache-template">
    <c:choose>
        <c:when test="${not empty availableClients}">
            <c:forEach var="client" items="${availableClients}">
                {{ if (${client.id} === value) { }}
                <span class="text-truncate-table">${client.shortname} ({{- value }})</span>
                {{ } }}
            </c:forEach>
        </c:when>
        <c:otherwise>
            <span class="text-truncate-table">{{- value }}</span>
        </c:otherwise>
    </c:choose>
</script>

<script id="delete-entry-btn" type="text/x-mustache-template">
    <a href="#" class="icon-btn icon-btn--danger" data-tooltip="<mvc:message code="button.Delete"/>" data-action="delete-entry" data-entry-id="{{- id }}" data-entry-cid="{{- companyId }}">
        <i class="icon icon-trash-alt"></i>
    </a>
</script>

<script id="email-replace-btn" type="text/x-mustache-template">
    <a href="#" class="icon-btn icon-btn--primary" data-tooltip="<mvc:message code="button.Replace"/>" data-action="single-replace" data-entry-id="{{- id }}" data-entry-cid="{{- companyId }}">
        <i class="icon icon-pen"></i>
    </a>
</script>

<script id="replace-email-modal" type="text/x-mustache-template">
    <div class="modal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title"><mvc:message code="button.Replace"/></h1>
                    <button type="button" class="btn-close" data-confirm-negative>
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>

                <div class="modal-body">
                    <label for="email-address" class="form-label"><mvc:message code="settings.RestfulUser.email" /> *</label>
                    <input id="new-email-address" type="text" class="form-control" placeholder="${emailPlaceholder}">
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" data-action="replace-email">
                        <i class="icon icon-save"></i>
                        <span class="text"><mvc:message code="button.Save"/></span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="confirm-single-delete-modal" type="text/x-mustache-template">
    <div class="modal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title">
                        {{ if (category === '${AddressManagementCategory.RECIPIENTS}') { }}
                            <mvc:message code="recipient.RecipientDelete" />
                        {{ } else if (category === '${AddressManagementCategory.USERS}') { }}
                            <mvc:message code="settings.admin.delete" />
                        {{ } else { }}
                            <mvc:message code="administration.user.email.delete" />
                        {{ } }}
                    </h1>
                    <button type="button" class="btn-close" data-confirm-negative>
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>

                <div class="modal-body">
                    <p>
                        {{ if (category === '${AddressManagementCategory.RECIPIENTS}') { }}
                            <mvc:message code="recipient.delete.question" arguments="${['{{- data.firstname }} {{- data.lastname }}']}" />
                        {{ } else if (category === '${AddressManagementCategory.USERS}') { }}
                            <mvc:message code="settings.admin.delete.question" arguments="${['{{- data.username }}']}" />
                        {{ } else { }}
                            <mvc:message code="administration.user.email.delete.question" />
                        {{ } }}
                    </p>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-danger" data-confirm-positive>
                        <i class="icon icon-trash-alt"></i>
                        <span class="text"><mvc:message code="button.Delete"/></span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="confirm-bulk-delete-modal" type="text/x-mustache-template">
    <div class="modal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title">
                        {{ if (category === '${AddressManagementCategory.RECIPIENTS}') { }}
                            <mvc:message code="bulkAction.delete.recipients" />
                        {{ } else if (category === '${AddressManagementCategory.USERS}') { }}
                            <mvc:message code="bulkAction.delete.admin" />
                        {{ } else { }}
                            <mvc:message code="button.delete.entries"/>
                        {{ } }}
                    </h1>
                    <button type="button" class="btn-close" data-confirm-negative>
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>

                <div class="modal-body">
                    <p>
                        {{ if (category === '${AddressManagementCategory.RECIPIENTS}') { }}
                            <mvc:message code="bulkAction.delete.recipient.question" />
                        {{ } else if (category === '${AddressManagementCategory.USERS}') { }}
                            <mvc:message code="bulkAction.delete.admin.question" />
                        {{ } else { }}
                            <mvc:message code="administration.user.delete.question"/>
                        {{ } }}
                    </p>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-danger" data-confirm-positive>
                        <i class="icon icon-trash-alt"></i>
                        <span class="text"><mvc:message code="button.Delete"/></span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="confirm-delete-all-modal" type="text/x-mustache-template">
    <div class="modal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title"><mvc:message code="button.delete.entries"/></h1>
                    <button type="button" class="btn-close" data-confirm-negative>
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>

                <div class="modal-body">
                    <p><mvc:message code="administration.user.delete.all.question" /></p>
                    {{ if (affectedCategories.some(c => ['${AddressManagementCategory.USERS}', '${AddressManagementCategory.RECIPIENTS}'].includes(c))) { }}
                        <br>
                        <p><mvc:message code="administration.user.note" /></p>
                    {{ } }}
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-danger" data-confirm-positive>
                        <i class="icon icon-trash-alt"></i>
                        <span class="text"><mvc:message code="button.Delete"/></span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</script>
