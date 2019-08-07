<%@ page language="java" import="com.agnitas.util.*, java.util.*, org.agnitas.web.EmmActionAction" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<% int index=((Integer)request.getAttribute("opIndex")).intValue(); %>

<div class="inline-tile-content">
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><bean:message key="To"/></label>
        </div>
        <div class="col-sm-8">
            <html:text styleClass="form-control" property='<%= "actions[" + index + "].toAddress" %>'/>
        </div>
    </div>
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><bean:message key="From"/></label>
        </div>
        <div class="col-sm-8">
            <html:text styleClass="form-control" property='<%= "actions[" + index + "].fromAddress" %>'/>
        </div>
    </div>
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><bean:message key="mailing.ReplyEmail"/></label>
        </div>
        <div class="col-sm-8">
            <html:text styleClass="form-control" property='<%= "actions[" + index + "].replyAddress" %>'/>
        </div>
    </div>
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label"><bean:message key="mailing.Subject"/></label>
        </div>
        <div class="col-sm-8">
            <html:text styleClass="form-control" property='<%= "actions[" + index + "].subjectLine" %>'/>
        </div>
    </div>
    <div>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><bean:message key="action.Format"/></label>
            </div>
            <div class="col-sm-8">
                <c:set var="HTMLVersion" value='<%= "#HTMLVersion_" + index %>'/>
                <c:set var="TextVersion" value='<%= "#TextVersion_" + index %>'/>

                <agn:agnSelect styleId='<%= "selectFormat_" + index %>' styleClass="form-control" property='<%= "actions[" + index + "].mailtype" %>' size="1" >
                    <agn:agnOption value="0"><bean:message key="Text"/></agn:agnOption>
                    <agn:agnOption value="1"><bean:message key="mailing.Text_HTML"/></agn:agnOption>
                </agn:agnSelect>
            </div>
        </div>
        <div id='<%= "TextVersion_" + index %>' class="inline-tile">
            <div class="inline-tile-header">
                <h2 class="headline"><bean:message key="Text_Version"/></h2>
                <ul class="inline-tile-header-actions">
                    <li>
                        <a href="#" data-modal="modal-editor" data-modal-set="title: <bean:message key="Text_Version"/>, target: <%= "textMail" + index %>, id: textTemplateLarge, type: text" data-tooltip="<bean:message key='editor.enlargeEditor'/>">
                           <i class="icon icon-arrows-alt"></i>
                        </a>
                    </li>
                </ul>
            </div>
            <div class="inline-tile-content">
                <div class="row">
                    <div class="col-sm-12">
                        <html:textarea property='<%= "actions[" + index + "].textMail" %>' styleId='<%= "textMail" + index %>' rows="14" cols="70" styleClass="form-control js-editor-text"/>
                    </div>
                </div>
            </div>
        </div>
        <div id='<%= "HTMLVersion_" + index %>' class="inline-tile">
            <div class="inline-tile-header">
                <h2 class="headline"><bean:message key="mailing.HTML_Version"/></h2>
                <ul class="inline-tile-header-actions">
                    <li>
                        <a href="#" data-modal="modal-editor" data-modal-set="title: <bean:message key="mailing.HTML_Version"/>, target: <%= "htmlMail" + index %>, id: htmlTemplateLarge" data-tooltip="<bean:message key='editor.enlargeEditor'/>">
                           <i class="icon icon-arrows-alt"></i>
                        </a>
                    </li>
                </ul>
            </div>
            <div class="inline-tile-content">
                <div class="row">
                    <div class="col-sm-12">
                        <html:textarea property='<%= "actions[" + index + "].htmlMail" %>' styleId='<%= "htmlMail" + index %>' rows="14" cols="70" styleClass="form-control js-editor" />
                    </div>
                </div>
            </div>
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
