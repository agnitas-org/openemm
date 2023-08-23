<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<div id="${param.containerId}-oneMailinglistWarning-dialog" class="oneMailinglistWarning-dialog-body" style="display: none;">
    <div class="form-group">
        <div class="col-xs-12">
            <div class="well">
                <bean:message key="workflow.mailing.oneMailinglistWarning"/>
            </div>
        </div>
    </div>

    <div class="form-group">
        <div class="col-xs-12">
            <div class="btn-group">
                <a href="#" class="btn btn-regular" data-action="${param.baseMailingEditor}-close">
                    <bean:message key="default.No"/>
                </a>
                <a href="#" class="btn btn-regular btn-primary" data-action="${param.baseMailingEditor}-accept">
                    <bean:message key="default.Yes"/>
                </a>
            </div>
        </div>
    </div>
</div>
