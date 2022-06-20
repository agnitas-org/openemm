<%--checked --%>
<%@ page language="java" import="org.agnitas.web.MailingSendAction" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComMailingSendActionBasic" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>

<c:set var="actionViewSend" value="<%= ComMailingSendActionBasic.ACTION_VIEW_SEND %>"/>


<emm:CheckLogon/>

<emm:Permission token="mailing.send.show"/>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">

            <agn:agnForm action="/mailingsend" data-form="resource">
                <html:hidden property="mailingID"/>
                <html:hidden property="action"/>
                <input type="hidden" id="kill" name="kill" value="true"/>

                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><bean:message key="button.Cancel"/></span></button>
                    <h4 class="modal-title"><bean:message key="Mailing"/>:&nbsp;${mailingSendForm.shortname}</h4>
                </div>


                <div class="modal-body">
                    <bean:message key="mailing.generation.resume.question"/>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="default.No"/></span>
                        </button>

                        <c:url value="/mailingsend.do" var="baseUrl">
                            <c:param name="action" value="${actionViewSend}"/>
                            <c:param name="mailingID" value="${mailingSendForm.mailingID}"/>
                        </c:url>

                        <button type="button" class="btn btn-primary btn-large" data-dismiss="modal" data-action="resume-mailing" data-base-url="${baseUrl}">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="default.Yes"/></span>
                        </button>
                    </div>
                </div>

            </agn:agnForm>
        </div>
    </div>
</div>
