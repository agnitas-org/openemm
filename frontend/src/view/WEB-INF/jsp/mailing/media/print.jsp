<%@ page language="java" import="org.agnitas.util.*, org.agnitas.web.*, com.agnitas.web.*,com.agnitas.web.forms.*, java.util.*, org.agnitas.beans.*" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<% int tmpMailingID=0;
   ComMailingBaseForm aForm=null;

   if((aForm=(ComMailingBaseForm)session.getAttribute("mailingBaseForm"))!=null) {
      tmpMailingID=aForm.getMailingID();
   }
   Locale aLocale=(Locale)session.getAttribute(org.apache.struts.Globals.LOCALE_KEY);
%>

<div class="tile">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tile-mediaPrint">
            <i class="tile-toggle icon icon-angle-up"></i>
            <bean:message key="mailing.MediaType.2"/>
        </a>
    </div>
    <div id="tile-mediaPrint" class="tile-content tile-content-forms">
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="mediaPrintPaperSize">
                    <bean:message key="print.papersize"/>
                </label>
            </div>
            <div class="col-sm-8">
                <select id="mediaPrintPaperSize" class="form-control" property="paperSize">
                    <option value="4">DIN A4</option>
                    <option value="5">DIN A5</option>
                    <option value="6">DIN A6</option>
                </select>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="mediaPrintQuality">
                    <bean:message key="print.quality"/>
                </label>
            </div>
            <div class="col-sm-8">
                <select property="mediaPrintQuality"  class="form-control">
                    <option value="0"><bean:message key="print.quality.low"/></option>
                    <option value="1"><bean:message key="print.quality.high"/></option>
                </select>
            </div>
        </div>

        <emm:ShowByPermission token="template.show">
            <% if(aForm.getAction()!=ComMailingBaseAction.ACTION_SAVE_PRINT_BG) { %>
                <div class="inline-tile">
                    <div class="inline-tile-header">
                        <h2 class="headline"><bean:message key="Template"/></h2>
                        <ul class="inline-tile-header-actions">
                            <li>
                                <a href="#" data-modal="modal-editor" data-modal-set="title: <bean:message key="mailing.MediaType.2"/>, target: mediaPrintTemplate, id: mediaPrintTemplateLarge, type: text" data-tooltip="<bean:message key='editor.enlargeEditor'/>">
                                    <i class="icon icon-arrows-alt"></i>
                                </a>
                            </li>
                        </ul>
                    </div>
                    <div class="inline-tile-content">
                        <div class="row">
                            <div class="col-sm-12">
                                <html:textarea styleId="mediaPrintTemplate" styleClass="form-control js-editor-text" property="print.template" rows="14" cols="75"/>
                            </div>
                        </div>
                    </div>
                </div>

                <% if(aForm.getMailingID()!=0) { %>
                <div class="form-group">
                    <div class="col-sm-12">
                        <html:link page='<%= "/mailingbase.do?mailingID=" + tmpMailingID + "&action=" + ComMailingBaseAction.ACTION_VIEW_PRINT_BG %>' styleClass="btn btn-regular btn-primary pull-right">Hintergrund &auml;ndern...</html:link>
                    </div>
                </div>
                <% } %>


            <% } else { %>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="form-control" for="mediaPrintBackground">
                            <bean:message key="template.print.background"/>
                        </label>
                    </div>
                    <div class="col-sm-4">
                        <html:file styleId="mediaPrintBackground" styleClass="form-control" property="printBackground"/>
                    </div>
                </div>
            <% } %>
      </emm:ShowByPermission>


    </div>

</div>
