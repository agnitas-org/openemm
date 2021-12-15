<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComMailingContentForm"%>
<%@ page import="com.agnitas.web.ComTrackableLinkAction"%>
<%@ page import="org.agnitas.actions.EmmAction" %>
<%@ page import="org.agnitas.beans.TrackableLink" %>
<%@ page import="org.agnitas.web.MailingSendAction" %>
<%@ page import="org.agnitas.beans.BaseTrackableLink" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>

<%--@elvariable id="notFormActions" type="java.util.List<org.agnitas.actions.EmmAction>"--%>
<%--@elvariable id="trackableLinkForm" type="com.agnitas.web.ComTrackableLinkForm"--%>
<%--@elvariable id="isTrackingOnEveryPositionAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="templateId" type="java.lang.Integer"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="trackableLinksList" type="java.util.List<com.agnitas.beans.ComTrackableLink>"--%>
<%--@elvariable id="paginatedTrackableLinks" type="org.agnitas.beans.impl.PaginatedListImpl"--%>

<c:set var="PROPERTY_NAME_PREFIX" value="<%= ComMailingContentForm.PROPERTY_NAME_PREFIX %>"/>
<c:set var="PROPERTY_VALUE_PREFIX" value="<%= ComMailingContentForm.PROPERTY_VALUE_PREFIX %>"/>

<c:set var="ACTION_SET_EXTEND_LINKS" value="<%= ComTrackableLinkAction.ACTION_SET_EXTEND_LINKS %>"/>
<c:set var="EXTEND_LINK_ACTION" value="<%= ComTrackableLinkAction.ACTION_LIST %>"/>
<c:set var="ACTION_SAVE_ALL" value="<%= ComTrackableLinkAction.ACTION_SAVE_ALL %>"/>
<c:set var="ACTION_VIEW" value="<%= ComTrackableLinkAction.ACTION_VIEW %>"/>
<c:set var="ACTION_LIST" value="<%= ComTrackableLinkAction.ACTION_LIST %>"/>
<c:set var="ACTION_SET_STANDARD_ACTION" value="<%= ComTrackableLinkAction.ACTION_SET_STANDARD_ACTION %>"/>
<c:set var="GLOBAL_USAGE_ACTION" value="<%= ComTrackableLinkAction.ACTION_GLOBAL_USAGE %>"/>
<c:set var="TYPE_FORM" value="<%= EmmAction.TYPE_FORM %>"/>
<c:set var="ACTION_CONFIRM_BULK_CLEAR_EXTENSIONS" value="<%= ComTrackableLinkAction.ACTION_CONFIRM_BULK_CLEAR_EXTENSIONS %>" />
<c:set var="ACTION_SHOW_BULK_ACTIONS" value="<%= ComTrackableLinkAction.ACTION_SHOW_BULK_ACTIONS %>" />
<c:set var="ACTION_PREVIEW_SELECT" value="<%= MailingSendAction.ACTION_PREVIEW_SELECT %>"/>

<c:set var="KEEP_UNCHANGED" value="<%= BaseTrackableLink.KEEP_UNCHANGED %>"/>
<c:set var="TRACKABLE_NONE" value="<%= TrackableLink.TRACKABLE_NONE %>"/>
<c:set var="TRACKABLE_ONLY_TEXT" value="<%= TrackableLink.TRACKABLE_ONLY_TEXT %>"/>
<c:set var="TRACKABLE_ONLY_HTML" value="<%= TrackableLink.TRACKABLE_ONLY_HTML %>"/>
<c:set var="TRACKABLE_TEXT_HTML" value="<%= TrackableLink.TRACKABLE_TEXT_HTML %>"/>

<%--@elvariable id="defaultExtensions" type="java.lang.String"--%>

<script type="text/javascript">
    KEEP_UNCHANGED = ${KEEP_UNCHANGED};
    ACTION_SAVE_ALL = ${ACTION_SAVE_ALL};
    AGN.Opt.DefaultExtensions = ${emm:toJson(defaultExtensions)};
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
                    data-form-target="#trackableLinkForm"
                    data-form-set="everyPositionLink: false"
                    data-action="save">

                <span class="text">
                    <bean:message key="button.Save"/>
                </span>
            </button>
        </tiles:add>
    </tiles:putList>

    <tiles:put name="content" type="string">
        <c:set var="tileContent">
        <div class="row">
            <div class="col-xs-12 row-1-1"
                 data-view-block="col-xs-12 row-1-1"
                 data-view-split="col-md-6"
                 data-view-hidden="col-xs-12 row-1-1"
                 data-controller="trackable-link-list">
                <agn:agnForm id="trackableLinkForm" action="/tracklink.do" data-form="search">
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
                                    <li class="dropdown">
                                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                            <i class="icon icon-pencil"></i>
                                            <span class="text"><bean:message key="bulkAction"/></span>
                                            <i class="icon icon-caret-down"></i>
                                        </a>

                                        <ul class="dropdown-menu">
                                            <li>
                                                <a href="#" data-form-confirm="${ACTION_SHOW_BULK_ACTIONS}">
                                                    <span class="text"><bean:message key="TrackableLink.edit"/></span>
                                                </a>
                                            </li>
                                            <li>
                                                <a href="#" data-form-confirm="${ACTION_CONFIRM_BULK_CLEAR_EXTENSIONS}">
                                                    <span class="text"><bean:message key="ClearAllProperties"/></span>
                                                </a>
                                            </li>
                                        </ul>
                                    </li>
                                </ul>
                                </emm:ShowByPermission>
                            </div>
                            <div id="tile-trackableLinkEditOne" class="tile-content">
                                <div class="table-wrapper">
                                    <display:table class="table table-bordered table-striped table-hover js-table"
                                                   id="link"
                                                   list="${paginatedTrackableLinks}"
                                                   sort="external"
                                                   excludedParams="*"
                                                   requestURI="/tracklink.do?action=${ACTION_LIST}"
                                                   partialList="false"
                                                   decorator="com.agnitas.emm.core.trackablelinks.web.TrackableLinkDecorator">

                                        <!-- Prevent table controls/headers collapsing when the table is empty -->
                                        <display:setProperty name="basic.empty.showtable" value="true"/>

                                        <display:setProperty name="paging.banner.placement" value="bottom"/>
                                        <display:setProperty name="basic.msg.empty_list" value=""/>
                                        <display:setProperty name="paging.banner.no_items_found"><div></display:setProperty> <%-- HTML breaks without this tag--%>
                                        <display:setProperty name="basic.msg.empty_list_row" value=""/>


                                        <display:column class="js-checkable" sortable="false" title="<input type='checkbox' data-form-bulk='bulkID' data-action='select-link'/>">
                                            <agn:agnCheckbox property="bulkID[${link.id}]" data-action="select-link"/>
                                        </display:column>

                                        <display:column headerClass="js-table-sort" titleKey="URL" sortable="true" sortProperty="fullUrlWithExtensions">
                                            <span class="multiline-sm-400 multiline-min-sm-150">
                                                <a href="${link.fullUrlWithExtensions}" class="badge icon icon-share-square-o" target="_blank">
                                                </a>
                                                <c:if test="${link.urlModified}">
                                                    <span class="badge badge-alert">
                                                        <bean:message key="mailing.trackablelinks.url_changed" />
                                                    </span>
                                                </c:if>
                                                <c:if test="${not empty link}">
                                                    ${link.fullUrlWithExtensions}
                                                </c:if>
                                            </span>
                                        </display:column>

                                        <display:column headerClass="js-table-sort" class="align-top" titleKey="Description" sortable="true" sortProperty="description">
                                            <agn:agnText property="linkItemName[${link.id}]" styleClass="form-control"/>
                                        </display:column>

                                        <display:column class="align-top" titleKey="Trackable" sortable="false">
                                            <c:choose>
                                                <c:when test="${link.usage ge 0}">
                                                    <agn:agnSelect property="linkItemTrackable[${link.id}]" styleClass="form-control">
                                                        <agn:agnOption value="${TRACKABLE_NONE}"><bean:message key="mailing.Not_Trackable" /></agn:agnOption>
                                                        <agn:agnOption value="${TRACKABLE_ONLY_TEXT}"><bean:message key="Only_Text_Version" /></agn:agnOption>
                                                        <agn:agnOption value="${TRACKABLE_ONLY_HTML}"><bean:message key="Only_HTML_Version" /></agn:agnOption>
                                                        <agn:agnOption value="${TRACKABLE_TEXT_HTML}"><bean:message key="Text_and_HTML_Version" /></agn:agnOption>
                                                    </agn:agnSelect>
                                                </c:when>
                                                <c:otherwise>
                                                    <bean:message key="Text_and_HTML_Version" />
                                                </c:otherwise>
                                            </c:choose>
                                        </display:column>

                                        <display:column class="align-top" titleKey="action.Action" sortable="false">
                                            <agn:agnSelect property="linkItemAction[${link.id}]" styleClass="form-control js-select">
                                                <agn:agnOption value="0"><bean:message key="settings.No_Action" /></agn:agnOption>
                                                <logic:iterate id="action" name="notFormActions" scope="request">
                                                    <agn:agnOption value="${action.id}">${action.shortname}</agn:agnOption>
                                                </logic:iterate>
                                            </agn:agnSelect>
                                        </display:column>

                                        <display:column class="js-checkable align-center" titleKey="AdminLink" sortable="false">
                                            <label class="toggle">
                                                <agn:agnCheckbox property="adminLink[${link.id}]"/>
                                                <div class="toggle-control"></div>
                                            </label>
                                        </display:column>
                                        
                                        <c:if test="${SHOW_CREATE_SUBSTITUTE_LINK}">
                                        	<display:column class="js-checkable align-center" titleKey="CreateSubstituteLink" sortable="false">
	                                           <label class="toggle">
	                                               <agn:agnCheckbox property="createSubstituteLinkFor[${link.id}]"/>
	                                               <div class="toggle-control"></div>
	                                           </label>
                                        	</display:column>
                                        </c:if>

                                        <c:set var="deepTrackingTitle">
                                            <bean:message key="deeptracking"/>
                                            <button class="icon icon-help" data-help="help_${helplanguage}/mailing/trackable_links/TrackingCookie.xml" tabindex="-1" type="button"></button>
                                        </c:set>

                                        <display:column class="align-top" sortable="false" title="${deepTrackingTitle}">
                                            <agn:agnSelect property="linkItemDeepTracking[${link.id}]" styleClass="form-control">
                                                <agn:agnOption value="0"><bean:message key="TrackableLink.deepTrack.non" /></agn:agnOption>
                                                <agn:agnOption value="1"><bean:message key="TrackableLink.deepTrack.cookie" /></agn:agnOption>
                                            </agn:agnSelect>
                                        </display:column>

                                        <emm:ShowByPermission token="mailing.extend_trackable_links">
                                            <display:column class="align-top" titleKey="mailing.extend_trackable_link" sortable="false">
                                                <c:if test="${not empty link}">
                                                    <c:set var="linkExtensionCount" value="${link.linkExtensionCount}"/>
                                                </c:if>
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
                                            </display:column>
                                            <c:set var="EXTEND_LINK_ACTION" value="${ACTION_SET_EXTEND_LINKS}" scope="page" />
                                        </emm:ShowByPermission>

                                        <display:column class="hidden" headerClass="hidden">
                                            <c:choose>
                                                <c:when test="${trackableLinkForm.isMailingGrid}">
                                                    <c:url var="editSingleLink" value="/tracklink.do">
                                                        <c:param name="action" value="${ACTION_VIEW}"/>
                                                        <c:param name="linkID" value="${link.id}"/>
                                                        <c:param name="mailingID" value="${link.mailingID}"/>
                                                        <c:param name="isMailingGrid" value="true"/>
                                                        <c:param name="templateId" value="${templateId}"/>
                                                    </c:url>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:url var="editSingleLink" value="/tracklink.do">
                                                        <c:param name="action" value="${ACTION_VIEW}"/>
                                                        <c:param name="linkID" value="${link.id}"/>
                                                        <c:param name="mailingID" value="${link.mailingID}"/>
                                                    </c:url>
                                                </c:otherwise>
                                            </c:choose>
                                            <a href="${editSingleLink}" class="hidden js-row-show"></a>
                                        </display:column>

                                    </display:table>
                                </div>
                            </div>
                            <!-- Tile Content END -->
                        </div>
                        <!-- Tile END -->

                        <div class="tile">
                            <div class="tile-header">
                                <a href="#" class="headline" data-toggle-tile="#tile-trackableLinkEditAll">
                                    <i class="tile-toggle icon icon-angle-up"></i>
                                    <bean:message key="default.settings" />
                                </a>
                            </div>

                            <div id="tile-trackableLinkEditAll" class="tile-content tile-content-forms" data-action="elem-edited">
                                <%@include file="trackablelinks/fragments/settings/link-extensions.jspf" %>

                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label class="control-label">
                                            <bean:message key="EveryPositionLink" />
                                            <button class="icon icon-help" data-help="help_${helplanguage}/mailing/trackable_links/TrackEveryPosition.xml" tabindex="-1" type="button"></button>
                                        </label>
                                    </div>
                                    <div class="col-sm-8">
                                        <c:set var="everyPositionDisabled" value="disabled='disabled'"/>
                                        <c:if test="${isTrackingOnEveryPositionAvailable}">
                                            <c:set var="everyPositionDisabled" value=""/>
                                        </c:if>

                                        <button type="button" class="btn btn-regular btn-primary"
                                                data-form-set="everyPositionLink: true"
                                                data-form-action="${ACTION_SAVE_ALL}" ${everyPositionDisabled}>
                                            <span><bean:message key="button.Activate" /></span>
                                        </button>
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

            <emm:ShowByPermission token="mailing.send.show">
                <%--@elvariable id="mailingBaseForm" type="org.agnitas.web.forms.MailingBaseForm"--%>
                <c:url var="onLoadUrl" value="/mailingsend.do">
                    <c:param name="action" value="${ACTION_PREVIEW_SELECT}"/>
                    <c:param name="mailingID" value="${mailingBaseForm.mailingID}"/>
                    <c:param name="previewForm.pure" value="true"/>
                </c:url>
                <div class="hidden" data-view-block="col-xs-12" data-view-split="col-md-6" data-view-hidden="hidden">
                    <div data-load="${onLoadUrl}" data-load-target="#preview"></div>
                </div>
            </emm:ShowByPermission>

        </div>
    </c:set>


        <c:choose>
            <c:when test="${trackableLinkForm.isMailingGrid}">
                <div class="tile-content-padded">
                    ${tileContent}
                </div>
            </c:when>

            <c:otherwise>
                ${tileContent}
            </c:otherwise>
        </c:choose>
    </tiles:put>
</tiles:insert>

<script>
  (function(){
    $('#tile-trackableLinkEditOne').ready(function(){
      $('#tile-trackableLinkEditOne').find('tr').attr('data-action', 'elem-edited');
      $('#link-' + ${trackableLinkForm.scrollToLinkId}).attr('data-sizing', 'scroll-top-target');
    })
  })(jQuery)
</script>
