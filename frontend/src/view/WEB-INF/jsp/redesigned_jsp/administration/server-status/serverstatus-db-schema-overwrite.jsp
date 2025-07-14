<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="snapshotVersion" type="java.lang.String"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="GWUA.dbSchema.upload" /></h1>
                <button type="button" class="btn-close" data-confirm-negative>
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <p><mvc:message code="GWUA.dbSchema.overwrite.question" arguments="${snapshotVersion}" /></p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger" data-confirm-negative>
                    <i class="icon icon-times"></i>
                    <span class="text"><mvc:message code="default.No"/></span>
                </button>

                <button type="button" class="btn btn-primary" data-form-target="#dbSchema-upload-form" data-form-set="overwrite: true"
                        data-form-submit data-bs-dismiss="modal">
                    <i class="icon icon-check"></i>
                    <span class="text"><mvc:message code="default.Yes"/></span>
                </button>
            </div>
        </div>
    </div>
</div>
