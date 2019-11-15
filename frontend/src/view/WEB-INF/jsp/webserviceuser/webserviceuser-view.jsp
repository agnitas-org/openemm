<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do"%>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="logic"   uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="html"    uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="c"		uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="webserviceUserForm" type="com.agnitas.emm.core.wsmanager.form.WebserviceUserForm"--%>

<mvc:form servletRelativeAction="/administration/wsmanager/user/update.action"
          data-form-focus="password"
          id="wsuser-edit-form"
          modelAttribute="webserviceUserForm"
          data-form="resource">

    <mvc:hidden path="userName"/>
    <mvc:hidden path="companyId"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="webserviceuser.edit" />
            </h2>
        </div>

        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="userNameTitle" class="control-label">
                        <mvc:message code="logon.username" />
                    </label>
                </div>
                <div class="col-sm-8">
                    <input type="text" value="${webserviceUserForm.userName}" disabled="disabled" id="userNameTitle" class="form-control">
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="email">
                        <mvc:message code="settings.Admin.email"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="email" id="email" cssClass="form-control" size="52" maxlength="99"/>
                </div>
            </div>
            <logic:messagesPresent property="email">
                <div class="form-group">
                    <div class="col-sm-4">&nbsp;</div>
                    <div class="col-sm-8">
                        <html:messages id="msg" message="false" property="email">
                            <i class="icon icon-exclamation-triangle"></i>&nbsp;${msg}<br />
                        </html:messages>
                    </div>
                </div>
            </logic:messagesPresent>

            <div class="form-group">
                <div class="col-sm-4">
                    <label for="contactInfo" class="control-label">
                        <mvc:message code="webserviceuser.contact_info" />
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:textarea path="contactInfo" cssClass="form-control" id="contactInfo"/>
                </div>
            </div>

            <div data-field="password">
                <%--Password--%>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="password" class="control-label" >
                            <mvc:message code="password.new"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <mvc:password path="password" id="password" cssClass="form-control js-password-strength" size="52" maxlength="99"/>
                            </div>
                            <div class="input-group-addon">
                                <span class="addon js-password-strength-indicator hidden">
                                    <i class="icon icon-check"></i>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <%--Password repeat--%>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="passwordRepeat" class="control-label" ><mvc:message code="password.repeat"/></label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <input type="password" id="passwordRepeat" class="form-control js-password-match" size="52" maxlength="99"/>
                            </div>
                            <div class="input-group-addon">
                                <span class="addon js-password-match-indicator hidden">
                                    <i class="icon icon-check"></i>
                                 </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <div class="checkbox">
                        <label>
                            <mvc:checkbox path="active" id="active"/>
                            <mvc:message code="default.status.active" />
                        </label>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <c:if test="${PERMISSIONS_ENABLED}">
	    <div class="tile">
	        <div class="tile-header">
	            <h2 class="headline">
	                <mvc:message code="webserviceuser.permissionGroupss" />
	            </h2>
	        </div>
	    	
	    	<div class="tile-content tile-content-forms checkboxes-content">
				<ul class="list-group">
		    		<c:forEach items="${PERMISSION_GROUPS.allPermissionGroups}" var="group">
						<li class="list-group-item">
							<label class="checkbox-inline">
								<mvc:checkbox path="permissionGroup[${group.id}]" value="true" />
								${group.name}
							</label>
						</li>
					</c:forEach>
				</ul>
	    	</div>
	    </div>
    
    
    
	    <div class="tile">
	        <div class="tile-header">
	            <h2 class="headline">
	                <mvc:message code="webserviceuser.permissions" />
	            </h2>
	        </div>
	    	
	    	<div class="tile-content tile-content-forms">
	    		<c:forEach items="${PERMISSIONS.categories}" var="category">
	    		
	    			<c:if test="${not empty category}">
		 				<div class="tile">
							<div class="tile-header">
		                        <a href="#" class="headline" data-toggle-tile="#userrights_category_${index.index}">
		                            <i class="icon tile-toggle icon-angle-up"></i>
									<bean:message key="webservice.permissionCategory.${category}"/>
								</a>
							</div>
							
							<div class="tile-content tile-content-forms checkboxes-content">
								<ul class="list-group">
									<c:forEach items="${PERMISSIONS.getPermissionsForCategory(category)}" var="permission">
										<li class="list-group-item">
											<label class="checkbox-inline">
												<mvc:checkbox path="endpointPermission[${permission.endpointName}]" value="true" />
												<bean:message key="webservice.permission.${permission.endpointName}" arg0="${permission.endpointName}" />
											</label>
										</li>
									</c:forEach>				
								</ul>
							</div>
						</div>
					</c:if>
				</c:forEach>
	    	</div>
	    </div>
	</c:if>
</mvc:form>
