<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<div id="workflow-${param.dialogName}-dialog">

    <div class="form-group">
        <div class="col-xs-12">
            <div class="well">
                <bean:message key="${param.messageKey}"/>
            </div>
        </div>
    </div>

    <hr>

    <div class="form-group">
        <div class="col-xs-12">
            <div class="btn-group">
                <a href="#" class="btn btn-regular btn-primary" onclick="workflow${param.dialogName}DialogHandler.closeDialog(); return false;">
                    <bean:message key="button.OK"/>
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
                title: '<span class="dialog-fat-title"><bean:message key="${param.titleKey}"/></span>',
                dialogClass: "no-close",
                width: 650,
                modal: true,
                resizable: false
            });
        }
    }
</script>
