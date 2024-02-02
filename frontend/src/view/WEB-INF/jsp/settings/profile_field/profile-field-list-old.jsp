<%@page import="com.agnitas.beans.ProfileFieldMode"%>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="profileForm" type="com.agnitas.emm.core.profilefields.form.ProfileFieldForm"--%>
<%--@elvariable id="fields" type="com.agnitas.beans.impl.ProfileFieldImpl"--%>

<c:set var="MAX_SORT_INDEX" value="${1000}"/>
<c:set var="MODE_EDIT_NOT_VISIBLE" value="<%= ProfileFieldMode.NotVisible %>"/>
<c:set var="MODE_EDIT_READONLY" value="<%= ProfileFieldMode.ReadOnly %>"/>

<mvc:form servletRelativeAction="/profiledbold/profiledb.action" data-form="resource" modelAttribute="profileForm">

    <input type="hidden" name="page" value="${profileFields.pageNumber}"/>
    <input type="hidden" name="sort" value="${profileFields.sortCriterion}"/>
    <input type="hidden" name="dir" value="${profileFields.sortDirection}"/>
    <input type="hidden" name="syncSorting" value="true"/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "profile-field-overview": {
                "rows-count": ${profileForm.numberOfRows},
                "column-name": ${emm:toJson(profileForm.sort)},
                "ascending-order": ${profileForm.dir eq 'asc'}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="default.Overview"/></h2>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><mvc:message code="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="listSize"/></li>
                        <li>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content">
            <div class="table-wrapper">
                <display:table class="table table-bordered table-striped table-hover js-table"
                               id="fields"
                               name="profileFields"
                               sort="external"
                               requestURI="/profiledbold/profiledb.action?syncSorting=true"
                               partialList="true"
                               size="${profileForm.numberOfRows}"
                               excludedParams="*">

                    <display:column headerClass="profile_fields_name_head" class="profile_fields_name" titleKey="settings.FieldName" sortable="true" sortProperty="shortname">
                        <span class="multiline-auto">${fields.shortname}</span>
                    </display:column>

                    <display:column headerClass="profile_fields_dbname_head" class="profile_fields_dbname" titleKey="settings.FieldNameDB" sortable="true" sortProperty="column">
                        <span class="multiline-auto">${fields.column}</span>
                    </display:column>

                    <display:column headerClass="profile_fields_type_head" class="profile_fields_type" titleKey="default.Type" sortable="true" sortProperty="dataType">
                        <mvc:message code="settings.fieldType.${fields.dataType}"/>
                    </display:column>

                    <display:column headerClass="profile_fields_length_head" class="profile_fields_length" titleKey="settings.Length" sortable="true" sortProperty="dataTypeLength">
                        <c:if test="${fields.dataTypeLength > 0}">
                            ${fields.dataTypeLength}
                        </c:if>
                    </display:column>

                    <display:column headerClass="profile_fields_defvalue_head" class="profile_fields_defvalue" titleKey="settings.Default_Value" sortable="true" sortProperty="defaultValue">
                        ${fn:escapeXml(fields.defaultValue)}
                    </display:column>

                    <display:column headerClass="profile_fields_visibility_head" class="profile_fields_visibility" titleKey="visibility" sortable="true" sortProperty="modeEdit">
                        <c:choose>
                            <c:when test="${fields.modeEdit eq MODE_EDIT_NOT_VISIBLE}">
                                <mvc:message code="notVisible"/>
                            </c:when>
                            <c:when test="${fields.modeEdit eq MODE_EDIT_READONLY}">
                                <mvc:message code="ReadOnly"/>
                            </c:when>
                            <c:otherwise>
                                <mvc:message code="visible"/>
                            </c:otherwise>
                        </c:choose>
                    </display:column>

                    <display:column headerClass="profile_fields_sort_head" class="profile_fields_sort" titleKey="FieldSort" sortable="true" sortProperty="sort">
                        <c:choose>
                            <c:when test="${fields.sort eq MAX_SORT_INDEX}">
                                <mvc:message code="noSort"/>
                            </c:when>
                            <c:when test="${fields.sort eq 1}">
                                <mvc:message code="first"/>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="field" items="${fieldsWithIndividualSortOrder}">
                                    <c:if test="${field.sort eq (fields.sort - 1)}">
                                        <mvc:message code="after"/> ${field.shortname}
                                    </c:if>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </display:column>

                    <display:column class="table-actions">
                        <c:if test="${fields.isHiddenField == false}">
                            <c:url var="viewProfileLink" value="/profiledbold/${fields.column}/view.action"/>

                            <a href="${viewProfileLink}" class="hidden js-row-show" title="<mvc:message code="settings.profile.ProfileEdit"/>"/>

                            <c:set var="actionsDelete">
                                <mvc:message code="settings.profile.ProfileDelete"/>
                            </c:set>
                            <c:url var="deleteProfileLink" value="/profiledbold/${fields.column}/confirmDelete.action">
                            	<c:param name="from_list_page" value="true" />
                            </c:url>

                            <a href="${deleteProfileLink}" class="btn btn-regular btn-alert js-row-delete" title="${actionsDelete}" data-tooltip="${actionsDelete}">
                                <i class="icon icon-trash-o"></i>
                            </a>
                        </c:if>
                    </display:column>
                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
