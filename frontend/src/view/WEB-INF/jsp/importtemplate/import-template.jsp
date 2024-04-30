<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common" prefix="emm" %>

<emm:ShowByPermission token="mailing.import">
    <jsp:include page="../mailing/import/mailing-import.jsp"/>
    <jsp:include page="../mailing/import/template-import.jsp"/>
</emm:ShowByPermission>
<emm:ShowByPermission token="forms.import">
    <jsp:include page="../userform/userform-import.jsp"/>
</emm:ShowByPermission>

<%@ include file="fragments/extended-imports.jspf" %>
