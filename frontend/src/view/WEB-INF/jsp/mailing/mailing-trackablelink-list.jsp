<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComMailingContentForm"%>
<%@ page import="com.agnitas.web.ComTrackableLinkAction"%>
<%@ page import="org.agnitas.actions.EmmAction" %>
<%@ page import="org.agnitas.beans.BaseTrackableLink" %>
<%@ page import="org.agnitas.beans.TrackableLink" %>
<%@ page import="org.agnitas.web.MailingSendAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax"%>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="notFormActions" type="java.util.List<org.agnitas.actions.EmmAction>"--%>
<%--@elvariable id="trackableLinkForm" type="com.agnitas.web.ComTrackableLinkForm"--%>
<%--@elvariable id="templateId" type="java.lang.Integer"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<c:set var="PROPERTY_NAME_PREFIX" value="<%= ComMailingContentForm.PROPERTY_NAME_PREFIX %>"/>
<c:set var="PROPERTY_VALUE_PREFIX" value="<%= ComMailingContentForm.PROPERTY_VALUE_PREFIX %>"/>

<c:set var="ACTION_SET_EXTEND_LINKS" value="<%= ComTrackableLinkAction.ACTION_SET_EXTEND_LINKS %>"/>
<c:set var="EXTEND_LINK_ACTION" value="<%= ComTrackableLinkAction.ACTION_LIST %>"/>
<c:set var="ACTION_SAVE_ADMIN_LINKS" value="<%= ComTrackableLinkAction.ACTION_SAVE_ADMIN_LINKS %>"/>
<c:set var="ACTION_SAVE_ALL" value="<%= ComTrackableLinkAction.ACTION_SAVE_ALL %>"/>
<c:set var="ACTION_VIEW" value="<%= ComTrackableLinkAction.ACTION_VIEW %>"/>
<c:set var="ACTION_SET_STANDARD_ACTION" value="<%= ComTrackableLinkAction.ACTION_SET_STANDARD_ACTION %>"/>
<c:set var="GLOBAL_USAGE_ACTION" value="<%= ComTrackableLinkAction.ACTION_GLOBAL_USAGE %>"/>
<c:set var="GLOBAL_RELEVANCE_ACTION" value="<%= ComTrackableLinkAction.ACTION_GLOBAL_RELEVANCE %>"/>
<c:set var="TYPE_FORM" value="<%= EmmAction.TYPE_FORM %>"/>
<c:set var="KEEP_UNCHANGED" value="<%= BaseTrackableLink.KEEP_UNCHANGED %>"/>
<c:set var="ACTION_DELETE_GLOBAL_AND_INDIVIDUAL_EXTENSION" value="<%= ComTrackableLinkAction.ACTION_DELETE_GLOBAL_AND_INDIVIDUAL_EXTENSION %>" />
<c:set var="ACTION_PREVIEW_SELECT" value="<%= MailingSendAction.ACTION_PREVIEW_SELECT %>"/>

<c:set var="TRACKABLE_NONE" value="<%= TrackableLink.TRACKABLE_NONE %>"/>
<c:set var="TRACKABLE_ONLY_TEXT" value="<%= TrackableLink.TRACKABLE_ONLY_TEXT %>"/>
<c:set var="TRACKABLE_ONLY_HTML" value="<%= TrackableLink.TRACKABLE_ONLY_HTML %>"/>
<c:set var="TRACKABLE_TEXT_HTML" value="<%= TrackableLink.TRACKABLE_TEXT_HTML %>"/>

<%--@elvariable id="defaultExtensions" type="java.lang.String"--%>

<script type="text/javascript">
    AGN.Opt.DefaultExtensions = ${empty defaultExtensions ? '{}' : defaultExtensions};
</script>

<tiles:insert page="template.jsp">
    <tiles:put name="header" type="string">
        <ul class="tile-header-nav">
            <!-- Tabs BEGIN -->
            <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
            <!-- Tabs END -->
        </ul>
    </tiles:put>

    <tiles:putList name="footerItems">
        <tiles:add>
            <button type="button" class="btn btn-large btn-primary pull-right"
                    data-form-target='#trackableLinkForm'
                    data-form-set='everyPositionLink: false'
                    data-form-action="${ACTION_SAVE_ALL}">

                <span class="text">
                    <bean:message key="button.Save"/>
                </span>
                    <%--<i class="icon icon-save"></i>--%>
            </button>
        </tiles:add>
    </tiles:putList>

    <tiles:put name="content" type="string">
        <c:if test="${trackableLinkForm.isMailingGrid}">
            <div class="tile-content-padded">
        </c:if>

        <div class="row">
            <div class="col-xs-12 row-1-1"
                 data-view-block="col-xs-12 row-1-1"
                 data-view-split="col-md-6"
                 data-view-hidden="col-xs-12 row-1-1"
                 data-controller="trackable-link-list">

                <agn:agnForm id="trackableLinkForm" action="/tracklink" data-form="search">
                    <html:hidden property="mailingID" />
                    <html:hidden property="action" />
                    <html:hidden property="defaultActionType" />
                    <html:hidden property="everyPositionLink" />

                    <div data-form-content data-action="scroll-to">

                        <div class="tile">
                            <div class="tile-header">
                                <a href="#" class="headline" data-toggle-tile="#tile-trackableLinkEditOne">
                                    <i class="tile-toggle icon icon-angle-up"></i>
                                    <bean:message key="TrackableLink.edit.one" />
                                </a>
								<emm:ShowByPermission token="mailing.extend_trackable_links">
					              <ul class="tile-header-actions">
					                  <li>
					                      <button type="button" class="btn btn-regular btn-alert" data-action="delete-all-links" data-form-action="${ACTION_DELETE_GLOBAL_AND_INDIVIDUAL_EXTENSION}">
					                          <i class="icon icon-trash-o"></i>
					                          <span class="text"><bean:message key="ClearAllProperties"/></span>
					                      </button>
					                  </li>
					              </ul>
								</emm:ShowByPermission>
                            </div>
                            <div id="tile-trackableLinkEditOne" class="tile-content">
                                <div class="table-responsive">
                                    <table class="table table-bordered table-striped table-hover table-form js-table">
                                        <thead>
                                        <th class="js-checkable">
                                            <input type='checkbox' data-form-bulk='bulkID'/>
                                        </th>
                                        <th><bean:message key="URL" /></th>
                                        <th><bean:message key="Description" /></th>
                                        <th><bean:message key="Trackable" /> <button class="icon icon-help" data-help="help_${helplanguage}/mailing/trackable_links/TrackingCookie.xml" tabindex="-1" type="button"></button></th>
                                        <th><bean:message key="action.Action" /></th>
                                        <th class="js-checkable"><bean:message key="AdminLink" /></th>
                                        <th><bean:message key="deeptracking" /></th>

                                        <emm:ShowByPermission token="mailing.extend_trackable_links">
                                            <th class="js-checkable">
                                                <input type="checkbox" data-form-bulk='extendLinkUrl_' /> <bean:message key="mailing.extend_trackable_link" />
                                            </th>
                                            <c:set var="EXTEND_LINK_ACTION" value="${ACTION_SET_EXTEND_LINKS}" scope="page" />
                                        </emm:ShowByPermission>

                                        <th class="hidden"></th>
                                        </thead>
                                        <tbody>
                                        <logic:iterate id="link" name="trackableLinkForm" property="links" indexId="index">
                                            <c:set var="linkExtensionCount" value="0"/>
                                            <c:set var="fullUrlWithExtensions" value="0"/>

                                            <c:if test="${not empty link}">
                                                <c:set var="linkExtensionCount" value="${link.linkExtensionCount}"/>
                                                <c:set var="fullUrlWithExtensions" value="${link.fullUrlWithExtensions}"/>
                                            </c:if>

                                            <tr id="link-${link.id}" data-action="elem-edited"
                                                <c:if test="${link.id eq trackableLinkForm.scrollToLinkId}">
                                                    data-sizing="scroll-top-target"
                                                </c:if>
                                                    >
                                                <td class="js-checkable align-top">
                                                    <agn:agnCheckbox property="bulkID[${link.id}]"/>
                                                </td>
                                                <td class="align-top">
                                                <span class="multiline-sm-400 multiline-min-sm-150">
                                                <a href="${fullUrlWithExtensions}" class="badge icon icon-share-square-o" target="_blank">
                                                </a>
                                                <c:if test="${link.urlModified}">
                                                    <span class="badge badge-alert">
                                                        <bean:message key="mailing.trackablelinks.url_changed" />
                                                    </span>
                                                </c:if>
                                                ${fullUrlWithExtensions}
                                                </span>
                                                </td>

                                                <td class="align-top">
                                                    <agn:agnText property="linkItemName[${link.id}]" styleClass="form-control"/>
                                                </td>

                                                <td class="align-top">
                                                    <agn:agnSelect property="linkItemTrackable[${link.id}]" styleClass="form-control">
                                                        <agn:agnOption value="${TRACKABLE_NONE}"><bean:message key="Not_Trackable" /></agn:agnOption>
                                                        <agn:agnOption value="${TRACKABLE_ONLY_TEXT}"><bean:message key="Only_Text_Version" /></agn:agnOption>
                                                        <agn:agnOption value="${TRACKABLE_ONLY_HTML}"><bean:message key="Only_HTML_Version" /></agn:agnOption>
                                                        <agn:agnOption value="${TRACKABLE_TEXT_HTML}"><bean:message key="Text_and_HTML_Version" /></agn:agnOption>
                                                    </agn:agnSelect>
                                                </td>

                                                <td class="align-top">
                                                    <agn:agnSelect property="linkItemAction[${link.id}]" styleClass="form-control js-select">
                                                        <agn:agnOption value="0"><bean:message key="settings.No_Action" /></agn:agnOption>
                                                        <logic:iterate id="action" name="notFormActions" scope="request">
                                                            <agn:agnOption value="${action.id}">${action.shortname}</agn:agnOption>
                                                        </logic:iterate>
                                                    </agn:agnSelect>
                                                </td>

                                                <td class="js-checkable align-top">
                                                    <agn:agnCheckbox property="adminLink[${link.id}]"/>
                                                </td>

                                                <td class="align-top">
                                                    <agn:agnSelect property="linkItemDeepTracking[${link.id}]" styleClass="form-control">
                                                        <agn:agnOption value="0"><bean:message key="No" /></agn:agnOption>
                                                        <agn:agnOption value="1"><bean:message key="TrackableLink.deepTrack.cookie" /></agn:agnOption>
                                                        <agn:agnOption value="2"><bean:message key="TrackableLink.deepTrack.url" /></agn:agnOption>
                                                        <agn:agnOption value="3"><bean:message key="TrackableLink.deepTrack.both" /></agn:agnOption>
                                                    </agn:agnSelect>
                                                </td>
												<emm:ShowByPermission token="mailing.extend_trackable_links">
	                                                <td class="align-top">
	                                                    <c:choose>
	                                                        <c:when test="${linkExtensionCount > 0}">
	                                                            <span class="badge badge-success">
	                                                                <bean:message key="default.Yes" /> (${linkExtensionCount})
	                                                            </span>
	                                                        </c:when>
	                                                        <c:otherwise>
	                                                            <span class="badge">
	                                                                <bean:message key="No" />
	                                                            </span>
	                                                        </c:otherwise>
	                                                    </c:choose>
	                                                </td>
												</emm:ShowByPermission>
                                                <td class="table-actions hidden">
                                                    <c:set var="editMessage">
                                                        <bean:message key="button.Edit"/>
                                                    </c:set>

                                                    <c:choose>
                                                        <c:when test="${trackableLinkForm.isMailingGrid}">
                                                            <agn:agnLink page="/tracklink.do?action=${ACTION_VIEW}&linkID=${link.id}&mailingID=${link.mailingID}&isMailingGrid=true&templateId=${templateId}"
                                                                         styleClass="hidden js-row-show" />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <agn:agnLink page="/tracklink.do?action=${ACTION_VIEW}&linkID=${link.id}&mailingID=${link.mailingID}" styleClass="hidden js-row-show" />
                                                        </c:otherwise>
                                                    </c:choose>


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
                                    <bean:message key="TrackableLink.edit.all" />:
                                </a>
                            </div>

                            <div id="tile-trackableLinkEditAll" class="tile-content tile-content-forms" data-action="elem-edited">

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
                                                            <c:set var="properties" value="${trackableLinkForm.commonLinkExtensions}"/>
                                                            <c:if test="${not empty properties}">
                                                                <c:forEach var="property" items="${properties}" varStatus="loopStatus">
                                                                    <c:set var="propertyIndex" value="${loopStatus.index + 1}"/>

                                                                    <c:set var="propertyNameId" value="${PROPERTY_NAME_PREFIX}${propertyIndex}"/>
                                                                    <c:set var="propertyValueId" value="${PROPERTY_VALUE_PREFIX}${propertyIndex}"/>

                                                                    <tr id="linkProperty_${propertyIndex}">
                                                                        <td>
                                                                            <input class="form-control" type="text" id="${propertyNameId}" name="${propertyNameId}" value="${property.propertyName}"/>
                                                                        </td>
                                                                        <td>
                                                                            <input class="form-control" type="text" id="${propertyValueId}" name="${propertyValueId}" value="${property.propertyValue ne null ? property.propertyValue : ""}"/>
                                                                        </td>
                                                                        <td class="table-actions">
                                                                            <a href="#" class="btn btn-regular btn-alert" data-tooltip="<bean:message key="button.Delete" />" data-action="delete-link" data-link-id="${propertyIndex}">
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
                                                            <c:if test="${trackableLinkForm.companyHasDefaultLinkExtension}">
                                                                <div class="col-sm-12 col-md-4">
                                                                    <a href="#" onclick="return false" data-action="add-default-extensions" class="btn btn-regular btn-block">
                                                                        <i class="icon icon-plus"></i>
                                                                        <span class="text"><bean:message key="AddDefaultProperties" /></span>
                                                                    </a>
                                                                </div>
                                                            </c:if>
                                                            <div class="col-sm-12 col-md-4">
                                                                <a href="#" onclick="return false" class="btn btn-regular btn-block btn-primary" data-action="add-extension">
                                                                    <i class="icon icon-plus"></i>
                                                                    <span class="text"><bean:message key="AddProperty" /></span>
                                                                </a>
                                                            </div>

                                                            <div class="col-sm-12 col-md-4">
                                                                <a href="#" onclick="return false" class="btn btn-regular btn-block btn-alert" data-action="delete-all-links">
                                                                    <i class="icon icon-trash-o"></i>
                                                                    <span class="text"><bean:message key="mailing.trackablelinks.clearPropertiesTable"/></span>
                                                                </a>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <div class="col-sm-4">
                                                <label for="keepExtensionsUnchanged" class="control-label">
                                                    <bean:message key="KeepLinkExtensionsUnchanged"/>
                                                </label>
                                            </div>
                                            <div class="col-sm-8">
                                                <label class="toggle">
                                                    <agn:agnCheckbox property="keepExtensionsUnchanged"
                                                                     styleId="keepExtensionsUnchanged"/>
                                                    <div class="toggle-control"></div>
                                                </label>
                                            </div>
                                        </div>
                                </emm:ShowByPermission>

                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label for="globalUsage" class="control-label">
                                            <bean:message key="Trackable" />
                                        </label>
                                    </div>
                                    <div class="col-sm-8">
                                        <html:select property="globalUsage" styleId="globalUsage" styleClass="form-control" value="${KEEP_UNCHANGED}">
                                            <html:option value="${KEEP_UNCHANGED}">
                                                <bean:message key="KeepUnchanged"/>
                                            </html:option>
                                            <html:option value="0">
                                                <bean:message key="Not_Trackable" />
                                            </html:option>
                                            <html:option value="1">
                                                <bean:message key="Only_Text_Version" />
                                            </html:option>
                                            <html:option value="2">
                                                <bean:message key="Only_HTML_Version" />
                                            </html:option>
                                            <html:option value="3">
                                                <bean:message key="Text_and_HTML_Version" />
                                            </html:option>
                                        </html:select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label for="globalRelevance" class="control-label">
                                            <bean:message key="Relevance" />
                                        </label>
                                    </div>
                                    <div class="col-sm-8">
                                        <html:select property="globalRelevance" styleId="globalRelevance" styleClass="form-control" value="${KEEP_UNCHANGED}">
                                            <html:option value="${KEEP_UNCHANGED}">
                                                <bean:message key="KeepUnchanged" />
                                            </html:option>
                                            <html:option value="0">
                                                <bean:message key="Relevance_0" />
                                            </html:option>
                                            <html:option value="1">
                                                <bean:message key="Relevance_1" />
                                            </html:option>
                                            <html:option value="2">
                                                <bean:message key="Relevance_2" />
                                            </html:option>
                                        </html:select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label class="control-label">
                                            <bean:message key="EveryPositionLink" />
                                            <button class="icon icon-help" data-help="help_${helplanguage}/mailing/trackable_links/TrackEveryPosition.xml" tabindex="-1" type="button"></button>
                                        </label>
                                    </div>
                                    <div class="col-sm-8">
                                        <button type="button" class="btn btn-regular btn-primary"
                                                data-form-set="everyPositionLink: true"
                                                data-form-action="${ACTION_SAVE_ALL}">
                                            <span><bean:message key="button.Activate" /></span>
                                        </button>
                                    </div>
                                </div>

                                    <div class="form-group">
                                        <div class="col-sm-4">
                                            <label for="linkAction" class="control-label">
                                                <bean:message key="DefaultAction" />
                                            </label>
                                        </div>
                                        <div class="col-sm-8">
                                            <html:select property="linkAction" styleClass="form-control js-select" styleId="linkAction" value="${KEEP_UNCHANGED}">
                                                <html:option value="${KEEP_UNCHANGED}">
                                                    <bean:message key="KeepUnchanged" />
                                                </html:option>
                                                <html:option value="0">
                                                    <bean:message key="settings.No_Action" />
                                                </html:option>
                                                <logic:iterate id="action" name="notFormActions" scope="request">
                                                    <html:option value="${action.id}">${action.shortname}</html:option>
                                                </logic:iterate>
                                            </html:select>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <div class="col-sm-4">
                                            <label for="openActionID" class="control-label">
                                                <bean:message key="mailing.OpenAction" />
                                            </label>
                                        </div>
                                        <div class="col-sm-8">
                                            <html:select property="openActionID" styleClass="form-control js-select" styleId="openActionID">
                                                <html:option value="0">
                                                    <bean:message key="settings.No_Action" />
                                                </html:option>
                                                <logic:iterate id="action" name="notFormActions" scope="request">
                                                    <html:option value="${action.id}">${action.shortname}</html:option>
                                                </logic:iterate>
                                            </html:select>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <div class="col-sm-4">
                                            <label for="clickActionID" class="control-label">
                                                <bean:message key="mailing.ClickAction" />
                                            </label>
                                        </div>
                                        <div class="col-sm-8">
                                            <html:select property="clickActionID" styleClass="form-control js-select" styleId="clickActionID">
                                                <html:option value="0">
                                                    <bean:message key="settings.No_Action" />
                                                </html:option>
                                                <logic:iterate id="action" name="notFormActions" scope="request">
                                                    <html:option value="${action.id}">${action.shortname}</html:option>
                                                </logic:iterate>
                                            </html:select>
                                        </div>
                                    </div>

								<%@include file="mailing-trackablelink-list-revenue.jspf" %>

                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label class="control-label" for="intelliAdIdString">
                                            <bean:message key="mailing.intelliad.enable" />
                                        </label>
                                    </div>

                                    <div class="col-sm-8">
                                        <input type="hidden" name="intelliAdShown" value="true" />
                                        <label class="toggle">
                                            <html:checkbox property="intelliAdEnabled" styleId="intelliAdEnabled" />
                                            <div class="toggle-control"></div>
                                        </label>
                                    </div>
                                </div>

                                <div class="form-group" data-show-by-checkbox="#intelliAdEnabled">
                                    <div class="col-sm-4">
                                        <label class="control-label" for="intelliAdIdString">
                                            <bean:message key="mailing.intelliad.idstring" />
                                        </label>
                                    </div>

                                    <div class="col-sm-8">
                                        <html:text property="intelliAdIdString" maxlength="500" styleId="intelliAdIdString" styleClass="form-control" />
                                    </div>
                                </div>

                            </div>
                            <!-- Tile Content END -->

                        </div>
                        <!-- Tile END -->

                    </div>

                </agn:agnForm>
            </div>
            <!-- Col END -->

            <emm:ShowByPermission token="mailing.send.show">
                <%--@elvariable id="mailingBaseForm" type="org.agnitas.web.forms.MailingBaseForm"--%>
                <c:url var="onLoadUrl" value="/mailingsend.do">
                    <c:param name="action" value="${ACTION_PREVIEW_SELECT}"/>
                    <c:param name="mailingID" value="${mailingBaseForm.mailingID}"/>
                    <c:param name="previewSelectPure" value="true"/>
                </c:url>
                <div class="hidden" data-view-block="col-xs-12" data-view-split="col-md-6" data-view-hidden="hidden">
                    <div data-load="${onLoadUrl}" data-load-target="#preview"></div>
                </div>
            </emm:ShowByPermission>
        </div>

        <c:if test="${trackableLinkForm.isMailingGrid}">
            </div>
        </c:if>
    </tiles:put>
</tiles:insert>



<script id="link-table-row" type="text/x-mustache-template">
    <tr id="linkProperty_{{= count }}" data-link-name="{{= linkName }}" data-link-id="{{= count }}" >
        <td>
            <input class="form-control" type="text" id="${PROPERTY_NAME_PREFIX}{{= count }}" name="${PROPERTY_NAME_PREFIX}{{= count }}" value="{{= linkName }}"/>
        </td>
        <td>
            <input class="form-control" type="text" id="${PROPERTY_VALUE_PREFIX}{{= count }}" name="${PROPERTY_VALUE_PREFIX}{{= count }}" value="{{= linkValue }}"/>
        </td>
        <td class="table-actions">
            <a href="#" class="btn btn-regular btn-alert" data-tooltip="<bean:message key="button.Delete" />" data-action="delete-link" data-link-id="{{= count }}">
                <i class="icon icon-trash-o"></i>
            </a>
        </td>
    </tr>
</script>
