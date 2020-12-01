<%@ page language="java" contentType="text/html; charset=utf-8"
         import="org.agnitas.util.*, java.util.*, java.text.*, org.agnitas.web.*, org.agnitas.beans.*"  errorPage="/error.do" %>
<%@ page import="org.agnitas.util.importvalues.ImportMode" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="updateOnlyModeCode" value="<%= ImportMode.UPDATE.getIntValue() %>"/>


<agn:agnForm action="/importwizard" enctype="multipart/form-data" data-form="resource">
    <html:hidden property="action"/>
    <input type="hidden" name="verifymissingfields_back" id="verifymissingfields_back" value="">

    <div class="col-md-10 col-md-push-1 col-lg-8 col-lg-push-2">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><i class="icon icon-file-o"></i> <bean:message key="import.Wizard"/></h2>
                <ul class="tile-header-actions">
                    <li class="">
                        <ul class="pagination">
                            <li>
                                <a href="#" data-form-set="verifymissingfields_back: verifymissingfields_back" data-form-submit>
                                    <i class="icon icon-angle-left"></i>
                                    <bean:message key="button.Back" />
                                </a>
                            </li>
                            <li class="disabled"><span>1</span></li>
                            <li class="disabled"><span>2</span></li>
                            <li class="disabled"><span>3</span></li>
                            <li class="active"><span>4</span></li>
                            <li class="disabled"><span>5</span></li>
                            <li class="disabled"><span>6</span></li>
                            <li class="disabled"><span>7</span></li>
                            <li>
                                <a href="#" data-form-submit>
                                    <bean:message key="button.Proceed" />
                                    <i class="icon icon-angle-right"></i>
                                </a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="table-wrapper">
                    <table class="table table-bordered table-striped">
                        <thead>
                            <logic:equal value="true" name="importWizardForm" property="genderMissing">
                                <tr>
                                    <th><bean:message key="error.import.column.gender.required"/></th>
                                    <th><bean:message key="import.gender.as.unknown"/></th>
                                    <html:hidden property="manualAssignedGender"/>
                                <tr>
                            </logic:equal>
                        </thead>
                        <tbody>
                            <c:if test="${importWizardForm.mailingTypeMissing and importWizardForm.getMode() != updateOnlyModeCode}">
                            <tr>
                                <td><bean:message key="recipient.mailingtype"/>&nbsp;&nbsp;</td>
                                <td>
                                    <html:select property="manualAssignedMailingType" styleClass="form-control js-select">
                                        <html:option value="${MAILTYPE_TEXT}">
                                            <bean:message key="recipient.mailingtype.text"/>
                                        </html:option>
                                        <html:option value="${MAILTYPE_HTML}">
                                            <bean:message key="HTML"/>
                                        </html:option>
                                        <html:option value="${MAILTYPE_HTML_OFFLINE}">
                                            <bean:message key="recipient.mailingtype.htmloffline"/>
                                        </html:option>
                                    </html:select>
                                </td>
                            <tr>
                                </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="tile-footer">
                <a href="#" class="btn btn-large pull-left" data-form-set="verifymissingfields_back: verifymissingfields_back" data-form-submit>
                    <i class="icon icon-angle-left"></i>
                    <span class="text"><bean:message key="button.Back"/></span>
                </a>
                <button type="button" class="btn btn-large btn-primary pull-right" data-form-submit>
                    <span class="text"><bean:message key="button.Proceed"/></span>
                    <i class="icon icon-angle-right"></i>
                </button>
                <span class="clearfix"></span>
            </div>
        </div>
    </div>
</agn:agnForm>

