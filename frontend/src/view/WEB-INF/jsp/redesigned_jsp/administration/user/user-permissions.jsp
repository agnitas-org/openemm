<%@page import="com.agnitas.emm.core.Permission"%>
<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/errorRedesigned.action" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm"%>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<c:set var="USERRIGHT_MESSAGEKEY_PREFIX" value="<%= Permission.USERRIGHT_MESSAGEKEY_PREFIX %>"/>

<%--@elvariable id="isRestfulUser" type="java.lang.Boolean"--%>
<%--@elvariable id="adminRightsForm" type="com.agnitas.emm.core.admin.form.AdminRightsForm"--%>

<c:set var="actionUrl" value="/${isRestfulUser ? 'restfulUser' : 'admin'}/${adminRightsForm.adminID}/rights/view.action" />
<mvc:form id="PermissionForm" servletRelativeAction="${actionUrl}" modelAttribute="adminRightsForm"
          data-controller="user|groups-permissions"
          cssClass="tiles-container d-flex hidden" data-editable-view="${agnEditViewKey}">

    <%@ include file="../permissions.jspf" %>

</mvc:form>
