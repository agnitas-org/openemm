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
							<a href="#" data-toggle-tiles-all="expand">
								<span class="text">
									<bean:message key="settings.admin.expand_all"/>
								</span>
							</a>
						</li>
						<li>
							<a href="#" data-toggle-tiles-all="collapse">
								<span class="text">
									<bean:message key="settings.admin.collapse_all"/>
								</span>
							</a>
						</li>
					</ul>
				</li>
				<li class="dropdown">
					<a href="#" class="dropdown-toggle" data-toggle="dropdown">
						<i class="icon icon-pencil"></i>
						<span class="text">
							<bean:message key="ToggleOnOff"/>
						</span>
						<i class="icon icon-caret-down"></i>
					</a>
					<ul class="dropdown-menu">
						<li>
							<a href="#" data-toggle-checkboxes-all="on">
								<i class="icon icon-check-square-o"></i>
								<span class="text">
									<bean:message key="toggle.allOn"/>
								</span>
							</a>
						</li>
						<li>
							<a href="#" data-toggle-checkboxes-all="off">
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

		<div class="tile-content tile-content-forms">
			<logic:iterate collection="${permissionCategories}" id="category" indexId="categoryIndex">
				<div class="tile">
					<div class="tile-header">
						<a href="#" class="headline" data-toggle-tile="#userrights_category${categoryIndex}">
							<i class="icon tile-toggle icon-angle-up"></i>
							<bean:message key="${category}"/>
						</a>
						<ul class="tile-header-actions">
							<!-- dropdown for toggle all on/off -->
							<li class="dropdown">
								<a href="#" class="dropdown-toggle" data-toggle="dropdown">
									<i class="icon icon-pencil"></i>
									<span class="text"><bean:message key="ToggleOnOff"/></span>
									<i class="icon icon-caret-down"></i>
								</a>
								<ul class="dropdown-menu">
									<li>
										<a href="#" data-toggle-checkboxes="on">
											<i class="icon icon-check-square-o"></i>
											<span class="text"><bean:message key="toggle.on"/></span>
										</a>
									</li>
									<li>
										<a href="#" data-toggle-checkboxes="off">
											<i class="icon icon-square-o"></i>
											<span class="text"><bean:message key="toggle.off"/></span>
										</a>
									</li>
								</ul>
							</li>
						</ul>
					</div>
					<c:if test="${not empty subCategoriesByCategory[category]}">
                    <div id="userrights_category${categoryIndex}" class="tile-content tile-content-forms">
                        <bean:size id="categorysSize" collection="${subCategoriesByCategory[category]}"/>

						<logic:iterate collection="${subCategoriesByCategory[category]}" id="subCategory" indexId="subCategoryIndexId">
							<c:if test="${not empty subCategory}">
                                <div class="tile-header">
                                    <h3 class="headline"><bean:message key="${subCategory}"/></h3>
                                </div>
                            </c:if>

							<div>
								<ul class="list-group">
									<c:if test="${not empty permissionsByCategory[category][subCategory]}">
										<logic:iterate collection="${permissionsByCategory[category][subCategory]}" id="right" indexId="rightIndex">
											<li class="list-group-item">
												<label class="checkbox-inline">
													<input type="checkbox" id='${category}.${right}' name="user_right_${categoryIndex}_${subCategoryIndexId}_${rightIndex}" value="user__${right}" ${permissionGranted[right]} ${permissionChangeable[right]}>
													&nbsp;
													<c:if test="${category == 'others'}">
														${right}
													</c:if>
													<c:if test="${category != 'others'}">
														<c:if test="${empty subCategory}">
															<bean:message key='UserRight.${category}.${right}' /><br>
														</c:if>
														<c:if test="${not empty subCategory}">
															<bean:message key='UserRight.${category}#${subCategory}.${right}' /><br>
														</c:if>
													</c:if>
												</label>
											</li>
										</logic:iterate>
									</c:if>
								</ul>
							</div>
                            <logic:notEqual name="subCategoryIndexId" value="categorysSize">
                                <div class="tile-separator"></div>
                            </logic:notEqual>
						</logic:iterate>
                    </div>
					</c:if>
				</div>
			</logic:iterate>
		</div>
	</div>
</html:form>

<script type="application/javascript">
	(function(){
		var Tile = AGN.Lib.Tile,
			Action = AGN.Lib.Action;

		Action.new({'click': '[data-toggle-tiles-all]'}, function() {
			var action = this.el.data('toggle-tiles-all');

			_.each($('[data-toggle-tile]'), function(field) {
				var $trigger = $(field);
				if (action == 'expand') {
					Tile.show($trigger);
				} else if (action == 'collapse') {
					Tile.hide($trigger);
				}
			});
		});

		Action.new({'click': '[data-toggle-checkboxes-all]'}, function() {
			var action = this.el.data('toggle-checkboxes-all');
			var isChecked = (action == "on") ? true : false;

			_.each($('[name^="user_right"]:enabled'), function(field) {
				$(field).prop('checked', isChecked);
			});
		});

		Action.new({'click': '[data-toggle-checkboxes]'}, function() {
			var action = this.el.data('toggle-checkboxes');
			var isChecked = (action == "on") ? true : false;

			var tileContentElId = this.el.closest('.tile-header').find('[data-toggle-tile]').data('toggle-tile');
			_.each($(tileContentElId + ' [name^="user_right"]:enabled'), function(field) {
				$(field).prop('checked', isChecked);
			});
		});

	})();
</script>
