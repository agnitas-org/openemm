<%--checked --%>
<%@ page language="java" import="org.agnitas.web.MailingSendAction" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<c:set var="ACTION_VIEW_SEND" value="<%= MailingSendAction.ACTION_VIEW_SEND %>"/>

<html:form action="/mailingsend">
    <html:hidden property="mailingID"/>
    <html:hidden property="action"/>

    <br>
    <b><bean:message key="mailing.generation.cancel.deny"/></b>
    <br>

    <p>
        <html:link page="/mailingsend.do?action=${ACTION_VIEW_SEND}&mailingID=${mailingBaseForm.mailingID}" >
            <html:img src="button?msg=button.Back" border="0"/>
        </html:link>
    </p>

</html:form>
    
