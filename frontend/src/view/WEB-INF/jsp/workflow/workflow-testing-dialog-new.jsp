<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<div id="workflow-testing-dialog-new" data-initializer="testing-dialog-initializer" data-config="shortname:${workflowForm.shortname}">
    <div class="form-group">
        <div class="col-xs-12">
            <div class="well">
                <bean:message key="${param.dialogMessage}"/>
            </div>
        </div>
    </div>

    <hr/>

    <div class="form-group">
        <div class="col-xs-12">
            <div class="btn-group">
                <a href="#" data-action="workflowTestingDialogSubmitButton" class="btn btn-regular btn-primary" data-form-target='#workflowForm' data-form-set='statusString:${param.newStatus}'>
                    <span><bean:message key="${param.positiveButtonName}"/></span>
                </a>
                <a href="#" data-action="workflowTestingDialogCancelButton" class="btn btn-regular">
                    <span><bean:message key="button.Cancel"/></span>
                </a>
            </div>
        </div>
    </div>
</div>
