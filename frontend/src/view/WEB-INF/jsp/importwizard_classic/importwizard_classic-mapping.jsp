<%@page import="org.agnitas.util.importvalues.ImportMode"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
         import="org.agnitas.util.*,java.util.*, com.agnitas.web.ComImportWizardForm, com.agnitas.beans.ComAdmin"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<% ComImportWizardForm aForm = null;
	Map<String, CsvColInfo> aDbAllColumns = new HashMap<>();
    List<CsvColInfo> aCsvList = null;
    Map<String, CsvColInfo> columnMapping = null;
    int aMode = 0;
    if ((aForm = (ComImportWizardForm) session.getAttribute("importWizardForm")) != null) {
        aDbAllColumns = aForm.getDbAllColumns();
        aCsvList = aForm.getCsvAllColumns();
        columnMapping = aForm.getColumnMapping();
        aMode = aForm.getMode();
    } %>

<agn:agnForm action="/importwizard" enctype="multipart/form-data" data-form="resource">
    <html:hidden property="action"/>
    <input type="hidden" name="mapping_back" id="mapping_back" value="">

    <div class="col-md-10 col-md-push-1 col-lg-8 col-lg-push-2">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><i class="icon icon-file-o"></i> <bean:message key="import.Wizard"/></h2>
                <ul class="tile-header-actions">
                    <li class="">
                        <ul class="pagination">
                            <li>
                                <a href="#" data-form-set="mapping_back: mapping_back" data-form-submit>
                                    <i class="icon icon-angle-left"></i>
                                    <bean:message key="button.Back" />
                                </a>
                            </li>
                            <li class="disabled"><span>1</span></li>
                            <li class="disabled"><span>2</span></li>
                            <li class="active"><span>3</span></li>
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
            <div class="tile-notification tile-notification-info">
                <bean:message key="export.CsvMappingMsg"/>
                <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_3/Csvmapping.xml"></button>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="table-wrapper">
                    <table class="table table-bordered table-striped">
                        <thead>
                            <th><bean:message key="import.CsvColumn"/></th>
                            <th><bean:message key="import.DbColumn"/></th>
                        </thead>
                        <tbody>
                        <%
                            Map<String, String> linkedMap = new LinkedHashMap<String, String>();
                        %>
                        <emm:ShowColumnInfo id="agnTbl" table="<%= AgnUtils.getCompanyID(request) %>"
                                            hide="timestamp, change_date, creation_date, bounceload, datasource_id, lastopen_date, lastclick_date, lastsend_date, latest_datasource_id, sys_tracking_veto, cleaned_date, facebook_status, foursquare_status, google_status, twitter_status, xing_status">
                            <%
                                String colName = (String) pageContext.getAttribute("_agnTbl_column_name");
                                String aliasName = (String) pageContext.getAttribute("_agnTbl_shortname");

                                linkedMap.put(colName, aliasName);
                            %>
                        </emm:ShowColumnInfo>
                        <% int customerID_allowed = 0; %>
                        <emm:ShowByPermission token="import.customerid">
                            <% customerID_allowed = 1; %>
                        </emm:ShowByPermission>
                        <%
                            String aktCsvColname = "";
                            CsvColInfo aCsvColInfo = null;
                            for (int j = 0; j < aCsvList.size(); j++) {
                                aCsvColInfo = (CsvColInfo) aCsvList.get(j);%>
                        <tr>
                            <td class="import_classic_columns_csv">
                                <%= aCsvColInfo.getName() %>
                            </td>
                            <td class="import_classic_columns_db">
                                <select name='<%= "map_" + (j + 1) %>' class='form-control js-select'>
                                    <option value="NOOP"<%= columnMapping != null && columnMapping.get(aCsvColInfo.getName().trim()) == null ? " selected": "" %>><bean:message key="import.column.skip"/></option>
                                    <%
                                        Iterator<String> i = linkedMap.keySet().iterator();

                                        while (i.hasNext()) {
                                            String colName = i.next();
                                            String aliasName = linkedMap.get(colName);

                                            if(customerID_allowed == 1 && (!colName.equalsIgnoreCase("CUSTOMER_ID") || (colName.equalsIgnoreCase("CUSTOMER_ID") && aMode != ImportMode.ADD.getIntValue() && aMode != ImportMode.ADD_AND_UPDATE.getIntValue())) ) {
                                    %>
                                    <option value="<%= colName %>"
                                            <% if ((columnMapping != null && columnMapping.containsKey(aCsvColInfo.getName().trim())
                                                    && colName.trim().equalsIgnoreCase((columnMapping.get(aCsvColInfo.getName().trim())).getName().trim()))
                                                    || (columnMapping == null && colName.trim().equalsIgnoreCase(aCsvColInfo.getName().trim()))) { %>
                                            selected
                                            <% } %>><%= aliasName %>
                                    </option>
                                    <%
                                    } else if(!colName.equalsIgnoreCase("CUSTOMER_ID")) { %>
                                    <option value="<%= colName %>"
                                            <%if ((columnMapping != null && columnMapping.containsKey(aCsvColInfo.getName().trim())
                                                    && colName.trim().equalsIgnoreCase((columnMapping.get(aCsvColInfo.getName().trim())).getName().trim()))
                                                    || (columnMapping == null && colName.trim().equalsIgnoreCase(aCsvColInfo.getName().trim()))) { %>
                                            selected
                                            <% } %>><%= aliasName %>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                            </td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="tile-footer">
                <a href="#" class="btn btn-large pull-left" data-form-set="mapping_back: mapping_back" data-form-submit>
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


