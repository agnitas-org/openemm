<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="profileForm" type="com.agnitas.emm.core.profilefields.form.ProfileFieldForm"--%>
<%--@elvariable id="isNewField" type="java.lang.Boolean"--%>

<mvc:form servletRelativeAction="/profiledb/save.action" data-form="resource" id="profileFieldForm" method="POST"
		  data-controller="profile-field-view" modelAttribute="profileForm">

	<script type="application/json" data-initializer="profile-field-view">
		{
			"isHistorizationFeatureEnabled": ${HISTORY_FEATURE_ENABLED},
			"isHistorizationEnabled": "${profileForm.includeInHistory}",
			"dependentWorkflowName": "${emm:escapeJs(profileForm.dependentWorkflowName)}",
			"translations": {
				"warning.profilefield.inuse": "<bean:message key="warning.profilefield.inuse" arg0="%s"/>"
			}
		}
	</script>

	<div class="tile">
		<div class="tile-header">
			<h2 class="headline"><bean:message key="settings.EditProfileDB_Field"/></h2>
		</div>
		<div class="tile-content tile-content-forms">
			<div class="form-group" data-field="required">
				<div class="col-sm-4">
					<label class="control-label" for="fieldShortname"><bean:message key="settings.FieldName"/></label>
				</div>
				<div class="col-sm-8">
					<mvc:text path="shortname" id="fieldShortname" cssClass="form-control" maxlength="99" size="32" data-field-required=""/>
				</div>
			</div>
			<div class="form-group has-info has-feedback">
				<div class="col-sm-4">
					<label class="control-label" for="fieldDescription"><bean:message key="Description"/></label>
				</div>
				<div class="col-sm-8">
					<mvc:text path="description" id="fieldDescription" cssClass="form-control"/>
					<span class="icon icon-state-info form-control-feedback"></span>
					<div class="form-control-feedback-message"><bean:message key="profiledb.description.hint"/></div>
				</div>
			</div>
		</div>
	</div>

	<div class="tile">
		<div class="tile-header">
			<h2 class="headline"><bean:message key="default.settings"/></h2>
		</div>
		<div class="tile-content tile-content-forms">

			<div class="form-group" data-field="required">
				<div class="col-sm-4">
					<label class="control-label"><bean:message key="settings.FieldNameDB"/></label>
				</div>
				<div class="col-sm-8">
					<c:choose>
						<c:when test="${isNewField}">
							<mvc:text path="fieldname" cssClass="form-control" size="32" data-field-required=""/>
						</c:when>
						<c:otherwise>
							<mvc:hidden path="fieldname"/>
							<div class="form-control-static">${profileForm.fieldname}</div>
						</c:otherwise>
					</c:choose>
				</div>
			</div>

			<div data-field="toggle-vis">
				<div class="form-group">
					<div class="col-sm-4">
						<label class="control-label" for="fieldType"><bean:message key="default.Type"/></label>
					</div>
					<div class="col-sm-8">
						<c:if test="${isNewField}">
							<mvc:select path="fieldType" size="1" id="fieldType" cssClass="form-control js-select" data-field-vis="">
								<mvc:option value="INTEGER" data-field-vis-hide="#fieldLengthDiv" data-field-vis-show="#interestDiv">
									<bean:message key="settings.fieldType.INTEGER"/>
								</mvc:option>
								<mvc:option value="FLOAT" data-field-vis-hide="#fieldLengthDiv" data-field-vis-show="#interestDiv">
									<bean:message key="settings.fieldType.Float"/>
								</mvc:option>
								<mvc:option value="VARCHAR" data-field-vis-show="#fieldLengthDiv" data-field-vis-hide="#interestDiv">
									<bean:message key="settings.fieldType.VARCHAR"/>
								</mvc:option>
								<mvc:option value="DATE" data-field-vis-hide="#fieldLengthDiv, #interestDiv">
									<bean:message key="settings.fieldType.DATE"/>
								</mvc:option>
								<mvc:option value="DATETIME" data-field-vis-hide="#fieldLengthDiv, #interestDiv">
									<bean:message key="settings.fieldType.DATETIME"/>
								</mvc:option>
							</mvc:select>
						</c:if>
						<c:if test="${not isNewField}">
							<mvc:hidden path="fieldType"/>
							<div class="form-badge">
								<bean:message key="settings.fieldType.${profileForm.fieldType}"/>
							</div>
						</c:if>
					</div>
				</div>

				<c:if test="${isNewField}">
					<div id="fieldLengthDiv" class="form-group" data-field="validator">
						<div class="col-sm-4">
							<label class="control-label" for="fieldLength"><bean:message key="settings.Length"/></label>
						</div>
						<div class="col-sm-8">
							<mvc:text path="fieldLength" id="fieldLength" cssClass="form-control"
										data-field-validator="number"
										data-validator-options="min: 1, max: 4000, required: true, strict: true"/>
						</div>
					</div>
				</c:if>

				<c:if test="${not isNewField}">
					<c:if test="${profileForm.fieldType == 'VARCHAR'}">
						<div class="form-group">
							<div class="col-sm-4">
								<label class="control-label"><bean:message key="settings.Length"/></label>
							</div>
							<div class="col-sm-8">
								<div class="form-control-static">
									<mvc:hidden path="fieldLength"/>
									<c:out value="${profileForm.fieldLength}"/>
								</div>
							</div>
						</div>
					</c:if>
				</c:if>

				<div class="form-group">
					<div class="col-sm-4">
						<label class="control-label" for="fieldDefault"><bean:message key="settings.Default_Value"/></label>
					</div>
					<div class="col-sm-8">
						<mvc:text path="fieldDefault" id="fieldDefault" cssClass="form-control" size="32"  maxlength="199"/>
					</div>
				</div>

				<div class="form-group">
					<div class="col-sm-4">
						<label class="control-label" for="fieldNull"><bean:message key="settings.NullAllowed"/></label>
					</div>
					<div class="col-sm-8">
						<c:choose>
							<c:when test="${isNewField}">
								<label class="toggle">
									<mvc:checkbox path="fieldNull" id="fieldNull"/>
									<div class="toggle-control"></div>
								</label>
							</c:when>
							<c:otherwise>
								<mvc:hidden path="fieldNull"/>
								<div class="form-badge">
									<c:choose>
										<c:when test="${profileForm.fieldNull}">
											<bean:message key="default.Yes"/>
										</c:when>
										<c:otherwise>
											<bean:message key="No"/>
										</c:otherwise>
									</c:choose>
								</div>
							</c:otherwise>
						</c:choose>
					</div>
				</div>

				<emm:ShowByPermission token="profileField.visible">
					<div class="form-group">
						<div class="col-sm-4">
							<label class="control-label" for="fieldVisible"><bean:message key="FieldVisible"/></label>
						</div>
						<div class="col-sm-8">
							<label class="toggle">
								<mvc:checkbox path="fieldVisible" id="fieldVisible"/>
								<div class="toggle-control"></div>
							</label>
						</div>
					</div>
				</emm:ShowByPermission>

				<div class="form-group">
					<div class="col-sm-4">
						<label class="control-label" for="line"><bean:message key="line_after"/></label>
					</div>
					<div class="col-sm-8">
						<label class="toggle">
							<mvc:checkbox path="line" id="line"/>
							<div class="toggle-control"></div>
						</label>
					</div>
				</div>
				<c:if test="${profileForm.fieldType == 'INTEGER' || profileForm.fieldType == 'DOUBLE'}">
					<div id="interestDiv" class="form-group">
						<div class="col-sm-4">
							<label class="control-label" for="interest"><bean:message key="FieldIsInterest"/></label>
						</div>
						<div class="col-sm-8">
							<label class="toggle">
								<mvc:checkbox path="interest" id="interest"/>
								<div class="toggle-control"></div>
							</label>
						</div>
					</div>
				</c:if>
			</div>

			<c:if test="${HISTORY_FEATURE_ENABLED}">
				<div class="form-group">
					<div class="col-sm-4">
						<label class="control-label" for="includeInHistory">
							<bean:message key="profileHistory.includeField"/>
							<button type="button" data-help="help_${helplanguage}/recipient/profileField/HistorisationAdd.xml" class="icon icon-help"></button>
						</label>
					</div>
					<div class="col-sm-8">
						<label class="toggle">
							<mvc:checkbox path="includeInHistory" id="includeInHistory" data-action="toggleHistorization"/>
							<div class="toggle-control"></div>
						</label>
					</div>
				</div>
			</c:if>

			<div class="form-group">
				<div class="col-sm-4">
					<label class="control-label" for="fieldSort"><bean:message key="FieldSort"/>:</label>
				</div>
				<div class="col-sm-8">
					<select class="form-control js-select" name="fieldSort" id="fieldSort">
						<option value="1000"<c:if test="${profileForm.fieldSort == 1000}"> selected</c:if>>
							<bean:message key="noSort"/></option>
						<option value="1"<c:if test="${profileForm.fieldSort == 1}"> selected</c:if>><bean:message key="first"/></option>

						<c:forEach var="field" items="${fieldsWithIndividualSortOrder}">
							<option value='${field.sort + 1}' <c:if
									test="${profileForm.fieldSort == field.sort + 1}"> selected</c:if>>
								<bean:message key="after"/> ${field.shortname}</option>
						</c:forEach>
					</select>
				</div>
			</div>

			<c:if test="${not empty creationDate}">
				<div class="form-group">
					<div class="col-sm-4">
						<label class="control-label" for="creationDate"><bean:message
								key="default.creationDate"/></label>
					</div>
					<div class="col-sm-8">
						<input type="text" class="form-control" readonly value="${creationDate}"/>

					</div>
				</div>
			</c:if>

			<c:if test="${not empty changeDate}">
				<div class="form-group">
					<div class="col-sm-4">
						<label class="control-label" for="changeDate"><bean:message key="default.changeDate"/></label>
					</div>
					<div class="col-sm-8">
						<input type="text" class="form-control" readonly value="${changeDate}"/>
					</div>
				</div>
			</c:if>

			<div data-field="toggle-vis">
				<div class="hidden" data-field-vis-default="" data-field-vis-hide="#allowedValuesFormGroup"></div>

				<div class="form-group">
					<div class="col-sm-4">
						<label class="control-label" for="useAllowedValues"><bean:message
								key="settings.FieldFixedValue"/></label>
					</div>
					<div class="col-sm-8">
						<label class="toggle">
							<mvc:checkbox path="useAllowedValues" id="useAllowedValues" data-field-vis=""
										  data-field-vis-show="#allowedValuesFormGroup"/>
							<div class="toggle-control"></div>
						</label>
					</div>
				</div>

				<div class="form-group" id="allowedValuesFormGroup">
					<div class="col-sm-offset-4 col-sm-8">
						<div class="table-responsive" data-controller="allowed-profile-field-values">
							<table class="table table-bordered table-striped">
								<thead>
								<tr>
									<th><bean:message key="Value"/></th>
									<th class="squeeze-column"></th>
								</tr>
								</thead>
								<tbody>
								<c:set var="allowedValues" value="${profileForm.allowedValues}"/>
								<c:if test="${not empty allowedValues}">
									<c:set var="allowedValuesValidationResults"
										   value="${profileForm.allowedValuesValidationResults}"/>
									<c:forEach var="allowedValue" items="${allowedValues}" varStatus="status">
										<c:if test="${not empty allowedValue}">
											<c:set var="isInvalid"
												   value="${not empty allowedValuesValidationResults and not allowedValuesValidationResults[status.index]}"/>
											<tr class="js-allowed-value">
												<td class="${isInvalid ? 'has-alert has-feedback js-form-error' : ''}">
													<input type="text" name="allowedValues" class="form-control" value="${fn:escapeXml(allowedValue)}">
													<c:if test="${isInvalid}">
														<div class="form-control-feedback-message js-form-error-msg">
															<bean:message key="default.Invalid"/>
														</div>
													</c:if>
												</td>
												<td class="table-actions">
													<button type="button" class="btn btn-regular btn-alert"
															data-action="deleteValue">
														<i class="icon icon-trash-o"></i>
													</button>
												</td>
											</tr>
										</c:if>
									</c:forEach>
								</c:if>
								<tr class="js-allowed-value">
									<td>
										<input type="text" name="allowedValues" class="form-control"/>
									</td>
									<td class="table-actions">
										<button type="button" class="btn btn-regular btn-primary"
												data-action="addNewValue">
											<i class="icon icon-plus"></i>
										</button>
									</td>
								</tr>
								</tbody>
							</table>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</mvc:form>

<script id="allowed-value-new-row" type="text/x-mustache-template">
	<tr class="js-allowed-value">
		<td>
			<input type="text" class="form-control" name="allowedValues"/>
		</td>

		<td class="table-actions">
			{{ if (isLastRow) { }}
			<button type="button" class="btn btn-regular btn-primary" data-action="addNewValue">
				<i class="icon icon-plus"></i>
			</button>
			{{ } else { }}
			<button type="button" class="btn btn-regular btn-alert" data-action="deleteValue">
				<i class="icon icon-trash-o"></i>
			</button>
			{{ } }}
		</td>
	</tr>
</script>
