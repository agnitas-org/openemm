<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--@elvariable id="SHOW_SUPERVISOR_PERMISSION_MANAGEMENT" type="java.lang.Boolean"--%>
<%--@elvariable id="SUPERVISOR_LIST" type="java.util.List"--%>
<%--@elvariable id="supervisorGrantLoginPermissionForm" type="com.agnitas.web.forms.SupervisorGrantLoginPermissionForm"--%>
<%--@elvariable id="LOCALE_DATE_PATTERN" type="java.lang.String"--%>

<c:if test="${SHOW_SUPERVISOR_PERMISSION_MANAGEMENT}">
	<agn:agnForm id="supervisorGrantLoginPermissionForm" action="grantSupervisorLoginPermission.do">
		<div class="tile">
			<div class="tile-header">
				<h2 class="headline"><bean:message key="settings.supervisor.manageLoginPermissions"/></h2>		
				
				<ul class="tile-header-actions">
                   	<li>
                  	     	<button type="button" class="btn btn-regular btn-primary" data-form-persist="isShowStatistic: true" data-form-submit-static>
                           	<i class="icon icon-plus"></i>
                           	<span class="text"><bean:message key="settings.supervisor.grantLoginPermission"/></span>
                       	</button>
                   	</li>
              		</ul>						
			</div>
			<div class="tile-content tile-content-forms">
				<div class="form-group">
					<div class="col-sm-4">
						<label class="control-label" for="departmentID"><bean:message key="supervisor.department"/></label>
					</div>
					<div class="col-sm-8">
						<select class="form-control js-select" name="departmentID" id="departmentID">
							<option value="0"><bean:message key="settings.supervisor.chooseDepartment"/></option>
							<option value="<%= com.agnitas.web.ComUserSelfServiceGrantSupervisorLoginPermissionAction.ALL_DEPARTMENTS_ID %>"><bean:message key="settings.supervisor.allDepartments"/></option>
							<c:forEach var="department" items="${DEPARTMENT_LIST}">
								<option value="${department.id}"><bean:message key="department.slugs.${department.slug}" /></option>
							</c:forEach>
						</select>
					</div>
				</div>
				<div class="form-group">
					<div class="col-sm-4">
						<label class="control-label"><bean:message key="settings.supervisor.loginPermission.validity"/></label>
					</div>
					<div class="col-sm-8">
						<ul class="list-group">
							<li class="list-group-item">
                                <div class="form-vertical">
                                    <div class="row">
                                        <div class="col-sm-4">
                                            <label class="radio-inline">
                                                <agn:agnRadio styleId="limited" property="limit" value="LIMITED" />
                                                <bean:message key="settings.supervisor.loginPermission.grantedUntilIncluding"/>
                                            </label>
                                        </div>
                                        <div class="col-sm-8">
                                            <div class="control">
                                                <div class="input-group">
                                                    <div class="input-group-controls">
                                                        <input type="text"
                                                            value="${supervisorGrantLoginPermissionForm.expireDateLocalized}"
                                                            class="form-control datepicker-input js-datepicker"
                                                            name="expireDateLocalized"
                                                            id="expireDateLocalized"
                                                                data-datepicker-options="format: '${fn:toLowerCase(LOCALE_DATE_PATTERN)}', formatSubmit: '${fn:toLowerCase(LOCALE_DATE_PATTERN)}'"/>
                                                    </div>
                                                    <div class="input-group-btn">
                                                        <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                                            <i class="icon icon-calendar-o"></i>
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
							</li>
							<li class="list-group-item">
								<label class="radio-inline">
									<agn:agnRadio styleId="limited" property="limit" value="UNLIMITED" />
									<bean:message key="settings.supervisor.loginPermission.grantedUnlimited"/>
	                           	</label>
							</li>							
						</ul>
					</div>
				</div>
			</div>
			<div class="tile-content">
           		<div class="table-wrapper table-overflow-visible">
					<display:table 
						class="table table-bordered table-striped table-hover js-table"
                      		id="permissionData"
                      		name="ACTIVE_LOGIN_PERMISSIONS"
                      		sort="list"
                      		excludedParams="*">
                      		
                      		<display:column headerClass="head_action" class="action" titleKey="CreationDate"><fmt:formatDate value="${permissionData.granted}" pattern="${adminDateTimeFormatWithSeconds}" timeZone="${adminTimeZone}" /></display:column>
                      		<display:column headerClass="head_action" class="action" titleKey="date.expiry">
                      			<c:choose>
                      				<c:when test="${not empty permissionData.expireDate}">
	                      				<fmt:formatDate value="${permissionData.expireDate}" pattern="${adminDateTimeFormatWithSeconds}" timeZone="${adminTimeZone}" />
                      				</c:when>
                      				<c:otherwise>
                      					<bean:message key="settings.supervisor.loginPermission.grantedUnlimited" />
                      				</c:otherwise>
                      			</c:choose>
                      		</display:column>
                      		
                      		<c:choose>
                      			<c:when test="${permissionData.allDepartmentsPermission}">
	                       			<display:column headerClass="head_action" class="action" titleKey="supervisor.department"><bean:message key="settings.supervisor.allDepartments"/></display:column>
                      			</c:when>
                      			<c:otherwise>
	                       			<display:column headerClass="head_action" class="action" titleKey="supervisor.department"><bean:message key="department.slugs.${permissionData.departmentSlugOrNull}" /></display:column>
	                       		</c:otherwise>
                      		</c:choose>
                      		
                      		<c:url value="revokeSupervisorLoginPermission.do" var="REVOCATION_URL">
                      			<c:param name="permissionID" value="${permissionData.permissionID}" />
                      		</c:url>
                      		
						<display:column headerClass="table-actions" class="action" titleKey="settings.supervisor.revokeLoginPermission">
							<agn:agnLink class="btn btn-regular btn-alert js-row-delete" href="${REVOCATION_URL}"><i class="icon icon-trash-o"></i></agn:agnLink>
						</display:column>                       	
					</display:table>
				</div>
			</div>
		</div>
	</agn:agnForm>
</c:if>
