<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.action" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="webserviceUserListForm" type="com.agnitas.emm.core.wsmanager.form.WebserviceUserListForm"--%>
<emm:ShowByPermission token="webservice.user.create">
	<%@include file="webserviceuser-create.jsp"%>
</emm:ShowByPermission>
<mvc:form servletRelativeAction="/administration/wsmanager/users.action" id="wsuser-list-form"
          modelAttribute="webserviceUserListForm" data-form="resource">
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "ws-manager-overview": {
                "rows-count": ${webserviceUserListForm.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="default.Overview" />
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
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="200"/>
                                <span class="label-text">200</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-submit="">
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content">
            <div class="table-wrapper">
                <c:set var="allowedChange" value="false"/>
                <emm:ShowByPermission token="webservice.user.change">
                    <c:set var="allowedChange" value="true"/>
                </emm:ShowByPermission>

                <display:table class="table table-bordered table-striped table-hover js-table"
                               id="wsUser"
                               name="webserviceUserList"
                               requestURI="/administration/wsmanager/users.action"
                               pagesize="${webserviceUserListForm.numberOfRows}"
                               excludedParams="*">

                    <!-- Prevent table controls/headers collapsing when the table is empty -->
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <display:setProperty name="basic.msg.empty_list_row" value=" "/>

                    <display:column headerClass="js-table-sort"
                                    property="userName" titleKey="logon.username"
                                    sortable="true" sortProperty="username"/>

                    <display:column headerClass="js-table-sort"
                                    titleKey="default.status.active"
                                    sortable="true" sortProperty="active">
                        <c:set var="activeMsgKey" value="${wsUser.active ? 'default.status.active' : 'webserviceuser.not_active'}"/>
                        <mvc:message code="${activeMsgKey}"/>
                    </display:column>

                    <display:column headerClass="js-table-sort"
                                    property="companyId" titleKey="webserviceuser.company"
                                    sortable="true" sortProperty="company_id"/>

                    <display:column headerClass="js-table-sort"
                                    property="dataSourceId" titleKey="webserviceuser.default_datasource_id"
                                    sortable="true" sortProperty="default_data_source_id"/>


                    <display:column class="table-actions ${allowedChange ? '' : 'hidden'}" headerClass="${allowedChange ? '' : 'hidden'}" sortable="false">
                        <c:url var="viewWsUserLink" value="/administration/wsmanager/user/${wsUser.userName}/view.action"/>

                        <a href="${viewWsUserLink}" class="hidden js-row-show"></a>

                    </display:column>

                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
