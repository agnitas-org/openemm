<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<div id="workflow-undoHistoryIsEmpty-dialog" data-initializer="undo-history-dialog-initializer">
    <div class="editor-main-content">
        <bean:message key="workflow.undo.historyIsEmptyDialog.description"/>
    </div>
    <div class="editor-button-container">
        <div class="action_button">
            <a href="#" data-action="undo-history-dialog-ok'">
                <span>
                    <bean:message key="button.OK"/>
                </span>
            </a>
        </div>
        <div class="clear_both"></div>
    </div>
</div>
