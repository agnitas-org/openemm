<%@ page language="java" import="org.agnitas.util.*, org.agnitas.web.*, com.agnitas.web.*,com.agnitas.web.forms.*, java.util.*" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
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
        <a href="#" class="headline" data-toggle-tile="#tile-mediaFax">
            <i class="tile-toggle icon icon-angle-up"></i>
            <bean:message key="mailing.MediaType.1" />
        </a>
    </div>
    <div id="tile-mediaFax" class="tile-content tile-content-forms">
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="mediaFaxSubject">
                   <bean:message key="mailing.Subject"/>
                </label>
            </div>
            <div class="col-sm-8">
                <html:text styleClass="form-control" styleId="mediaFaxSubject" property="fax.subject"/>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="mediaFaxQuality"><bean:message key="fax.quality"/>:</label>
            </div>
            <div class="col-sm-8">
                <html:select styleId="mediaFaxQuality" styleClass="form-control" property="fax.resolution">
                    <html:option value="0"><bean:message key="fax.quality.low"/></html:option>
                    <html:option value="1"><bean:message key="fax.quality.high"/></html:option>
                </html:select>
            </div>
        </div>

        <emm:ShowByPermission token="template.show">
            <% if(aForm.getAction()!=ComMailingBaseAction.ACTION_SAVE_FAX_BG) { %>
                <div class="inline-tile">
                    <div class="inline-tile-header">
                        <h2 class="headline"><bean:message key="Template"/></h2>
                        <ul class="inline-tile-header-actions">
                            <li>
                                <a href="#" data-modal="modal-editor" data-modal-set="title: <bean:message key="mailing.MediaType.1"/>, target: mediaFaxTemplate, id: mediaFaxTemplateLarge, type: text" data-tooltip="<bean:message key='editor.enlargeEditor'/>">
                                    <i class="icon icon-arrows-alt"></i>
                                </a>
                            </li>
                        </ul>
                    </div>
                    <div class="inline-tile-content">
                        <div class="row">
                            <div class="col-sm-12">
                                <html:textarea styleId="mediaFaxTemplate" styleClass="form-control js-editor-text" property="fax.template" rows="14" cols="75"/>
                            </div>
                        </div>
                    </div>
                </div>

                <% if(aForm.getMailingID()!=0) { %>
                <div class="form-group">
                    <div class="col-sm-12">
                        <html:link page='<%= "/mailingbase.do?mailingID=" + tmpMailingID + "&action=" + ComMailingBaseAction.ACTION_VIEW_FAX_BG %>' styleClass="btn btn-regular btn-primary pull-right">Hintergrund &auml;ndern...</html:link>
                    </div>
                </div>
                <% } %>

            <% } else { %>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="form-control" for="mediaFaxBackground">
                            <bean:message key="template.fax.background"/>
                        </label>
                    </div>
                    <div class="col-sm-4">
                        <html:file styleId="mediaFaxBackground" styleClass="form-control" property="faxBackground"/>
                    </div>
                </div>
            <% } %>
        </emm:ShowByPermission>
    </div>

</div>
