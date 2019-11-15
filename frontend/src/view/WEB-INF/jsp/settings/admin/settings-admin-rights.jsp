<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>

<html:form styleId="PermissionForm" action="admin">
	<html:hidden property="adminID"/>
	<html:hidden property="companyID"/>
	<html:hidden property="action"/>

	<div class="tile">
		<div class="tile-header">
			<h2 class="headline"><bean:message key="UserRights.edit"/></h2>

			<ul class="tile-header-actions">
				<li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
						<i class="icon icon-eye"></i>
						<span class="text">
							<bean:message key="visibility"/>
						</span>
                        <i class="icon icon-caret-down"></i>
					</a>
					<ul class="dropdown-menu">
						<li>
                            <a href="#" data-toggle-tile-all="expand">
								<span class="text">
									<bean:message key="settings.admin.expand_all"/>
								</span>
							</a>
						</li>
						<li>
                            <a href="#" data-toggle-tile-all="collapse">
								<span class="text">
									<bean:message key="settings.admin.collapse_all"/>
								</span>
							</a>
						</li>
					</ul>
				</li>
			</ul>
		</div>

        <div class="tile-content tile-content-forms">
			<%--@elvariable id="permissionCategories" type="java.util.Set<com.agnitas.emm.core.admin.web.PermissionsOverviewData.PermissionCategoryEntry>"--%>
			<c:forEach items="${permissionCategories}" var="category" varStatus="index">
				<div class="tile">
					<div class="tile-header">
                        <a href="#" class="headline" data-toggle-tile="#userrights_category_${index.index}">
                            <i class="icon tile-toggle icon-angle-up"></i>
							<bean:message key="${category.name}"/>
						</a>
						<ul class="tile-header-actions">
							<!-- dropdown for toggle all on/off -->
							<li class="dropdown">
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown" >
									<i class="icon icon-pencil"></i>
									<span class="text"><bean:message key="ToggleOnOff"/></span>
									<i class="icon icon-caret-down"></i>
								</a>
								<ul class="dropdown-menu" >
									<li>
                                        <a href="#" data-toggle-checkboxes="on">
											<i class="icon icon-check-square-o"></i>
											<span class="text">
												<bean:message key="toggle.allOn"/>
											</span>
										</a>
									</li>
									<li>
                                        <a href="#" data-toggle-checkboxes="off">
											<i class="icon icon-square-o"></i>
											<span class="text">
												<bean:message key="toggle.allOff"/>
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
									<div class="tile-header">
										<h3 class="headline"><bean:message key="${subCategory.name}"/></h3>
									</div>
								</c:if>
								<div>
									<ul class="list-group">
										<c:forEach items="${subCategory.permissions}" var="permission" varStatus="permIndex">

											<c:set var="adminGroupTooltipMsg" value=""/>
											<c:if test="${permission.showInfoTooltip}">
												<c:set var="adminGroupTooltipMsg">
													<bean:message key="permission.group.set" arg1="${permission.adminGroup.shortname}"/>
												</c:set>
											</c:if>

											<li class="list-group-item">
												<label class="checkbox-inline" data-tooltip="${adminGroupTooltipMsg}">
													<input type="checkbox" id='${category.name}.${permission.name}' name="user_right_${index.index}_${subIndex.index}_${permIndex.index}"
														   value="user__${permission.name}"
														${permission.granted ? 'checked' : ''}
														${permission.changeable ? '' : 'disabled'} />
													&nbsp;
													<c:if test="${category.name == 'others'}">
														${permission.name}
													</c:if>
													<c:if test="${category.name != 'others'}">
														<c:if test="${empty subCategory.name}">
															<bean:message key='UserRight.${category.name}.${permission.name}' /><br>
														</c:if>
														<c:if test="${not empty subCategory.name}">
															<bean:message key='UserRight.${category.name}#${subCategory.name}.${permission.name}' /><br>
														</c:if>
													</c:if>
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
</html:form>
