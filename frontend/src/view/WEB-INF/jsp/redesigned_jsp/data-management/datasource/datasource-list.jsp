<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="datasources" type="org.agnitas.beans.impl.PaginatedListImpl<com.agnitas.emm.core.datasource.bean.DataSource>"--%>
<%--@elvariable id="datasourceForm" type="com.agnitas.emm.core.datasource.form.DatasourceForm"--%>

<mvc:message var="datasourceIdMsg" code="recipient.DatasourceId"/>
<mvc:message var="descriptionMsg" code="Description"/>

<c:forEach var="datasource" items="${datasources}">
    <c:url var="viewLink" value="/recipient/list.action?dataSourceId=${datasource['id']}"/>
    <c:set target="${datasource}" property="show" value="${viewLink}"/>
</c:forEach>

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "datasource-overview": {
                "rows-count": ${datasourceForm.numberOfRows}
            }
        }
    </script>
    
    <div id="table-tile" class="tile js-data-table" data-table="datasource-id-overview" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview"/></h1>
        </div>
        <div class="tile-body d-flex flex-column gap-3">
            <div class="notification-simple notification-simple--info">
                <p><mvc:message code="recipient.datasource.info"/></p>
            </div>
            <div class="js-data-table-body" data-web-storage="datasource-overview" style="height: 100%;">
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
                                "editable": false,
                                "cellRenderer": "NotEscapedStringCellRenderer"
                            }
                        ],
                        "data": ${datasources}
                    }
                </script>
            </div>
        </div>
    </div>
    <div id="filter-tile" class="tile" data-toggle-tile="mobile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up desktop-hidden"></i><mvc:message code="report.mailing.filter"/>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-icon-sm btn-inverse" id="reset-filter" data-form-clear="#filter-tile" data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
                <a class="btn btn-icon btn-icon-sm btn-primary" id="apply-filter" data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <label class="form-label" for="id-filter">${datasourceIdMsg}</label>
                    <input type="number" id="id-filter" class="form-control" placeholder="${datasourceIdMsg}" pattern="\d+"/>
                </div>
                <div class="col-12">
                    <label class="form-label" for="description-filter">${descriptionMsg}</label>
                    <input type="text" id="description-filter" class="form-control" placeholder="${descriptionMsg}"/>
                </div>
            </div>
        </div>
    </div>
</div>
