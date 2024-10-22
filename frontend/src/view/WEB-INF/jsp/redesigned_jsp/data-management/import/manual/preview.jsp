<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.imports.form.RecipientImportForm"--%>
<%--@elvariable id="columnsData" type="java.util.List<java.ulit.List<java.lang.String>>"--%>
<%--@elvariable id="columnsNames" type="java.ulit.List<java.lang.String>"--%>
<%--@elvariable id="enforcedMailinglist" type="org.agnitas.beans.Mailinglist"--%>
<%--@elvariable id="mailinglists" type="java.util.List<org.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="possibleToSelectMailinglist" type="java.lang.Boolean"--%>

<c:set var="columnsLength" value="${fn:length(columnsNames)}" />

<mvc:form cssClass="tiles-container" servletRelativeAction="/recipient/import/execute.action" enctype="multipart/form-data" data-form="resource" modelAttribute="form">

    <mvc:hidden path="profileId" />
    <mvc:hidden path="attachmentCsvFileID" />

    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="import.standard"/></h1>
            <div class="tile-controls">
                <a href="${backUrl}" type="button" class="btn btn-icon btn-inverse" data-tooltip="<mvc:message code="button.Back" />">
                    <i class="icon icon-angle-left fs-1"></i>
                </a>
                <div class="progress">
                    <div class="progress-bar-white-bg"></div>
                    <div class="progress-bar"
                         role="progressbar"
                         aria-valuenow="2"
                         aria-valuemin="0"
                         aria-valuemax="2"
                         style="width: 100%"></div>
                    <div class="progress-bar-primary-bg"></div>
                    <div class="progress-fraction">2/2</div>
                </div>
                <button type="button" class="btn btn-sm-horizontal btn-primary" data-form-confirm>
                    <i class="icon icon-play-circle"></i>
                    <span><mvc:message code="button.Import_Start"/></span>
                </button>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <div class="table-wrapper ${possibleToSelectMailinglist ? 'h-50' : ''}" data-js-table="manual-import-preview">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Preview" /></h1>
                    <div class="table-wrapper__controls">
                        <%@include file="../../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../../common/table/entries-label.jsp" />
                    </div>
                </div>
            </div>

            <c:if test="${possibleToSelectMailinglist}">
                <div class="bordered-box-sm mt-2">
                    <div class="row g-2">
                        <div class="col-12">
                            <h3 class="text-dark"><mvc:message code="import.SubscribeLists"/></h3>
                        </div>

                        <c:choose>
                            <c:when test="${empty enforcedMailinglist}">
                                <c:forEach var="mlist" items="${mailinglists}">
                                    <div class="col-12">
                                        <div class="form-check form-switch">
                                            <mvc:checkbox id="mailinglist-toggle-${mlist.id}" path="selectedMailinglist[${mlist.id}]" cssClass="form-check-input" role="switch" value="true" />
                                            <label class="form-label form-check-label fw-normal text-truncate" for="mailinglist-toggle-${mlist.id}">
                                                ${mlist.shortname}
                                            </label>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="col-12">
                                    <mvc:hidden path="selectedMailinglist[${enforcedMailinglist.id}]" value="true"/>

                                    <div class="form-check form-switch">
                                        <input type="checkbox" name="mailinglist[${enforcedMailinglist.id}]" class="form-check-input" role="switch" checked disabled />
                                        <label class="form-label form-check-label fw-normal text-truncate">
                                            ${enforcedMailinglist.shortname}
                                        </label>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:if>
        </div>

        <script id="manual-import-preview" type="application/json">
                {
                    "columns": [
                        <c:forEach var="columnName" items="${columnsNames}" varStatus="loop_status">
                            {
                            "headerName": "${columnName}",
                            "editable": false,
                            "cellRenderer": "NotEscapedStringCellRenderer",
                            "field": "${columnName}"
                            }${loop_status.index + 1 lt columnsLength ? ',' : ''}
                        </c:forEach>
                    ],
                    "data": ${columnsData},
                     "options": {
                        "pagination": false,
                        "showRecordsCount": "simple"
                    }
                }
            </script>
    </div>
</mvc:form>
