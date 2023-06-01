<%--checked --%>
<%@ page language="java" import="org.agnitas.web.MailingSendAction" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>

<emm:CheckLogon/>

<emm:Permission token="mailing.send.show"/>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">

            <agn:agnForm action="/mailingsend">
                <html:hidden property="mailingID"/>
                <html:hidden property="action"/>
                <input type="hidden" id="kill" name="kill" value="true"/>

                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><bean:message key="button.Cancel"/></span></button>
                    <h4 class="modal-title"><bean:message key="Mailing"/>:&nbsp;${mailingSendForm.shortname}</h4>
                </div>


                <div class="modal-body">
                    <bean:message key="mailing.generation.resumeByCopy.question"/>
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

            </agn:agnForm>
        </div>
    </div>
</div>
