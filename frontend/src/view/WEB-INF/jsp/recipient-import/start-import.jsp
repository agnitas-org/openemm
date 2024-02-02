<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.imports.form.RecipientImportForm"--%>
<%--@elvariable id="csvFiles" type="java.util.List<com.agnitas.emm.core.upload.bean.UploadData>"--%>
<%--@elvariable id="importProfiles" type="java.util.List<org.agnitas.beans.ImportProfile>"--%>

<mvc:form id="import-form" servletRelativeAction="/recipient/import/preview.action" modelAttribute="form"
          enctype="multipart/form-data" data-form="resource" data-controller="recipient-import-view">

    <script data-initializer="recipient-import-view" type="application/json">
        {
            "attachmentCsvFileID": ${form.attachmentCsvFileID}
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="button.Import_Start" />
            </h2>
        </div>
        <div class="tile-content">
            <div class="tile-content-forms form-vertical">

                <div class="form-group">
                    <label class="control-label">
                        <mvc:message code="import.csv.file"/>*
                    </label>

                    <c:choose>
                        <c:when test="${form.fileName ne null and not empty form.fileName}">
                            <div id="uploaded-file-container">
                                <table class="table table-bordered">
                                    <tbody>
                                    <tr>
                                        <td><mvc:message code="import.current.csv.file" />: ${form.fileName}</td>
                                        <td>
                                            <button type="button" class="btn btn-regular btn-alert" data-tooltip="<mvc:message code="button.Delete"/>" data-action="delete-file">
                                                <i class="icon icon-trash-o"></i>
                                            </button>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>

                            <input id="uploadFile" type="file" name="uploadFile" class="form-control hidden">
                        </c:when>
                        <c:otherwise>
                            <input id="uploadFile" type="file" name="uploadFile" class="form-control">
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="form-group">
                    <label class="toggle">
                        <input type="checkbox" id="useCsvUpload" name="useCsvUpload" data-toggle-usability="#attachment_csv_file_id, #uploadFile" />
                        <div class="toggle-control"></div>
                        <span class="text">
                               <mvc:message code="import.wizard.uploadCsvFile"/>
                           </span>
                    </label>

                    <mvc:select path="attachmentCsvFileID" cssClass="form-control" id="attachment_csv_file_id" disabled="true" data-show-by-checkbox="#useCsvUpload">
                        <mvc:options items="${csvFiles}" itemLabel="filename" itemValue="uploadID" />
                    </mvc:select>
                </div>

                <div class="form-group">
                    <div class="col-sm-2">
                        <label for="recipient-import-profile" class="control-label">
                            <mvc:message code="import.wizard.selectImportProfile"/>
                        </label>
                    </div>

                    <div class="input-group">
                        <div class="input-group-controls">
                            <mvc:select id="recipient-import-profile" path="profileId" cssClass="js-select form-control">
                                <mvc:options items="${importProfiles}" itemLabel="name" itemValue="id" />
                            </mvc:select>
                        </div>
                        <div class="input-group-btn">
                            <a href="<c:url value="/import-profile/create.action" />" class="btn btn-regular">
                                <span class="text">
                                    <mvc:message code="import.NewImportProfile"/>
                                </span>
                            </a>
                        </div>
                    </div>
                </div>

                <div class="notification notification-info">
                    <div class="notification-header">
                        <p class="headline">
                            <i class="icon icon-state-info"></i>
                            <span class="text"><mvc:message code="Info" /></span>
                        </p>
                    </div>
                    <div class="notification-content">
                        <p><mvc:message code="import.title.start" /></p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</mvc:form>
