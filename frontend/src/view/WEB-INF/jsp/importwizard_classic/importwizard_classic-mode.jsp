<%@page import="org.agnitas.util.importvalues.ImportMode"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
         import="org.agnitas.util.*, com.agnitas.web.ComImportWizardForm,org.agnitas.beans.ImportStatus,com.agnitas.beans.Admin"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="customerID_allowed" value="false" scope ="page"/>
<emm:ShowByPermission token="import.customerid">
    <c:set var="customerID_allowed" value="true" scope ="page"/>
</emm:ShowByPermission>

<agn:agnForm action="/importwizard" data-form="resource">
    <html:hidden property="action"/>
    <input type="hidden" name="mode_back" id="mode_back" value="">

    <div class="col-md-10 col-md-push-1 col-lg-8 col-lg-push-2">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><i class="icon icon-file-o"></i> <bean:message key="import.Wizard"/></h2>
                <ul class="tile-header-actions">
                    <li class="">
                        <ul class="pagination">
                            <li>
                                <a href="#" data-form-set="mode_back: mode_back" data-form-submit>
                                    <i class="icon icon-angle-left"></i>
                                    <bean:message key="button.Back" />
                                </a>
                            </li>
                            <li class="disabled"><span>1</span></li>
                            <li class="active"><span>2</span></li>
                            <li class="disabled"><span>3</span></li>
                            <li class="disabled"><span>4</span></li>
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
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label">
                            <bean:message key="Mode"/>
                            <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_2/Mode.xml"></button>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:select property="mode" size="1" styleClass="form-control js-select">
                            <emm:ShowByPermission token="import.mode.add">
                                <html:option value="<%=Integer.toString(ImportMode.ADD.getIntValue())%>"><bean:message
                                        key="import.mode.add"/></html:option>
                            </emm:ShowByPermission>
                            <emm:ShowByPermission token="import.mode.add_update">
                                <html:option value="<%=Integer.toString(ImportMode.ADD_AND_UPDATE.getIntValue())%>"><bean:message
                                        key="import.mode.add_update"/></html:option>
                            </emm:ShowByPermission>
                            <emm:ShowByPermission token="import.mode.only_update">
                                <html:option value="<%=Integer.toString(ImportMode.UPDATE.getIntValue())%>"><bean:message
                                        key="import.mode.only_update"/></html:option>
                            </emm:ShowByPermission>
                            <emm:ShowByPermission token="import.mode.bounce">
                                <html:option value="<%=Integer.toString(ImportMode.MARK_BOUNCED.getIntValue())%>"><bean:message
                                        key="import.mode.bounce"/></html:option>
                            </emm:ShowByPermission>
                            <emm:ShowByPermission token="import.mode.blacklist">
                                <html:option value="<%=Integer.toString(ImportMode.TO_BLACKLIST.getIntValue())%>"><bean:message
                                        key="import.mode.blacklist"/></html:option>
                            </emm:ShowByPermission>
                            <emm:ShowByPermission token="import.mode.blacklist_exclusive">
                                <html:option value="<%=Integer.toString(ImportMode.BLACKLIST_EXCLUSIVE.getIntValue())%>"><bean:message
                                        key="import.mode.blacklist_exclusive"/></html:option>
                            </emm:ShowByPermission>
                            <emm:ShowByPermission token="import.mode.remove_status">
                                <html:option value="<%=Integer.toString(ImportMode.MARK_SUSPENDED.getIntValue())%>"><bean:message
                                        key="import.mode.remove_status"/></html:option>
                            </emm:ShowByPermission>
                            <emm:ShowByPermission token="import.mode.reactivateSuspended">
                                <html:option value="<%=Integer.toString(ImportMode.REACTIVATE_SUSPENDED.getIntValue())%>"><bean:message
                                        key="import.mode.reactivateSuspended"/></html:option>
                            </emm:ShowByPermission>
                            <emm:ShowByPermission token="import.mode.unsubscribe">
                                <html:option value="<%=Integer.toString(ImportMode.MARK_OPT_OUT.getIntValue())%>"><bean:message
                                        key="import.mode.unsubscribe"/></html:option>
                            </emm:ShowByPermission>
                        </html:select>
                    </div>
                </div>

 				<%@include file="/WEB-INF/jsp/importwizard_classic/importwizard_classic-mode_nullvalues.jspf" %>

                 <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label">
                            <bean:message key="import.keycolumn"/>
                            <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_2/KeyColumn.xml"></button>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:select property="status.keycolumn" size="1" styleClass="form-control js-select">
                            <emm:ShowColumnInfo id="agnTbl" table="<%=AgnUtils.getCompanyID(request)%>">
                                <c:if test="${customerID_allowed || fn:toLowerCase(_agnTbl_column_name) != 'customer_id'}">
                                    <html:option value='${_agnTbl_column_name}'>${_agnTbl_shortname}</html:option>
                                </c:if>
                            </emm:ShowColumnInfo>
                        </html:select>
                    </div>
                </div>

                <emm:ShowByPermission token="import.mode.doublechecking">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label">
                                <bean:message key="import.doublechecking"/>
                                <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_2/Doublechecking.xml"></button>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <html:select property="status.doubleCheck" size="1" styleClass="form-control js-select">
                                <html:option value="<%=Integer.toString(ImportStatus.DOUBLECHECK_FULL)%>"><bean:message
                                        key="default.Yes"/></html:option>
                                <html:option value="<%=Integer.toString(ImportStatus.DOUBLECHECK_NONE)%>"><bean:message
                                        key="default.No"/></html:option>
                            </html:select>
                        </div>
                    </div>
                </emm:ShowByPermission>

                <emm:ShowByPermission token="import.mode.duplicates">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label">
                                <bean:message key="import.profile.updateAllDuplicates"/>
                                <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_2/Doublechecking.xml"></button>
                            </label>
                        </div>
                        <div class="col-sm-8">
			                <html:hidden property="__STRUTS_CHECKBOX_updateAllDuplicates" value="false"/>
			                <label data-form-change class="toggle">
			                    <html:checkbox styleId="import_duplicates" property="updateAllDuplicates" />
			                    <div class="toggle-control"></div>
			                </label>
                        </div>
                    </div>
                </emm:ShowByPermission>
            </div>
            <div class="tile-footer">
                <a href="#" class="btn btn-large pull-left" data-form-set="mode_back: mode_back" data-form-submit>
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

<% out.flush(); %>
