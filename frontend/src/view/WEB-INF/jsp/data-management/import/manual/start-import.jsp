<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.imports.form.RecipientImportForm"--%>
<%--@elvariable id="importProfiles" type="java.util.List<com.agnitas.beans.ImportProfile>"--%>

<mvc:form id="import-form" servletRelativeAction="/recipient/import/preview.action" enctype="multipart/form-data" modelAttribute="form"
          cssClass="tiles-container" data-form="resource" data-controller="recipient-import-view">

    <script data-initializer="recipient-import-view" type="application/json">
        {
            "attachmentCsvFileID": ${form.attachmentCsvFileID}
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="import.standard"/></h1>
            <div class="tile-controls">
                <div class="progress">
                    <div class="progress-bar"
                         role="progressbar"
                         aria-valuenow="1"
                         aria-valuemin="0"
                         aria-valuemax="2"
                         style="width: 50%"></div>
                    <div class="progress-fraction">1/2</div>
                </div>

                <button type="button" class="btn btn-icon btn-primary" data-tooltip="<mvc:message code="button.Proceed" />" data-form-submit>
                    <i class="icon icon-angle-right fs-1"></i>
                </button>
            </div>
        </div>
        <div class="tile-body vstack gap-3 js-scrollable">
            <div class="notification-simple notification-simple--lg notification-simple--info">
                <span><mvc:message code="import.title.start" /></span>
            </div>

            <div>
                <label for="import-file" class="form-label"><mvc:message code="mediapool.file" /> *</label>

                <div class="row g-1">
                    <div class="col-12">
                        <c:if test="${not empty form.fileName}">
                            <div class="d-flex gap-1">
                                <div class="input-group">
                                    <span class="input-group-text input-group-text--disabled"><mvc:message code="import.current.csv.file" /></span>
                                    <input type="text" class="form-control" value="${form.fileName}" readonly>
                                </div>
                                <button type="button" class="btn btn-icon btn-danger" data-tooltip="<mvc:message code="button.Delete"/>" data-action="delete-file">
                                    <i class="icon icon-trash-alt"></i>
                                </button>
                            </div>
                        </c:if>

                        <div class="${not empty form.fileName ? 'hidden' : ''}">
                            <input id="import-file" type="file" name="uploadFile" class="form-control" ${not empty form.fileName ? 'disabled' : ''}>
                        </div>
                    </div>

                    <%@ include file="fragments/start-import-uploaded-files.jspf" %>
                </div>
            </div>

            <div>
                <label for="import-profile" class="form-label"><mvc:message code="import.ImportProfile" /> *</label>
                <div class="d-flex gap-1">
                    <mvc:select id="import-profile" path="profileId" cssClass="form-control">
                        <mvc:options items="${importProfiles}" itemLabel="name" itemValue="id" />
                    </mvc:select>

                    <a href="<c:url value="/import-profile/create.action" />" class="btn btn-icon btn-primary" data-tooltip="<mvc:message code="import.NewImportProfile"/>">
                        <i class="icon icon-plus"></i>
                    </a>
                </div>
            </div>
        </div>
    </div>
</mvc:form>

