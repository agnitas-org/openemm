<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div id="workflow-save-before-pdf-dialog" data-initializer="save-before-pdf-initializer">
    <div class="form-group">
        <div class="col-xs-12">
            <div class="dialog-message well">
            </div>
        </div>
    </div>

    <hr/>

    <div class="form-group">
        <div class="col-xs-12">
            <div class="btn-group">
                <a href="#" class="btn btn-regular btn-primary" id="save-before-pdf-btn-save" data-action="save-before-pdf-btn-save">
                    <span>
                        <mvc:message code="button.Save"/>
                    </span>
                </a>
                <a href="#" class="btn btn-regular" id="save-before-pdf-btn-cancel" data-action="save-before-pdf-btn-cancel">
                    <span>
                        <mvc:message code="button.Cancel"/>
                    </span>
                </a>
            </div>
        </div>
    </div>
</div>
