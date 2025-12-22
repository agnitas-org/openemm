<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@page import="com.agnitas.beans.ProfileFieldMode"%>
<%@ page import="com.agnitas.util.DbColumnType" %>
<%@ page import="com.agnitas.emm.core.service.RecipientStandardField" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="profileForm" type="com.agnitas.emm.core.profilefields.form.ProfileFieldForm"--%>
<%--@elvariable id="fields" type="com.agnitas.beans.impl.ProfileFieldImpl"--%>
<%--@elvariable id="field" type="com.agnitas.emm.core.service.RecipientFieldDescription"--%>

<c:set var="TYPE_NUMBERIC" value="<%= DbColumnType.SimpleDataType.Numeric %>"/>
<c:set var="TYPE_FLOAT" value="<%= DbColumnType.SimpleDataType.Float %>"/>
<c:set var="TYPE_CHARS" value="<%= DbColumnType.SimpleDataType.Characters %>"/>
<c:set var="TYPE_DATE" value="<%= DbColumnType.SimpleDataType.Date %>"/>
<c:set var="TYPE_DATE_TIME" value="<%= DbColumnType.SimpleDataType.DateTime %>"/>

<mvc:message var="deleteMsg" code="settings.profile.ProfileDelete" />
<mvc:message var="profileHistoryMsg" code="profile.history.include" />
<c:url var="deleteUrl" value="/profiledb/delete.action" />

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" servletRelativeAction="/profiledb/profiledb.action" modelAttribute="profileForm" method="GET" cssClass="tile" data-editable-tile="main">

        <script type="application/json" data-initializer="web-storage-persist">
            {
                "profile-field-overview": {
                    "rows-count": ${profileForm.numberOfRows}
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
                                <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${deleteMsg}" data-form-url="${deleteUrl}" data-form-confirm>
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </div>
                        </div>

                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp">
                            <jsp:param name="filteredEntries" value="${profileFields.fullListSize}"/>
                            <jsp:param name="totalEntries" value="${profileFields.notFilteredFullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table var="field" modelAttribute="profileFields" cssClass="table table-hover table--borderless js-table"
                               decorator="com.agnitas.taglib.table.decorator.impl.ProfileFieldListDecorator">

                        <c:set var="checkboxSelectAll">
                            <input class="form-check-input" type="checkbox" data-bulk-checkboxes autocomplete="off" />
                        </c:set>

                        <emm:column title="${checkboxSelectAll}" cssClass="mobile-hidden" headerClass="mobile-hidden">
                            <input class="form-check-input" type="checkbox" name="columns" value="${field.columnName}"
                                   autocomplete="off" ${field.standardField ? 'disabled' : 'data-bulk-checkbox'} />
                        </emm:column>

                        <emm:column titleKey="settings.FieldName" sortable="true" sortProperty="shortname">
                            <div class="hstack gap-2 overflow-wrap-anywhere">
                                <c:if test="${RecipientStandardField.isHistorized(field)}">
                                    <span class="status-badge status.clipboard" data-tooltip="${profileHistoryMsg}"></span>
                                </c:if>
                                <span class="text-truncate-table">${field.shortName}</span>
                            </div>
                        </emm:column>

                        <emm:column titleKey="settings.FieldNameDB" sortable="true" sortProperty="column" property="columnName" />
                        <emm:column titleKey="Description" sortable="true" property="description" />

                        <emm:column titleKey="default.Type" sortable="true" sortProperty="dataType">
                            <span><mvc:message code="${field.simpleDataType.messageKey}"/></span>
                        </emm:column>

                        <emm:column titleKey="settings.Length" headerClass="fit-content" sortable="true" sortProperty="dataTypeLength">
                            <c:if test="${field.characterLength > 0}">
                                <span>${field.characterLength}</span>
                            </c:if>
                        </emm:column>

                        <emm:column titleKey="settings.Default_Value" sortable="true" sortProperty="defaultValue">
                            <span>${fn:escapeXml(field.defaultValue)}</span>
                        </emm:column>

                        <emm:column titleKey="visibility" headerClass="fit-content" sortable="true" sortProperty="modeEdit">
                            <span><mvc:message code="${field.defaultPermission.messageKey}"/></span>
                        </emm:column>

                        <emm:column>
                            <c:if test="${not field.standardField}">
                                <a href="<c:url value="/profiledb/${field.columnName}/view.action" />" class="hidden" data-view-row="page"></a>

                                <a href="${deleteUrl}?columns=${field.columnName}" class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${deleteMsg}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </c:if>
                        </emm:column>
                    </emm:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/profiledb/search.action" modelAttribute="profileForm"
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

        <div class="tile-body form-column js-scrollable">
            <div>
                <label class="form-label" for="filter-fieldName"><mvc:message code="settings.FieldName"/></label>
                <mvc:text id="filter-fieldName" path="filterFieldName" cssClass="form-control"/>
            </div>

            <div>
                <label class="form-label" for="filter-dbFieldName"><mvc:message code="settings.FieldNameDB"/></label>
                <mvc:text id="filter-dbFieldName" path="filterDbFieldName" cssClass="form-control"/>
            </div>

            <div>
                <label class="form-label" for="filter-description"><mvc:message code="Description"/></label>
                <mvc:text id="filter-description" path="filterDescription" cssClass="form-control"/>
            </div>

            <div>
                <label class="form-label" for="filter-type"><mvc:message code="default.Type"/></label>
                <mvc:select id="filter-type" path="filterType" cssClass="form-control">
                    <mvc:option value=""><mvc:message code="default.All" /></mvc:option>

                    <c:forEach var="type" items="${[TYPE_NUMBERIC, TYPE_FLOAT, TYPE_CHARS, TYPE_DATE, TYPE_DATE_TIME]}">
                        <mvc:option value="${type}"><mvc:message code="${type.messageKey}"/></mvc:option>
                    </c:forEach>
                </mvc:select>
            </div>

            <div>
                <label class="form-label" for="filter-visibility"><mvc:message code="visibility"/></label>
                <mvc:select id="filter-visibility" path="filterMode" cssClass="form-control">
                    <mvc:option value=""><mvc:message code="default.All" /></mvc:option>
                    <c:forEach var="mode" items="${ProfileFieldMode.values()}">
                        <mvc:option value="${mode}"><mvc:message code="${mode.messageKey}"/></mvc:option>
                    </c:forEach>
                </mvc:select>
            </div>

            <div>
                <label class="form-label" for="filter-visibility"><mvc:message code="profile.history.include"/></label>
                <mvc:select id="historization-filter" path="historized" cssClass="form-control">
                    <mvc:option value=""><mvc:message code="default.All"/></mvc:option>
                    <mvc:option value="true"><mvc:message code="default.Yes"/></mvc:option>
                    <mvc:option value="false"><mvc:message code="default.No"/></mvc:option>
                </mvc:select>
            </div>
        </div>
    </mvc:form>
</div>
