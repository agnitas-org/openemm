<%@ page language="java" import="org.agnitas.web.EmmActionAction" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<% int index=((Integer)request.getAttribute("opIndex")).intValue(); %>
<div class="inline-tile-content">
    <div class="inline-tile">
        <div class="inline-tile-header">
            <h2 class="headline"><bean:message key="Script"/></h2>
            <ul class="inline-tile-header-actions">
                <li>
                    <a href="#" data-modal="modal-editor" data-modal-set="title: <bean:message key="Script"/>, target: <%= "script" + index %>, id: textTemplateLarge, type: text" data-tooltip="<bean:message key='editor.enlargeEditor'/>">
                       <i class="icon icon-arrows-alt"></i>
                    </a>
                </li>
            </ul>
        </div>
        <div class="inline-tile-content">
            <html:textarea styleClass="form-control js-editor" property='<%= "actions[" + index+"].script" %>' styleId='<%= "script" + index %>' rows="25" cols="75"/>
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
