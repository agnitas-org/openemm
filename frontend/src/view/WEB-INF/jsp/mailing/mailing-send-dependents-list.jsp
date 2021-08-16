<%@ page language="java" import="org.agnitas.web.StrutsActionBase" contentType="text/html; charset=utf-8" errorPage="/error.do"%>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>
<c:set var="ACTION_VIEW" value="<%= StrutsActionBase.ACTION_VIEW %>" scope="page"/>
<c:set var="ACTION_MAILING_SEND_VIEW" value="<%=ComMailingSendActionBasic.ACTION_VIEW_SEND%>" scope="page"/>

<agn:agnForm action="/mailingsend" id="dependentListMailSendForm">
    <html:hidden property="mailingID"/>
    <html:hidden property="action"/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "mailing-send-dependents-overview": {
                "rows-count": ${mailingSendForm.numberOfRows},
                "types": ${emm:toJson(mailingSendForm.filterTypes)}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="default.usedIn"/>
                <button class="icon icon-help" data-help="help_${helplanguage}/mailing/MailingDependentsList.xml"
                        tabindex="-1" type="button"></button>
            </h2>
            <ul class="tile-header-actions">
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
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" data-form-change data-form-submit type="button">
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content">
            <!-- Filters -->
            <div class="hidden">
                <!-- dropdown for type -->
                <div class="dropdown filter" data-field="filter" data-filter-target=".js-filter-type">
                    <html:hidden property="__STRUTS_MULTIPLE_filterTypes" value=""/>

                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-left">
                        <li>
                            <label class="label">
                                <agn:agnCheckbox property="filterTypes" value="ACTION" data-field-filter=""/>
                                <bean:message key="action.Action"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <agn:agnCheckbox property="filterTypes" value="WORKFLOW" data-field-filter=""/>
                                <bean:message key="workflow.single"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <agn:agnCheckbox property="filterTypes" value="BOUNCE_FILTER" data-field-filter=""/>
                                <bean:message key="settings.Mailloop"/>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterTypes: ''">
                                <bean:message key="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change="" data-form-submit="">
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Apply"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="table-wrapper table-overflow-visible">
                <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        id="item"
                        list="${mailingSendForm.dependents}"
                        pagesize="${mailingSendForm.numberOfRows}"
                        sort="list"
                        excludedParams="*"
                        requestURI="/mailingsend.do?action=${ACTION_MAILING_SEND_VIEW}&mailingID=${mailingSendForm.mailingID}&init=true"
                        partialList="false">

                    <!-- Prevent table controls/headers collapsing when the table is empty -->
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <display:setProperty name="basic.msg.empty_list_row">
                        <tr class="empty">
                            <td colspan="{0}">
                                <i class="icon icon-info-circle"></i>
                                <strong><bean:message key="warning.mailing.action.sending.non"/></strong>
                            </td>
                        </tr>
                    </display:setProperty>

                    <display:column headerClass="js-table-sort js-filter-type" sortProperty="type" sortable="true" titleKey="default.Type">
                        <c:choose>
                            <c:when test="${item.type == 'ACTION'}">
                                <emm:ShowByPermission token="actions.show">
                                    <html:link styleClass="hidden js-row-show"
                                               page="/action/${item.id}/view.action"/>
                                </emm:ShowByPermission>
                                <bean:message key="action.Action"/>
                            </c:when>
                            <c:when test="${item.type == 'WORKFLOW'}">
                                <emm:ShowByPermission token="workflow.show">
                                    <html:link styleClass="hidden js-row-show"
                                               page="/workflow/${item.id}/view.action"/>
                                </emm:ShowByPermission>
                                <bean:message key="workflow.single"/>
                            </c:when>

                            <c:when test="${item.type == 'BOUNCE_FILTER'}">
                                <emm:ShowByPermission token="mailloop.show">
                                    <html:link styleClass="hidden js-row-show"
                                               page="/administration/bounce/${item.id}/view.action"/>
                                </emm:ShowByPermission>
                                <bean:message key="settings.Mailloop"/>
                            </c:when>
                        </c:choose>
                    </display:column>
                    <display:column headerClass="js-table-sort" property="shortname" sortProperty="name" sortable="true" titleKey="Name" escapeXml="true"/>
                </display:table>
            </div>
        </div>
    </div>
</agn:agnForm>
