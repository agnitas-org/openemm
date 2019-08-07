<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb"  errorPage="/error.do" %>
<%--<%@ page import="com.agnitas.web.forms.ComMailingBaseForm" %>--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<c:if test="${not mailingBaseForm.isTemplate}">
    <emm:Permission token="mailing.delete"/>
</c:if>
<c:if test="${mailingBaseForm.isTemplate}">
    <emm:Permission token="template.delete"/>
</c:if>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><bean:message key="button.Cancel"/></span></button>
                <h4 class="modal-title">
                    <logic:equal name="mailingBaseForm" property="isTemplate" value="true">
                        <bean:message key="bulkAction.delete.template"/>
                    </logic:equal>

                    <logic:equal name="mailingBaseForm" property="isTemplate" value="false">
                        <bean:message key="bulkAction.delete.mailing"/>
                    </logic:equal>
                </h4>
            </div>

            <html:form action="/mailingbase">
                <html:hidden property="action"/>

                <logic:iterate id="mailingID" name="mailingBaseForm" property="bulkIds">
                    <input type="hidden" name="bulkID[${mailingID}]" value="on"/>
                </logic:iterate>

                <div class="modal-body">
                    <logic:equal name="mailingBaseForm" property="isTemplate" value="true">
                        <bean:message key="bulkAction.delete.template.question"/>
                    </logic:equal>

                    <logic:equal name="mailingBaseForm" property="isTemplate" value="false">
                        <bean:message key="bulkAction.delete.mailing.question"/>
                    </logic:equal>
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
