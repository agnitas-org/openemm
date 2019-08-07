<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.util.*, org.agnitas.web.*"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title">
                    <bean:message key="recipient.Salutation"/>:&nbsp;${salutationForm.shortname}
                </h4>
            </div>

            <agn:agnForm action="/salutation">
                <html:hidden property="salutationID"/>
                <html:hidden property="action"/>
                <html:hidden property="previousAction"/>
                <input type="hidden" id="kill" name="kill" value="" />

                <div class="modal-body">
                    <bean:message key="settings.DeleteSalutationQuestion"/>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-form-set="kill: kill" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="button.Delete"/></span>
                        </button>
                    </div>
                </div>
            </agn:agnForm>
        </div>
    </div>
</div>
