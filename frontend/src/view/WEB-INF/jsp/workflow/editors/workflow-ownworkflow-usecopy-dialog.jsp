<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<div id="ownworkflow-use-original-dialog"  data-initializer="ownworkflow-copy-dialog-initializer" >
    <div class="well">
        <bean:message key="workflow.ownWorkflow.copyWorkflowQuestion"/>
    </div>

    <hr>

    <div class="form-group">
        <div class="col-xs-12">
            <div class="btn-group">
                <a href="#" class="btn btn-regular disable-for-active" data-action="ownworkflow-copy-dialog" data-config="useOriginal: true">
                    <span><bean:message key="workflow.ownWorkflow.copyAndEdit"/></span>
                </a>
                <a href="#" class="btn btn-regular disable-for-edit" data-action="ownworkflow-copy-dialog" data-config="useOriginal: false">
                    <span><bean:message key="workflow.ownWorkflow.editOriginal"/></span>
                </a>
            </div>
        </div>
    </div>
</div>
