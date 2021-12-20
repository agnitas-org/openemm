<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="importProfileForm" type="org.agnitas.web.forms.ImportProfileForm"--%>

<c:set var="isNewProfile" value="${importProfileForm.profileId == 0}" />
<c:set var="notAllowedMsg" ><bean:message key="default.error.notallowed"/></c:set>
<c:set var="mailinglistShowPermissionAllowed" value="${false}"/>
<emm:ShowByPermission token="mailinglist.show">
    <c:set var="mailinglistShowPermissionAllowed" value="${true}"/>
</emm:ShowByPermission>

<c:choose>
    <c:when test="${isNewProfile}">
        <c:set var="headline" scope="page">
            <bean:message key="import.NewImportProfile"/>
        </c:set>
    </c:when>
    <c:otherwise>
        <c:set var="headline" scope="page">
            <bean:message key="import.EditImportProfile"/>
        </c:set>
    </c:otherwise>
</c:choose>

<agn:agnForm action="/importprofile" data-form-focus="${isGenderSectionFocused ? 'genderTextValue' : 'profile.name'}" id="importProfileForm" 
             data-form="resource" 
             data-controller="import-profile"
             data-initializer="import-profile-view">
    <script id="config:import-profile-view" type="application/json">
        {
            "mailinglistShowPermissionAllowed": ${mailinglistShowPermissionAllowed},        
            "genderMappings": ${emm:toJson(importProfileForm.profile.genderMappingJoined)}
        }
    </script>

    <html:hidden property="profileId"/>
    <html:hidden property="action"/>

    <input type="hidden" id="save" name="save" value=""/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">${headline}</h2>
        </div>

        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="profileName" class="control-label"><bean:message key="default.Name"/></label>
                </div>
                <div class="col-sm-8">
                    <html:text styleId="profileName" styleClass="form-control" property="profile.name" maxlength="99" />
                </div>
            </div>

        </div>
    </div>

    <%@include file="/WEB-INF/jsp/importwizard/profile/file_settings.jsp" %>
    <%@include file="/WEB-INF/jsp/importwizard/profile/action_settings.jsp" %>
    <%@include file="/WEB-INF/jsp/importwizard/profile/gender_settings.jsp" %>
    <%@include file="/WEB-INF/jsp/importwizard/profile/mailinglist_settings.jsp" %>
    <%@include file="/WEB-INF/jsp/importwizard/profile/mediatype_settings.jspf" %>

</agn:agnForm>
