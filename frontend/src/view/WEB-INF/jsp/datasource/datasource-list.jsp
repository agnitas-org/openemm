<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="datasources" type="org.agnitas.beans.impl.PaginatedListImpl<com.agnitas.emm.core.datasource.bean.DataSource>"--%>
<%--@elvariable id="datasourceForm" type="com.agnitas.emm.core.datasource.form.DatasourceForm"--%>

<mvc:form servletRelativeAction="/importexport/datasource/list.action"
          id="datasourceForm"
          modelAttribute="datasourceForm"
          data-form="resource">
    <mvc:hidden path="page"/>
    <mvc:hidden path="dir"/>
    <mvc:hidden path="order"/>
    <mvc:hidden path="sort"/>
    
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "datasource-overview": {
                "rows-count": ${datasourceForm.numberOfRows}
            }
        }
    </script>
    
    <div class="tile js-data-table" data-table="datasource-id-overview">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="default.Overview"/></h2>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><mvc:message code="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="listSize"/></li>
                        <li>
                            <label class="label js-data-table-paginate" data-page-size="20"
                                   data-table-body=".js-data-table-body"
                                   data-web-storage="datasource-overview">
                                <span class="label-text">20</span>
                            </label>
                            <label class="label js-data-table-paginate" data-page-size="50"
                                   data-table-body=".js-data-table-body"
                                   data-web-storage="datasource-overview">
                                <span class="label-text">50</span>
                            </label>
                            <label class="label js-data-table-paginate" data-page-size="100"
                                   data-table-body=".js-data-table-body"
                                   data-web-storage="datasource-overview">
                                <span class="label-text">100</span>
                            </label>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        
        <div class="tile-content" data-sizing="scroll">
            <div class="l-tile-recipient-info-box align-left">
                <span> <mvc:message code="recipient.datasource.info"/></span>
            </div>
            <div class="js-data-table-body" data-web-storage="datasource-overview" style="height: 100%;"></div>
        </div>

        <c:forEach var="entry" items="${datasources}">
            <c:url var="viewLink" value="/recipient/list.action?dataSourceId=${entry['id']}"/>
            <c:set target="${entry}" property="show" value="${viewLink}"/>
        </c:forEach>

        <script id="datasource-id-overview" type="application/json">
        {
            "columns": [
                 {
                    "field": "id",
                    "headerName": "<mvc:message code='recipient.DatasourceId'/>",
                    "cellStyle": {"user-select": "text"},
                    "editable": false,
                    "cellRenderer": "StringCellRenderer"
                },
                {
                    "field": "description",
                    "headerName": "<mvc:message code='Description'/>",
                    "cellStyle": {"user-select": "text"},                    
                    "editable": false,
                    "cellRenderer": "NotEscapedStringCellRenderer"
                }
            ],
            "data": ${datasources}
        }
        </script>
    </div>
</mvc:form>
