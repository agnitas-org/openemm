<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComEmmActionAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="emmActionForm" type="com.agnitas.web.forms.ComEmmActionForm"--%>
<%--@elvariable id="emmactionList" type="java.util.List<org.agnitas.actions.EmmAction>"--%>

<c:set var="ACTION_VIEW" value="<%= ComEmmActionAction.ACTION_VIEW %>"/>
<c:set var="ACTION_LIST" value="<%= ComEmmActionAction.ACTION_LIST %>"/>
<c:set var="ACTION_CONFIRM_DELETE" value="<%= ComEmmActionAction.ACTION_CONFIRM_DELETE %>"/>
<c:set var="ACTION_BULK_CONFIRM_DELETE" value="<%= ComEmmActionAction.ACTION_BULK_CONFIRM_DELETE %>"/>
<c:set var="ACTION_SAVE_ACTIVENESS" value="<%= ComEmmActionAction.ACTION_SAVE_ACTIVENESS %>"/>

<html:form action="/action">
    <html:hidden property="action"/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "action-overview": {
                "rows-count": ${emmActionForm.numberOfRows}
            }
        }
    </script>

    <div class="tile" data-controller="emm-activeness">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="default.Overview"/></h2>
            <ul class="tile-header-actions">
                <emm:ShowByPermission token="actions.delete">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-pencil"></i>
                            <span class="text"><bean:message key="bulkAction"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>

                        <ul class="dropdown-menu">
                            <li>
                                <a href="#" data-form-confirm="${ACTION_BULK_CONFIRM_DELETE}">
                                    <bean:message key="bulkAction.delete.action"/>
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
                            <logic:iterate collection="${emmActionForm.columnwidthsList}" indexId="i" id="width">
                                <html:hidden property="columnwidthsList[${i}]"/>
                            </logic:iterate>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
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
                                <agn:agnRadio property="activenessFilter" value="active" data-field-filter="" style="margin: 0;"/>
                                <b><bean:message key="workflow.view.status.active"/></b>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <agn:agnRadio property="activenessFilter" value="inactive" data-field-filter="" style="margin: 0;"/>
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
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change="" data-form-submit="">
                                    <i class="icon icon-refresh"></i>
                                    <span class="text"><bean:message key="button.Apply"/></span>
                                </button>
                            </p>
                        </li>

                    </ul>
                </div>
            </div>

            <div class="table-wrapper table-overflow-visible">
                <display:table class="table table-bordered table-striped table-hover js-table"
                               id="emmaction"
                               name="emmactionList"
                               sort="external"
                               pagesize="${emmActionForm.numberOfRows}"
                               requestURI="/action.do?action=${ACTION_LIST}&__fromdisplaytag=true"
                               excludedParams="*">

                    <!-- Prevent table controls/headers collapsing when the table is empty -->
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <emm:ShowByPermission token="actions.delete">
                        <display:column class="js-checkable" sortable="false" title="<input type='checkbox' data-form-bulk='bulkID'/>">
                            <html:checkbox property="bulkID[${emmaction.id}]"></html:checkbox>
                        </display:column>
                    </emm:ShowByPermission>

                    <display:column headerClass="js-table-sort" titleKey="action.Action" sortable="true" sortProperty="shortname">
                        <span class="multiline-auto">${emmaction.shortname}</span>
                    </display:column>

                    <display:column headerClass="js-table-sort" titleKey="Description"
                                    sortable="true" sortProperty="description">
                        <span class="multiline-auto">${emmaction.description}</span>
                    </display:column>

                    <display:column headerClass="js-table-sort" titleKey="used">
                        <logic:greaterThan name="emmaction" property="used" value="0">
                           <span class="badge badge-highlighted" data-tooltip="<bean:message key="default.Name"/>: ${emmaction.formNames}">
                                <bean:message key="Yes"/>
                            </span>
                        </logic:greaterThan>
                        <logic:lessThan name="emmaction" property="used" value="1">
                            <span class="badge"><bean:message key="No"/></span>
                        </logic:lessThan>
                    </display:column>

                    <display:column headerClass="js-table-sort" titleKey="default.creationDate" sortable="true"
                                    format="{0,date,yyyy-MM-dd}" property="creationDate" sortProperty="creationDate"/>

                    <display:column headerClass="js-table-sort" titleKey="default.changeDate" sortable="true"
                                    format="{0,date,yyyy-MM-dd}" property="changeDate" sortProperty="changeDate"/>

                    <display:column class="table-actions align-center js-checkable" headerClass="js-table-sort squeeze-column js-filter-activeness" titleKey="mailing.status.active"
                                    sortable="true" sortProperty="active">
                        <label class="toggle">
                            <input type="checkbox" ${emmaction.isActive ? 'checked' : ''} data-item-id="${emmaction.id}"
                                   data-initial-state="${emmaction.isActive}" data-action="toggle-active"/>
                            <div class="toggle-control"></div>
                        </label>
                    </display:column>

                    <display:column class="table-actions">
                        <emm:ShowByPermission token="actions.change">
                            <html:link styleClass="js-row-show hidden" titleKey="action.Edit_Action" page="/action.do?action=${ACTION_VIEW}&actionID=${emmaction.id}"></html:link>
                        </emm:ShowByPermission>
                        <emm:ShowByPermission token="actions.delete">
                            <c:set var="actionsDelete">
                                <bean:message key="action.ActionsDelete"/>
                            </c:set>
                            <agn:agnLink styleClass="btn btn-regular btn-alert js-row-delete" data-tooltip="${actionsDelete}" page="/action.do?action=${ACTION_CONFIRM_DELETE}&actionID=${emmaction.id}&fromListPage=true"><i class="icon icon-trash-o"></i></agn:agnLink>
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
