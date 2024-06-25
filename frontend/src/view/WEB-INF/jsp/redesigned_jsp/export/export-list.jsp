<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="export" type="org.agnitas.beans.ExportPredef"--%>
<%--@elvariable id="exports" type="java.util.List<org.agnitas.beans.ExportPredef>"--%>
<%--@elvariable id="form" type="org.agnitas.web.forms.PaginationForm"--%>

<mvc:message var="exportDeleteMessage" code="export.ExportDelete"/>

<div class="tiles-container">
    <mvc:form servletRelativeAction="/export/list.action" modelAttribute="form" cssClass="tile">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "export-profile-overview": {
                    "rows-count": ${form.numberOfRows}
                }
            }
        </script>

        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
        </div>

        <div class="tile-body">
            <div class="table-box">
                <div class="table-scrollable">
                    <display:table class="table table-hover table-rounded js-table" pagesize="${form.numberOfRows}" id="export" list="${exports}"
                                   requestURI="/export/list.action" excludedParams="*" length="${fn:length(exports)}" sort="list">

                        <%@ include file="../displaytag/displaytag-properties.jspf" %>

                        <display:column headerClass="js-table-sort" sortable="true" titleKey="default.Name" property="shortname"/>
                        <display:column headerClass="js-table-sort" sortable="true" titleKey="Description" property="description"/>

                        <display:column headerClass="fit-content">
                            <a href='<c:url value="/export/${export.id}/view.action"/>' class="hidden" data-view-row="page"></a>

                            <emm:ShowByPermission token="export.delete">
                                <a href='<c:url value="/export/${export.id}/delete.action"/>' class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="${exportDeleteMessage}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </emm:ShowByPermission>
                        </display:column>

                    </display:table>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
