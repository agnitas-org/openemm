<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="availableGenderIntValues" type="java.util.List"--%>

<div class="tile" data-initializer="import-profile-view">
    <script id="config:import-profile-view" type="application/json">
        {
            "genderMappings": ${emm:toJson(importProfileForm.profile.genderMappingJoined)}
        }
    </script>
    
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#recipient-import-gender-settings">
            <i class="tile-toggle icon icon-angle-up"></i>
            <bean:message key="import.profile.gender.settings"/>
        </a>
    </div>
    <div id="recipient-import-gender-settings" class="tile-content tile-content-forms">
        <div class="table-responsive">
            <table class="table table-bordered table-striped">
                <thead>
                    <tr>
                        <th><bean:message key="import.profile.gender.string"/></th>
                        <th><bean:message key="import.profile.gender.int"/></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                <%-- this block load by JS--%>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script id="gender-settings-table-row" type="text/x-mustache-template">
    <tr data-gender-settings-row>
        {{ if (textValues != '' || intValue != '') { }}
            <td data-gender-text-value>{{- textValues}}</td>
            <td data-gender-int-value>{{- intValue}}</td>
        <td class="table-actions">
            <button type="button" class="btn btn-regular btn-alert" data-action="delete-gender-mapping" data-tooltip="<bean:message key='button.Delete'/>">
                <i class="icon icon-trash-o"></i>
            </button>
        </td>
        {{ } else { }}
            <c:if test="${fn:length(availableGenderIntValues) > 0}">
                <td>
                    <input type="text" name="genderTextValue" class="form-control" data-gender-text-value value="{{- textValues}}" data-action="gender-enterdown"/>
                </td>
                <td>
                    <select data-gender-int-value class="form-control">
                        <c:forEach var="gender" items="${availableGenderIntValues}">
                            {{ var selected = intValue == ${gender} ? 'selected="selected"' : '';}}
                            <option value="${gender}">${gender} (<bean:message key="recipient.gender.${gender}.short"/>)</option>
                        </c:forEach>
                    </select>
                </td>
                <td class="table-actions">
                    <button type="button" class="btn btn-regular btn-primary" data-action="add-gender-mapping" data-tooltip="<bean:message key='button.Add'/>">
                        <i class="icon icon-plus"></i>
                    </button>
                </td>
            </c:if>
        {{ } }}
    </tr>
</script>
