<%@ page language="java" contentType="text/html; charset=utf-8"%>
<%@ page import="com.agnitas.emm.core.Permission" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="userGroupForm" type="com.agnitas.emm.core.usergroup.form.UserGroupForm"--%>
<%--@elvariable id="permissionCategoryList" type="java.util.List<java.lang.String>"--%>
<%--@elvariable id="permissionCategoriesMap" type="java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.String>>"--%>
<%--@elvariable id="permissionChangeable" type="java.util.Set<java.lang.String>"--%>

<c:set var="USERRIGHT_MESSAGEKEY_PREFIX" value="<%= Permission.USERRIGHT_MESSAGEKEY_PREFIX%>"/>

<c:set var="allowedUserGroupChange" value="false"/>
<emm:ShowByPermission token="role.change">
    <c:set var="allowedUserGroupChange" value="true"/>
</emm:ShowByPermission>

<mvc:form servletRelativeAction="/administration/usergroup/save.action"
          modelAttribute="userGroupForm"
          id="userGroupForm"
          data-form="resource"
          data-form-focus="shortname">

    <mvc:hidden path="id"/>
    <mvc:hidden path="companyId"/>

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
                    <label class="control-label" for="userGroupShortname"><mvc:message code="default.Name"/></label>
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
            <%--@elvariable id="permissionCategories" type="java.util.Set<com.agnitas.emm.core.admin.web.PermissionsOverviewData.PermissionCategoryEntry>"--%>
            <c:forEach items="${permissionCategories}" var="category" varStatus="categoryIndex">
                <div class="tile">
                    <div class="tile-header">
                        <a href="#" class="headline" data-toggle-tile="#permission_category_${categoryIndex.index}">
                            <i class="icon tile-toggle icon-angle-up"></i>
							<bean:message key="${category.name}"/>
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
                                    <div class="tile-header">
                                        <h3 class="headline"><bean:message key="${subCategory.name}"/></h3>
                                    </div>
                                </c:if>

                                <div>
                                    <ul class="list-group">
                                        <c:forEach items="${subCategory.permissions}" var="permission">
                                            <li class="list-group-item">
                                                <label class="checkbox-inline">
                                                    <mvc:checkbox path="grantedUserPermissions" value="${permission.name}" cssClass="js-form-change checkboxes-item"
                                                                  disabled="${not permission.changeable}"/>
                                                    &nbsp;
                                                    <c:if test="${category.name eq 'others'}">
                                                        ${permission.name}
                                                    </c:if>
                                                    <c:if test="${category.name ne 'others'}">
                                                        <c:if test="${empty subCategory.name}">
                                                            <mvc:message code="${USERRIGHT_MESSAGEKEY_PREFIX}${category.name}.${permission.name}" /><br>
                                                        </c:if>
                                                        <c:if test="${not empty subCategory.name}">
                                                            <mvc:message code="${USERRIGHT_MESSAGEKEY_PREFIX}${category.name}#${subCategory.name}.${permission.name}" /><br>
                                                        </c:if>
                                                    </c:if>
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
