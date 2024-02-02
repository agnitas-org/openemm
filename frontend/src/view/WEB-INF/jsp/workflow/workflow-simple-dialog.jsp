<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div id="workflow-${param.dialogName}-dialog">

    <div class="form-group">
        <div class="col-xs-12">
            <div class="well">
                <mvc:message code="${param.messageKey}"/>
            </div>
        </div>
    </div>

    <hr>

    <div class="form-group">
        <div class="col-xs-12">
            <div class="btn-group">
                <a href="#" class="btn btn-regular btn-primary" onclick="workflow${param.dialogName}DialogHandler.closeDialog(); return false;">
                    <mvc:message code="button.OK"/>
                </a>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    var workflow${param.dialogName}DialogHandler = {
        closeDialog : function() {
            jQuery('#workflow-${param.dialogName}-dialog').dialog('close');
            return false;
        },
        showDialog : function() {
            jQuery('#workflow-${param.dialogName}-dialog').dialog({
                title: '<span class="dialog-fat-title"><mvc:message code="${param.titleKey}"/></span>',
                dialogClass: "no-close",
                width: 650,
                modal: true,
                resizable: false
            });
        }
    }
</script>
