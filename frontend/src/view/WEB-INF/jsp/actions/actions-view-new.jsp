<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.mediatypes.common.MediaTypes" %> <!-- necessary for ops-new/ActivateDoubleOptIn.jsp-->
<%@ page import="org.agnitas.util.AgnUtils" %> <!-- necessary for ops-new/SubscribeCustomer.jsp-->
<%@ page import="com.agnitas.emm.core.mediatypes.common.MediaTypes" %> <!-- necessary for ops-new/ActivateDoubleOptIn.jsp-->

<%@ page import="static com.agnitas.emm.core.action.operations.ActionOperationType.ACTIVATE_DOUBLE_OPT_IN" %>
<%@ page import="static com.agnitas.emm.core.action.operations.ActionOperationType.UNSUBSCRIBE_CUSTOMER" %>
<%@ page import="static com.agnitas.emm.core.action.operations.ActionOperationType.UPDATE_CUSTOMER" %>
<%@ page import="static com.agnitas.emm.core.action.operations.ActionOperationType.SUBSCRIBE_CUSTOMER" %>
<%@ page import="static com.agnitas.emm.core.action.operations.ActionOperationType.SERVICE_MAIL" %>
<%@ page import="static com.agnitas.emm.core.action.operations.ActionOperationType.SEND_MAILING" %>
<%@ page import="static com.agnitas.emm.core.action.operations.ActionOperationType.IDENTIFY_CUSTOMER" %>
<%@ page import="static com.agnitas.emm.core.action.operations.ActionOperationType.GET_CUSTOMER" %>
<%@ page import="static com.agnitas.emm.core.action.operations.ActionOperationType.GET_ARCHIVE_MAILING" %>
<%@ page import="static com.agnitas.emm.core.action.operations.ActionOperationType.GET_ARCHIVE_LIST" %>
<%@ page import="static com.agnitas.emm.core.action.operations.ActionOperationType.EXECUTE_SCRIPT" %>
<%@ page import="static com.agnitas.emm.core.action.operations.ActionOperationType.CONTENT_VIEW" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="operationList" type="java.util.List"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.action.form.EmmActionForm"--%>
<%--@elvariable id="eventBasedMailings" type="java.util.List<org.agnitas.beans.Campaign>"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<c:if test="${not empty operationList}">

    <mvc:form servletRelativeAction="/action/save.action" data-form="resource"
              data-controller="action-view-new" data-initializer="action-view-new" data-action="save-action-data"
              modelAttribute="form" id="emmActionForm" cssClass="hidden">
        <mvc:hidden path="id"/>

        <script type="application/json" id="config:action-view-new">
            {
                "modules" : ${emm:toJson(form.modulesSchema)},
                "operationTypes" : {
                    "ACTIVATE_DOUBLE_OPT_IN" : "<%= ACTIVATE_DOUBLE_OPT_IN.getName() %>",
                    "CONTENT_VIEW" : "<%= CONTENT_VIEW.getName() %>",
                    "EXECUTE_SCRIPT" : "<%= EXECUTE_SCRIPT.getName() %>",
                    "GET_ARCHIVE_LIST" : "<%= GET_ARCHIVE_LIST.getName() %>",
                    "GET_ARCHIVE_MAILING" : "<%= GET_ARCHIVE_MAILING.getName() %>",
                    "GET_CUSTOMER" : "<%= GET_CUSTOMER.getName() %>",
                    "IDENTIFY_CUSTOMER" : "<%= IDENTIFY_CUSTOMER.getName() %>",
                    "SEND_MAILING" : "<%= SEND_MAILING.getName() %>",
                    "SERVICE_MAIL" : "<%= SERVICE_MAIL.getName() %>",
                    "SUBSCRIBE_CUSTOMER" : "<%= SUBSCRIBE_CUSTOMER.getName() %>",
                    "UNSUBSCRIBE_CUSTOMER" : "<%= UNSUBSCRIBE_CUSTOMER.getName() %>",
                    "UPDATE_CUSTOMER" : "<%= UPDATE_CUSTOMER.getName() %>"
                }
            }
        </script>

        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><mvc:message code="action.Edit_Action"/></h2>

                <ul class="tile-header-actions">
                    <li>
                        <label class="btn btn-regular btn-ghost toggle">
                            <span class="text"><mvc:message code="mailing.status.active"/></span>
                            <mvc:checkbox path="active" id="active"/>
                            <div class="toggle-control"></div>
                        </label>
                    </li>
                </ul>
            </div>

            <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="shortname"><mvc:message code="Name"/></label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="shortname" id="shortname" maxlength="50" size="42" cssClass="form-control" />
                    </div>
                </div>

                <div class="form-group">
                	<div class="col-sm-4">
                    	<label class="control-label" for="type"><mvc:message code="Usage"/></label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:select path="type" id="type" cssClass="form-control js-select" size="1">
                            <mvc:option value="0"><mvc:message code="actionType.link"/></mvc:option>
                            <mvc:option value="1"><mvc:message code="actionType.form"/></mvc:option>
                            <mvc:option value="9"><mvc:message code="actionType.all"/></mvc:option>
                        </mvc:select>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="description"><mvc:message code="default.description"/></label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:textarea path="description" id="description" cssClass="form-control" rows="5" cols="12"/>
                    </div>
                </div>
            </div>
        </div>

        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><mvc:message code="Steps"/></h2>
            </div>
            <div class="tile-content">
                <div class="tile-content-forms">
                    <div id="module-list">
                        <%-- this block load by JS--%>
                    </div>

                    <div id="action-line" class="inline-tile">
                        <div class="inline-tile-header">
                            <h2 class="headline"><mvc:message code="action.step.add"/></h2>
                        </div>
                        <div class="inline-tile-content">
                            <div class="form-group">
                                <div class="col-sm-4">
                                    <label class="control-label" for="moduleName"><mvc:message code="default.Type"/></label>
                                </div>
                                <div class="col-sm-8">
                                    <select id="moduleName" size="1" class="form-control js-select">
                                        <c:forEach  items="${operationList}" var="module">
                                            <option value="${module.name}"><mvc:message code="action.op.${module.name}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="inline-tile-footer">
                            <emm:ShowByPermission token="actions.change">
                                <!--todo: implement JS add new module action-->
                                <button class="btn btn-primary btn-regular" type="button" data-action="add-new-module" data-config="moduleTypeSelector: '#moduleName'">
                                    <i class="icon icon-plus-circle"></i>
                                    <mvc:message code="button.Add"/>
                                </button>
                            </emm:ShowByPermission>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </mvc:form>

    <%@include file="ops-new/ActivateDoubleOptIn.jsp"%>
    <%@include file="ops-new/ContentView.jsp"%>
    <%@include file="ops-new/ExecuteScript.jsp"%>
    <%@include file="ops-new/GetArchiveList.jsp"%>
    <%@include file="ops-new/GetArchiveMailing.jsp"%>
    <%@include file="ops-new/GetCustomer.jsp"%>
    <%@include file="ops-new/SendMailing.jsp"%>
    <%@include file="ops-new/ServiceMail.jsp"%>
    <%@include file="ops-new/SubscribeCustomer.jsp"%>
    <%@include file="ops-new/UnsubscribeCustomer.jsp"%>

    <%@include file="ops-new/IdentifyCustomer.jsp"%>
    <%@include file="ops-new/UpdateCustomer.jsp"%>

    <script id="common-module-data" type="text/x-mustache-template">
        <div class="inline-tile" data-action-module >
            <div class="inline-tile-header">
                <h2 class="headline"><span class="module-count"></span>&nbsp;{{- moduleName}} </h2>
            </div>

        </div>
        <div class="tile-separator"></div>
    </script>

    <script id="action-modal-editor" type="text/x-mustache-template">
        <div class="modal modal-editor">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close-icon close" data-dismiss="modal">
                            <i aria-hidden="true" class="icon icon-times-circle"></i>
                        </button>
                        <h4 class="modal-title">{{= title }}</h4>
                    </div>
                    <div class="modal-body">
                        <textarea id="{{= id }}" data-sync="\#{{= target}}" class="form-control js-editor{{- (typeof(type) == 'undefined') ? '' : '-' + type }}"></textarea>
                    </div>
                    <div class="modal-footer">
                        <div class="btn-group">
                            <button type="button" class="btn btn-default btn-large" data-dismiss="modal">
                                <i class="icon icon-times"></i>
                                <span class="text"><mvc:message code="button.Cancel"/></span>
                            </button>
                            <emm:ShowByPermission token="actions.change">
                                <button type="button" class="btn btn-primary btn-large" data-sync-from="\#{{= id }}" data-sync-to="\#{{= target }}" data-dismiss="modal" data-form-target="#emmActionForm" data-form-submit-event="">
                                    <i class="icon icon-save"></i>
                                    <span class="text"><mvc:message code="button.Save"/></span>
                                </button>
                            </emm:ShowByPermission>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </script>
</c:if>

