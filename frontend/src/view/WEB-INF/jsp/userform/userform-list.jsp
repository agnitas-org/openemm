<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="com.agnitas.emm.core.commons.ActivenessStatus" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.userform.form.UserFormsForm"--%>
<%--@elvariable id="userFormList" type="org.agnitas.beans.impl.PaginatedListImpl<com.agnitas.emm.core.userform.dto.UserFormDto>"--%>
<%--@elvariable id="userFormURLPattern" type="java.lang.String"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>

<mvc:form servletRelativeAction="/webform/list.action" data-form="resource" modelAttribute="form" id="userFormForm">
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "userform-overview": {
                "rows-count": ${form.numberOfRows}
            }
        }
    </script>

    <mvc:hidden path="sort"/>
    <mvc:hidden path="dir"/>
    <mvc:hidden path="page"/>

    <div class="tile" data-controller="emm-activeness-new">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="default.Overview"/>
            </h2>
            <ul class="tile-header-nav"></ul>

            <ul class="tile-header-actions">
                <emm:ShowByPermission token="forms.delete">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-pencil"></i>
                            <span class="text"><mvc:message code="bulkAction"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>
                        <ul class="dropdown-menu">
                            <li>
                                <c:url var="confirmBulkDelete" value="/webform/confirmBulkDelete.action"/>
                                <a href="#" data-form-url="${confirmBulkDelete}" data-form-confirm="">
                                    <mvc:message code="bulkAction.delete.userform"/>
                                </a>
                            </li>
                        </ul>
                    </li>
                </emm:ShowByPermission>

                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><mvc:message code="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>

                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="listSize"/></li>
                        <li>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-submit="" data-form-repsist="page: 1">
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content">
            <div class="hidden">
                <div class="dropdown filter" data-field="filter" data-filter-target=".js-filter-activeness">
                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>

                    <ul class="dropdown-menu dropdown-menu-right">
                        <li>
                            <label class="label">
                                <mvc:radiobutton path="filter" value="${ActivenessStatus.ACTIVE}" data-field-filter="" cssStyle="margin: 0"/>
                                <b><mvc:message code="default.status.active"/></b>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <mvc:radiobutton path="filter" value="${ActivenessStatus.INACTIVE}" data-field-filter="" cssStyle="margin: 0"/>
                                <b><mvc:message code="workflow.view.status.inActive"/></b>
                            </label>
                        </li>

                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filter: '${ActivenessStatus.NONE}'">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button"
                                        data-form-change="" data-form-submit="">
                                    <i class="icon icon-refresh"></i>
                                    <span class="text"><mvc:message code="button.Apply"/></span>
                                </button>
                            </p>
                        </li>

                    </ul>
                </div>
            </div>

            <div class="table-wrapper table-overflow-visible" data-sizing="scroll">

                <c:set var="isDeletionAllowed" value="false"/>
                <emm:ShowByPermission token="forms.delete">
                    <c:set var="isDeletionAllowed" value="true"/>
                </emm:ShowByPermission>

                <display:table id="userForm" name="userFormList" requestURI="/webform/list.action"
                               class="table table-bordered table-striped table-hover js-table"
                                sort="external"
                                partialList="true"
                                size="${userFormList.fullListSize}"
                                pagesize="${userFormList.objectsPerPage}"
                                excludedParams="*">

                    <c:set var="checkboxSelectAll">
                        <input type="checkbox" class="js-bulk-ids" data-form-bulk="bulkIds" title="select all"/>
                    </c:set>

                    <c:if test="${isDeletionAllowed}">
                        <display:column title="${checkboxSelectAll}" sortable="false" class="js-checkable" headerClass="squeeze-column">
                            <input type="checkbox" name="bulkIds" value="${userForm.id}" title="bulk delete"/>
                        </display:column>
                    </c:if>

                    <display:column titleKey="Form" property="name" sortable="true" sortProperty="formName" headerClass="js-table-sort"/>
                    <display:column titleKey="Description" property="description" sortable="true" sortProperty="description" headerClass="js-table-sort"/>

                    <display:column titleKey="userform.usesActions" headerClass="js-table-sort">
                        <c:if test="${userForm.isUseActions()}">
                            <span class="badge badge-highlighted" data-tooltip="<mvc:message code="default.Name"/>: ${StringUtils.join(userForm.actionNames, ", ")}">
                                <mvc:message code="default.Yes"/>
                            </span>
                        </c:if>
                        <c:if test="${not userForm.isUseActions()}">
                            <span class="badge"><mvc:message code="No"/></span>
                        </c:if>
                    </display:column>

                    <display:column headerClass="js-table-sort" titleKey="default.url" sortable="false" >
                        <span class="multiline-auto">${fn:replace(userFormURLPattern, "{user-form-name}", userForm.name)}</span>
                    </display:column>

                    <display:column titleKey="default.creationDate" sortable="true" sortProperty="creation_date" headerClass="js-table-sort">
                        <fmt:formatDate value="${userForm.creationDate}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}" />
                    </display:column>

                    <display:column titleKey="default.changeDate" sortable="true" sortProperty="change_date" headerClass="js-table-sort">
                        <fmt:formatDate value="${userForm.changeDate}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}" />
                    </display:column>

                    <display:column titleKey="mailing.status.active" headerClass="js-table-sort squeeze-column js-filter-activeness"
                                    sortable="true"
                                    sortProperty="active"
                                    class="table-actions align-center js-checkable">
                        <label class="toggle">
                            <input type="checkbox" ${userForm.active ? 'checked' : ''} data-item-id="${userForm.id}"
                                   data-initial-state="${userForm.active}" data-action="toggle-active"/>
                            <div class="toggle-control"></div>
                        </label>
                    </display:column>
                    <c:if test="${isDeletionAllowed}">
                        <display:column class="table-actions">
                            <c:url var="deleteUserFormLink" value="/webform/${userForm.id}/confirmDelete.action"/>
                            <c:set var="deleteMessage"><mvc:message code="button.Delete"/></c:set>
                            <a href="${deleteUserFormLink}" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${deleteMessage}">
                                <i class="icon icon-trash-o"></i>
                            </a>
                        </display:column>
                    </c:if>
                    <display:column headerClass="hidden" class="hidden">
                        <c:url var="viewLink" value="/webform/${userForm.id}/view.action"/>
                        <a href="${viewLink}" class="hidden js-row-show"></a>
                    </display:column>
                </display:table>
            </div>
        </div>

        <div class="tile-footer">
            <c:url var="saveActivenessUrl" value="/webform/saveActiveness.action"/>
            <button type="button" class="btn btn-large btn-primary pull-right disabled" data-form-url="${saveActivenessUrl}" data-action="save">
                <i class="icon icon-save"></i>
                <span class="text"><mvc:message code="button.Save"/></span>
            </button>
        </div>
    </div>
</mvc:form>

<script id="userform-overview-filters" type="text/x-mustache-template" data-initializer="userform-overview-filters">
    <div class='well'>
        <strong><mvc:message code="yourCompanyID"/></strong>
        ${AgnUtils.getCompanyID(pageContext.request)}
    </div>
</script>
