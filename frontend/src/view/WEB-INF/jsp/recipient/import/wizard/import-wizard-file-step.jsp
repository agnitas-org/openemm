<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<mvc:form servletRelativeAction="/recipient/import/wizard/step/file.action" modelAttribute="importWizardSteps" enctype="multipart/form-data" data-form="resource">
    <c:set var="tileContent">
        <div class="tile-content tile-content-forms">
            <%@include file="fragments/import-wizard-use-upload-switch.jspf" %>
            
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <mvc:message code="settings.FileName"/>*
                        <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/FileName.xml"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <input type="file" name="fileStep.csvFile" class="form-control" id="csvFile" data-hide-by-checkbox="#useCsvUpload"/>
                    <%@include file="fragments/import-wizard-uploads-select.jspf" %>
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
    <c:url var="backUrl" value="/recipient/import/chooseMethod.action"/>

    <%@ include file="fragments/import-wizard-step-template.jspf" %>
</mvc:form>
