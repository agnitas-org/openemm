<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="checkResult" type="com.agnitas.emm.core.db_schema.bean.DbSchemaCheckResult"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="server.status.db.schema.difference" /></h1>
                <button type="button" class="btn-close" data-confirm-negative>
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body vstack gap-3">
                <c:if test="${not empty checkResult.missingTables or not empty checkResult.obsoleteTables}">
                    <div class="table-wrapper">
                        <div class="table-wrapper__header"></div>
                        <div class="table-wrapper__body">
                            <table class="table table--borderless">
                                <thead>
                                <tr>
                                    <th><mvc:message code="server.status.db.schema.tables.missing" /></th>
                                    <th><mvc:message code="server.status.db.schema.tables.obsolete" /></th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <td class="align-top">
                                        <c:if test="${not empty checkResult.missingTables}">
                                            <c:forEach var="missingTableName" items="${checkResult.missingTables}">
                                                <div class="py-2">${missingTableName}</div>
                                            </c:forEach>
                                        </c:if>
                                    </td>
                                    <td class="align-top">
                                        <c:if test="${not empty checkResult.obsoleteTables}">
                                            <c:forEach var="obsoleteTableName" items="${checkResult.obsoleteTables}">
                                                <div class="py-2">${obsoleteTableName}</div>
                                            </c:forEach>
                                        </c:if>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="table-wrapper__footer"></div>
                    </div>
                </c:if>

                <c:if test="${not empty checkResult.missingColumns}">
                    <div class="table-wrapper">
                        <div class="table-wrapper__header">
                            <h1 class="table-wrapper__title"><mvc:message code="server.status.db.schema.columns.missing" /></h1>
                        </div>
                        <div class="table-wrapper__body">
                            <table class="table table--borderless">
                                <thead>
                                <tr>
                                    <c:forEach var="missingColumnEntry" items="${checkResult.missingColumns}">
                                        <th>${missingColumnEntry.key}</th>
                                    </c:forEach>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <c:forEach var="missingColumnEntry" items="${checkResult.missingColumns}">
                                        <td class="align-top">
                                            <c:forEach var="missingColumn" items="${missingColumnEntry.value}">
                                                <div class="py-2">${missingColumn}</div>
                                            </c:forEach>
                                        </td>
                                    </c:forEach>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="table-wrapper__footer"></div>
                    </div>
                </c:if>

                <c:if test="${not empty checkResult.columnsWithMismatchedTypes}">
                    <div class="table-wrapper">
                        <div class="table-wrapper__header">
                            <h1 class="table-wrapper__title"><mvc:message code="server.status.db.schema.columns.types" /></h1>
                        </div>
                        <div class="table-wrapper__body">
                            <table class="table table--borderless">
                                <thead>
                                <tr>
                                    <c:forEach var="columnWithMismatchedTypeEntry" items="${checkResult.columnsWithMismatchedTypes}">
                                        <th>${columnWithMismatchedTypeEntry.key}</th>
                                    </c:forEach>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <c:forEach var="columnWithMismatchedTypeEntry" items="${checkResult.columnsWithMismatchedTypes}">
                                        <td class="align-top">
                                            <c:forEach var="columnWithMismatchedType" items="${columnWithMismatchedTypeEntry.value}">
                                                <div class="py-2">${columnWithMismatchedType}</div>
                                            </c:forEach>
                                        </td>
                                    </c:forEach>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="table-wrapper__footer"></div>
                    </div>
                </c:if>

                <c:if test="${not empty checkResult.columnsWithMismatchedLength}">
                    <div class="table-wrapper">
                        <div class="table-wrapper__header">
                            <h1 class="table-wrapper__title"><mvc:message code="server.status.db.schema.columns.length" /></h1>
                        </div>
                        <div class="table-wrapper__body">
                            <table class="table table--borderless">
                                <thead>
                                <tr>
                                    <c:forEach var="columnWithMismatchedLengthEntry" items="${checkResult.columnsWithMismatchedLength}">
                                        <th>${columnWithMismatchedLengthEntry.key}</th>
                                    </c:forEach>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <c:forEach var="columnWithMismatchedLengthEntry" items="${checkResult.columnsWithMismatchedLength}">
                                        <td class="align-top">
                                            <c:forEach var="columnWithMismatchedLength" items="${columnWithMismatchedLengthEntry.value}">
                                                <div class="py-2">${columnWithMismatchedLength}</div>
                                            </c:forEach>
                                        </td>
                                    </c:forEach>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="table-wrapper__footer"></div>
                    </div>
                </c:if>
            </div>
            <div class="modal-footer">
                <a href="<c:url value="/serverstatus/schema/diff/download.action" />" class="btn btn-primary" data-prevent-load>
                    <i class="icon icon-file-download"></i>
                    <span><mvc:message code="button.Download" /></span>
                </a>
            </div>
        </div>
    </div>
</div>
