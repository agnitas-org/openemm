<%@ page import="com.agnitas.emm.core.datasource.enums.DataSourceType" %>
<%@ page import="com.agnitas.emm.core.datasource.enums.SourceGroupType" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="datasources" type="org.json.JSONArray"--%>
<%--@elvariable id="datasourceForm" type="com.agnitas.web.forms.PaginationForm"--%>

<mvc:message var="datasourceIdMsg" code="recipient.DatasourceId" />
<mvc:message var="descriptionMsg"  code="Description" />
<mvc:message var="timestampMsg"    code="recipient.Timestamp" />
<mvc:message var="typeMsg"         code="default.Type" />

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "datasource-overview": {
                "rows-count": ${datasourceForm.numberOfRows}
            }
        }
    </script>
    
    <div id="table-tile" class="tile" data-editable-tile="main">
        <div class="tile-body vstack gap-3">
            <div class="notification-simple notification-simple--info">
                <p><mvc:message code="recipient.datasource.info"/></p>
            </div>
            <div class="table-wrapper" data-web-storage="datasource-overview" data-js-table="datasource-id-overview">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp" />
                    </div>
                </div>

                <script id="datasource-id-overview" type="application/json">
                    {
                        "columns": [
                             {
                                "field": "id",
                                "headerName": "${datasourceIdMsg}",
                                "cellStyle": {"user-select": "text"},
                                "type": "numberColumn",
                                "editable": false,
                                "cellRenderer": "StringCellRenderer"
                            },
                            {
                                "field": "description",
                                "headerName": "${descriptionMsg}",
                                "cellStyle": {"user-select": "text"},
                                "type": "textCaseInsensitiveColumn",
                                "cellRenderer": "NotEscapedStringCellRenderer"
                            },
                            {
                                "field": "type",
                                "headerName": "${typeMsg}",
                                "editable": false,
                                "suppressSizeToFit": true,
                                "cellRenderer": "MustacheTemplateCellRender",
                                "cellRendererParams": {"templateName": "datasource-type"}
                            },
                            {
                                "headerName": "${timestampMsg}",
                                "editable": false,
                                "sort": "desc",
                                "field": "timestamp",
                                "type": "dateColumn",
                                "suppressSizeToFit": true
                            }
                        ],
                        "data": ${datasources},
                        "options": {"viewLinkTemplate": "datasource-view-link"}
                    }
                </script>
            </div>
        </div>
    </div>
    <div id="filter-tile" class="tile" data-toggle-tile="" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-secondary" id="reset-filter" data-form-clear="#filter-tile" data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" id="apply-filter" data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body vstack gap-3 js-scrollable">
            <div>
                <label class="form-label" for="id-filter">${datasourceIdMsg}</label>
                <input type="number" id="id-filter" class="form-control" placeholder="${datasourceIdMsg}" pattern="\d+"/>
            </div>
            <div>
                <label class="form-label" for="description-filter">${descriptionMsg}</label>
                <input type="text" id="description-filter" class="form-control" placeholder="${descriptionMsg}"/>
            </div>
            <div data-date-range>
                <label class="form-label" for="timestamp-from-filter">${timestampMsg}</label>
                <div class="date-picker-container mb-1">
                    <input type="text" id="timestamp-from-filter" placeholder="<mvc:message code='From'/>" class="form-control js-datepicker"/>
                </div>
                <div class="date-picker-container">
                    <input type="text" id="timestamp-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker"/>
                </div>
            </div>
            <div>
                <label for="type-filter" class="form-label">${typeMsg}</label>
                <select id="type-filter" class="form-control">
                    <option value=""><mvc:message code="default.All"/></option>
                    <c:forEach var="type" items="${DataSourceType.values()}">
                        <option value="${type}"><mvc:message code="${type.messageKey}" /></option>
                    </c:forEach>
                </select>
            </div>
        </div>
    </div>
</div>

<script id="datasource-type" type="text/x-mustache-template">
    <c:forEach var="type" items="${DataSourceType.values()}">
        {{ if ('${type}' === value) { }}
            <span class="text-truncate-table"><mvc:message code="${type.messageKey}" /></span>
        {{ } }}
    </c:forEach>
</script>

<script id="datasource-view-link" type="text/x-mustache-template">
   {{ if (sourceGroupType === '${SourceGroupType.SoapWebservices}') { }}
      {{ if (extraData) { }}
        /administration/wsmanager/user/{{- extraData }}/view.action
      {{ } else { }}
        /administration/wsmanager/users.action
      {{ } }}
   {{ } else if (sourceGroupType === '${SourceGroupType.AutoinsertForms}') { }}
      /webform/list.action
   {{ } else { }}
      /recipient/list.action?dataSourceId={{- id }}
   {{ } }}
</script>
