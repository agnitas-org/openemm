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
        <a href="#" class="headline" data-toggle-tile="#tile-mediaMms">
            <i class="tile-toggle icon icon-angle-up"></i>
            <bean:message key="mailing.MediaType.3"/>
        </a>
    </div>
    <div id="tile-mediaMms" class="tile-content tile-content-forms">
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="mediaMmsSubject">
                   <bean:message key="mailing.Subject"/>
                </label>
            </div>
            <div class="col-sm-8">
                <html:text styleClass="form-control" styleId="mediaMmsSubject" property="mms.subject"/>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="mediaMmsSender">
                    <bean:message key="mailing.Sender_Adress"/>
                </label>
            </div>
            <div class="col-sm-8">
                <html:text styleClass="form-control" styleId="mediaMmsSender" property="mms.fromAdr" />
            </div>
        </div>

        <emm:ShowByPermission token="template.show">
            <div class="inline-tile">
                <div class="inline-tile-header">
                    <h2 class="headline"><bean:message key="Template"/></h2>
                    <ul class="inline-tile-header-actions">
                        <li>
                            <a href="#" data-modal="modal-editor" data-modal-set="title: <bean:message key="mailing.MediaType.3"/>, target: mediaMmsTemplate, id: mediaMmsTemplateLarge, type: text" data-tooltip="<bean:message key='editor.enlargeEditor'/>">
                                <i class="icon icon-arrows-alt"></i>
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="inline-tile-content">
                    <div class="row">
                        <div class="col-sm-12">
                            <html:textarea styleClass="form-control js-editor-text" styleId="mediaMmsTemplate" property="mms.template" rows="14" cols="75"/>
                        </div>
                    </div>
                </div>
            </div>
        </emm:ShowByPermission>
    </div>
</div>
