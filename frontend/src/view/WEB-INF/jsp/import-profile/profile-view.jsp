<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.import_profile.form.ImportProfileForm"--%>
<%--@elvariable id="genderMappingJoined" type="java.lang.String"--%>
<%--@elvariable id="isGenderSectionFocused" type="java.lang.Boolean"--%>

<c:set var="isNewProfile" value="${form.id == 0}"/>
<c:set var="mailinglistShowPermissionAllowed" value="${false}"/>
<emm:ShowByPermission token="mailinglist.show">
    <c:set var="mailinglistShowPermissionAllowed" value="${true}"/>
</emm:ShowByPermission>

<c:choose>
    <c:when test="${isNewProfile}">
        <mvc:message var="headline" code="import.NewImportProfile" scope="page"/>
    </c:when>
    <c:otherwise>
        <mvc:message var="headline" code="import.EditImportProfile" scope="page"/>
    </c:otherwise>
</c:choose>

<mvc:form servletRelativeAction="/import-profile/save.action" modelAttribute="form" id="importProfileForm"
          data-form="resource" data-controller="import-profile-new" data-initializer="import-profile-view"
          data-form-focus="${isGenderSectionFocused ? 'genderTextValue' : 'name'}">

    <script id="config:import-profile-view" type="application/json">
        {
            "mailinglistShowPermissionAllowed": ${mailinglistShowPermissionAllowed},
            "genderMappings": ${emm:toJson(genderMappingJoined)}
        }
    </script>

    <mvc:hidden path="id" />

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">${headline}</h2>
        </div>

        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="profileName" class="control-label">
                        <mvc:message code="default.Name"/>*
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="name" cssClass="form-control" id="profileName" maxlength="99" />
                </div>
            </div>

        </div>
    </div>

    <%@include file="fragments/file-settings.jspf" %>
    <%@include file="fragments/action_settings.jspf" %>
    <%@include file="fragments/gender_settings.jspf" %>
    <%@include file="fragments/mailinglist_settings.jspf" %>
    <%@include file="fragments/mediatype_settings.jspf" %>

</mvc:form>
