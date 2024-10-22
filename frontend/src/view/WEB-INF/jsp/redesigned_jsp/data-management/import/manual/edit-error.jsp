<%@ page import="org.agnitas.dao.ImportRecipientsDao" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag" %>
<%@ taglib prefix="mvc"        uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"        uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"         uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"          uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.imports.form.RecipientImportForm"--%>
<%--@elvariable id="columns" type="org.agnitas.service.impl.CSVColumnState[]"--%>
<%--@elvariable id="recipientList" type="org.agnitas.beans.impl.PaginatedListImpl<java.util.Map<java.lang.String, java.lang.Object>>"--%>

<c:set var="VALIDATOR_RESULT_RESERVED" value="<%= ImportRecipientsDao.VALIDATOR_RESULT_RESERVED %>"/>
<c:set var="ERROR_EDIT_REASON_KEY_RESERVED" value="<%= ImportRecipientsDao.ERROR_EDIT_REASON_KEY_RESERVED %>"/>

<mvc:form servletRelativeAction="/recipient/import/errors/save.action" modelAttribute="form" cssClass="tiles-container"
          data-controller="recipient-import-errors-edit" data-initializer="recipient-import-errors-edit"
          data-form="resource"  data-action="save-errors">
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "import-wizard-errors-overview": {
                "rows-count": ${form.numberOfRows}
            }
        }
    </script>

    <input type="hidden" name="invalidRecipientsSize" value="${recipientList.fullListSize}">

    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="import.standard"/></h1>
            <div class="tile-controls">
                <a href="<c:url value="/recipient/import/view.action" />" type="button" class="btn btn-icon btn-inverse" data-tooltip="<mvc:message code="button.Back" />">
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
                <button type="button" class="btn btn-sm-horizontal btn-danger" data-action="ignore-errors">
                    <i class="icon icon-times"></i>
                    <span><mvc:message code="button.import.ignore"/></span>
                </button>
                <button type="button" class="btn btn-sm-horizontal btn-primary" data-form-confirm>
                    <i class="icon icon-save"></i>
                    <span><mvc:message code="button.import.save.proceed"/></span>
                </button>
            </div>
        </div>
        <div class="tile-body d-flex flex-column gap-3">
            <div class="notification-simple notification-simple--lg notification-simple--info">
                <span><mvc:message code="import.title.error_edit" /></span>
            </div>
            <div class="table-wrapper">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="Values" /></h1>
                    <div class="table-wrapper__controls">
                        <%@include file="../../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../../common/table/entries-label.jsp">
                            <jsp:param name="totalEntries" value="${recipientList.fullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <c:set var="errorIndex" value="0" />
                    <agnDisplay:table id="recipient" name="recipientList" class="table table--borderless js-table" pagesize="${form.numberOfRows}"
                                      sort="external" excludedParams="*" requestURI="/recipient/import/errors/edit.action" size="${recipientList.fullListSize}">

                        <%@ include file="../../../common/displaytag/displaytag-properties.jspf" %>

                        <%--@elvariable id="recipient" type="java.util.Map<java.lang.String, java.lang.Object>"--%>

                        <agnDisplay:column titleKey="errorReason">
                            <span><mvc:message code="${recipient[ERROR_EDIT_REASON_KEY_RESERVED]}" /></span>
                        </agnDisplay:column>

                        <c:forEach items="${columns}" var="column">
                            <c:if test="${column.importedColumn}">
                                <agnDisplay:column title="${column.colName}">
                                    <c:set var="erroneousFieldName" value="${recipient[VALIDATOR_RESULT_RESERVED]}" />
                                    <c:set var="fieldValid" value="${erroneousFieldName ne null and column.colName ne erroneousFieldName}" />

                                    <c:choose>
                                        <c:when test="${fieldValid}">
                                            <span>${recipient[column.colName]}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="inputNamePrefix" value="errorsFixes[${errorIndex}]" />
                                            <c:set var="errorIndex" value="${errorIndex + 1}" />

                                            <input type="hidden" name="${inputNamePrefix}.index" value="${recipient.ERROR_EDIT_RECIPIENT_EDIT_RESERVED.temporaryId}">
                                            <input type="hidden" name="${inputNamePrefix}.fieldName" value="${column.colName}">
                                            <div class="input-status-container">
                                                <input type="text" class="form-control text-danger border-danger" name="${inputNamePrefix}.value" value="${fn:escapeXml(recipient[column.colName])}">
                                                <span class="status-badge status.error"></span>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </agnDisplay:column>
                            </c:if>
                        </c:forEach>
                    </agnDisplay:table>
                </div>
            </div>
        </div>
    </div>
</mvc:form>
