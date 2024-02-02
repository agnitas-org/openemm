<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="org.agnitas.dao.ImportRecipientsDao" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.imports.form.RecipientImportForm"--%>
<%--@elvariable id="columns" type="org.agnitas.service.impl.CSVColumnState[]"--%>
<%--@elvariable id="recipientList" type="org.agnitas.beans.impl.PaginatedListImpl<java.util.Map<java.lang.String, java.lang.Object>>"--%>

<c:set var="VALIDATOR_RESULT_RESERVED" value="<%= ImportRecipientsDao.VALIDATOR_RESULT_RESERVED %>"/>
<c:set var="ERROR_EDIT_REASON_KEY_RESERVED" value="<%= ImportRecipientsDao.ERROR_EDIT_REASON_KEY_RESERVED %>"/>

<c:set var="automaticCancelMigration" value="true" />
<emm:ShowByPermission token="automatic.import.cancel.rollback">
    <c:set var="automaticCancelMigration" value="false" />
</emm:ShowByPermission>

<mvc:form servletRelativeAction="/recipient/import/errors/save.action" modelAttribute="form" id="errors-form" data-form="resource"
          data-controller="${automaticCancelMigration ? 'recipient-import-errors-edit' : ''}" data-initializer="recipient-import-errors-edit"
          data-action="save-errors">
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
            <h2 class="headline"><mvc:message code="import.edit.data"/></h2>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><mvc:message code="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>

                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="listSize"/></li>
                        <li>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="20" />
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="50" />
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="100" />
                                <span class="label-text">100</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show" /></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content">
            <div class="table-control">
                <div class="well well-info block"><mvc:message code="import.title.error_edit" /></div>
            </div>
            <div class="table-wrapper">
                <c:set var="errorIndex" value="0" />

                <display:table class="table table-bordered table-striped table-form js-table"
                               pagesize="${form.numberOfRows}"
                               id="recipient"
                               name="recipientList"
                               sort="external"
                               excludedParams="*"
                               requestURI="/recipient/import/errors/edit.action?__fromdisplaytag=true"
                               partialList="true"
                               size="${recipientList.fullListSize}">

                    <%--@elvariable id="recipient" type="java.util.Map<java.lang.String, java.lang.Object>"--%>

                    <display:column class="import_errors_head_name" headerClass="import_errors_head_name" titleKey="errorReason">
                        <mvc:message code="${recipient[ERROR_EDIT_REASON_KEY_RESERVED]}" />
                    </display:column>

                    <c:forEach items="${columns}" var="column">
                        <c:if test="${column.importedColumn}">
                            <display:column class="import_errors_head_name" headerClass="import_errors_head_name" title="${column.colName}">
                                <div class="has-warning has-feedback">
                                    <c:set var="erroneousFieldName" value="${recipient[VALIDATOR_RESULT_RESERVED]}" />
                                    <c:set var="fieldValid" value="${erroneousFieldName ne null and column.colName ne erroneousFieldName}" />

                                    <c:choose>
                                        <c:when test="${fieldValid}">
                                            ${recipient[column.colName]}
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="inputNamePrefix" value="errorsFixes[${errorIndex}]" />
                                            <c:set var="errorIndex" value="${errorIndex + 1}" />

                                            <input type="hidden" name="${inputNamePrefix}.index" value="${recipient.ERROR_EDIT_RECIPIENT_EDIT_RESERVED.temporaryId}">
                                            <input type="hidden" name="${inputNamePrefix}.fieldName" value="${column.colName}">

                                            <input type="text" class="form-control" name="${inputNamePrefix}.value" value="${recipient[column.colName]}">
                                            <span class="icon icon-state-warning form-control-feedback"></span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </display:column>
                        </c:if>
                    </c:forEach>
                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
