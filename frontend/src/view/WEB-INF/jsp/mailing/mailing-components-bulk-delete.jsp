<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComMailingComponentsAction" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingComponentsForm" type="com.agnitas.web.forms.ComMailingComponentsForm"--%>

<emm:CheckLogon/>
<emm:Permission token="mailing.components.change"/>

<c:set var="ACTION_BULK_DELETE" value="<%= ComMailingComponentsAction.ACTION_BULK_DELETE%>"/>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                    <i aria-hidden="true" class="icon icon-times-circle"></i>
                    <span class="sr-only"><bean:message key="button.Cancel"/></span>
                </button>
                <h4 class="modal-title">
                    <bean:message key="bulkAction.delete.image"/>
                </h4>
            </div>

            <html:form action="/mcomponents">
                <html:hidden property="mailingID"/>
                <c:forEach items="${mailingComponentsForm.bulkIds}" var="componentId">
                    <input type="hidden" name="bulkID[${componentId}]" value="on"/>
                </c:forEach>
                <html:hidden property="action" value="${ACTION_BULK_DELETE}"/>

                <div class="modal-body">
                    <bean:message key="bulkAction.delete.image.question"/>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="button.Delete"/></span>
                        </button>
                    </div>
                </div>

            </html:form>

        </div>
    </div>
</div>
