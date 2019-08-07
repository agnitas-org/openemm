<%@ page language="java" import="com.agnitas.web.*" contentType="text/html; charset=utf-8" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="targetForm" type="com.agnitas.web.forms.ComTargetForm"--%>

<c:set var="ACTION_DELETE_FROM_MAILINGWIZARD" value="<%= ComTargetAction.ACTION_DELETE_FROM_MAILINGWIZARD %>"/>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <html:form action="/mwNewTarget">
                <html:hidden property="targetID"/>
                <html:hidden property="action"/>
                <html:hidden property="previousAction" value="${ACTION_DELETE_FROM_MAILINGWIZARD}"/>

                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                        <span class="sr-only">
                            <bean:message key="button.Cancel"/>
                        </span>
                    </button>
                    <h4 class="modal-title"><bean:message key="target.Target"/>:&nbsp;${fn:escapeXml(targetForm.shortname)}</h4>
                </div>
                <div class="modal-body">
                    <bean:message key="target.Target"/>:<br>
                        ${fn:escapeXml(targetForm.shortname)}<br>
                    <bean:message key="target.delete.question"/>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <emm:ShowByPermission token="targets.delete">
                            <button type="button" class="btn btn-primary btn-large" data-form-submit-static data-dismiss="modal">
                                <i class="icon icon-check"></i>
                                <span class="text"><bean:message key="button.Delete"/></span>
                            </button>
                        </emm:ShowByPermission>
                    </div>
                </div>
            </html:form>
        </div>
    </div>
</div>
