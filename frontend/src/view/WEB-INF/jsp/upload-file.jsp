<%@ page language="java"
         contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%--Use this inside form with parameters: enctype='multipart/form-data'  data-form="upload"--%>

<%--Name of the field in the form, example: "newFile"--%>
<c:set var="fileName" value="${param.fileName}"/>

<%--Is multiple file upload, example: "newFile"--%>
<c:set var="multiple" value="${empty param.multiple ? true : param.multiple}"/>

<%--True if upload button should be showed--%>
<c:set var="showSubmitButton" value="${empty param.showSubmitButton ? true : param.showSubmitButton}"/>

<%--Params which will be set in the form when upload button is pressed, example "mailingID:3, method:save"--%>
<c:set var="requestParams" value="${param.requestParams}"/>

<%--ID of the mustache template for table header, example: "tableId"--%>
<c:set var="tableHeaderTemplate" value="${empty param.tableHeaderTemplate ? 'default-template-header' : param.tableHeaderTemplate}"/>

<%--ID of the mustache template for table row, example: "tableRowId"--%>
<c:set var="tableRowTemplate" value="${empty param.tableRowTemplate ? 'default-template-add' : param.tableRowTemplate}"/>


<div class="dropzone">
    <div class="dropzone-text">
        <strong>
            <i class="icon icon-reply"></i>
            &nbsp;<bean:message key="upload_dropzone.title"/>
        </strong>
        <span class="btn btn-regular btn-primary btn-upload">
            <i class="icon icon-cloud-upload"></i>
            <span class="text"><bean:message key="button.multiupload.select"/></span>
            <input type="file" name="${fileName}" ${multiple ? "multiple='multiple'" : ""} data-upload>
        </span>
    </div>
</div>

<div class="hidden" data-upload-add>
    <c:if test="${multiple}">
        <div class="actions actions-top">
            <div class="action-left">
                <button type="button" class="btn btn-regular" data-form-reset="">
                    <i class="icon icon-times"></i>
                    <span class="text">
                        <bean:message key="button.Cancel"/>
                    </span>
                </button>
            </div>
            <c:if test="${showSubmitButton}">
                <div class="action-right">
                    <button type="button" class="btn btn-regular btn-primary" data-form-set="${requestParams}" data-form-submit>
                        <i class="icon icon-cloud-upload"></i>
                        <span class="text">
                        <bean:message key="button.Upload"/>
                    </span>
                    </button>
                </div>
            </c:if>
        </div>
    </c:if>

    <table class="table table-bordered table-striped">
        <thead id="tableHeader">
            <tr>
                <th><bean:message key="mailing.files"/></th>
                <th><bean:message key="Description"/></th>
                <th><bean:message key="ComponentLink"/></th>
                <th><bean:message key="mailing.Graphics_Component.sourceForMobile"/></th>
            </tr>
        </thead>
        <tbody data-upload-add-template="${tableRowTemplate}">

        </tbody>
    </table>

    <div class="actions">
        <div class="action-left">
            <button type="button" class="btn btn-regular" data-form-reset="">
                <i class="icon icon-times"></i>
                <span class="text">
                    <bean:message key="button.Cancel"/>
                </span>
            </button>
        </div>
        <c:if test="${showSubmitButton}">
            <div class="action-right">
                <button type="button" class="btn btn-regular btn-primary" data-form-set="${requestParams}" data-form-submit>
                    <i class="icon icon-cloud-upload"></i>
                    <span class="text">
                        <bean:message key="button.Upload"/>
                    </span>
                </button>
            </div>
        </c:if>
    </div>
</div>

<div class="hidden" data-upload-progress>
    <div class="actions actions-top actions-bottom">
        <div class="action-right">
            <button type="button" class="btn btn-regular" data-form-abort="">
                <i class="icon icon-times"></i>
                <span class="text">
                    <bean:message key="button.Cancel"/>
                </span>
            </button>
        </div>
    </div>
    <div class="progress-wrapper" data-upload-progress-template="upload-template-progress">
        <div class="progress">
            <div class="progress-bar"
                 role="progressbar"
                 aria-valuenow="0"
                 aria-valuemin="0"
                 aria-valuemax="100">
                0%
            </div>
        </div>
    </div>
    <div class="actions actions-top">
        <div class="action-right">
            <button type="button" class="btn btn-regular" data-form-abort="">
                <i class="icon icon-times"></i>
                <span class="text">
                    <bean:message key="button.Cancel"/>
                </span>
            </button>
        </div>
    </div>
</div>

<script id="upload-template-progress" type="text/x-mustache-template">
    <div class="progress">
        <div class="progress-bar" role="progressbar" aria-valuenow="{{= currentProgress }}" aria-valuemin="0" aria-valuemax="100" style="width: {{= currentProgress }}%">
            {{= currentProgress }}%
        </div>
    </div>
</script>

<script id="default-template-header" type="text/x-mustache-template">
    <tr>
        <th><bean:message key="settings.FileName"/></th>
    </tr>
</script>

<script id="default-template-add" type="text/x-mustache-template">
    <tr>
        <td>
            {{- filename }}
        </td>
    </tr>
</script>
