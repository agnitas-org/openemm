<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComMailingBaseAction"%>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ACTION_MAILING_IMPORT" value="<%= ComMailingBaseAction.ACTION_MAILING_IMPORT %>"/>
<c:set var="ACTION_IMPORT_TEMPLATES" value="<%= ComMailingBaseAction.ACTION_IMPORT_TEMPLATES %>"/>
<c:set var="importAction" value="${ACTION_MAILING_IMPORT}"/>
<c:if test="${not empty param.importFromTemplates and param.importFromTemplates}">
	<c:set var="importAction" value="${ACTION_IMPORT_TEMPLATES}"/>
</c:if>

<agn:agnForm action="/mailingbase" id="mailingImport" enctype="multipart/form-data" data-form="static">
	<html:hidden property="action"/>
	<html:hidden property="isGrid"/>
	<html:hidden property="isTemplate" value="true"/>

	<div class="tile">
		<div class="tile-header">
			<h2 class="headline">
				<bean:message key="template.import" />
			</h2>
		</div>
		<div class="tile-content">
			<div class="tile-content-forms form-vertical">
				<div class="form-group">
					<div class="input-group">
						<div class="input-group-controls">
							<html:file property="uploadFile" styleId="uploadFile" styleClass="form-control" />
						</div>
						<div class="input-group-btn">
							<button type="button" class="btn btn-regular btn-primary" data-form-persist="upload_file: 'upload_file', action: ${importAction}" data-form-submit>
								<i class="icon icon-cloud-upload"></i>
								<span class="text">
									<bean:message key="template.import"/>
								</span>
							</button>
						</div>
					</div>
                </div>
				<div class="col-sm-4">
					<html:hidden property="__STRUTS_CHECKBOX_importTemplateOverwrite" value="false" />
					<table>
						<tr>
							<td>
								<label data-form-change class="toggle">
									<html:checkbox styleId="import_duplicates" property="importTemplateOverwrite" />
									<div class="toggle-control"></div>
								</label>
								&nbsp;
							</td>
							<td>
								<label class="control-label">
									<bean:message key="import.template.overwrite" />
									<button type="button" class="icon icon-help" data-help="help_${helplanguage}/mailing/OverwriteTemplate.xml"></button>
								</label>
							</td>
						</tr>
					</table>
				</div>
			</div>
		</div>
	</div>
</agn:agnForm>
