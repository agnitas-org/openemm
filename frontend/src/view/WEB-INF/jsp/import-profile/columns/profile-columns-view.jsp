<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.import_profile.form.ImportProfileColumnsForm"--%>
<%--@elvariable id="profileFields" type="java.util.Map<java.lang.String, com.agnitas.beans.ProfileField>"--%>
<%--@elvariable id="columnMappings" type="java.util.List<org.agnitas.beans.ColumnMapping>"--%>
<%--@elvariable id="isReadonly" type="java.lang.Boolean"--%>

<c:set var="profileFieldsAsJson" value="${emm:toJson(profileFields)}"/>

<mvc:form id="importProfileColumnsForm" servletRelativeAction="/import-profile/columns/save.action" modelAttribute="form"
		  enctype="multipart/form-data" data-form="resource" data-controller="import-profile-mappings"
		  data-initializer="import-profile-mappings" data-validator="import-profile-mappings/form" data-action="save-mappings">

	<mvc:hidden path="profileId" />

    <script type="application/json" id="config:import-profile-mappings-validator">
        {
            "columns": ${profileFieldsAsJson}
        }
    </script>    
    
    <script type="application/json" id="config:import-profile-mappings">
        {
            "columns" : ${profileFieldsAsJson},
            "columnMappings": ${emm:toJson(columnMappings)},
            "urls": {
		    	"UPLOAD": "<c:url value="/import-profile/columns/upload.action"/>"
		  	}
        }
    </script>

	<div class="tile">
		<div class="tile-header">
			<h2 class="headline">
				<mvc:message code="import.ManageColumns" />
			</h2>

		</div>
		<div class="tile-content">
			<div class="tile-content-forms form-vertical">
				<div class="form-group">
					<label class="control-label">
						<mvc:message code="import.csv.file"/>*
					</label>

					<div id="upload-file-container" class="input-group">
						<div class="input-group-controls">
							<input id="uploadFile" type="file" name="uploadFile" class="form-control">
						</div>
						<div class="input-group-btn">

							<button type="button" class="btn btn-regular btn-primary" data-action="upload">
								<i class="icon icon-cloud-upload"></i>
								<span class="text">
									<mvc:message code="button.Upload"/>
								</span>
							</button>
						</div>
					</div>
				</div>

				<div id="content-container" class="${columnMappings.size() == 0 ? 'hidden' : ''}">
					<c:if test="${not isReadonly}">
						<div class="tile-header table-controls" style="margin-bottom: 15px">
							<div class="table-control pull-left">
								<div class="well well-info block">
									<mvc:message code="export.CsvMappingMsg"/>
								</div>
							</div>
							<ul class="tile-header-actions">
								<li class="dropdown">
									<a href="#" class="dropdown-toggle" data-toggle="dropdown">
										<i class="icon icon-pencil"></i>
										<span class="text"><mvc:message code="bulkAction"/></span>
										<i class="icon icon-caret-down"></i>
									</a>
									<ul class="dropdown-menu">
										<li>
											<a href="#" data-action="bulk-delete">
												<mvc:message code="bulkAction.delete.csvColumn"/>
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
								<th><mvc:message code="import.CsvColumn"/></th>
								<th><mvc:message code="import.DbColumn"/></th>
								<c:if test="${not isReadonly}">
									<th><mvc:message code="import.profile.column.mandatory"/></th>
									<c:if test="${isEncryptedImportAllowed}">
										<th><mvc:message code="import.profile.column.encrypted"/></th>
									</c:if>
									<th><mvc:message code="settings.Default_Value"/></th>
									<th></th>
								</c:if>
							</tr>
							</thead>
							<tbody>
								<%-- this block load by JS--%>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>
</mvc:form>

<%@ include file="fragments/import-column-mapping-row-templates.jspf" %>

<script id="uploaded-file-selected" type="text/x-mustache-template">
	<table id="uploaded-file-container" class="table table-bordered">
		<tbody>
		<tr>
			<td><mvc:message code="import.current.csv.file" />: {{= fileName}}</td>
			<td class="table-actions">
				<button type="button" class="btn btn-regular btn-alert" data-tooltip="<mvc:message code="button.Delete"/>" data-action="delete-file">
					<i class="icon icon-trash-o"></i>
				</button>
			</td>
		</tr>
		</tbody>
	</table>
</script>
