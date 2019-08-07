<%@ page import="org.agnitas.web.RecipientAction"  errorPage="/error.do" %>
<%@ page import="org.agnitas.web.RecipientForm" %>
<%@ page import="org.agnitas.web.StrutsActionBase" %>
<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ACTION_BULK_DELETE" value="<%=StrutsActionBase.ACTION_DELETE%>" />

<%
    RecipientForm aForm = (RecipientForm) session.getAttribute("recipientForm");
    int cancelAction = RecipientAction.ACTION_VIEW;
    if(aForm.getFromListPage()) {
        cancelAction = RecipientAction.ACTION_LIST;
    }
%>
<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <html:form action="/recipient">
                <html:hidden property="recipientID"/>
                <html:hidden property="action" value="${ACTION_BULK_DELETE}"/>
                <html:hidden property="user_type"/>
                <html:hidden property="user_status"/>
                <html:hidden property="listID"/>
                <input type="hidden" name="kill" value=""/>

                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><bean:message key="button.Cancel"/></span></button>
                    <h4 class="modal-title"><bean:message key="Recipient"/>:&nbsp;${recipientForm.firstname} ${recipientForm.lastname}</h4>
                </div>
                <div class="modal-body">
                    <bean:message key="recipient.confirm_delete"/>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal" data-form-set="kill:true">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="button.Delete"/></span>
                        </button>
                    </div>
                </div>
            </html:form>
        </div>
    </div>
</div>
