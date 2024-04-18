<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.util.LinkUtils" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="KEEP_UNCHANGED" value="<%= LinkUtils.KEEP_UNCHANGED %>"/>
<c:set var="TRACKABLE_NO" value="<%= LinkUtils.TRACKABLE_NO %>"/>
<c:set var="TRACKABLE_YES" value="<%= LinkUtils.TRACKABLE_YES %>"/>

<%--@elvariable id="userFormId" type="java.lang.Integer"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.trackablelinks.form.FormTrackableLinksForm"--%>
<%--@elvariable id="hasCompanyDefaultExtensions" type="java.lang.Boolean"--%>
<%--@elvariable id="defaultExtensions" type="java.util.List"--%>
<%--@elvariable id="link" type="com.agnitas.emm.core.trackablelinks.dto.FormTrackableLinkDto"--%>

<c:set var="isExtensionsPermitted" value="false"/>
<emm:ShowByPermission token="mailing.extend_trackable_links">
    <c:set var="isExtensionsPermitted" value="true"/>
</emm:ShowByPermission>

<mvc:form servletRelativeAction="/webform/${userFormId}/trackablelink/bulkSave.action"
          cssClass="tiles-container d-flex hidden"
          data-form="resource"
          id="userFormTrackableLinksForm"
          method="post" modelAttribute="form"
          data-controller="form-trackable-links"
          data-action="save-all" data-editable-view="${agnEditViewKey}">

    <div id="table-tile" class="tile" style="flex: 3" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="TrackableLink.edit.one"/></h1>
        </div>
        <div class="tile-body">
            <div class="table-box">
                <div class="table-scrollable">
                <table class="table table-hover table-rounded js-table">
                    <thead>
                        <th><mvc:message code="URL"/></th>
                        <th><mvc:message code="Description"/></th>
                        <th><mvc:message code="LinkTracking"/></th>
                        <emm:ShowByPermission token="mailing.extend_trackable_links">
                            <th class="fit-content"><mvc:message code="default.advanced"/></th>
                        </emm:ShowByPermission>
                        <th class="fit-content"></th>
                    </thead>

                    <tbody>
                    <c:forEach items="${form.links}" var="link" varStatus="loopStatus">
                        <c:set var="fullLinkURL" value="${emm:getFullUrlWithExtensions(link.url, link.properties)}"/>
                        <tr>
                            <td>
                                <mvc:hidden path="links[${loopStatus.index}].id"/>
                                <span class="multiline-sm-400">${fullLinkURL}</span>
                            </td>

                            <td>
                                <mvc:text path="links[${loopStatus.index}].shortname" cssClass="form-control"/>
                            </td>

                            <td>
                                <mvc:select path="links[${loopStatus.index}].trackable" cssClass="form-control">
                                    <mvc:option value="0"><mvc:message code="NotTrackedLink"/></mvc:option>
                                    <mvc:option value="1"><mvc:message code="TrackedLink"/></mvc:option>
                                </mvc:select>
                            </td>

                            <c:if test="${isExtensionsPermitted}">
                                <c:set var="extensionCount" value="${emm:countExtensions(link.properties)}"/>
                                <td>
                                    <div class="flex-center">
                                        <c:choose>
                                            <c:when test="${extensionCount gt 0}">
                                                <span class="pill-badge"><mvc:message code="default.Yes" />: ${extensionCount}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="pill-badge"><mvc:message code="No" /></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </td>
                            </c:if>

                            <td>
                                <a href="<c:url value="/webform/${userFormId}/trackablelink/${link.id}/view.action"/>" class="hidden" data-view-row></a>

                                <a href="${fullLinkURL}"
                                   class="btn btn-icon-sm btn-inverse" target="_blank">
                                    <i class="icon icon-external-link-alt"></i>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                </div>
            </div>
        </div>
    </div>

    <div id="settings-tile" class="tile" data-editable-tile style="flex: 1">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="Settings"/></h1>
        </div>
        <div class="tile-body grid gap-3 js-scrollable" style="--bs-columns:1">
            <c:if test="${isExtensionsPermitted}">
                <div class="tile">
                    <div class="tile-header p-2 border-bottom">
                        <span class="text-dark fw-medium"><mvc:message code="mailing.trackablelinks.extensions.add"/></span>
                    </div>
                    <div class="tile-body p-2">
                        <div id='links-common-extensions' data-trackable-link-extensions>
                            <script data-config type="application/json">
                              {
                                  "data": ${emm:toJson(form.commonExtensions)},
                                  "defaultExtensions": ${emm:toJson(defaultExtensions)}
                              }
                            </script>
                            <div class="row pt-2 g-2">
                                <c:if test="${not empty defaultExtensions}">
                                    <div class="col d-flex">
                                        <a href="#"
                                           data-add-default-extensions
                                           class="col flex-grow-1 btn btn-inverse text-nowrap px-1">
                                            <i class="icon icon-plus"></i>
                                            <mvc:message code="AddDefaultProperties"/>
                                        </a>
                                    </div>
                                </c:if>
                                <div class="col d-flex">
                                    <a href="#"
                                       class="col flex-grow-1 btn btn-danger text-nowrap px-1"
                                       data-delete-all-extensions>
                                        <i class="icon icon-trash-alt"></i>
                                        <mvc:message code="mailing.trackablelinks.clearPropertiesTable"/>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div>
                    <label for="linkExtension" class="form-label"><mvc:message code="TrackableLink.extendLinks"/></label>
                    <div class="d-flex gap-1">
                        <input type="text" id="linkExtension" name="linkExtension" value="${form.commonExtensionsString}" class="form-control" maxlength="500"/>
                        <c:url var="saveCommonExtensionText" value="/webform/${userFormId}/trackablelink/saveCommonExtensionText.action"/>
                        <a href="#" class="btn btn-icon-sm btn-primary" data-form-url="${saveCommonExtensionText}" data-form-submit=""
                           data-tooltip="<mvc:message code="AddProperties"/>">
                            <i class="icon icon-plus"></i>
                        </a>
                    </div>
                </div>
            </c:if>

            <div>
                <label for="trackable" class="form-label"><mvc:message code="DefaultLinkTracking"/></label>
                <div class="d-flex gap-1">
                    <select name="trackable" id="trackable" class="form-control">
                        <option value="${KEEP_UNCHANGED}" selected="selected"><mvc:message code="KeepUnchanged" /></option>
                        <option value="${TRACKABLE_NO}"><mvc:message code="NotTrackedLink"/></option>
                        <option value="${TRACKABLE_YES}"><mvc:message code="TrackedLink"/></option>
                    </select>
                    <c:url var="bulkSaveUsage" value="/webform/${userFormId}/trackablelink/bulkSaveUsage.action"/>
                    <a href="#" class="btn btn-icon-sm btn-primary" data-form-url="${bulkSaveUsage}" data-form-submit=""
                                data-tooltip="<mvc:message code="button.Save" />">
                        <i class="icon icon-save"></i>
                    </a>
                </div>
            </div>
        </div>
    </div>
</mvc:form>
