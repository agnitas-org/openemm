<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div id="parameter-editor" data-initializer="parameter-editor-initializer" >
    <mvc:form action="" id="parameterForm" name="parameterForm">
        <input name="id" type="hidden">

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label">
                    <mvc:message code="Value"/>
                </label>
            </div>
            <div class="col-sm-8">
                <select name="value" class="form-control">
                    <%-- Filled by JS --%>
                </select>
            </div>
        </div>

        <hr>

        <div class="form-group">
            <div class="col-xs-12">
                <div class="btn-group">
                    <a href="#" class="btn btn-regular" data-action="editor-cancel">
                        <mvc:message code="button.Cancel"/>
                    </a>

                    <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="parameter-editor-save">
                        <mvc:message code="button.Apply"/>
                    </a>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
