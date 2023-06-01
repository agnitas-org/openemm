<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common" prefix="emm" %>

<%--@elvariable id="emmLayoutBase" type="org.agnitas.beans.EmmLayoutBase"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>

<emm:ShowByPermission token="mailing.import">
    <jsp:include page="../mailing/mailing-import.jsp">
        <jsp:param name="importFromTemplates" value="true"/>
    </jsp:include>
    <jsp:include page="../mailing/template-import.jsp">
        <jsp:param name="importFromTemplates" value="true"/>
    </jsp:include>
</emm:ShowByPermission>
<emm:ShowByPermission token="forms.import">
    <jsp:include page="../userform/userform-import.jsp">
        <jsp:param name="importFromTemplates" value="true"/>
    </jsp:include>
</emm:ShowByPermission>
