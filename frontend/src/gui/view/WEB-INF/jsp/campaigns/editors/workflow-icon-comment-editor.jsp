<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div id="icon-comment-editor" data-initializer="icon-comment-editor-initializer">
    <mvc:form action="" id="iconCommentForm" name="iconCommentForm">
        <textarea id="iconComment" name="iconComment" class="form-control" draggable="false" rows="5"></textarea>
        <div class="d-flex gap-1 mt-1">
            <button type="button" class="btn btn-sm btn-danger flex-grow-1" data-action="icon-comment-editor-cancel">
                <mvc:message code="button.Cancel"/>
            </button>
            <button type="button" class="btn btn-sm btn-primary flex-grow-1" data-action="icon-comment-editor-save">
                <mvc:message code="button.Apply"/>
            </button>
        </div>
    </mvc:form>
</div>
