<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>

<div id="ownWorkflow-editor" data-initializer="ownworkflow-editor-initializer">
    <div class="status_error editor-error-messages well" style="display: none;"></div>

    <form action="" id="ownWorkflowForm" name="ownWorkflowForm">
        <input name="id" type="hidden">

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label">
                    <bean:message key="Workflow"/>
                </label>
            </div>
            <div class="col-sm-8">
                <select name="ownWorkflowId" class="form-control js-select">
                    <logic:iterate id="curWorkflow" collection="${allWorkflows}">
                        <option value="${curWorkflow.workflowId}" status="${curWorkflow.status}">${curWorkflow.shortname}</option>
                    </logic:iterate>
                </select>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-8 col-sm-push-4">
            <label class="radio-inline">
                    <input type="radio" name="copyContent" value="true" checked="checked"/>
                    <bean:message key="workflow.ownWorkflow.withContent"/>
                </label>
                <label class="radio-inline">
                    <input type="radio" name="copyContent" value="false" class="workflow-copy-content-radio"/>
                    <bean:message key="workflow.ownWorkflow.onlyWorkflow"/>
                </label>
            </div>
        </div>

        <hr>

        <div class="form-group">
            <div class="col-xs-12">
                <div class="btn-group">
                    <a href="#" class="btn btn-regular" data-action="ownworkflow-editor-cancel">
                        <bean:message key="button.Cancel"/>
                    </a>

                    <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="ownworkflow-editor-save">
                        <bean:message key="button.Apply"/>
                    </a>
                </div>
            </div>
        </div>
    </form>
</div>
