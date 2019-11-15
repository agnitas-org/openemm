<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common" prefix="emm" %>

<%--@elvariable id="emmLayoutBase" type="org.agnitas.beans.EmmLayoutBase"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>

<div class="col-md-offset-3 col-md-6">
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="import.templates.div"/>:</h2>
        </div>

        <div class="tile-content">
        	<emm:ShowByPermission token="mailing.import">
            	<jsp:include page="../mailing/mailing-import.jsp"/>
            	<jsp:include page="../mailing/template-import.jsp"/>
            </emm:ShowByPermission>
            <emm:ShowByPermission token="forms.import">
            	<jsp:include page="../userform/userform-import.jsp"/>
            </emm:ShowByPermission>
        </div>
    </div>
</div>
