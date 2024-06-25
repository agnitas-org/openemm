<%@page import="com.agnitas.beans.ProfileFieldMode"%>
<%@ page import="org.agnitas.util.DbColumnType" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="profileForm" type="com.agnitas.emm.core.profilefields.form.ProfileFieldForm"--%>
<%--@elvariable id="fields" type="com.agnitas.beans.impl.ProfileFieldImpl"--%>

<c:set var="FIELD_MODES" value="<%= ProfileFieldMode.values() %>"/>

<c:set var="TYPE_NUMBERIC" value="<%= DbColumnType.SimpleDataType.Numeric %>"/>
<c:set var="TYPE_FLOAT" value="<%= DbColumnType.SimpleDataType.Float %>"/>
<c:set var="TYPE_CHARS" value="<%= DbColumnType.SimpleDataType.Characters %>"/>
<c:set var="TYPE_DATE" value="<%= DbColumnType.SimpleDataType.Date %>"/>
<c:set var="TYPE_DATE_TIME" value="<%= DbColumnType.SimpleDataType.DateTime %>"/>

<mvc:message var="deleteTooltip" code="settings.profile.ProfileDelete"/>

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" servletRelativeAction="/profiledb/profiledb.action" modelAttribute="profileForm" cssClass="tile" data-editable-tile="main">
        <input type="hidden" name="page" value="${profileFields.pageNumber}"/>
        <input type="hidden" name="sort" value="${profileFields.sortCriterion}"/>
        <input type="hidden" name="dir" value="${profileFields.sortDirection}"/>

        <script type="application/json" data-initializer="web-storage-persist">
            {
                "profile-field-overview": {
                    "rows-count": ${profileForm.numberOfRows}
                }
            }
        </script>

        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
        </div>
        <div class="tile-body">
            <div class="table-box">
                <div class="table-scrollable">
                    <display:table class="table table-hover table-rounded js-table" id="fields" name="profileFields"
                                   sort="external" requestURI="/profiledb/profiledb.action" partialList="true"
                                   size="${profileForm.numberOfRows}" excludedParams="*">

                        <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                        <display:column titleKey="settings.FieldName" sortable="true" sortProperty="shortname" property="shortName"/>
                        <display:column titleKey="settings.FieldNameDB" sortable="true" sortProperty="column" property="columnName"/>
                        <display:column titleKey="Description" sortable="true" sortProperty="description" property="description"/>

                        <display:column titleKey="default.Type" sortable="true" sortProperty="dataType">
                            <mvc:message code="${fields.simpleDataType.messageKey}"/>
                        </display:column>

                        <display:column titleKey="settings.Length" headerClass="fit-content" sortable="true" sortProperty="dataTypeLength">
                            <c:if test="${fields.characterLength > 0}">
                                ${fields.characterLength}
                            </c:if>
                        </display:column>

                        <display:column titleKey="settings.Default_Value" sortable="true" sortProperty="defaultValue">
                            ${fn:escapeXml(fields.defaultValue)}
                        </display:column>

                        <display:column titleKey="visibility" headerClass="fit-content" sortable="true" sortProperty="modeEdit">
                            <mvc:message code="${fields.defaultPermission.messageKey}"/>
                        </display:column>

                        <display:column headerClass="fit-content">
                            <c:if test="${fields.standardField == false}">
                                <c:url var="viewProfileLink" value="/profiledb/${fields.columnName}/view.action"/>
                                <a href="${viewProfileLink}" class="hidden" data-view-row="page"></a>

                                <c:url var="deleteProfileLink" value="/profiledb/${fields.columnName}/delete.action">
                                    <c:param name="from_list_page" value="true" />
                                </c:url>

                                <a href="${deleteProfileLink}" class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="${deleteTooltip}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </c:if>
                        </display:column>
                    </display:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/profiledb/search.action" modelAttribute="profileForm"
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
                    <label class="form-label" for="filter-fieldName"><mvc:message code="settings.FieldName" /></label>
                    <mvc:text id="filter-fieldName" path="filterFieldName" cssClass="form-control"/>
                </div>

                <div class="col-12">
                    <label class="form-label" for="filter-dbFieldName"><mvc:message code="settings.FieldNameDB" /></label>
                    <mvc:text id="filter-dbFieldName" path="filterDbFieldName" cssClass="form-control"/>
                </div>

                <div class="col-12">
                    <label class="form-label" for="filter-description"><mvc:message code="Description" /></label>
                    <mvc:text id="filter-description" path="filterDescription" cssClass="form-control"/>
                </div>

                <div class="col-12">
                    <label class="form-label" for="filter-type"><mvc:message code="default.Type" /></label>
                    <mvc:select id="filter-type" path="filterType" cssClass="form-control">
                        <mvc:option value=""><mvc:message code="default.All" /></mvc:option>

                        <c:forEach var="type" items="${[TYPE_NUMBERIC, TYPE_FLOAT, TYPE_CHARS, TYPE_DATE, TYPE_DATE_TIME]}">
                            <mvc:option value="${type}">
                                <mvc:message code="${type.messageKey}"/>
                            </mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <div class="col-12">
                    <label class="form-label" for="filter-visibility"><mvc:message code="visibility" /></label>
                    <mvc:select id="filter-visibility" path="filterMode" cssClass="form-control">
                        <mvc:option value=""><mvc:message code="default.All" /></mvc:option>
                        <c:forEach var="mode" items="${FIELD_MODES}">
                            <mvc:option value="${mode.name()}">
                                <mvc:message code="${mode.messageKey}"/>
                            </mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
