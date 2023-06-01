<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.ImportProfileAction" %>

<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ACTION_NEW" value="<%= ImportProfileAction.ACTION_NEW %>"/>

<%--@elvariable id="newImportWizardForm" type="com.agnitas.web.forms.ComNewImportWizardForm"--%>

<agn:agnForm action="/newimportwizard" id="newImportWizardForm" enctype="multipart/form-data" data-form="resource">
    <html:hidden property="action"/>
    <html:hidden property="upload_file" value="1"/>
    <input type="hidden" id="start_proceed" name="start_proceed" value="" />

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="button.Import_Start" />
            </h2>
        </div>
        <div class="tile-content">
            <div class="tile-content-forms form-vertical">

                <div class="form-group">
                    <label class="control-label">
                        <bean:message key="import.csv.file"/>*
                    </label>
                    <c:set var="currentFileName" value="${newImportWizardForm.currentFileName}" scope="page" />
                    <c:set var="hasFile" value="${newImportWizardForm.hasFile}" scope="page" />

                    <c:choose>
                        <c:when test="${hasFile == 'true'}">
                            <table class="table table-bordered">
                                <tbody>
                                    <tr>
                                        <td><bean:message key="import.current.csv.file" />: ${currentFileName}</td>
                                        <td>
                                            <button type="button" class="btn btn-regular btn-alert" data-tooltip="<bean:message key="button.Delete"/>" data-form-set="remove_file: 'remove_file'" data-form-submit>
                                                <i class="icon icon-trash-o"></i>
                                            </button>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </c:when>
                        <c:otherwise>
                            <html:file property="csvFile" styleId="csvFile" styleClass="form-control" />
                        </c:otherwise>
                    </c:choose>

                </div>

                <div class="form-group">
                    <label class="toggle">
                        <input type="checkbox" id="useCsvUpload" name="useCsvUpload" data-toggle-usability="#attachment_csv_file_id, #csvFile" />
                        <div class="toggle-control"></div>
                           <span class="text">
                               <bean:message key="import.wizard.uploadCsvFile"/>
                           </span>
                    </label>

                    <agn:agnSelect property="attachmentCsvFileID" styleClass="form-control" styleId="attachment_csv_file_id" disabled="true" data-show-by-checkbox="#useCsvUpload">
                        <c:forEach var="csvFile" items="${newImportWizardForm.csvFiles}">
                             <html:option value="${csvFile.uploadID}">${csvFile.filename}</html:option>
                        </c:forEach>
                    </agn:agnSelect>
                </div>

                <div class="form-group">
                    <div class="col-sm-2">
                        <label for="recipient-import-profile" class="control-label">
                            <bean:message key="import.wizard.selectImportProfile"/>
                        </label>
                    </div>

                    <div class="input-group">
                        <div class="input-group-controls">
                            <html:select styleId="recipient-import-profile" styleClass="js-select form-control" property="defaultProfileId">
                                <c:forEach items="${newImportWizardForm.importProfiles}" var="profile">
                                    <html:option value="${profile.id}"> ${profile.name} </html:option>
                                </c:forEach>
                            </html:select>
                        </div>
                        <div class="input-group-btn">
                            <c:url var="newImportProfileLink" value="/importprofile.do">
                                <c:param name="action" value="${ACTION_NEW}"/>
                            </c:url>

                            <a href="${newImportProfileLink}" class="btn btn-regular">
                                <span class="text">
                                    <bean:message key="import.NewImportProfile"/>
                                </span>
                            </a>
                        </div>
                    </div>
                </div>

                <div class="notification notification-info">
                    <div class="notification-header">
                        <p class="headline">
                            <i class="icon icon-state-info"></i>
                            <span class="text"><bean:message key="Info" /></span>
                        </p>
                    </div>
                    <div class="notification-content">
                        <p><bean:message key="import.title.start" /></p>
                    </div>
                </div>
            </div>

            <html:errors property="default"/>

        </div>
    </div>
</agn:agnForm>
