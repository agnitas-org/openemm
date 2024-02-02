<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
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

<c:set var="isExtensionsPermitted" value="false"/>
<emm:ShowByPermission token="mailing.extend_trackable_links">
    <c:set var="isExtensionsPermitted" value="true"/>
</emm:ShowByPermission>

<div class="row">

    <div class="col-xs-12 row-1-1"
         data-view-block="col-xs-12 row-1-1"
         data-view-split="col-md-6"
         data-view-hidden="col-xs-12 row-1-1">

        <mvc:form servletRelativeAction="/webform/${userFormId}/trackablelink/bulkSave.action"
                  data-form="resource"
                  id="userFormTrackableLinksForm"
                  method="post" modelAttribute="form"
                  data-controller="form-trackable-link"
                  data-action="bulk-save">

            <div class="tile">
                <div class="tile-header">
                    <a href="#" class="headline" data-toggle-tile="#form-trackable-links">
                        <i class="tile-toggle icon icon-angle-up"></i>
                        <mvc:message code="TrackableLink.edit.one"/>
                    </a>
                </div>

                <div id="form-trackable-links" class="tile-content">
                    <div class="table-responsive">
                        <table class="table table-bordered table-striped js-table table-form table-hover">
                            <thead>
                            <th><mvc:message code="URL"/></th>
                            <th><mvc:message code="Description"/></th>
                            <th><mvc:message code="LinkTracking"/></th>

                            <emm:ShowByPermission token="mailing.extend_trackable_links">
                                <th><mvc:message code="mailing.extend_trackable_link"/></th>
                            </emm:ShowByPermission>
                            <th></th>
                            </thead>

                            <tbody>
                            <%--@elvariable id="link" type="com.agnitas.emm.core.trackablelinks.dto.FormTrackableLinkDto"--%>
                            <c:forEach items="${form.links}" var="link" varStatus="loopStatus">
                                <c:set var="fullLinkURL" value="${emm:getFullUrlWithExtensions(link.url, link.properties)}"/>
                                <tr>
                                    <mvc:hidden path="links[${loopStatus.index}].id"/>
                                    <td>
                                        <span class="multiline-sm-400">${fullLinkURL}</span>
                                    </td>
                                    <td class="align-top">
                                        <mvc:text path="links[${loopStatus.index}].shortname" cssClass="form-control"/>
                                    </td>

                                    <td class="align-top">
                                        <mvc:select path="links[${loopStatus.index}].trackable" cssClass="form-control">
                                            <mvc:option value="0"><mvc:message code="NotTrackedLink"/></mvc:option>
                                            <mvc:option value="1"><mvc:message code="TrackedLink"/></mvc:option>
                                        </mvc:select>
                                    </td>

                                    <c:if test="${isExtensionsPermitted}">
                                    <c:set var="extensionCount" value="${emm:countExtensions(link.properties)}"/>
                                    <td class="align-top">
                                        <c:choose>
                                            <c:when test="${extensionCount gt 0}">
                                                    <span class="badge badge-success">
                                                        <mvc:message code="default.Yes" /> (${extensionCount})
                                                    </span>
                                            </c:when>
                                            <c:otherwise>
                                                    <span class="badge">
                                                        <mvc:message code="No" />
                                                    </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    </c:if>

                                    <td class="table-actions">
                                        <c:url var="viewLink" value="/webform/${userFormId}/trackablelink/${link.id}/view.action"/>
                                        <a href="${viewLink}" class="hidden js-row-show"><mvc:message code="template.edit"/></a>

                                        <a href="${fullLinkURL}"
                                           class="btn btn-regular" target="_blank">
                                            <i class="icon icon-share-square-o"></i>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>

                    </div>
                </div>
            </div>

            <div class="tile">
                <div class="tile-header">
                    <a href="#" class="headline" data-toggle-tile="form-trackable-links-bulk-edit">
                        <i class="tile-toggle icon icon-angle-up"></i>
                        <mvc:message code="Settings"/>
                    </a>
                </div>

                <div id="form-trackable-links-bulk-edit" class="tile-content tile-content-forms">
                    <c:if test="${isExtensionsPermitted}">
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label for="linkExtension" class="control-label">
                                    <mvc:message code="TrackableLink.extendLinks"/>
                                </label>
                            </div>

                            <div class="col-sm-8">
                                <div class="input-group">
                                    <div class="input-group-controls">
                                        <input type="text" id="linkExtension" name="linkExtension" value="${form.commonExtensionsString}" class="form-control" maxlength="500"/>
                                    </div>
                                    <div class="input-group-btn">
                                        <c:url var="saveCommonExtensionText" value="/webform/${userFormId}/trackablelink/saveCommonExtensionText.action"/>
                                        <a href="#" class="btn btn-regular" data-form-url="${saveCommonExtensionText}" data-form-submit=""
                                           data-tooltip="<mvc:message code="AddProperties"/>">
                                            <i class="icon icon-plus"></i>
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="form-group" data-initializer="trackable-link-extensions">

                            <script id="config:trackable-link-extensions" type="application/json">
                            {
                                "extensions": ${emm:toJson(form.commonExtensions)},
                                "defaultExtensions": ${emm:toJson(defaultExtensions)}
                            }
                            </script>
                            <div class="col-sm-4">
                                <label class="control-label">
                                    <mvc:message code="LinkExtensions"/>
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <div id="link-common-extensions" class="table-responsive">
                                    <table class="table table-bordered table-striped" id="extensions-table">
                                        <thead>
                                        <th><mvc:message code="Name"/></th>
                                        <th><mvc:message code="Value"/></th>
                                        <th></th>
                                        </thead>
                                        <tbody>
                                        <%-- this block load by JS--%>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-8 col-sm-push-4">
                                <div class="btn-group">
                                    <div class="row">
                                        <c:if test="${not empty defaultExtensions}">
                                            <div class="col-sm-12 col-md-4">
                                                <a href="#"
                                                   data-action="add-default-extensions"
                                                   class="btn btn-regular btn-block">
                                                    <i class="icon icon-plus"></i>
                                                    <span class="text"><mvc:message code="AddDefaultProperties"/></span>
                                                </a>
                                            </div>
                                        </c:if>

                                        <div class="col-sm-12 col-md-4">
                                            <a href="#"
                                               class="btn btn-regular btn-block btn-primary"
                                               data-action="add-extension">
                                                <i class="icon icon-plus"></i>
                                                <span class="text"><mvc:message code="AddProperty"/></span>
                                            </a>
                                        </div>


                                        <div class="col-sm-12 col-md-4">
                                            <a href="#"
                                               class="btn btn-regular btn-block btn-alert"
                                               data-action="delete-all-extensions">
                                                <i class="icon icon-trash-o"></i>
                                                <span class="text"><mvc:message code="mailing.trackablelinks.clearPropertiesTable"/></span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="vspace-bottom-20"></div>
                    </c:if>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label for="trackable" class="control-label">
                                <mvc:message code="DefaultLinkTracking"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <select name="trackable" id="trackable" class="form-control">
                                        <option value="${KEEP_UNCHANGED}" selected="selected"><mvc:message code="KeepUnchanged" /></option>
                                        <option value="${TRACKABLE_NO}"><mvc:message code="NotTrackedLink"/></option>
                                        <option value="${TRACKABLE_YES}"><mvc:message code="TrackedLink"/></option>
                                    </select>
                                </div>
                                <div class="input-group-btn">
                                    <c:url var="bulkSaveUsage" value="/webform/${userFormId}/trackablelink/bulkSaveUsage.action"/>
                                    <a href="#" class="btn btn-regular" data-form-url="${bulkSaveUsage}" data-form-submit=""
                                                data-tooltip="<mvc:message code="button.Save" />">
                                        <i class="icon icon-save"></i>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </mvc:form>
    </div>
</div>

<%@ include file="extension-row-template.jspf" %>
