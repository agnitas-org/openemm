<%@ page import="com.agnitas.web.ImportProfileColumnsAction" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.beans.ColumnMapping" %>
<%@ page import="org.agnitas.web.ProfileImportAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_BULK_REMOVE" value="<%= ImportProfileColumnsAction.ACTION_BULK_REMOVE %>"/>
<c:set var="ACTION_SAVE" value="<%= ImportProfileColumnsAction.ACTION_SAVE %>"/>
<c:set var="ACTION_SAVE_AND_START" value="<%= ImportProfileColumnsAction.ACTION_SAVE_AND_START %>"/>
<c:set var="ACTION_UPLOAD" value="<%= ImportProfileColumnsAction.ACTION_UPLOAD %>"/>

<agn:agnForm action="/importprofile_columns" id="importProfileColumnsForm" enctype="multipart/form-data" data-form="resource"
             data-controller="importprofile-fields"
             data-initializer="importprofile-fields"   
             data-validator="importprofile-fields/form">
	<html:hidden property="profileId"/>
	<html:hidden property="action"/>

    <script type="application/json" id="config:importprofile-fields-validator">
        {
            "columns": ${emm:toJson(importProfileColumnsForm.profileFields)}
        }
    </script>    
    
    <script type="application/json" id="config:importprofile-fields">
        {
            "ACTION_SAVE": "${ACTION_SAVE}",
            "ACTION_SAVE_AND_START": "${ACTION_SAVE_AND_START}",
            "columns" : ${emm:toJson(importProfileColumnsForm.profileFields)}
        }
    </script>    
    
	<%--@elvariable id="importProfileColumnsForm" type="com.agnitas.web.forms.ImportProfileColumnsForm"--%>
	<c:forEach var="columnsDefaults" items="${importProfileColumnsForm.dbColumnsDefaults}">
		<input type="hidden" id="default.${columnsDefaults.key}" value="${columnsDefaults.value}"/>
	</c:forEach>

	<input type="hidden" name="save" value=""/>
	<input type="hidden" id="upload_file" name="upload_file" value=""/>

	<div class="tile">
		<div class="tile-header">
			<h2 class="headline">
				<bean:message key="import.ManageColumns" />
			</h2>

		</div>
		<div class="tile-content">
			<div class="tile-content-forms form-vertical">

				<div class="form-group">
					<label class="control-label">
						<bean:message key="import.csv.file"/>*
					</label>
					<c:set var="currentFileName" value="${importProfileColumnsForm.currentFileName}" scope="page" />
					<c:set var="hasFile" value="${importProfileColumnsForm.hasFile}" scope="page" />

					<c:choose>
						<c:when test="${hasFile == 'true'}">
							<input type="hidden" id="remove_file" name="remove_file" value=""/>

							<table class="table table-bordered">
								<tbody>
									<tr>
										<td><bean:message key="import.current.csv.file" />: ${currentFileName}</td>
										<td class="table-actions">
											<button type="button" class="btn btn-regular btn-alert" data-tooltip="<bean:message key="button.Delete"/>" data-form-persist="remove_file: 'remove_file'" data-form-submit>
												<i class="icon icon-trash-o"></i>
											</button>
										</td>
									</tr>
								</tbody>
							</table>
						</c:when>
						<c:otherwise>
							<div class="input-group">
								<div class="input-group-controls">
									<html:file property="csvFile" styleId="csvFile" styleClass="form-control" />
								</div>
								<div class="input-group-btn">
									<button type="button" class="btn btn-regular btn-primary" data-form-persist="upload_file: 'upload_file', action: ${ACTION_UPLOAD}" data-form-submit>
										<i class="icon icon-cloud-upload"></i>
										<span class="text">
											<bean:message key="button.Upload"/>
										</span>
									</button>
								</div>
							</div>
						</c:otherwise>
					</c:choose>

				</div>

				<c:if test="${importProfileColumnsForm.mappingNumber > 0}">
					<c:if test="${not isReadonly}">
						<div class="tile-header table-controls" style="margin-bottom: 15px">
							<div class="table-control pull-left">
								<div class="well well-info block">
									<bean:message key="export.CsvMappingMsg"/>
								</div>
							</div>
							<ul class="tile-header-actions">
								<li class="dropdown">
									<a href="#" class="dropdown-toggle" data-toggle="dropdown">
										<i class="icon icon-pencil"></i>
										<span class="text"><bean:message key="bulkAction"/></span>
										<i class="icon icon-caret-down"></i>
									</a>
									<ul class="dropdown-menu">
										<li>
											<a href="#" data-form-persist="action: ${ACTION_BULK_REMOVE}" data-form-submit>
												<bean:message key="bulkAction.delete.csvColumn"/>
											</a>
										</li>
									</ul>
								</li>
							</ul>
						</div>
					</c:if>

					<div>
						<table id="columnMappings" class="table table-bordered table-striped table-form">
							<thead>
								<tr>
									<c:if test="${not isReadonly}">
										<th><input type="checkbox" data-form-bulk="columnIndex"/></th>
									</c:if>
									<th><bean:message key="import.CsvColumn"/></th>
									<th><bean:message key="import.DbColumn"/></th>
									<c:if test="${not isReadonly}">
										<th><bean:message key="import.profile.column.mandatory"/></th>
										<emm:ShowByPermission token="recipient.import.encrypted">
											<th><bean:message key="import.profile.column.encrypted"/></th>
										</emm:ShowByPermission>
										<th><bean:message key="settings.Default_Value"/></th>
										<th></th>
									</c:if>
								</tr>
							</thead>
							<tbody>
								<c:set var="column_index" value="0"/>
								<c:forEach var="mapping" items="${importProfileColumnsForm.profile.columnMapping}">
									<tr>
										<c:if test="${not isReadonly}">
											<td>
												<input type="checkbox"  name='columnIndex[${column_index}]'/>
											</td>
										</c:if>
										<td>${mapping.fileColumn}</td>
										<td>
											<c:choose>
												<c:when test="${isReadonly}">
													<div class="list-group-item disabled">${mapping.databaseColumn}</div>
												</c:when>
												<c:otherwise>
													<select name="dbColumn_${column_index}" id="id_dbColumn_${column_index}" class="form-control js-select" data-action="changeExistColName">
														<option value="<%= ColumnMapping.DO_NOT_IMPORT %>">
															<bean:message key="import.column.skip"/>
														</option>
														<c:forEach var="dbColumn" items="${importProfileColumnsForm.profileFields.keySet()}">
															<c:if test="${dbColumn == mapping.databaseColumn}">
																<option value="${dbColumn}" selected="selected">${dbColumn}</option>
															</c:if>
															<c:if test="${dbColumn != mapping.databaseColumn}">
																<option value="${dbColumn}">${dbColumn}</option>
															</c:if>
														</c:forEach>
													</select>
												</c:otherwise>
											</c:choose>
										</td>
										<c:if test="${not isReadonly}">
											<td>
												<label class="toggle">
													<c:if test="${mapping.mandatory}">
														<input name="mandatory_${column_index}" type="checkbox" checked="checked" />
													</c:if>
													<c:if test="${!mapping.mandatory}">
														<input name="mandatory_${column_index}" type="checkbox" />
													</c:if>
													<div class="toggle-control"></div>
												</label>
											</td>
											<emm:ShowByPermission token="recipient.import.encrypted">
												<td>
													<label class="toggle">
														<c:if test="${mapping.encrypted}">
															<input name="encrypted_${column_index}" type="checkbox" checked="checked" />
														</c:if>
														<c:if test="${!mapping.encrypted}">
															<input name="encrypted_${column_index}" type="checkbox" />
														</c:if>
														<div class="toggle-control"></div>
													</label>
												</td>
											</emm:ShowByPermission>
											<td>
												<input type="text" name="default_value_${column_index}" id="id_default_value_${column_index}" class="form-control" value="${mapping.defaultValue}">
											</td>
											<td class="table-actions">
												<button type="button" class="btn btn-regular btn-alert" data-tooltip="<bean:message key='button.Delete'/>" data-col_index="${column_index}" data-action="deleteMapping">
													<i class="icon icon-trash-o"></i>
												</button>
											</td>
										</c:if>
									</tr>

									<c:set var="column_index" value="${column_index + 1}"/>

								</c:forEach>

							</tbody>
						</table>
					</div>
				</c:if>
			</div>
		</div>
	</div>
</agn:agnForm>

<script id="new-mapping-row" type="text/x-mustache-template">
    <c:if test="${not isReadonly}">
        <tr>
            <td></td>
            <td></td>
            <td>
                <select name="newColumnMapping.databaseColumn" class="form-control js-select" data-action="changeNewColName">
                    <option value="<%= ColumnMapping.DO_NOT_IMPORT %>">
                        <bean:message key="import.column.skip"/>
                    </option>
                    <c:forEach var="dbColumn" items="${importProfileColumnsForm.profileFields.keySet()}">
                        <option value="${dbColumn}">${dbColumn}</option>
                    </c:forEach>
                </select>
            </td>
            <td>
                <label class="toggle">
                    <input name="newColumnMapping.mandatory" type="checkbox" />
                    <div class="toggle-control"></div>
                </label>
            </td>
            <emm:ShowByPermission token="recipient.import.encrypted">
                <td>
                    <label class="toggle">
                        <input name="newColumnMapping.encrypted" type="checkbox" />
                        <div class="toggle-control"></div>
                    </label>
                </td>
            </emm:ShowByPermission>
            <td>
                <input class="form-control" name="newColumnMapping.defaultValue" />
            </td>
            <td class="table-actions">
                <button type="button" class="btn btn-regular btn-secondary" data-action="addMapping">
                    <i class="icon icon-plus"></i>
                </button>
            </td>
        </tr>
    </c:if>
</script>

<script id="date-def-val-input" type="text/x-mustache-template">
    <div class="row">
        <div class="col-sm-2" style="text-align: center">
            <label class="checkbox-inline">
                <input type="checkbox" data-action="changeDateInput">
                <span class="text">
                    {{ if (withTime) { }}
                        <bean:message key="now"/>
                    {{ } else { }}
                        <bean:message key="calendar.today.button"/>
                    {{ } }}
                </span>
            </label>
        </div>
        <div class="col-sm-10" id="dateInput">
        {{ if (withTime) { }}
            <div data-type="datetime" class="js-datetime-field" data-field="datetime"
                 data-property="newColumnMapping.defaultValue"
                 data-field-options="value: {{- val}}, dateFormat: 'dd.mm.yyyy'"></div>
        {{ } else { }}
            <div data-type="date" class="input-group">
                <div class="input-group-controls">
                    <input type="text" name="newColumnMapping.defaultValue"
                           data-value="{{- val}}"
                           class="form-control datepicker-input js-datepicker"
                           data-datepicker-options="format: 'dd.mm.yyyy'"/>
                </div>
                <div class="input-group-btn">
                  <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                      <i class="icon icon-calendar-o"></i>
                  </button>
                </div>
            </div>
        {{ } }}
        </div>
        
        <div class="col-sm-10" id="daysInput" style="display: none;">
            <div class="col-sm-11">
                <input class="form-control" type="text" id="daysCount" value="+0">
            </div>
            <div class="col-sm-1">
                <div class="form-badge"><bean:message key="days"/></div>
            </div>
        </div>
    </div>
</script>

<script id="text-def-val-input" type="text/x-mustache-template">
    <div data-type="text"><input class="form-control" name="newColumnMapping.defaultValue"/></div>
</script>
