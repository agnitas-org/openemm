<%@ page import="com.agnitas.mailing.autooptimization.beans.ComOptimization"  errorPage="/error.do" %>
<%@ page language="java" contentType="text/html; charset=utf-8"
         buffer="64kb" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://ajaxanywhere.sourceforge.net/" prefix="aa" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax" %>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><bean:message key="button.Cancel"/></span></button>
                <h4 class="modal-title">
                    ${optimizationForm.shortname}
                </h4>
            </div>

            <html:form action="/optimize">
                <html:hidden property="optimizationID" />
                <html:hidden property="companyID" />
                <html:hidden property="campaignID" />
                <html:hidden property="method" value="delete" />
                <html:hidden property="previousAction" value="${optimizationForm.previousAction}" />
                <div class="modal-body">
                    <bean:message key="mailing.autooptimization.delete" arg0="${optimizationForm.shortname}"/>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal" data-form-set="method: cancelled">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal" data-form-set="method: delete">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="button.Delete"/></span>
                        </button>
                    </div>
                </div>

            </html:form>
        </div>
    </div>
</div>
