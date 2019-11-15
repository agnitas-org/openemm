<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<div id="workflow-copy-dialog-new" data-initializer="copy-dialog-initializer">
    <div class="form-group">
        <div class="col-xs-12">
            <div class="well">
                <bean:message key="workflow.copy.withContentQuestion"/>
            </div>
        </div>
    </div>

    <hr/>

    <div class="form-group">
        <div class="col-xs-12">
            <div class="btn-group">
                <a id="workflowBtnCopy" href="#" class="btn btn-regular btn-primary" data-action="copy-dialog-copy">
                    <span>
                        <bean:message key="button.Copy"/>
                    </span>
                </a>
                <a id="workflowBtnWithContent" href="#" class="btn btn-regular btn-primary" data-action="copy-dialog-copy">
                    <span>
                        <bean:message key="workflow.copy.btn.withContent"/>
                    </span>
                </a>
                <a id="workflowBtnOnlyChain" href="#" class="btn btn-regular btn-primary" data-action="copy-dialog-chain-copy">
                    <span>
                        <bean:message key="workflow.copy.btn.onlyChain"/>
                    </span>
                </a>
                <a href="#" class="btn btn-regular" data-action="copy-dialog-cancel">
                    <span>
                        <bean:message key="button.Cancel"/>
                    </span>
                </a>
            </div>
        </div>
    </div>
    <script id="copy-editor-data-new" type="application/json">
        {
            "url":"<html:rewrite page="/workflow/copy.action"/>"
        }
    </script>
</div>
