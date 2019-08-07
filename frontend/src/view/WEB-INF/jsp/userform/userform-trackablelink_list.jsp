<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="org.agnitas.util.AgnUtils"%>
<%@page import="com.agnitas.emm.core.Permission"%>
<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.userform.trackablelinks.web.ComTrackableUserFormLinkForm" %>
<%@ page import="com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink" %>
<%@ page import="com.agnitas.beans.LinkProperty" %>
<%@ page import="org.agnitas.util.SafeString" %>
<%@ page import="java.util.List" %>
<%@ page import="com.agnitas.web.ComTrackableLinkAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<div class="row">

    <div class="col-xs-12 row-1-1" data-view-block="col-xs-12 row-1-1" data-view-split="col-md-6" data-view-hidden="col-xs-12 row-1-1" data-controller="trackable-link-list">

        <div class="tile">
            <div class="tile-header">
                <a href="#" class="headline" data-toggle-tile="#tile-trackableLinkEditOne">
                    <i class="tile-toggle icon icon-angle-up"></i>
                    <bean:message key="TrackableLink.edit.one" />
                </a>
            </div>
            <div id="tile-trackableLinkEditOne" class="tile-content">
                <div class="table-responsive">

                    <table class="table table-bordered table-striped js-table table-hover">
                        <thead>
                            <th><bean:message key="URL" /></th>
                            <th><bean:message key="Description" /></th>
                            <th><bean:message key="LinkTracking" /></th>
                            <th><bean:message key="Relevance" /></th>

                            <emm:ShowByPermission token="mailing.extend_trackable_links">
                                <th><bean:message key="mailing.extend_trackable_link" /></th>
                                <c:set var="EXTEND_LINK_ACTION" value="${ACTION_SET_EXTEND_LINKS}" scope="page" />
                            </emm:ShowByPermission>

                            <th></th>
                        </thead>
                        <tbody>
                        <logic:iterate id="link" name="trackableUserFormLinkForm" property="links" indexId="index">
                            <%
                                ComTrackableUserFormLink aLink = (ComTrackableUserFormLink) pageContext.getAttribute("link");
                                int linkExtensionCount = 0;
                                if (aLink.getProperties() != null) {
                                    for (LinkProperty property : aLink.getProperties()) {
                                        if (property.getPropertyType() == LinkProperty.PropertyType.LinkExtension) {
                                            linkExtensionCount++;
                                        }
                                    }
                                }
                                String fullUrlWithExtensions = aLink.createDirectLinkWithOptionalExtensionsWithoutUserData();
                                String description = StringEscapeUtils.escapeHtml(AgnUtils.shortenStringToMaxLength(aLink.getShortname(), 30));
                            %>

                            <tr>
                                <td>
                                    <span class="multiline-sm-400">
                                        <%= StringEscapeUtils.escapeHtml(fullUrlWithExtensions) %>
                                    </span>
                                </td>

                                <td>
                                    <%= description %>
                                </td>

                                <td>
                                    <%
                                        if (aLink.getUsage() == ComTrackableUserFormLink.TRACKABLE_NO) {
                                    %>
                                    <bean:message key="NotTrackedLink" />
                                    <%
                                    } else {
                                    %>
                                    <bean:message key="TrackedLink" />
                                    <%
                                    }
                                    %>
                                </td>

                                <td>
                                    <%
                                    if (aLink.getRelevance() == 0) {
                                    %>
                                    <bean:message key="Relevance_0" />
                                    <%
                                    } else if (aLink.getRelevance() == 1) {
                                    %>
                                    <bean:message key="Relevance_1" />
                                    <%
                                    } else if (aLink.getRelevance() == 2) {
                                    %>
                                    <bean:message key="Relevance_2" />
                                    <%
                                    }
                                    %>
                                </td>

                                <emm:ShowByPermission token="mailing.extend_trackable_links">
                                    <td>
                                        <%
                                        if (linkExtensionCount > 0) {
                                        %>
                                        <bean:message key="Yes" /> (<%= Integer.toString(linkExtensionCount) %>)
                                        <%
                                        } else {
                                        %>
                                        <bean:message key="No" />
                                        <%
                                        }
                                        %>
                                    </td>
                                </emm:ShowByPermission>

                                <td class="table-actions">
                                    <html:link titleKey="template.edit" styleClass="hidden js-row-show"
                                               page="/trackuserformlink.do?method=view&linkID=${link.id}&formID=${trackableUserFormLinkForm.formID}">
                                    </html:link>

                                    <a href="<%= StringEscapeUtils.escapeHtml(fullUrlWithExtensions) %>" class="btn btn-regular" target="_blank">
                                        <i class="icon icon-share-square-o"></i>
                                    </a>
                                </td>
                            </tr>
                        </logic:iterate>
                        </tbody>
                    </table>

                </div>
                <!-- Table Responsive END -->

            </div>
            <!-- Tile Content END -->

        </div>
        <!-- Tile END -->

        <div class="tile">
            <div class="tile-header">
                <a href="#" class="headline" data-toggle-tile="#tile-trackableLinkEditAll">
                    <i class="tile-toggle icon icon-angle-up"></i>
                    <bean:message key="TrackableLink.edit.all" />
                </a>
            </div>

            <div id="tile-trackableLinkEditAll" class="tile-content tile-content-forms">
                <agn:agnForm id="trackableUserFormLinkForm" action="/trackuserformlink" data-form="resource">
                    <html:hidden property="formID" />

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label for="linkExtension" class="control-label">
                                <bean:message key="TrackableLink.extendLinks"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <html:text property="linkExtension" maxlength="500" styleId="linkExtension" styleClass="form-control" />
                                </div>
                                <div class="input-group-btn">
                                    <a href="#" class="btn btn-regular" data-form-set="method: addExtensions" data-form-action="" data-tooltip="<bean:message key="AddProperties" />">
                                        <i class="icon icon-plus"></i>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>

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
                                        <th class="table-actions">
                                            <a href="#" class="btn btn-regular btn-alert" data-action="delete-all-links" data-tooltip="<bean:message key="ClearAllProperties"/>">
                                                <i class="icon icon-trash-o"></i>
                                            </a>
                                        </th>
                                    </thead>
                                    <tbody>
                                    <%
                                        ComTrackableUserFormLinkForm comTrackableUserFormLinkForm = (ComTrackableUserFormLinkForm) request.getAttribute("trackableUserFormLinkForm");
                                        List<LinkProperty> properties = comTrackableUserFormLinkForm.getCommonLinkExtensions();
                                        int propertyCount = 0;
                                        if (properties != null) {
                                            for (LinkProperty property : properties) {
                                                propertyCount++;
                                    %>
                                    <tr id="linkProperty_<%= Integer.toString(propertyCount) %>">
                                        <td>
                                            <input class="form-control" type="text" name="<%= ComTrackableUserFormLinkForm.PROPERTY_NAME_PREFIX + propertyCount %>" value="<%= property.getPropertyName() %>" />
                                        </td>
                                        <td>
                                            <input class="form-control" type="text" name="<%= ComTrackableUserFormLinkForm.PROPERTY_VALUE_PREFIX + propertyCount %>" value="<%= property.getPropertyValue() == null ? "" : property.getPropertyValue() %>" />
                                        </td>
                                        <td class="table-actions">
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
                                <a href="#" class="btn btn-regular" data-form-set="method: addDefaultExtensions" data-form-action="">
                                    <i class="icon icon-plus"></i>
                                    <span class="text"><bean:message key="AddDefaultProperties" /></span>
                                </a>

                                <a href="#" class="btn btn-regular btn-primary" data-action="add-extension">
                                    <i class="icon icon-plus"></i>
                                    <span class="text"><bean:message key="AddProperty" /></span>
                                </a>

                                <a href="#" class="btn btn-regular btn-primary pull-right" data-form-set="method: replaceCommonLinkLinkProperties" data-form-action="">
                                    <i class="icon icon-save"></i>
                                    <span class="text"><bean:message key="button.Save" /></span>
                                </a>
                            </div>
                        </div>
                    </div>

                    <div class="vspace-bottom-20"></div>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label for="trackable" class="control-label">
                                <bean:message key="DefaultLinkTracking" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <html:select property="trackable" styleId="trackable" styleClass="form-control">
                                        <html:option value="<%= Integer.toString(ComTrackableUserFormLink.TRACKABLE_NO) %>">
                                            <bean:message key="NotTrackedLink" />
                                        </html:option>
                                        <html:option value="<%= Integer.toString(ComTrackableUserFormLink.TRACKABLE_YES) %>">
                                            <bean:message key="TrackedLink" />
                                        </html:option>
                                    </html:select>
                                </div>
                                <div class="input-group-btn">
                                    <a href="#" class="btn btn-regular" data-form-set="method: setStandardUsage" data-form-action="" data-tooltip="<bean:message key="button.Save" />">
                                        <i class="icon icon-save"></i>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>

                </agn:agnForm>

            </div>

        </div>

    </div>
    <!-- Col END -->

</div>

<script id="link-table-row" type="text/x-mustache-template">
    <tr id="linkProperty_{{= count }}" data-link-name="{{= linkName }}" data-link-id="{{= count }}" >
        <td>
            <input class="form-control" type="text" id="<%= ComTrackableUserFormLinkForm.PROPERTY_NAME_PREFIX%>{{= count }}" name="<%= ComTrackableUserFormLinkForm.PROPERTY_NAME_PREFIX%>{{= count }}" value="{{= linkName }}"/>
        </td>
        <td>
            <input class="form-control" type="text" id="<%= ComTrackableUserFormLinkForm.PROPERTY_VALUE_PREFIX%>{{= count }}" name="<%= ComTrackableUserFormLinkForm.PROPERTY_VALUE_PREFIX%>{{= count }}" value="{{= linkValue }}"/>
        </td>
        <td class="table-actions">
            <a href="#" class="btn btn-regular btn-alert" data-tooltip="<bean:message key="button.Delete" />" data-action="delete-link" data-link-id="{{= count }}">
                <i class="icon icon-trash-o"></i>
            </a>
        </td>
    </tr>
</script>
