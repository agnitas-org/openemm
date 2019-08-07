<%@ page language="java" contentType="text/html; charset=utf-8"
         import="org.agnitas.web.ImportProfileAction, org.agnitas.web.forms.ImportProfileForm"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><bean:message key="button.Cancel"/></span></button>
                <h4 class="modal-title">
                    <bean:message key="import.ImportProfile" />
                    ${importProfileForm.profile.name}
                </h4>
            </div>

            <agn:agnForm action="/importprofile.do">
                <html:hidden property="profileId"/>
                <html:hidden property="action"/>

                <input type="hidden" id="kill" name="kill" value=""/>


                <div class="modal-body">
                    <bean:message key="import.DeleteImportProfileQuestion" />
                </div>
                <div class="modal-footer">

                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal" data-form-set="kill: true">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="button.Delete"/></span>
                        </button>
                    </div>


                </div>

            </agn:agnForm>
        </div>
    </div>
</div>
