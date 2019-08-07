<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.util.*, org.agnitas.web.*" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<emm:CheckLogon/>

<display:table name="errorReport" id="reportRow" class="dataTable" >
		<display:column value="${reportRow[0]}" group="1" headerClass="head_name" class="name"  sortable="false" titleKey="Text_Module"/>
		<display:column value="${reportRow[1]}" headerClass="head_name" class="name"  sortable="false" titleKey="mailing.tag"/>
		<display:column value="${reportRow[2]}" headerClass="head_error" class="error"  sortable="false" titleKey="Description"/>
	</display:table>
