<%@page import="com.agnitas.emm.core.Permission"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	import="com.agnitas.userform.trackablelinks.web.ComTrackableUserFormLinkForm,
	com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink,
	com.agnitas.beans.LinkProperty,
	java.util.List" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%
	ComTrackableUserFormLinkForm aForm = (ComTrackableUserFormLinkForm) request.getAttribute("trackableUserFormLinkForm");
%>

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


                    <c:if test="${trackableUserFormLinkForm.altText ne null && trackableUserFormLinkForm.altText ne ''}">
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
                                <html:option value="<%= Integer.toString(ComTrackableUserFormLink.TRACKABLE_NO) %>"><bean:message key="NotTrackedLink" /></html:option>
                                <html:option value="<%= Integer.toString(ComTrackableUserFormLink.TRACKABLE_YES) %>"><bean:message key="TrackedLink" /></html:option>
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
                                            <%
                                                List<LinkProperty> properties = aForm.getLinkToView().getProperties();
                                                int propertyCount = 0;
                                                if (properties != null) {
                                                    for (LinkProperty property : properties) {
                                                        propertyCount++;
                                            %>
                                            <tr id="linkProperty_<%= Integer.toString(propertyCount) %>">
                                                <td>
                                                    <input class="form-control" type="text" id="<%= ComTrackableUserFormLinkForm.PROPERTY_NAME_PREFIX + propertyCount %>" name="<%= ComTrackableUserFormLinkForm.PROPERTY_NAME_PREFIX + propertyCount %>" value="<%= property.getPropertyName() %>"/>
                                                </td>
                                                <td>
                                                    <input class="form-control" type="text" id="<%= ComTrackableUserFormLinkForm.PROPERTY_VALUE_PREFIX + propertyCount %>" name="<%= ComTrackableUserFormLinkForm.PROPERTY_VALUE_PREFIX + propertyCount %>" value="<%= property.getPropertyValue() == null ? "" : property.getPropertyValue() %>"/>
                                                </td>
                                                <td>
                                                    <a href="#" class="btn btn-regular btn-alert" data-tooltip="<bean:message key="button.Delete" />" data-action="delete-link" data-link-id="<%= Integer.toString(propertyCount) %>">
                                                        <i class="icon icon-trash-o"></i>
                                                    </a>
                                                </td>
                                            </tr>
                                            <%
                                                    }
                                                }
                                            %>
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
                                                <a href="#" class="btn btn-regular btn-block btn-primary" data-action="add-extension">
                                                    <i class="icon icon-plus"></i>
                                                    <span class="text"><bean:message key="AddProperty" /></span>
                                                </a>
                                            </div>

                                            <div class="col-sm-12 col-md-4">
                                                <a href="#" class="btn btn-regular btn-block btn-alert" data-action="delete-all-links">
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
            <input class="form-control" type="text" id="<%= ComTrackableUserFormLinkForm.PROPERTY_NAME_PREFIX%>{{= count }}" name="<%= ComTrackableUserFormLinkForm.PROPERTY_NAME_PREFIX%>{{= count }}" value="{{= linkName }}"/>
        </td>
        <td>
            <input class="form-control" type="text" id="<%= ComTrackableUserFormLinkForm.PROPERTY_VALUE_PREFIX%>{{= count }}" name="<%= ComTrackableUserFormLinkForm.PROPERTY_VALUE_PREFIX%>{{= count }}" value="{{= linkValue }}"/>
        </td>
        <td>
            <a href="#" class="btn btn-regular btn-alert" data-tooltip="<bean:message key="button.Delete" />" data-action="delete-link" data-link-id="{{= count }}">
                <i class="icon icon-trash-o"></i>
            </a>
        </td>
    </tr>
</script>
