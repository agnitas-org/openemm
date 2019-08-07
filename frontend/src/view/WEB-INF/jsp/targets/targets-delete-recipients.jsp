<%@ page language="java" import="com.agnitas.web.ComTargetAction" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ACTION_DELETE_RECIPIENTS" value="<%= ComTargetAction.ACTION_DELETE_RECIPIENTS %>"/>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <html:form action="/target">
                <html:hidden property="action" value="${ACTION_DELETE_RECIPIENTS}"/>
                <html:hidden property="targetID"/>
                <input type="hidden" id="kill" name="kill" value=""/>
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><bean:message key="button.Cancel"/></span></button>
                    <h4 class="modal-title"><bean:message key="target.delete.recipients"/></h4>
                </div>
                <div class="modal-body">
                    <bean:message key="target.delete.recipients.question" arg0="${targetForm.numOfRecipients}" />&nbsp;
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal" data-form-set="kill: true">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="button.Delete"/></span>
                        </button>
                    </div>
                </div>
            </html:form>
        </div>
    </div>
</div>
