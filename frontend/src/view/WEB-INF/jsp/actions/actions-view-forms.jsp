<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.UserFormEditAction" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_VIEW_USER_FORM" value="<%= UserFormEditAction.ACTION_VIEW%>"/>

<%--@elvariable id="emmActionForm" type="com.agnitas.web.forms.ComEmmActionForm"--%>
<agn:agnForm action="/action" id="emmActionForm" data-form="resource">
    <html:hidden property="actionID"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="workflow.panel.forms"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <c:if test="${empty emmActionForm.usedByFormsNames}">
                <div class="empty-list well">
                    <i class="icon icon-info-circle"></i><strong><bean:message key="default.nomatches"/></strong>
                </div>
            </c:if>
            <c:if test="${not empty emmActionForm.usedByFormsNames}">
                <ul class="list-group">
                    <c:forEach var="userFormTuple" items="${emmActionForm.usedByFormsNames}">
                        <li class="list-group-item">
                            <emm:ShowByPermission token="forms.show">
                                <c:url var="formLink" value="/userform.do">
                                    <c:param name="action" value="${ACTION_VIEW_USER_FORM}"/>
                                    <c:param name="formID" value="${userFormTuple.first}"/>
                                </c:url>
                                <a href="${formLink}"> ${userFormTuple.second} </a>
                            </emm:ShowByPermission>
                            <emm:HideByPermission token="forms.show">
                                ${userFormTuple.second}
                            </emm:HideByPermission>
                        </li>
                    </c:forEach>
                </ul>
            </c:if>
        </div>
    </div>
</agn:agnForm>
