<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>

<div id="icon-comment-editor" data-initializer="icon-comment-editor-initializer">
    <form action="" id="iconCommentForm" name="iconCommentForm">
        <div class="form-group">
            <div class="col-sm-12">
                <textarea id="iconComment" name="iconComment" class="form-control non-resizable" draggable="false" rows="5"></textarea>
            </div>
        </div>
        <div class="form-group">
            <div class="col-xs-12">
                <div class="btn-group">
                    <button type="button" class="btn btn-regular" data-action="icon-comment-editor-cancel">
                        <bean:message key="button.Cancel"/>
                    </button>
                    <button type="button" class="btn btn-regular btn-primary" data-action="icon-comment-editor-save">
                        <bean:message key="button.Apply"/>
                    </button>
                </div>
            </div>
        </div>
    </form>
</div>
