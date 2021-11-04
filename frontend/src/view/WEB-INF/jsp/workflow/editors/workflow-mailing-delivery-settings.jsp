<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowMailing" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="AUTOMATIC_REPORT_NONE" value="<%= WorkflowMailing.AUTOMATIC_REPORT_NONE %>" scope="page"/>
<c:set var="AUTOMATIC_REPORT_1DAY" value="<%= WorkflowMailing.AUTOMATIC_REPORT_1DAY %>" scope="page"/>
<c:set var="AUTOMATIC_REPORT_2DAYS" value="<%= WorkflowMailing.AUTOMATIC_REPORT_2DAYS %>" scope="page"/>
<c:set var="AUTOMATIC_REPORT_7DAYS" value="<%= WorkflowMailing.AUTOMATIC_REPORT_7DAYS %>" scope="page"/>

<div class="editor-main-content delivery-settings">
    <div class="form-group">
        <div class="col-xs-12">
            <div id="sendSettingsToggle_${param.editorId}" class="toggle_closed wm-mailing-send-settings-link btn-group">
                <a href="#" class="btn btn-regular" data-action="mailing-editor-base-toggle-settings"
                   data-config="editorId:${param.editorId}">
                    <bean:message key="workflow.mailing.DeliverySettings"/>
                </a>
            </div>
        </div>
    </div>

    <div id="sendSettings_${param.editorId}" class="wm-mailing-send-settings" style="display: none">
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><bean:message key="workflow.mailing.AutomaticReport"/></label>
            </div>
            <div class="col-sm-8">
                <select class="form-control" name="autoReport">
                    <option value="${AUTOMATIC_REPORT_NONE}"><bean:message key="default.none"/></option>
                    <option value="${AUTOMATIC_REPORT_1DAY}"><bean:message key="mailing.send.report.24h"/></option>
                    <option value="${AUTOMATIC_REPORT_2DAYS}"><bean:message key="mailing.send.report.48h"/></option>
                    <option value="${AUTOMATIC_REPORT_7DAYS}"><bean:message key="mailing.send.report.1week"/></option>
                </select>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-8 col-sm-push-4">
                <label class="checkbox-inline">
                    <input id="dontDeliverMailing_${param.editorId}" type="checkbox" class="mailing-send-settings-checkbox" name="skipEmptyBlocks"/>
                    <bean:message key="skipempty.email"/>
                </label>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-8 col-sm-push-4">
                <label class="checkbox-inline">
                    <input id="duplicateCheck_${param.editorId}" type="checkbox" value="true" class="mailing-send-settings-checkbox" name="doubleCheck"/>
                    <bean:message key="doublechecking.email"/>
                </label>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label">
                    <bean:message key="setMaxRecipients"/>
                    <button type="button" class="icon icon-help" tabindex="-1" data-help="help_${helplanguage}/mailing/MailingMaxsendquantyMsg.xml"></button>
                </label>
            </div>
            <div class="col-sm-8">
                <input type="text" class="form-control" value="0" name="maxRecipients"/>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="mailType">
                    <bean:message key="mailing.mailsperhour"/>
                </label>
            </div>
            <div class="col-sm-8">
                <select class="form-control" name="blocksize" id="mailType">
                    <option value="0"><bean:message key="mailing.unlimited"/></option>
                    <option value="500000">500.000</option>
                    <option value="250000">250.000</option>
                    <option value="100000">100.000</option>
                    <option value="50000">50.000</option>
                    <option value="25000">25.000</option>
                    <option value="10000">10.000</option>
                    <option value="5000">5.000</option>
                    <option value="1000">1.000</option>
                </select>
            </div>
        </div>
    </div>
</div>
