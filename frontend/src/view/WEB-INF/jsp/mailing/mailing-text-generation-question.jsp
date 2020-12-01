<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComMailingContentAction" %>

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common" prefix="emm" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>

<%--@elvariable id="mailingContentForm" type="com.agnitas.web.ComMailingContentForm"--%>

<c:set var="ACTION_GENERATE_TEXT_FROM_HTML"	value="<%= ComMailingContentAction.ACTION_GENERATE_TEXT_FROM_HTML %>"/>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">

            <html:form action="/mailingcontent" >
                <html:hidden property="mailingID" value="${mailingContentForm.mailingID}"/>
                <html:hidden property="action" value="${ACTION_GENERATE_TEXT_FROM_HTML}"/>

                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><bean:message key="button.Cancel"/></span></button>
                    <h4 class="modal-title"><bean:message key="Mailing"/>:&nbsp;${mailingContentForm.shortname}</h4>
                </div>


                <div class="modal-body">
                    <bean:message key="mailing.generateText.question"/>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="default.No"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="default.Yes"/></span>
                        </button>
                    </div>
                </div>

            </html:form>
        </div>
    </div>
</div>
