<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink" %>
<%@ page import="com.agnitas.userform.trackablelinks.web.ComTrackableUserFormLinkForm" %>
<%@ page import="org.agnitas.beans.BaseTrackableLink" %>

<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="trackableUserFormLinkForm" type="com.agnitas.userform.trackablelinks.web.ComTrackableUserFormLinkForm"--%>

<c:set var="TRACKABLE_NO" value="<%= BaseTrackableLink.TRACKABLE_NO %>" />
<c:set var="TRACKABLE_YES" value="<%= ComTrackableUserFormLink.TRACKABLE_YES %>" />
<c:set var="PROPERTY_NAME_PREFIX" value="<%= ComTrackableUserFormLinkForm.PROPERTY_NAME_PREFIX %>" />
<c:set var="PROPERTY_VALUE_PREFIX" value="<%= ComTrackableUserFormLinkForm.PROPERTY_VALUE_PREFIX %>" />

<div class="row">
    <div class="col-xs-12 row-1-1" data-controller="trackable-link">

        <agn:agnForm action="/trackuserformlink?method=save" id="trackableUserFormLinkForm" data-form="resource">
            <html:hidden property="formID" />
            <html:hidden property="linkID" />

            <div class="tile">
                <div class="tile-header">
                    <h2 class="headline">
                        <bean:message key="TrackableLink.editLink"/>
                    </h2>
                </div>
                <div class="tile-content tile-content-forms">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="linkUrl">
                                <bean:message key="URL" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <html:text property="linkUrl" styleId="linkUrl" styleClass="form-control" readonly="true"/>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="linkName">
                                <bean:message key="Description" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <html:text property="linkName" styleId="linkName" styleClass="form-control" />
                        </div>
                    </div>


                    <c:if test="${not empty trackableUserFormLinkForm.altText}">
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label class="control-label" for="altText">
                                    <bean:message key="Title" />
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <html:text property="altText" styleId="altText" styleClass="form-control" readonly="true"/>
                            </div>
                        </div>
                    </c:if>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="trackable">
                                <bean:message key="LinkTracking" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <agn:agnSelect property="trackable" styleId="trackable" styleClass="form-control" data-action="trackable">
                                <html:option value="${TRACKABLE_NO}"><bean:message key="NotTrackedLink" /></html:option>
                                <html:option value="${TRACKABLE_YES}"><bean:message key="TrackedLink" /></html:option>
                            </agn:agnSelect>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="relevance">
                                <bean:message key="Relevance" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <html:select property="relevance" styleId="relevance" styleClass="form-control">
                                <html:option value="0"><bean:message key="Relevance_0" /></html:option>
                                <html:option value="1"><bean:message key="Relevance_1" /></html:option>
                                <html:option value="2"><bean:message key="Relevance_2" /></html:option>
                            </html:select>
                        </div>
                    </div>

                    <emm:ShowByPermission token="mailing.extend_trackable_links">
                        <div id="linkProperties">

                            <div class="form-group">
                                <div class="col-sm-4">
                                    <label class="control-label">
                                        <bean:message key="LinkExtensions" />
                                    </label>
                                </div>
                                <div class="col-sm-8">
                                    <div id="link_extension_changes" class="table-responsive">
                                        <table class="table table-bordered table-striped" id="linkPropertyTable">
                                            <thead>
                                                <th><bean:message key="Name"/></th>
                                                <th><bean:message key="Value"/></th>
                                                <th></th>
                                            </thead>
                                            <tbody>
                                            <c:if test="${not empty trackableUserFormLinkForm.linkToView.properties}">
                                                <c:forEach items="${trackableUserFormLinkForm.linkToView.properties}" var="property" varStatus="count">
                                                    <tr id="linkProperty_${count.count}">
                                                        <td>
                                                            <input class="form-control" type="text"
                                                                   id="${PROPERTY_NAME_PREFIX}${count.index}"
                                                                   name="${PROPERTY_NAME_PREFIX}${count.index}"
                                                                   value="${property.propertyName}"/>
                                                        </td>
                                                        <td>
                                                            <input class="form-control" type="text"
                                                                   id="${PROPERTY_VALUE_PREFIX}${count.index}"
                                                                   name="${PROPERTY_VALUE_PREFIX}${count.index}"
                                                                   value="${property.propertyValue ne null ? property.propertyValue : ''}"/>
                                                        </td>
                                                        <td>
                                                            <a href="#" class="btn btn-regular btn-alert"
                                                               data-tooltip="<bean:message key="button.Delete" />"
                                                               data-action="link-details-delete-link" data-link-id="${count.index}">
                                                                <i class="icon icon-trash-o"></i>
                                                            </a>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </c:if>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="col-sm-8 col-sm-push-4">
                                    <div class="btn-group">
                                        <div class="row">
                                            <div class="col-sm-12 col-md-4">
                                                <a href="#" class="btn btn-regular btn-block btn-primary" data-action="link-details-add-extension">
                                                    <i class="icon icon-plus"></i>
                                                    <span class="text"><bean:message key="AddProperty" /></span>
                                                </a>
                                            </div>

                                            <div class="col-sm-12 col-md-4">
                                                <a href="#" class="btn btn-regular btn-block btn-alert" data-action="link-details-delete-all-links">
                                                    <i class="icon icon-trash-o"></i>
                                                    <span class="text"><bean:message key="ClearAllProperties"/></span>
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </emm:ShowByPermission>

                </div>
                <!-- Tile Content END -->

            </div>
            <!-- Tile END -->

        </agn:agnForm>
    </div>
</div>

<script id="link-table-row" type="text/x-mustache-template">
    <tr id="linkProperty_{{= count }}" data-link-id="{{= count }}" >
        <td>
            <input class="form-control" type="text" id="${PROPERTY_NAME_PREFIX}{{= count }}" name="${PROPERTY_NAME_PREFIX}{{= count }}" value="{{= linkName }}"/>
        </td>
        <td>
            <input class="form-control" type="text" id="${PROPERTY_VALUE_PREFIX}{{= count }}" name="${PROPERTY_VALUE_PREFIX}{{= count }}" value="{{= linkValue }}"/>
        </td>
        <td>
            <a href="#" class="btn btn-regular btn-alert" data-tooltip="<bean:message key="button.Delete" />" data-action="link-details-delete-link" data-link-id="{{= count }}">
                <i class="icon icon-trash-o"></i>
            </a>
        </td>
    </tr>
</script>
