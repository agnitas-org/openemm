<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="pluginListForm" type="com.agnitas.emm.core.pluginmanager.form.PluginListForm"--%>
<%--@elvariable id="pluginList" type="org.agnitas.beans.impl.PaginatedListImpl"--%>

<mvc:form servletRelativeAction="/administration/pluginmanager/plugins.action" modelAttribute="pluginListForm">
    <input type="hidden" name="page" value="${pluginList.pageNumber}"/>
    <input type="hidden" name="sort" value="${pluginList.sortCriterion}"/>
    <input type="hidden" name="dir" value="${pluginList.sortDirection}"/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "plugin-manager-overview": {
                "rows-count": ${pluginListForm.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="default.Overview"/></h2>

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

                        <li><p>
                            <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
                            </button>
                        </p></li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content">
            <div class="table-wrapper">
                <display:table class="table table-bordered table-striped table-hover js-table"
                               id="pluginItem"
                               name="pluginList"
                               requestURI="/administration/pluginmanager/plugins.action"
                               pagesize="${pluginListForm.numberOfRows}"
                               excludedParams="*">

                    <%-- Prevent table controls/headers collapsing when the table is empty --%>
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <display:setProperty name="basic.msg.empty_list_row" value=" "/>

                    <display:column headerClass="js-table-sort plugin_id_header" class="plugin_id"
                                    property="id" titleKey="pluginmanager.plugin.id"
                                    sortable="true" sortProperty="id"/>

                    <display:column headerClass="js-table-sort plugin_name_header" class="plugin_name"
                                    property="name" titleKey="pluginmanager.plugin.name"
                                    sortable="true" sortProperty="name"/>

                    <display:column headerClass="js-table-sort plugin_description_header" class="plugin_description"
                                    property="description" titleKey="Description"
                                    sortable="true" sortProperty="description"/>

                    <display:column headerClass="js-table-sort plugin_version_header" class="plugin_description"
                                    property="version" titleKey="pluginmanager.plugin.version"
                                    sortable="true" sortProperty="version"/>

                    <display:column headerClass="js-table-sort plugin_vendor_header" class="plugin_vendor"
                                    property="vendor" titleKey="pluginmanager.plugin.vendor"
                                    sortable="true" sortProperty="vendor"/>

                    <display:column headerClass="js-table-sort plugin_activated_header" class="plugin_version"
                                    titleKey="mailing.status.active"
                                    sortable="true" sortProperty="active">
                        <c:set var="activeMsgKey" value="${pluginItem.active ? 'default.status.active' : 'pluginmanager.plugin.deactivated'}"/>
                        <bean:message key="${activeMsgKey}"/>
                    </display:column>

                    <display:column class="table-actions hidden" headerClass="hidden" sortable="false">
                        <c:url var="viewPluginLink" value="/administration/pluginmanager/plugin/${pluginItem.id}/view.action"/>

                        <a href="${viewPluginLink}" class="hidden js-row-show"></a>

                    </display:column>

                </display:table>
            </div>
        </div>
    </div>

</mvc:form>
