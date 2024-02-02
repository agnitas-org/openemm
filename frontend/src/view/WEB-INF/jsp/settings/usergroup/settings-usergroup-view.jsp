<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.agnitas.emm.core.Permission" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%--@elvariable id="userGroupForm" type="com.agnitas.emm.core.usergroup.form.UserGroupForm"--%>
<%--@elvariable id="permissionChangeable" type="java.util.Set<java.lang.String>"--%>
<%--@elvariable id="permissionCategories" type="java.util.Set<com.agnitas.emm.core.admin.web.PermissionsOverviewData.PermissionCategoryEntry>"--%>

<c:set var="USERRIGHT_MESSAGEKEY_PREFIX" value="<%= Permission.USERRIGHT_MESSAGEKEY_PREFIX %>"/>

<c:set var="allowedUserGroupChange" value="false"/>
<emm:ShowByPermission token="role.change">
    <c:set var="allowedUserGroupChange" value="true"/>
</emm:ShowByPermission>

<mvc:form servletRelativeAction="/administration/usergroup/save.action"
          modelAttribute="userGroupForm"
          id="userGroupForm"
          data-form="resource"
          data-form-focus="shortname"
          data-controller="user|groups-permissions">

    <mvc:hidden path="id"/>
    <mvc:hidden path="companyId"/>

    <%@ include file="../admin/fragments/user-permissions-filter-tile.jspf" %>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="settings.usergroup.edit"/></h2>
        </div>

        <div class="tile-content tile-content-forms">
            <emm:ShowByPermission token="master.show">
                <div class="form-group">
            	 	<div class="col-sm-4">
                    	<label class="control-label" for="userGroupId"><mvc:message code="MailinglistID"/></label>
                	</div>
                	 <div class="col-sm-8">
                         <mvc:text path="id" id="userGroupId" size="52" maxlength="99" readonly="true" cssClass="form-control"/>
                	</div>
                </div>
                <div class="form-group">
                	<div class="col-sm-4">
                    	<label class="control-label" for="userGroupCopmanyId"><mvc:message code="settings.Company"/></label>
                	</div>
                	 <div class="col-sm-8">
                         <mvc:text path="companyId" id="userGroupCopmanyId" size="52" maxlength="99" readonly="true" cssClass="form-control"/>
                	</div>
                </div>
            </emm:ShowByPermission>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="userGroupShortname"><mvc:message code="default.Name"/>*</label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="shortname" id="userGroupShortname" size="52" maxlength="99" cssClass="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="userGroupDescription"><mvc:message code="Description"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="description" id="userGroupDescription" size="52" maxlength="99" cssClass="form-control"/>
                </div>
            </div>

			<div class="form-group">
				<div class="col-sm-4">
					<label class="control-label" for="groupIDs"><mvc:message code="settings.Usergroup" /></label>
				</div>
				<div class="col-sm-8">
					<mvc:select path="parentGroupIDs" id="parentGroupIDs" cssClass="form-control js-select" multiple="true">
						<c:forEach var="adminGroup" items="${availableAdminGroups}">
							<mvc:option value="${adminGroup.groupID}">${fn:escapeXml(adminGroup.shortname)}</mvc:option>
						</c:forEach>
					</mvc:select>
				</div>
			</div>
		</div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="settings.usergroup.rights.edit"/></h2>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text">
                            <mvc:message code="visibility"/>
                        </span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu" >
                        <li>
                            <a href="#" data-toggle-tile-all="expand">
                                <span class="text">
                                    <mvc:message code="settings.admin.expand_all"/>
                                </span>
                            </a>
                        </li>
                        <li>
                            <a href="#" data-toggle-tile-all="collapse">
                                <span class="text">
                                    <mvc:message code="settings.admin.collapse_all"/>
                                </span>
                            </a>
                        </li>
                    </ul>
                </li>
                <li class="dropdown ${allowedUserGroupChange} ? '' : 'hidden'">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-pencil"></i>
                        <span class="text">
                            <mvc:message code="ToggleOnOff"/>
                        </span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu" >
                        <li>
                            <a href="#" data-toggle-checkboxes-all="on">
                                <i class="icon icon-check-square-o"></i>
                                <span class="text">
                                    <mvc:message code="toggle.allOn"/>
                                </span>
                            </a>
                        </li>
                        <li>
                            <a href="#" data-toggle-checkboxes-all="off">
                                <i class="icon icon-square-o"></i>
                                <span class="text">
                                    <mvc:message code="toggle.allOff"/>
                                </span>
                            </a>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content tile-content-forms">
            <c:forEach items="${permissionCategories}" var="category" varStatus="categoryIndex">
                <div class="tile permission-categories-tile">
                    <div class="tile-header">
                        <a href="#" class="headline" data-toggle-tile="#permission_category_${categoryIndex.index}">
                            <i class="icon tile-toggle icon-angle-up"></i>
							<mvc:message code="${category.name}"/>
                        </a>
                        <ul class="tile-header-actions">
                            <!-- dropdown for toggle all on/off -->
                            <li class="dropdown">
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown" >
                                    <i class="icon icon-pencil"></i>
                                    <span class="text"><mvc:message code="ToggleOnOff"/></span>
                                    <i class="icon icon-caret-down"></i>
                                </a>
                                <ul class="dropdown-menu">
                                    <li>
                                        <a href="#" data-toggle-checkboxes="on">
                                            <i class="icon icon-check-square-o"></i>
                                            <span class="text"><mvc:message code="toggle.on"/></span>
                                        </a>
                                    </li>
                                    <li>
                                        <a href="#" data-toggle-checkboxes="off">
                                            <i class="icon icon-square-o"></i>
                                            <span class="text"><mvc:message code="toggle.off"/></span>
                                        </a>
                                    </li>
                                </ul>
                            </li>
                        </ul>
                    </div>
                    <c:if test="${not empty category.subCategories}">
                        <div id="permission_category_${categoryIndex.index}" class="tile-content tile-content-forms checkboxes-content">
                            <c:forEach items="${category.subCategories.values()}" var="subCategory">
                            <%--@elvariable id="subCategory" type="com.agnitas.emm.core.admin.web.PermissionsOverviewData.PermissionSubCategoryEntry"--%>

                                <c:if test="${not empty subCategory.name}">
                                    <div class="tile-header sub-category-header">
                                        <h3 class="headline"><mvc:message code="${subCategory.name}"/></h3>
                                    </div>
                                </c:if>

                                <div>
                                    <ul class="list-group">
                                        <c:forEach items="${subCategory.permissions}" var="permission">
                                       		<c:set var="adminGroupTooltipMsg" value=""/>
											<c:if test="${permission.showInfoTooltip}">
												<c:set var="adminGroupTooltipMsg">
													<mvc:message code="permission.group.set" arguments="${permission.adminGroup.shortname}"/>
												</c:set>
											</c:if>

											<li class="list-group-item">
												<label class="checkbox-inline" data-tooltip="${adminGroupTooltipMsg}">
													<input type="checkbox" id='${permission.name}' name="grantedPermissions" value="${permission.name}"
														${permission.granted ? 'checked' : ''}
														${permission.changeable ? '' : 'disabled'} />
													&nbsp;
													<mvc:message code ='${USERRIGHT_MESSAGEKEY_PREFIX}${permission.name}' /><br>
												</label>
											</li>
                                        </c:forEach>
                                    </ul>
                                </div>
                            </c:forEach>
                            <div class="tile-separator"></div>
                        </div>
                    </c:if>
                </div>
            </c:forEach>
        </div>
    </div>

</mvc:form>
