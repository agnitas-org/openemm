<%@ page import="com.agnitas.emm.core.imports.web.ImportController" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="MAILING" value="<%= ImportController.ImportType.MAILING %>" />
<c:set var="TEMPLATE" value="<%= ImportController.ImportType.TEMPLATE %>" />
<c:set var="USER_FORM" value="<%= ImportController.ImportType.USER_FORM %>" />
<c:set var="BUILDING_BLOCK" value="<%= ImportController.ImportType.BUILDING_BLOCK %>" />

<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="type" type="com.agnitas.emm.core.imports.web.ImportController.ImportType"--%>

<c:set var="showCloseBtn" value="true"/>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content border-0">
            <div class="modal-body p-0">
                <c:if test="${type eq MAILING}">
                    <%@ include file="../../mailing/import/mailing-import-tile.jspf" %>
                </c:if>
                <c:if test="${type eq TEMPLATE}">
                    <%@ include file="../../mailing/import/template-import-tile.jspf" %>
                </c:if>
                <c:if test="${type eq USER_FORM}">
                    <%@ include file="../../pages-and-forms/userform/userform-import-tile.jspf" %>
                </c:if>
                <c:if test="${type eq BUILDING_BLOCK}">
                    <%@ include file="../../layout-builder/building-block/div-container-import.jspf" %>
                </c:if>
            </div>
        </div>
    </div>
</div>
