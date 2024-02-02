<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action"%>
<%@ page import="com.agnitas.emm.core.profilefields.bean.ProfileFieldDependentType" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="dependents" type="java.util.List<com.agnitas.emm.core.beans.Dependent>"--%>

<c:set var="DEPENDENT_TYPE_WORKFLOW" value="<%= ProfileFieldDependentType.WORKFLOW %>"/>
<c:set var="DEPENDENT_TYPE_TARGET_GROUP" value="<%= ProfileFieldDependentType.TARGET_GROUP %>"/>

<div class="tile js-data-table" data-table="profile-field-dependents-table">

    <div class="tile-header">
        <h2 class="headline"><mvc:message code="default.usedIn"/></h2>

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
                               data-table-body=".js-data-table-body" data-web-storage="profile-fields-dependents-overview">
                            <span class="label-text">20</span>
                        </label>
                        <label class="label js-data-table-paginate" data-page-size="50"
                               data-table-body=".js-data-table-body" data-web-storage="profile-fields-dependents-overview">
                            <span class="label-text">50</span>
                        </label>
                        <label class="label js-data-table-paginate" data-page-size="100"
                               data-table-body=".js-data-table-body" data-web-storage="profile-fields-dependents-overview">
                            <span class="label-text">100</span>
                        </label>
                    </li>
                </ul>
            </li>
        </ul>
    </div>

    <div class="tile-content">
        <div class="js-data-table-body" data-web-storage="profile-fields-dependents-overview" style="height: 100%;"></div>
    </div>

    <emm:instantiate var="data" type="java.util.LinkedHashMap">
        <c:forEach var="dependent" items="${dependents}" varStatus="status">
            <emm:instantiate var="entry" type="java.util.HashMap">
                <c:set target="${data}" property="${status.index}" value="${entry}"/>

                <c:choose>
                    <c:when test="${dependent.type eq DEPENDENT_TYPE_WORKFLOW}">
                        <c:set target="${entry}" property="entityType">
                            <mvc:message code="workflow.single"/>
                        </c:set>

                        <c:url var="viewLink" value="/workflow/${dependent.id}/view.action"/>
                    </c:when>
                    <c:when test="${dependent.type eq DEPENDENT_TYPE_TARGET_GROUP}">
                        <c:set target="${entry}" property="entityType">
                            <mvc:message code="Target"/>
                        </c:set>

                        <c:url var="viewLink" value="/target/${dependent.id}/view.action"/>
                    </c:when>
                </c:choose>

                <c:set target="${entry}" property="entityName" value="${dependent.shortname}"/>
                <c:set target="${entry}" property="show" value="${viewLink}"/>
            </emm:instantiate>
        </c:forEach>
    </emm:instantiate>

    <script id="profile-field-dependents-table" type="application/json">
        {
            "columns": [
                {
                    "headerName": "<mvc:message code='default.Type'/>",
                    "editable": false,
                    "cellRenderer": "StringCellRenderer",
                    "field": "entityType"
                },
                {
                    "headerName": "<mvc:message code='Name'/>",
                    "editable": false,
                    "cellRenderer": "StringCellRenderer",
                    "field": "entityName"
                }
            ],
            "data": ${emm:toJson(data.values())}
        }
    </script>
</div>
