<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="csvFiles" type="java.util.List<com.agnitas.emm.core.upload.bean.UploadData>"--%>

<mvc:form servletRelativeAction="/recipient/import/wizard/step/file.action" modelAttribute="importWizardSteps" enctype="multipart/form-data" data-form="resource">
    <c:set var="tileContent">
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label checkbox-control-label" for="useCsvUpload">
                        <mvc:message code="import.wizard.uploadCsvFile"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <label class="toggle">
                        <mvc:checkbox path="fileStep.useCsvUpload" id="useCsvUpload" />
                        <div class="toggle-control"></div>
                    </label>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <mvc:message code="settings.FileName"/>*
                        <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/FileName.xml"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <input type="file" name="fileStep.csvFile" class="form-control" id="csvFile" data-hide-by-checkbox="#useCsvUpload"/>
                    <mvc:select path="fileStep.attachmentCsvFileId" cssClass="form-control js-select" size="1" id="attachment_csv_file_id" data-show-by-checkbox="#useCsvUpload">
                        <c:forEach var="csvFile" items="${csvFiles}">
                            <mvc:option value="${csvFile.uploadID}">${csvFile.filename}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <mvc:message code="Separator"/>
                        <button class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/Separator.xml"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="helper.status.separator" cssClass="form-control js-select" size="1">
                        <mvc:option value=";">;</mvc:option>
                        <mvc:option value=",">,</mvc:option>
                        <mvc:option value="|">|</mvc:option>
                        <mvc:option value="t">Tab</mvc:option>
                        <mvc:option value="^">^</mvc:option>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <mvc:message code="Delimiter"/>
                        <button class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/Delimiter.xml"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="helper.status.delimiter" cssClass="form-control js-select" size="1">
                        <option value=""><mvc:message code="delimiter.none"/></option>
                        <option value='"'><mvc:message code="delimiter.doublequote"/></option>
                        <option value="'"><mvc:message code="delimiter.singlequote"/></option>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <mvc:message code="mailing.Charset"/>
                        <button class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/Charset.xml"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="helper.status.charset" cssClass="form-control js-select" size="1">
                        <emm:ShowByPermission token="charset.use.iso_8859_15">
                            <mvc:option value="ISO-8859-15"><mvc:message code="mailing.iso-8859-15" /></mvc:option>
                        </emm:ShowByPermission>
                        <emm:ShowByPermission token="charset.use.utf_8">
                            <mvc:option value="UTF-8"><mvc:message code="mailing.utf-8" /></mvc:option>
                        </emm:ShowByPermission>
                        <%@include file="fragments/import-wizard-premium_charsets.jspf" %>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="import.dateFormat"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="helper.dateFormat" cssClass="form-control js-select" size="1">
                        <mvc:option value="dd.MM.yyyy HH:mm">dd.MM.yyyy HH:mm</mvc:option>
                        <mvc:option value="dd.MM.yyyy">dd.MM.yyyy</mvc:option>
                        <mvc:option value="yyyyMMdd">yyyyMMdd</mvc:option>
                        <mvc:option value="yyyyMMdd HH:mm">yyyyMMdd HH:mm</mvc:option>
                        <mvc:option value="yyyy-MM-dd HH:mm:ss">yyyy-MM-dd HH:mm:ss</mvc:option>
                        <mvc:option value="dd.MM.yyyy HH:mm:ss">dd.MM.yyyy HH:mm:ss</mvc:option>
                        <mvc:option value="dd.MM.yy">dd.MM.yy</mvc:option>
                    </mvc:select>
                </div>
            </div>
        </div>
    </c:set>
    <c:set var="step" value="1"/>
    <emm:ShowByPermission token="import.init.rollback">
        <c:url var="backUrl" value="/newimportwizard.do?action=10"/>
    </emm:ShowByPermission>
    <emm:HideByPermission token="import.init.rollback">
        <c:url var="backUrl" value="/recipient/import/chooseMethod.action"/>
    </emm:HideByPermission>

    <%@ include file="fragments/import-wizard-step-template.jspf" %>
</mvc:form>
