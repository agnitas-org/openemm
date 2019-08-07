<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<%--@elvariable id="availableGenderIntValues" type="java.util.List"--%>

<div class="tile">
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

                    <c:forEach var="entry" items="${importProfileForm.profile.genderMappingJoined}">
                        <tr>
                            <td>${entry.key}</td>
                            <td>${entry.value}</td>
                            <td>
                               <input type="hidden" name="removeGender_${entry.value}" value=""/>
                               <button type="button" data-form-set="removeGender_${entry.value}: '${entry.key}'" data-form-submit class="btn btn-regular btn-alert" data-tooltip="<bean:message key='button.Delete'/>">
                                   <i class="icon icon-trash-o"></i>
                               </button>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${fn:length(availableGenderIntValues) > 0}">
                        <tr>
                            <td>
                                <html:text property="addedGender" styleClass="form-control" />
                            </td>
                            <td>
                                <html:select property="addedGenderInt" styleClass="form-control">
                                    <c:forEach var="gender" items="${availableGenderIntValues}">
                                        <html:option value="${gender}">
                                            ${gender}
                                        </html:option>
                                    </c:forEach>
                                </html:select>
                            </td>
                            <td class="table-actions">
                                <button type="button" class="btn btn-regular btn-primary" data-tooltip="<bean:message key='button.Add'/>" data-form-set="addGender: 'add'" data-form-submit>
                                    <i class="icon icon-plus"></i>
                                </button>
                            </td>
                        </tr>
                    </c:if>

                </tbody>

            </table>
        </div>
    </div>
</div>
