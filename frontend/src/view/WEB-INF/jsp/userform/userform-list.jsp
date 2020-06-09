<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComUserFormEditAction" %>
<%@ page import="org.agnitas.util.AgnUtils" %>

<%@ taglib prefix="agn"     uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html"    uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic"   uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST"                    value="<%= ComUserFormEditAction.ACTION_LIST %>"/>
<c:set var="ACTION_VIEW"                    value="<%= ComUserFormEditAction.ACTION_VIEW %>"/>
<c:set var="ACTION_CONFIRM_DELETE"          value="<%= ComUserFormEditAction.ACTION_CONFIRM_DELETE %>"/>
<c:set var="ACTION_BULK_CONFIRM_DELETE"     value="<%= ComUserFormEditAction.ACTION_BULK_CONFIRM_DELETE %>"/>
<c:set var="ACTION_SAVE_ACTIVENESS"         value="<%= ComUserFormEditAction.ACTION_SAVE_ACTIVENESS %>"/>
<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN"  value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN %>"/>

<%--@elvariable id="userFormEditForm" type="com.agnitas.web.forms.ComUserFormEditForm"--%>

<html:form action="/userform">
    <html:hidden property="action"/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "userform-overview": {
                "rows-count": ${userFormEditForm.numberOfRows}
            }
        }
    </script>

    <div class="tile" data-controller="emm-activeness">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="default.Overview"/>
            </h2>
            <ul class="tile-header-nav"></ul>

            <ul class="tile-header-actions">
                <emm:ShowByPermission token="forms.delete">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-pencil"></i>
                            <span class="text"><bean:message key="bulkAction"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>
                        <ul class="dropdown-menu">
                            <li>
                                <a href="#" data-form-confirm="${ACTION_BULK_CONFIRM_DELETE}">
                                    <bean:message key="bulkAction.delete.userform"/>
                                </a>
                            </li>
                        </ul>
                    </li>
                </emm:ShowByPermission>

                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><bean:message key="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>

                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><bean:message key="listSize"/></li>
                        <li>
                            <label class="label">
                                <html:radio property="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <html:radio property="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <html:radio property="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-action="${ACTION_LIST}">
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
                                </button>
                            </p>
                            <logic:iterate collection="${userFormEditForm.columnwidthsList}" indexId="i" id="width">
                                <html:hidden property="columnwidthsList[${i}]"/>
                            </logic:iterate>
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
                                    <agn:agnRadio property="activenessFilter" value="active" data-field-filter=""
                                                  style="margin: 0;"/>
                                    <b><bean:message key="default.status.active"/></b>
                                </label>
                            </li>
                            <li>
                                <label class="label">
                                    <agn:agnRadio property="activenessFilter" value="inactive" data-field-filter=""
                                                  style="margin: 0;"/>
                                    <b><bean:message key="workflow.view.status.inActive"/></b>
                                </label>
                            </li>

                            <li class="divider"></li>
                            <li>
                                <a href="#" class="js-dropdown-open" data-form-persist="activenessFilter: ''">
                                    <bean:message key="filter.reset"/>
                                </a>
                            </li>
                            <li class="divider"></li>
                            <li>
                                <p>
                                    <button class="btn btn-block btn-secondary btn-regular" type="button"
                                            data-form-change="" data-form-submit="">
                                        <i class="icon icon-refresh"></i>
                                        <span class="text"><bean:message key="button.Apply"/></span>
                                    </button>
                                </p>
                            </li>

                        </ul>
                    </div>
                </div>

            <div class="table-wrapper table-overflow-visible" data-sizing="scroll">
                <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        id="userform"
                        name="userformlist"
                        pagesize="${userFormEditForm.numberOfRows}"
                        requestURI="/userform.do?action=${ACTION_LIST}&__fromdisplaytag=true&numberOfRows=${userFormEditForm.numberOfRows}"
                        excludedParams="*">

                    <emm:ShowByPermission token="forms.delete">
                        <display:column class="js-checkable" sortable="false"
                                        title="<input type='checkbox' data-form-bulk='bulkID'/>">
                            <html:checkbox property="bulkID[${userform.id}]"/>
                        </display:column>
                    </emm:ShowByPermission>

                    <display:column headerClass="js-table-sort" sortProperty="formName" sortable="true" titleKey="Form">
                        <span class="multiline-auto">${userform.formName}</span>
                    </display:column>

                    <display:column headerClass="js-table-sort" titleKey="Description" sortable="true" sortProperty="description">
                        <span class="multiline-auto">${userform.description}</span>
                    </display:column>

                    <display:column headerClass="js-table-sort" titleKey="userform.usesActions">
                        <c:if test="${userform.usesActions}">
                            <span class="badge badge-highlighted" data-tooltip="<bean:message key="default.Name"/>: ${userform.actionNames}">
                                <bean:message key="default.Yes"/>
                            </span>
                        </c:if>
                        <c:if test="${not userform.usesActions}">
                            <span class="badge"><bean:message key="No"/></span>
                        </c:if>
                    </display:column>

                    <display:column headerClass="js-table-sort" titleKey="default.url" sortable="false" >
                        <span class="multiline-auto">${userFormEditForm.formUrl}${userform.formName}</span>
                    </display:column>

                    <display:column headerClass="js-table-sort" titleKey="default.creationDate" sortable="true" format="{0,date,${adminDateFormat}}" property="creationDate"/>

                    <display:column headerClass="js-table-sort" titleKey="default.changeDate" sortable="true" format="{0,date,${adminDateFormat}}" property="changeDate"/>

                        <display:column class="table-actions align-center js-checkable"
                                        headerClass="js-table-sort squeeze-column js-filter-activeness"
                                        titleKey="mailing.status.active"
                                        sortable="true"
                                        sortProperty="active">
                            <label class="toggle">
                                <input type="checkbox" ${userform.isActive ? 'checked' : ''} data-item-id="${userform.id}"
                                       data-initial-state="${userform.isActive}" data-action="toggle-active"/>
                                <div class="toggle-control"></div>
                            </label>
                        </display:column>

                    <display:column class="table-actions">
                        <html:link titleKey="target.Edit" styleClass="hidden js-row-show"
                                   page="/userform.do?action=${ACTION_VIEW}&formID=${userform.id}"/>

                        <emm:ShowByPermission token="forms.delete">
                            <c:set var="formDeleteMessage" scope="page">
                                <bean:message key="settings.form.delete"/>
                            </c:set>
                            <agn:agnLink class="btn btn-regular btn-alert js-row-delete"
                                         data-tooltip="${formDeleteMessage}"
                                         page="/userform.do?action=${ACTION_CONFIRM_DELETE}&formID=${userform.id}&fromListPage=true">
                                <i class="icon icon-trash-o"></i>
                            </agn:agnLink>
                        </emm:ShowByPermission>
                    </display:column>
                </display:table>
            </div>
        </div>

            <div class="tile-footer">
                <button type="button" class="btn btn-large pull-left" data-action="back">
                    <i class="icon icon-angle-left"></i>
                    <span class="text"><bean:message key="button.Back"/></span>
                </button>

                <button type="button" class="btn btn-large btn-primary pull-right disabled" data-form-set="action: ${ACTION_SAVE_ACTIVENESS}" data-action="save">
                    <i class="icon icon-save"></i>
                    <span class="text"><bean:message key="button.Save"/></span>
                </button>
            </div>
    </div>
</html:form>

<script id="userform-overview-filters" type="text/x-mustache-template" data-initializer="userform-overview-filters">
    <div class='well'>
        <strong><bean:message key="yourCompanyID"/></strong>
        ${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN].company.id}
    </div>
</script>
