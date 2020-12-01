<%@ page language="java" import="org.agnitas.web.EmmActionAction" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<% int index=((Integer)request.getAttribute("opIndex")).intValue(); %>

<div class="inline-tile-content">
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><bean:message key="Mailing"/></label>
        </div>
        <div class="col-sm-8">
            <html:select styleClass="form-control js-select" property='<%= "actions[" + index + "].mailingID" %>' size="1">
            	<html:option value="0"><bean:message key="error.report.select_mailing"/></html:option>
                <logic:iterate id="mailing" name="emmActionForm" property="mailings">
                    <html:option value="${mailing.id}">${mailing.shortname}</html:option>
                </logic:iterate>
            </html:select>
        </div>
    </div>
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><bean:message key="Delay"/></label>
        </div>
        <div class="col-sm-8">
            <html:select styleClass="form-control js-select" property='<%= "actions[" + index + "].delayMinutes" %>' size="1">
                <html:option value="0"><bean:message key="action.No_Delay"/></html:option>
                <html:option value="60">1&nbsp;<bean:message key="Hour"/></html:option>
                <html:option value="360">6&nbsp;<bean:message key="Hours"/></html:option>
                <html:option value="720">12&nbsp;<bean:message key="Hours"/></html:option>
                <html:option value="1440">1&nbsp;<bean:message key="Day"/></html:option>
                <html:option value="2880">2&nbsp;<bean:message key="Days"/></html:option>
                <html:option value="5760">4&nbsp;<bean:message key="Days"/></html:option>
                <html:option value="10080">7&nbsp;<bean:message key="Days"/></html:option>
            </html:select>
        </div>
    </div>
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label">
            	<bean:message key="action.address.bcc"/>
            	<button class="icon icon-help" data-help="help_${helplanguage}/actions/BCCMsg.xml" tabindex="-1" type="button"></button>
            </label>
        </div>
        
        <div class="col-sm-8">
            <html:text property='<%= "actions[" + index + "].bcc" %>' styleClass="form-control"/>
        </div>
        
    </div>
</div>
<div class="inline-tile-footer">
    <emm:ShowByPermission token="actions.change">
        <a class="btn btn-regular" href="#" data-form-set="action: <%= EmmActionAction.ACTION_REMOVE_MODULE %>, deleteModule: <%= index %>" data-form-submit>
            <i class="icon icon-trash-o"></i>
            <span class="text"><bean:message key="button.Delete"/></span>
        </a>
    </emm:ShowByPermission>
</div>
