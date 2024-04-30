<%@ page language="java" contentType="text/html; charset=utf-8" import="com.agnitas.web.ComImportWizardAction" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>

<agn:agnForm action="/importwizard" enctype="multipart/form-data" data-form="resource">
    <html:hidden property="action"/>
    <input type="hidden" name="prescan_back" id="prescan_back" value="">

    <div class="col-md-10 col-md-push-1 col-lg-8 col-lg-push-2">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><i class="icon icon-file-o"></i> <bean:message key="import.Wizard"/></h2>
                <ul class="tile-header-actions">
                    <li class="">
                        <ul class="pagination">
                            <li>
                                <a href="#" data-form-set="prescan_back: prescan_back" data-form-submit>
                                    <i class="icon icon-angle-left"></i>
                                    <bean:message key="button.Back" />
                                </a>
                            </li>
                            <li class="disabled"><span>1</span></li>
                            <li class="disabled"><span>2</span></li>
                            <li class="disabled"><span>3</span></li>
                            <li class="disabled"><span>4</span></li>
                            <li class="disabled"><span>5</span></li>
                            <li class="active"><span>6</span></li>
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
            <div class="tile-notification tile-notification-info">
                <bean:message key="ResultMsg"/>
                <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_5/CsvErrors.xml"></button>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="import.csv_errors_email"/></label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <input class="form-control" type="text" readonly value="<bean:write name="importWizardForm" property="status.error(email)" scope="session"/>">
                            </div>
                            <logic:greaterThan name="importWizardForm" scope="session" value="0"
                                    property="status.error(email)">
                                <div class="input-group-btn">
                                    <html:link styleClass="btn btn-regular"
                                        page='<%= "/importwizard.do?action=" + ComImportWizardAction.ACTION_GET_ERROR_EMAIL + "&downloadName=error_email" %>'>
                                        <i class="icon icon-download"></i>
                                        <bean:message key="button.Download"/>
                                    </html:link>
                                </div>
                            </logic:greaterThan>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="import.csv_errors_blacklist"/></label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <input class="form-control" type="text" readonly value="<bean:write name="importWizardForm" property="status.error(blacklist)" scope="session"/>">
                            </div>
                            <logic:greaterThan name="importWizardForm" scope="session" value="0"
                                    property="status.error(blacklist)">
                                <div class="input-group-btn">
                                    <html:link styleClass="btn btn-regular"
                                        page='<%= "/importwizard.do?action=" + ComImportWizardAction.ACTION_GET_ERROR_BLACKLIST + "&downloadName=error_blacklist" %>'>
                                        <i class="icon icon-download"></i>
                                        <bean:message key="button.Download"/>
                                    </html:link>
                                </div>
                            </logic:greaterThan>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="import.csv_errors_double"/></label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <input class="form-control" type="text" readonly value="<bean:write name="importWizardForm" property="status.error(keyDouble)" scope="session"/>">
                            </div>
                            <logic:greaterThan name="importWizardForm" scope="session" value="0"
                                    property="status.error(keyDouble)">
                                <div class="input-group-btn">
                                    <html:link styleClass="btn btn-regular"
                                            page='<%= "/importwizard.do?action=" + ComImportWizardAction.ACTION_GET_ERROR_EMAILDOUBLE + "&downloadName=double_email" %>'>
                                        <i class="icon icon-download"></i>
                                        <bean:message key="button.Download"/>
                                    </html:link>
                                </div>
                            </logic:greaterThan>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="import.csv_errors_numeric"/></label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <input class="form-control" type="text" readonly value="<bean:write name="importWizardForm" property="status.error(numeric)" scope="session"/>">
                            </div>
                            <logic:greaterThan name="importWizardForm" scope="session" value="0"
                                    property="status.error(numeric)">
                                <div class="input-group-btn">
                                    <html:link
                                            styleClass="btn btn-regular"
                                        page='<%= "/importwizard.do?action=" + ComImportWizardAction.ACTION_GET_ERROR_NUMERIC + "&downloadName=error_numeric" %>'>
                                        <i class="icon icon-download"></i>
                                        <bean:message key="button.Download"/>
                                    </html:link>
                                </div>
                            </logic:greaterThan>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="import.csv_errors_mailtype"/></label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <input class="form-control" type="text" readonly value="<bean:write name="importWizardForm" property="status.error(mailtype)" scope="session"/>">
                            </div>
                            <logic:greaterThan name="importWizardForm" scope="session" value="0"
                                property="status.error(mailtype)">
                                <div class="input-group-btn">
                                    <html:link
                                            styleClass="btn btn-regular"
                                        page='<%= "/importwizard.do?action=" + ComImportWizardAction.ACTION_GET_ERROR_MAILTYPE + "&downloadName=error_mailtype" %>'>
                                        <i class="icon icon-download"></i>
                                        <bean:message key="button.Download"/>
                                    </html:link>
                                </div>
                            </logic:greaterThan>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="import.csv_errors_gender"/></label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <input class="form-control" type="text" readonly value="<bean:write name="importWizardForm" property="status.error(gender)" scope="session"/>">
                            </div>
                            <logic:greaterThan name="importWizardForm" scope="session" value="0"
                                    property="status.error(gender)">
                                <div class="input-group-btn">
                                    <html:link
                                            styleClass="btn btn-regular"
                                        page='<%= "/importwizard.do?action=" + ComImportWizardAction.ACTION_GET_ERROR_GENDER + "&downloadName=error_gender" %>'>
                                        <i class="icon icon-download"></i>
                                        <bean:message key="button.Download"/>
                                    </html:link>
                                </div>
                            </logic:greaterThan>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="import.csv_errors_date"/></label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <input class="form-control" type="text" readonly value="<bean:write name="importWizardForm" property="status.error(date)" scope="session"/>">
                            </div>
                            <logic:greaterThan name="importWizardForm" scope="session" value="0"
                                    property="status.error(date)">
                                <div class="input-group-btn">
                                    <html:link
                                            styleClass="btn btn-regular"
                                            page='<%= "/importwizard.do?action=" + ComImportWizardAction.ACTION_GET_ERROR_DATE + "&downloadName=error_date" %>'>
                                        <i class="icon icon-download"></i>
                                        <bean:message key="button.Download"/>
                                    </html:link>
                                </div>
                            </logic:greaterThan>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="csv_errors_linestructure"/></label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <input class="form-control" type="text" readonly value="<bean:write name="importWizardForm" property="status.error(structure)" scope="session"/>">
                            </div>
                            <logic:greaterThan
                                    name="importWizardForm" scope="session" value="0"
                                    property="status.error(structure)">
                                <div class="input-group-btn">
                                    <html:link
                                            styleClass="btn btn-regular"
                                        page='<%= "/importwizard.do?action=" + ComImportWizardAction.ACTION_GET_ERROR_STRUCTURE + "&downloadName=error_structure" %>'>
                                        <i class="icon icon-download"></i>
                                        <bean:message key="button.Download"/>
                                    </html:link>
                                </div>
                            </logic:greaterThan>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="import.csv_summary"/></label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <input class="form-control" type="text" readonly value="<bean:write name="importWizardForm" property="linesOK" scope="session"/>">
                            </div>
                            <logic:greaterThan name="importWizardForm" scope="session" value="0" property="linesOK">
                                <div class="input-group-btn">
                                    <html:link
                                            styleClass="btn btn-regular"
                                        page='<%= "/importwizard.do?action=" + ComImportWizardAction.ACTION_GET_DATA_PARSED + "&downloadName=import_ok" %>'>
                                        <i class="icon icon-download"></i>
                                        <bean:message key="button.Download"/>
                                    </html:link>
                                </div>
                            </logic:greaterThan>
                        </div>
                    </div>
                </div>
            </div>
            <div class="tile-footer">
                <a href="#" class="btn btn-large pull-left" data-form-set="prescan_back: prescan_back" data-form-submit>
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
