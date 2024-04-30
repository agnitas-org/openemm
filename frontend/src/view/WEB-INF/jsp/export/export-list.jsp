<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="export" type="org.agnitas.beans.ExportPredef"--%>
<%--@elvariable id="exports" type="java.util.List<org.agnitas.beans.ExportPredef>"--%>

<div class="tile" data-controller="export">
    <div class="tile-header">
        <h2 class="headline">
            <mvc:message code="export"/>
        </h2>
    </div>
    <div class="tile-content">
        <div class="table-wrapper">
            <c:set var="index" value="0" scope="request"/>

            <display:table
                    class="table table-bordered table-striped table-hover js-table"
                    pagesize="20"
                    id="export"
                    list="${exports}"
                    requestURI="/export/list.action"
                    excludedParams="*"
                    length="${fn:length(exports)}"
                    sort="list">

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
