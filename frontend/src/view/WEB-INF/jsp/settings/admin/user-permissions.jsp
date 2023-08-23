<%@page import="com.agnitas.emm.core.Permission"%>
<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.do" %>

<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags"   prefix="agn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm"%>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<c:set var="USERRIGHT_MESSAGEKEY_PREFIX" value="<%= Permission.USERRIGHT_MESSAGEKEY_PREFIX %>"/>

<%--@elvariable id="permissionCategories" type="java.util.Set<com.agnitas.emm.core.admin.web.PermissionsOverviewData.PermissionCategoryEntry>"--%>
<%--@elvariable id="isRestfulUser" type="java.lang.Boolean"--%>

<c:choose>
    <c:when test="${isRestfulUser}">
        <c:set var="actionUrl" value="/restfulUser/${adminRightsForm.adminID}/rights/view.action" />
    </c:when>
    <c:otherwise>
        <c:set var="actionUrl" value="/admin/${adminRightsForm.adminID}/rights/view.action" />
    </c:otherwise>
</c:choose>
<mvc:form id="PermissionForm" servletRelativeAction="${actionUrl}" modelAttribute="adminRightsForm" data-controller="user-permissions">
    <div class="tile">
        <script data-initializer="user-permissions" type="application/json">        
            {
                "permissionCategories": ${emm:toJson(permissionCategories)}
            }
        </script>
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="Search"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="permission-filter"><mvc:message code="settings.permission.type"/></label>
                </div>
                <div class="col-sm-8">
                    <select name="filter" id="permission-filter" multiple class="form-control js-select-tags">
                        <option value="granted"><mvc:message code="settings.permission.granted"/></option>
                        <option value="grantedForUser"><mvc:message code="settings.permission.granted.user"/></option>
                        <option value="grantedByGroup"><mvc:message code="settings.permission.granted.group"/></option>
                        <option value="grantable"><mvc:message code="settings.permission.grantable"/></option>
                        <option value="notGrantable"><mvc:message code="settings.permission.notavailable"/></option>
                        <option value="new"><mvc:message code="New"/></option>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-12">
                    <div class="btn-group pull-right">
                        <button type="button" class="btn btn-regular" data-action="reset-filter">
                            <mvc:message code="button.search.reset"/>
                        </button>
                        <button type="button" class="btn btn-primary btn-regular pull-right"
                                data-action="apply-filter">
                            <i class="icon icon-search"></i>
                            <span class="text"><mvc:message code="Search"/></span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
	<div class="tile">
		<div class="tile-header">
			<h2 class="headline"><mvc:message code ="UserRights.edit"/></h2>

			<ul class="tile-header-actions">
				<li class="dropdown">
					<a href="#" class="dropdown-toggle" data-toggle="dropdown">
						<i class="icon icon-eye"></i>
						<span class="text">
                            <mvc:message code ="visibility"/>
                        </span>
						<i class="icon icon-caret-down"></i>
					</a>
					<ul class="dropdown-menu">
						<li>
							<a href="#" data-toggle-tile-all="expand">
                                <span class="text">
                                    <mvc:message code ="settings.admin.expand_all"/>
                                </span>
							</a>
						</li>
						<li>
							<a href="#" data-toggle-tile-all="collapse">
                                <span class="text">
                                    <mvc:message code ="settings.admin.collapse_all"/>
                                </span>
							</a>
						</li>
					</ul>
				</li>
			</ul>
		</div>

		<div class="tile-content tile-content-forms">
			<c:forEach items="${permissionCategories}" var="category" varStatus="index">
				<div class="tile">
					<div class="tile-header">
						<a href="#" class="headline" data-toggle-tile="#userrights_category_${index.index}">
							<i class="icon tile-toggle icon-angle-up"></i>
							<mvc:message code="${category.name}"/>
						</a>
						<ul class="tile-header-actions">
							<!-- dropdown for toggle all on/off -->
							<li class="dropdown">
								<a href="#" class="dropdown-toggle" data-toggle="dropdown" >
									<i class="icon icon-pencil"></i>
									<span class="text"><mvc:message code ="ToggleOnOff"/></span>
									<i class="icon icon-caret-down"></i>
								</a>
								<ul class="dropdown-menu" >
									<li>
										<a href="#" data-toggle-checkboxes="on">
											<i class="icon icon-check-square-o"></i>
											<span class="text">
                                                <mvc:message code ="toggle.allOn"/>
                                            </span>
										</a>
									</li>
									<li>
										<a href="#" data-toggle-checkboxes="off">
											<i class="icon icon-square-o"></i>
											<span class="text">
                                                <mvc:message code ="toggle.allOff"/>
                                            </span>
										</a>
									</li>
								</ul>
							</li>
						</ul>
					</div>

					<c:if test="${not empty category.subCategories}">
						<div id="userrights_category_${index.index}" class="tile-content tile-content-forms checkboxes-content">
							<c:forEach items="${category.subCategories.values()}" var="subCategory" varStatus="subIndex">
								<%--@elvariable id="subCategory" type="com.agnitas.emm.core.admin.web.PermissionsOverviewData.PermissionSubCategoryEntry"--%>
								<c:if test="${not empty subCategory.name}">
									<div class="tile-header sub-category-header">
										<h3 class="headline"><mvc:message code ="${subCategory.name}"/></h3>
									</div>
								</c:if>
								<div>
									<ul class="list-group">
										<c:forEach items="${subCategory.permissions}" var="permission" varStatus="permIndex">

											<c:set var="adminGroupTooltipMsg" value=""/>
											<c:if test="${permission.showInfoTooltip}">
												<c:set var="adminGroupTooltipMsg">
													<mvc:message code="permission.group.set" arguments="${permission.adminGroup.shortname}"/>
												</c:set>
											</c:if>

											<li class="list-group-item">
												<label class="checkbox-inline" data-tooltip="${adminGroupTooltipMsg}">
													<input type="checkbox" id='${permission.name}' name="userRights"
														   value="${permission.name}"
														${permission.granted ? 'checked' : ''}
														${permission.changeable ? '' : 'disabled'} />
													&nbsp;
													<mvc:message code ='${USERRIGHT_MESSAGEKEY_PREFIX}${permission.name}' /><br>
												</label>
											</li>
										</c:forEach>
									</ul>
								</div>

								<c:if test="${subIndex.last}">
									<div class="tile-separator"></div>
								</c:if>
							</c:forEach>
						</div>

					</c:if>
				</div>
			</c:forEach>
		</div>
	</div>
</mvc:form>
