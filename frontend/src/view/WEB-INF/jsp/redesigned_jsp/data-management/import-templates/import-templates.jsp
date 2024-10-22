<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common" prefix="emm" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div class="tiles-container" style="display: grid; grid-template-rows: 1fr 1fr; grid-template-columns: 1fr 1fr" data-editable-view="${agnEditViewKey}">
    <emm:ShowByPermission token="mailing.import">
        <%@ include file="../../mailing/import/mailing-import-tile.jspf" %>
        <%@ include file="../../mailing/import/template-import-tile.jspf" %>
    </emm:ShowByPermission>
    <emm:ShowByPermission token="forms.import">
        <%@ include file="../../pages-and-forms/userform/userform-import-tile.jspf" %>
    </emm:ShowByPermission>
    <%@ include file="extended-imports.jspf" %>
</div>
