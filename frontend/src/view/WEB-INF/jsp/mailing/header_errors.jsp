<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<emm:CheckLogon/>

<display:table name="errorReport" id="reportRow" class="dataTable" >
	<display:column value="${reportRow[0]}" group="1" headerClass="head_name" class="name"  sortable="false" titleKey="Text_Module"/>
	<display:column value="${reportRow[1]}" headerClass="head_name" class="name"  sortable="false" titleKey="mailing.tag"/>
	<display:column value="${reportRow[2]}" headerClass="head_error" class="error"  sortable="false" titleKey="Description"/>
</display:table>
