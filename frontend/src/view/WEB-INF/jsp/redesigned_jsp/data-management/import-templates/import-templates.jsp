<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<div class="tiles-container" style="display: grid; grid-template-rows: 1fr 1fr; grid-template-columns: 1fr 1fr" data-editable-view="${agnEditViewKey}">
    <emm:ShowByPermission token="mailing.import">
        <mvc:form id="mailing-import-tile" cssClass="tile" servletRelativeAction="/import/execute.action" enctype="multipart/form-data" modelAttribute="form"
                  data-editable-tile="" data-form="resource" data-custom-loader="" data-initializer="upload">
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="mailing.import" /></h1>
            </div>
            <div class="tile-body">
                <%@ include file="../../data-management/import-templates/fragments/import-dropzone.jspf" %>
            </div>
        </mvc:form>

        <mvc:form id="template-import-tile" cssClass="tile" servletRelativeAction="/import/execute.action" enctype="multipart/form-data" modelAttribute="form"
                  data-editable-tile="" data-form="resource" data-custom-loader="" data-initializer="upload">
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="template.import" /></h1>
                <emm:ShowByPermission token="settings.extended">
                    <div class="tile-controls">
                        <div class="form-check form-switch">
                            <mvc:checkbox cssClass="form-check-input" path="overwriteTemplate" role="switch" id="import-duplicates-switch" />
                            <label class="form-label form-check-label fw-semibold" for="import-duplicates-switch">
                                <mvc:message code="import.template.overwrite"/>
                                <a href="#" class="icon icon-question-circle" data-help="mailing/OverwriteTemplate.xml"></a>
                            </label>
                        </div>
                    </div>
                </emm:ShowByPermission>
            </div>
            <div class="tile-body">
                <mvc:hidden path="template" value="true" />
                <%@ include file="../../data-management/import-templates/fragments/import-dropzone.jspf" %>
            </div>
        </mvc:form>
    </emm:ShowByPermission>
    <emm:ShowByPermission token="forms.import">
        <mvc:form id="userform-import-tile" cssClass="tile" servletRelativeAction="/webform/importUserForm.action" enctype="multipart/form-data"
                  data-editable-tile="" data-form="static" data-custom-loader="" data-initializer="upload">
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="forms.import" /></h1>
            </div>
            <div class="tile-body">
                <%@ include file="../../data-management/import-templates/fragments/import-dropzone.jspf" %>
            </div>
        </mvc:form>
    </emm:ShowByPermission>
    <%@ include file="extended-imports.jspf" %>
</div>
