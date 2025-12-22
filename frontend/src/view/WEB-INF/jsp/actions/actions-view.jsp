<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ page import="com.agnitas.emm.core.action.operations.ActionOperationType" %>
<%@ page import="com.agnitas.emm.core.action.bean.EmmActionDependency" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="operationList" type="java.util.List"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.action.form.EmmActionForm"--%>
<%--@elvariable id="eventBasedMailings" type="java.util.List<com.agnitas.beans.Campaign>"--%>

<c:set var="ACE_EDITOR_PATH" value="${emm:aceEditorPath(pageContext.request)}" scope="page"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/${ACE_EDITOR_PATH}/emm/ace.min.js"></script>

<c:set var="operationTypes" value="<%= ActionOperationType.values() %>" />
<c:set var="MAILING_DEPENDENCY_TYPE" value="<%= EmmActionDependency.Type.MAILING %>" />
<c:set var="FORM_DEPENDENCY_TYPE" value="<%= EmmActionDependency.Type.FORM %>" />

<c:if test="${not empty operationList}">
    <mvc:form id="emm-action-view" servletRelativeAction="/action/save.action" data-form="resource"
              data-controller="action-view" data-initializer="action-view" data-action="save-action-data"
              modelAttribute="form" cssClass="tiles-container" data-validator="action"
              data-editable-view="${agnEditViewKey}">

        <mvc:hidden path="id"/>

        <script type="application/json" id="config:action-view">
            {
                "modules" : ${emm:toJson(form.modulesSchema)},
                "operationTypes" : {
                    <c:forEach var="operationType" items="${operationTypes}" varStatus="status">
                        "${operationType.name()}": ${emm:toJson(operationType.name)}
                        <c:if test="${status.index + 1 lt fn:length(operationTypes)}">,</c:if>
                    </c:forEach>
                }
            }
        </script>

        <div class="tiles-block flex-column">
            <div id="settings-tile" class="tile" data-editable-tile style="flex: 1 0 min-content">
                <div class="tile-header">
                    <h1 class="tile-title text-truncate"><mvc:message code="Settings" /></h1>
                </div>
                <div class="tile-body vstack gap-3">
                    <div>
                        <label class="form-label" for="shortname">
                            <mvc:message var="nameMsg" code="default.Name"/>
                            ${nameMsg}*
                        </label>

                        <mvc:text path="shortname" id="shortname" maxlength="50" size="42" cssClass="form-control" placeholder="${nameMsg}" />
                    </div>

                    <div>
                        <label class="form-label" for="type"><mvc:message code="Usage"/></label>
                        <mvc:select path="type" id="type" cssClass="form-control">
                            <mvc:option value="0"><mvc:message code="actionType.link"/></mvc:option>
                            <mvc:option value="1"><mvc:message code="actionType.form"/></mvc:option>
                            <mvc:option value="9"><mvc:message code="actionType.all"/></mvc:option>
                        </mvc:select>
                    </div>

                    <div>
                        <label class="form-label" for="description">
                            <mvc:message var="descriptionMsg" code="Description"/>
                            ${descriptionMsg}
                        </label>
                        <mvc:textarea path="description" id="description" cssClass="form-control" rows="1" placeholder="${descriptionMsg}"/>
                    </div>

                    <div>
                        <div class="form-check form-switch">
                            <mvc:checkbox path="advertising" id="advertising" cssClass="form-check-input" role="switch"/>
                            <label class="form-label form-check-label" for="advertising">
                                <mvc:message code="mailing.contentType.advertising"/>
                                <a href="#" class="icon icon-question-circle" data-help="actions/AdvertisingMsg.xml"></a>
                            </label>
                        </div>
                    </div>
                </div>
            </div>

            <c:if test="${form.id gt 0}">
                <div id="used-in-tile" class="tile" data-editable-tile>
                    <div class="tile-body">
                        <div class="table-wrapper" data-js-table="dependencies-table">
                            <div class="table-wrapper__header">
                                <h1 class="table-wrapper__title"><mvc:message code="default.usedIn" /></h1>
                                <div class="table-wrapper__controls">
                                    <%@include file="../common/table/toggle-truncation-btn.jspf" %>
                                    <jsp:include page="../common/table/entries-label.jsp" />
                                </div>
                            </div>
                        </div>

                        <script id="dependencies-table" type="application/json">
                            {
                                "columns": [
                                    {
                                        "headerName": "<mvc:message code='default.Type'/>",
                                        "editable": false,
                                        "cellRenderer": "MustacheTemplateCellRender",
                                        "cellRendererParams": {"templateName": "action-dependency-type"},
                                        "field": "type"
                                    },
                                    {
                                        "headerName": "<mvc:message code='default.Name'/>",
                                        "editable": false,
                                        "cellRenderer": "NotEscapedStringCellRenderer",
                                        "field": "name"
                                    }
                                ],
                                "options": {
                                    "pagination": false,
                                    "showRecordsCount": "simple",
                                    "viewLinkTemplate": "action-dependency-view-link"
                                },
                                "data": ${dependencies}
                            }
                        </script>
                    </div>
                </div>
            </c:if>
        </div>

        <div id="steps-tile" class="tile" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="Steps"/></h1>
            </div>
            <div class="tile-body js-scrollable">
                <div class="vstack gap-3">
                    <div id="module-list" class="row g-3">
                        <%-- Loads by JS --%>
                    </div>
                    <div class="tile tile--md">
                        <div class="tile-header">
                            <h2 class="tile-title text-truncate"><mvc:message code="dashboard.tile.add" /></h2>

                            <emm:ShowByPermission token="actions.change">
                                <div class="tile-controls">
                                    <button class="btn btn-primary btn-icon" type="button" data-action="add-new-module">
                                        <i class="icon icon-plus"></i>
                                    </button>
                                </div>
                            </emm:ShowByPermission>
                        </div>

                        <div class="tile-body border-top">
                            <label class="form-label" for="moduleName"><mvc:message code="default.Type"/></label>
                            <select id="moduleName" class="form-control">
                                <c:forEach  items="${operationList}" var="module">
                                    <option value="${module.name}"><mvc:message code="action.op.${module.name}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </mvc:form>

    <%--load column info for module templates: IdentifyCustomer.jspf, SubscribeCustomer.jspf--%>
    <emm:instantiate var="columnInfo" type="java.util.LinkedHashMap">
        <emm:ShowColumnInfo id="agnTbl" table="<%= AgnUtils.getCompanyID(request) %>"
                            hide="change_date, timestamp, creation_date, datasource_id, bounceload, sys_tracking_veto,
                            cleaned_date, facebook_status, foursquare_status, google_status, twitter_status, xing_status, sys_encrypted_sending">
            <%--@elvariable id="_agnTbl_column_name" type="java.lang.String"--%>
            <%--@elvariable id="_agnTbl_shortname" type="java.lang.String"--%>

            <c:set target="${columnInfo}" property="${fn:toLowerCase(_agnTbl_column_name)}" value="${_agnTbl_shortname}"/>
        </emm:ShowColumnInfo>
    </emm:instantiate>

    <%@include file="fragments/ActivateDoubleOptIn.jspf"%>
    <%@include file="fragments/ContentView.jspf"%>
    <%@include file="fragments/GetArchiveList.jspf"%>
    <%@include file="fragments/GetArchiveMailing.jspf"%>
    <%@include file="fragments/GetCustomer.jspf"%>
    <%@include file="fragments/IdentifyCustomer.jspf"%>
    <%@include file="fragments/SendMailing.jspf"%>
    <%@include file="fragments/SubscribeCustomer.jspf"%>
    <%@include file="fragments/UnsubscribeCustomer.jspf"%>
    <%@include file="fragments/UpdateCustomer.jspf"%>
    <%@include file="fragments/ServiceMail.jspf"%>
    <%@include file="fragments/ExecuteScript.jspf"%>
    <%@include file="fragments/SendLastNewsletter.jspf"%>

    <script id="common-module-data" type="text/x-mustache-template">
        <div class="col-12" data-action-module>
            <div class="tile tile--md">
                <div class="tile-header border-bottom">
                    <h2 class="tile-title">
                        <span data-module-index></span>
                        <span class="text-truncate">{{- moduleName}}</span>
                    </h2>

                    <emm:ShowByPermission token="actions.change">
                        <div class="tile-controls">
                            <button class="btn btn-icon btn-danger" type="button" data-action="action-delete-module" data-tooltip="<mvc:message code="button.Delete"/>">
                                <i class="icon icon-trash-alt"></i>
                            </button>
                        </div>
                    </emm:ShowByPermission>
                </div>
            </div>
        </div>
    </script>

    <script id="action-modal-editor" type="text/x-mustache-template">
        <div class="modal modal-adaptive modal-editor">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h1 class="modal-title">{{= title }}</h1>
                        <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                            <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                        </button>
                    </div>

                    <div class="modal-body">
                        <div class="modal-editors-container">
                            <textarea id="{{= id }}" data-sync="\#{{= target}}" class="form-control js-editor{{- (typeof(type) == 'undefined') ? '' : '-' + type }}"></textarea>
                        </div>
                    </div>
                    <emm:ShowByPermission token="actions.change">
                        <div class="modal-footer">
                            <button type="button" class="btn btn-primary"
                                    data-sync-from="\#{{= id }}" data-sync-to="\#{{= target }}" data-bs-dismiss="modal" data-form-target="#emm-action-view" data-form-submit-event="">
                                <i class="icon icon-save"></i>
                                <span class="text"><mvc:message code="button.Save"/></span>
                            </button>
                        </div>
                    </emm:ShowByPermission>
                </div>
            </div>
        </div>
    </script>
</c:if>

<script id="action-dependency-view-link" type="text/x-mustache-template">
    {{ if ('${MAILING_DEPENDENCY_TYPE.name()}' === type) { }}
        /mailing/{{- id }}/settings.action
    {{ } else if ('${FORM_DEPENDENCY_TYPE.name()}' === type) { }}
        /webform/{{- id }}/view.action
    {{ } }}
</script>

<script id="action-dependency-type" type="text/x-mustache-template">
    <span class="text-truncate-table">
        {{ if ('${MAILING_DEPENDENCY_TYPE.name()}' === value) { }}
            <mvc:message code="Mailing" />
        {{ } else if ('${FORM_DEPENDENCY_TYPE.name()}' === value) { }}
            <mvc:message code="Form" />
        {{ } }}
    </span>
</script>
