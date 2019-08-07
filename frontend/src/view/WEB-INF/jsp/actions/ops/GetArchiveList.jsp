<%@ page language="java" import="org.agnitas.web.EmmActionAction" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%
int index=((Integer)request.getAttribute("opIndex")).intValue(); %>
<div class="inline-tile-content">
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><bean:message key="mailing.archive"/></label>
        </div>
        <div class="col-sm-8">
            <html:select styleClass="form-control js-select" property='<%= "actions[" + index + "].campaignID" %>' size="1">
                <logic:iterate id="agnTbl3" name="emmActionForm" property="campaigns" length="1000">
                    <html:option value="${agnTbl3.id}">${agnTbl3.shortname}</html:option>
                </logic:iterate>
            </html:select>
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
