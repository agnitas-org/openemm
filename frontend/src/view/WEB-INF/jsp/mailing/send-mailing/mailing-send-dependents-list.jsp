<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="dependents" type="java.util.List<Dependent<MailingDependentType>>"--%>

<mvc:form servletRelativeAction="/mailing/send/${form.mailingID}/view.action" modelAttribute="form" id="dependentListMailSendForm"
          data-form="resource" data-form-resource="#dependentListMailSendForm">

    <mvc:hidden path="mailingID"/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "mailing-send-dependents-overview": {
                "rows-count": ${form.numberOfRows},
                "types": ${emm:toJson(form.filterTypes)}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="default.usedIn"/>
                <button class="icon icon-help" data-help="help_${helplanguage}/mailing/MailingDependentsList.xml"
                        tabindex="-1" type="button"></button>
            </h2>
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
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="20" />
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="50" />
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="100" />
                                <span class="label-text">100</span>
                            </label>
                        </li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" data-form-change data-form-submit type="button">
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show"/></span>
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
                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-left">
                        <li>
                            <label class="label">
                                <mvc:checkbox path="filterTypes" value="ACTION" data-field-filter=""/>
                                <mvc:message code="action.Action"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <mvc:checkbox path="filterTypes" value="WORKFLOW" data-field-filter=""/>
                                <mvc:message code="workflow.single"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <mvc:checkbox path="filterTypes" value="BOUNCE_FILTER" data-field-filter=""/>
                                <mvc:message code="settings.Mailloop"/>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterTypes: ''">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change="" data-form-submit="">
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Apply"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="table-wrapper table-overflow-visible">
                <display:table class="table table-bordered table-striped table-hover js-table" id="item" list="${dependents}"
                        pagesize="${form.numberOfRows}" sort="list" excludedParams="*" requestURI="/mailing/send/${form.mailingID}/view.action"
                        partialList="false">

                    <!-- Prevent table controls/headers collapsing when the table is empty -->
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <display:setProperty name="basic.msg.empty_list_row">
                        <tr class="empty">
                            <td colspan="{0}">
                                <i class="icon icon-info-circle"></i>
                                <strong><mvc:message code="warning.mailing.action.sending.non"/></strong>
                            </td>
                        </tr>
                    </display:setProperty>

                    <display:column headerClass="js-table-sort js-filter-type" sortProperty="type" sortable="true" titleKey="default.Type">
                        <c:choose>
                            <c:when test="${item.type == 'ACTION'}">
                                <emm:ShowByPermission token="actions.show">
                                    <c:url var="actionViewLink" value="/action/${item.id}/view.action" />
                                    <a href="${actionViewLink}" class="hidden js-row-show"></a>
                                </emm:ShowByPermission>
                                <mvc:message code="action.Action"/>
                            </c:when>
                            <c:when test="${item.type == 'WORKFLOW'}">
                                <emm:ShowByPermission token="workflow.show">
                                    <c:url var="workflowViewLink" value="/workflow/${item.id}/view.action" />
                                    <a href="${workflowViewLink}" class="hidden js-row-show"></a>
                                </emm:ShowByPermission>
                                <mvc:message code="workflow.single"/>
                            </c:when>

                            <c:when test="${item.type == 'BOUNCE_FILTER'}">
                                <emm:ShowByPermission token="mailloop.show">
                                    <c:url var="bounceViewLink" value="/administration/bounce/${item.id}/view.action" />
                                    <a href="${bounceViewLink}" class="hidden js-row-show"></a>
                                </emm:ShowByPermission>
                                <mvc:message code="settings.Mailloop"/>
                            </c:when>
                        </c:choose>
                    </display:column>
                    <display:column headerClass="js-table-sort" property="shortname" sortProperty="name" sortable="true" titleKey="Name" escapeXml="true"/>
                </display:table>
            </div>
        </div>
    </div>
</mvc:form>

