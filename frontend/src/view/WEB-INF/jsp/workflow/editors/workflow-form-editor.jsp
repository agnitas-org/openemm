<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>

<div id="form-editor" data-initializer="form-editor-initializer">

    <div class="status_error editor-error-messages well" style="display: none;"></div>

    <form action="" id="formsForm" name="formsForm">
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label">
                    <bean:message key="workflow.panel.forms"/>
                </label>
            </div>
            <div class="col-sm-8">
                <div class="input-group">
                    <div class="input-group-controls">
                        <select class="form-control js-select" name="userFormId" id="formsEditorUserFormId"
                                data-action="form-editor-change">
                            <option value="0"><bean:message key="workflow.forms.select"/></option>
                            <c:forEach var="userForm" items="${allUserForms}">
                                <option value="${userForm.id}">${userForm.formName}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="input-group-btn">
                        <a href="#" class="btn btn-regular disable-for-edit" id="createFormLink"
                           data-action="form-editor-new">
                            <bean:message key="workflow.forms.create"/>
                        </a>
                        <a href="#" class="btn btn-regular disable-for-edit" id="editFormLink"
                           data-action="form-editor-form-edit">
                            <bean:message key="workflow.forms.edit"/>
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <hr>

        <div class="form-group">
            <div class="col-xs-12">
                <div class="btn-group">
                    <a href="#" class="btn btn-regular" data-action="editor-cancel">
                        <bean:message key="button.Cancel"/>
                    </a>
                    <a href="#" class="btn btn-regular btn-primary hide-for-edit"
                      data-action="form-editor-save">
                        <bean:message key="button.Apply"/>
                    </a>
                </div>
            </div>
        </div>
    </form>
</div>
