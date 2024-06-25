<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div id="icon-comment-editor" data-initializer="icon-comment-editor-initializer">
    <mvc:form action="" id="iconCommentForm" name="iconCommentForm">
        <div class="form-group">
            <div class="col-sm-12">
                <textarea id="iconComment" name="iconComment" class="form-control non-resizable" draggable="false" rows="5"></textarea>
            </div>
        </div>
        <div class="form-group">
            <div class="col-xs-12">
                <div class="btn-group">
                    <button type="button" class="btn btn-regular" data-action="icon-comment-editor-cancel">
                        <mvc:message code="button.Cancel"/>
                    </button>
                    <button type="button" class="btn btn-regular btn-primary" data-action="icon-comment-editor-save">
                        <mvc:message code="button.Apply"/>
                    </button>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
