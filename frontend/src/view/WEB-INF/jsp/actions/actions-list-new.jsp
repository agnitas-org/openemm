<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.commons.ActivenessStatus" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.action.form.EmmActionsForm"--%>
<%--@elvariable id="emmActions" type="java.util.List<org.agnitas.actions.EmmAction>"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>

<mvc:form servletRelativeAction="/action/list.action" data-form="resource" modelAttribute="form" id="emmActionForm">
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "action-overview": {
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
            <ul class="tile-header-actions">
                <emm:ShowByPermission token="actions.delete">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-pencil"></i>
                            <span class="text"><mvc:message code="bulkAction"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>

                        <ul class="dropdown-menu">
                            <li>
                                <c:url var="confirmBulkDelete" value="/action/confirmBulkDelete.action"/>
                                <a href="#" data-form-url="${confirmBulkDelete}" data-form-confirm="">
                                    <mvc:message code="bulkAction.delete.action"/>
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
                                <b><mvc:message code="workflow.view.status.active"/></b>
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

                <display:table id="emmAction" name="emmActions" requestURI="/action/list.action"
                               class="table table-bordered table-striped table-hover js-table"
                                sort="external"
                                partialList="true"
                                size="${emmActions.fullListSize}"
                                pagesize="${emmActions.objectsPerPage}"
                                excludedParams="*">

                    <!-- Prevent table controls/headers collapsing when the table is empty -->
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <c:set var="checkboxSelectAll">
                        <input type="checkbox" class="js-bulk-ids" data-form-bulk="bulkIds" title="select all"/>
                    </c:set>

                    <c:if test="${isDeletionAllowed}">
                        <display:column title="${checkboxSelectAll}" sortable="false" class="js-checkable" headerClass="squeeze-column">
                            <input type="checkbox" name="bulkIds" value="${emmAction.id}" title="bulk delete"/>
                        </display:column>
                    </c:if>

                    <display:column property="shortname" titleKey="action.Action" sortable="true" sortProperty="shortname" headerClass="js-table-sort" />
                    <display:column property="description" titleKey="Description" sortable="true" sortProperty="description" headerClass="js-table-sort" />

                    <display:column titleKey="used" headerClass="js-table-sort">
                        <c:if test="${emmAction.isUseForms()}">
                            <span class="badge badge-highlighted" data-tooltip="<mvc:message code="default.Name"/>: ${StringUtils.join(emmAction.formNames, ", ")}">
                                <mvc:message code="default.Yes"/>
                            </span>
                        </c:if>
                        <c:if test="${not emmAction.isUseForms()}">
                            <span class="badge"><mvc:message code="No"/></span>
                        </c:if>
                    </display:column>

                    <display:column titleKey="default.creationDate" sortable="true" sortProperty="creation_date" headerClass="js-table-sort">
                        <fmt:formatDate value="${emmAction.creationDate}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}" />
                    </display:column>

                    <display:column titleKey="default.changeDate" sortable="true" sortProperty="change_date" headerClass="js-table-sort">
                        <fmt:formatDate value="${emmAction.changeDate}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}" />
                    </display:column>

                    <display:column titleKey="mailing.status.active" headerClass="js-table-sort squeeze-column js-filter-activeness"
                                    sortable="true"
                                    sortProperty="active"
                                    class="table-actions align-center js-checkable">
                        <label class="toggle">
                            <input type="checkbox" ${emmAction.active ? 'checked' : ''} data-item-id="${emmAction.id}"
                                   data-initial-state="${emmAction.active}" data-action="toggle-active"/>
                            <div class="toggle-control"></div>
                        </label>
                    </display:column>

                    <display:column class="${isDeletionAllowed ? 'table-actions' : 'hidden'}" headerClass="${isDeletionAllowed ? '' : 'hidden'}">
                        <c:url var="viewLink" value="/action/${emmAction.id}/view.action"/>
                        <a href="${viewLink}" class="hidden js-row-show"></a>

                        <c:if test="${isDeletionAllowed}">
                            <c:url var="deleteLink" value="/action/${emmAction.id}/confirmDelete.action"/>
                            <c:set var="deleteMessage"><mvc:message code="button.Delete"/></c:set>
                            <a href="${deleteLink}" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${deleteMessage}">
                                <i class="icon icon-trash-o"></i>
                            </a>
                        </c:if>
                    </display:column>

                </display:table>
            </div>
        </div>

        <div class="tile-footer">
            <c:url var="saveActivenessUrl" value="/action/saveActiveness.action"/>
            <button type="button" class="btn btn-large btn-primary pull-right disabled" data-form-url="${saveActivenessUrl}" data-action="save">
                <i class="icon icon-save"></i>
                <span class="text"><mvc:message code="button.Save"/></span>
            </button>
        </div>
    </div>
</mvc:form>
