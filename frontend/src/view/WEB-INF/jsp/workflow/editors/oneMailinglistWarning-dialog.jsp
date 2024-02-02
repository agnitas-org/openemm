<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div id="${param.containerId}-oneMailinglistWarning-dialog" class="oneMailinglistWarning-dialog-body" style="display: none;">
    <div class="form-group">
        <div class="col-xs-12">
            <div class="well">
                <mvc:message code="workflow.mailing.oneMailinglistWarning"/>
            </div>
        </div>
    </div>

    <div class="form-group">
        <div class="col-xs-12">
            <div class="btn-group">
                <a href="#" class="btn btn-regular" data-action="${param.baseMailingEditor}-close">
                    <mvc:message code="default.No"/>
                </a>
                <a href="#" class="btn btn-regular btn-primary" data-action="${param.baseMailingEditor}-accept">
                    <mvc:message code="default.Yes"/>
                </a>
            </div>
        </div>
    </div>
</div>
