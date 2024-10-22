<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="export" type="org.agnitas.beans.ExportPredef"--%>
<%--@elvariable id="exports" type="java.util.List<org.agnitas.beans.ExportPredef>"--%>

<mvc:form servletRelativeAction="/export/list.action" modelAttribute="form">
    <div class="tile" data-controller="export">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "export-profile-overview": {
                    "rows-count": ${form.numberOfRows}
                }
            }
        </script>

        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="export"/>
            </h2>
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
                                <mvc:radiobutton path="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="200"/>
                                <span class="label-text">200</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>

        </div>
        <div class="tile-content">
            <div class="table-wrapper">
                <c:set var="index" value="0" scope="request"/>

                <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        pagesize="${form.numberOfRows}"
                        id="export"
                        name="exports"
                        requestURI="/export/list.action"
                        excludedParams="*">

                    <display:column headerClass="js-table-sort" sortable="true" titleKey="default.Name"
                                    property="shortname"/>
                    <display:column headerClass="js-table-sort" sortable="true" titleKey="default.description"
                                    property="description"/>

                    <display:column class="table-actions">
                        <a href='<c:url value="/export/${export.id}/view.action"/>' class="hidden js-row-show"></a>
                        <emm:ShowByPermission token="export.delete">
                            <c:set var="exportDeleteMessage" scope="page">
                                <mvc:message code="export.ExportDelete"/>
                            </c:set>
                            <a href='<c:url value="/export/${export.id}/confirmDelete.action"/>' class="btn btn-regular btn-alert js-row-delete" data-tooltip="${exportDeleteMessage}">
                                <i class="icon icon-trash-o"></i>
                            </a>
                        </emm:ShowByPermission>
                    </display:column>
                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
