<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="step" value="1" />

<mvc:form servletRelativeAction="/recipient/import/wizard/step/file.action" modelAttribute="importWizardSteps" enctype="multipart/form-data" cssClass="tiles-container" data-form="resource">
    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="import.Wizard" /></h1>
            <div class="tile-controls">
                <%@ include file="fragments/import-wizard-steps-navigation.jspf" %>
            </div>
        </div>
        <div class="tile-body vstack gap-3 js-scrollable">
            <div class="vstack gap-1 flex-grow-0">
                <div>
                    <label for="csvFile" class="form-label">
                        <mvc:message code="settings.FileName" /> *
                        <a href="#" type="button" class="icon icon-question-circle" data-help="importwizard/step_1/FileName.xml"></a>
                    </label>
                    <input type="file" name="fileStep.csvFile" class="form-control" id="csvFile" data-hide-by-checkbox="#useCsvUpload"/>

                    <%@include file="fragments/import-wizard-uploads-select.jspf" %>
                </div>

                <%@include file="fragments/import-wizard-use-upload-switch.jspf" %>
            </div>

            <div>
                <label class="form-label" for="separator">
                    <mvc:message code="Separator"/>
                    <a href="#" type="button" class="icon icon-question-circle" data-help="importwizard/step_1/Separator.xml"></a>
                </label>

                <mvc:select id="separator" path="helper.status.separator" cssClass="form-control" size="1">
                    <mvc:option value=";">;</mvc:option>
                    <mvc:option value=",">,</mvc:option>
                    <mvc:option value="|">|</mvc:option>
                    <mvc:option value="t">Tab</mvc:option>
                    <mvc:option value="^">^</mvc:option>
                </mvc:select>
            </div>

            <div>
                <label class="form-label" for="delimiter">
                    <mvc:message code="Delimiter"/>
                    <a href="#" type="button" class="icon icon-question-circle" data-help="importwizard/step_1/Delimiter.xml"></a>
                </label>

                <mvc:select id="delimiter" path="helper.status.delimiter" cssClass="form-control" size="1">
                    <option value=""><mvc:message code="delimiter.none"/></option>
                    <option value='"'><mvc:message code="delimiter.doublequote"/></option>
                    <option value="'"><mvc:message code="delimiter.singlequote"/></option>
                </mvc:select>
            </div>

            <div>
                <label class="form-label" for="charset">
                    <mvc:message code="mailing.Charset"/>
                    <a href="#" type="button" class="icon icon-question-circle" data-help="importwizard/step_1/Charset.xml"></a>
                </label>

                <mvc:select id="charset" path="helper.status.charset" cssClass="form-control" size="1">
                    <emm:ShowByPermission token="charset.use.iso_8859_15">
                        <mvc:option value="ISO-8859-15"><mvc:message code="mailing.iso-8859-15" /></mvc:option>
                    </emm:ShowByPermission>
                    <emm:ShowByPermission token="charset.use.utf_8">
                        <mvc:option value="UTF-8"><mvc:message code="mailing.utf-8" /></mvc:option>
                    </emm:ShowByPermission>
                    <%@include file="fragments/import-wizard-premium_charsets.jspf" %>
                </mvc:select>
            </div>

            <div>
                <label class="form-label" for="deteformat"><mvc:message code="import.dateFormat"/></label>
                <mvc:select id="deteformat" path="helper.dateFormat" cssClass="form-control" size="1">
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
</mvc:form>
