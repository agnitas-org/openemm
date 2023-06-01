<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.do" %>
<%@ page import="org.agnitas.beans.TrackableLink" %>
<%@ page import="org.agnitas.beans.BaseTrackableLink" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="tiles"   uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="SHOW_CREATE_SUBSTITUTE_LINK" type="java.lang.Boolean"--%>
<%--@elvariable id="isTrackingOnEveryPositionAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="originalUrls" type="java.util.Map<java.lang.Integer, java.lang.String>"--%>
<%--@elvariable id="notFormActions" type="java.util.List<org.agnitas.actions.EmmAction>"--%>
<%--@elvariable id="link" type="com.agnitas.emm.core.trackablelinks.form.TrackableLinkForm"--%>
<%--@elvariable id="defaultExtensions" type="java.util.List<com.agnitas.emm.core.trackablelinks.dto.ExtensionProperty>"--%>
<%--@elvariable id="paginatedTrackableLinks" type="org.agnitas.beans.impl.PaginatedListImpl<com.agnitas.emm.core.trackablelinks.form.TrackableLinkForm>"--%>

<c:set var="TRACKABLE_NONE"      value="<%= TrackableLink.TRACKABLE_NONE %>"/>
<c:set var="TRACKABLE_ONLY_TEXT" value="<%= TrackableLink.TRACKABLE_ONLY_TEXT %>"/>
<c:set var="TRACKABLE_ONLY_HTML" value="<%= TrackableLink.TRACKABLE_ONLY_HTML %>"/>
<c:set var="TRACKABLE_TEXT_HTML" value="<%= TrackableLink.TRACKABLE_TEXT_HTML %>"/>
<c:set var="KEEP_UNCHANGED"      value="<%= BaseTrackableLink.KEEP_UNCHANGED %>"/>
<c:set var="isMailingGrid"       value="${not empty gridTemplateId and gridTemplateId gt 0}" scope="request"/>

<mvc:form servletRelativeAction="/mailing/${mailingId}/trackablelink/list.action" id="trackableLinkForm" 
          modelAttribute="trackableLinksForm"
          data-form="search"
          data-controller="trackable-link-list-new"
          data-initializer="trackable-link-list">

    <script id="config:trackable-link-list" type="application/json">
        {
            "KEEP_UNCHANGED": ${KEEP_UNCHANGED},
            "SAVE_ALL_URL": "<c:url value='/mailing/${mailingId}/trackablelink/saveAll.action'/>",
            "defaultExtensions": ${emm:toJson(defaultExtensions)},
            "scrollToLinkId": ${scrollToLinkId}
        }
    </script>
    
    <tiles:insert page="/WEB-INF/jsp/mailing/template.jsp">
        <c:if test="${isMailingGrid}">
            <tiles:put name="header" type="string">
                <ul class="tile-header-nav">
                    <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
                </ul>
            </tiles:put>
        </c:if>    
    
        <tiles:put name="content" type="string">
            <c:if test="${isMailingGrid}">
                <div class="tile-content-padded">
            </c:if>
            <div data-form-content data-action="scroll-to">
            <div class="tile">
                <div class="tile-header">
                    <a href="#" class="headline" data-toggle-tile="#tile-trackableLinkEditOne">
                        <i class="tile-toggle icon icon-angle-up"></i>
                        <mvc:message code="TrackableLink.edit.one" />
                    </a>
                    <emm:ShowByPermission token="mailing.extend_trackable_links">
                    <ul class="tile-header-actions">
                        <li class="dropdown">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                <i class="icon icon-pencil"></i>
                                <span class="text"><mvc:message code="bulkAction"/></span>
                                <i class="icon icon-caret-down"></i>
                            </a>
        
                            <ul class="dropdown-menu">
                                <li>
                                    <a href="#" data-form-url="<c:url value='/mailing/${mailingId}/trackablelink/bulkActionsView.action'/>" data-form-confirm>     
                                        <span class="text"><mvc:message code="TrackableLink.edit"/></span>
                                    </a>
                                </li>
                                <li>
                                    <a href="#" data-form-url="<c:url value='/mailing/${mailingId}/trackablelink/confirmBulkClearExtensions.action'/>" data-form-confirm>     
                                        <span class="text"><mvc:message code="ClearAllProperties"/></span>
                                    </a>
                                </li>
                            </ul>
                        </li>
                    </ul>
                    </emm:ShowByPermission>
                </div>
                <div id="tile-trackableLinkEditOne" class="tile-content">
                    <div class="table-wrapper">
                        <c:set var="deepTrackingTitle">
                            <mvc:message code="stat.impression.retargeting"/>
                            <button class="icon icon-help" data-help="help_${helplanguage}/mailing/trackable_links/TrackingCookie.xml" tabindex="-1" type="button"></button>
                        </c:set>
                        <c:set var="rowIndex" value="0"/>
                        <display:table class="table table-bordered table-striped table-hover js-table"
                                       id="link"
                                       list="${paginatedTrackableLinks}"
                                       sort="external"
                                       excludedParams="*"
                                       requestURI="/mailing/${mailingId}/trackablelink/list.action"
                                       partialList="false"
                                       decorator="com.agnitas.emm.core.trackablelinks.web.TrackableLinkListDecorator">
                            
                            <display:column title="<input type='checkbox' data-form-bulk='bulkIds'/>" class="js-checkable" sortable="false" headerClass="squeeze-column">
                                <mvc:hidden path="links[${rowIndex}].id"/>
                                <mvc:checkbox path="bulkIds" value="${link.id}"/>
                            </display:column>
        
                            <display:column headerClass="js-table-sort" titleKey="URL" sortable="true" sortProperty="fullUrlWithExtensions">
                                <span class="multiline-sm-400 multiline-min-sm-150">
                                    <c:set var="fullLinkURL" value="${emm:getFullUrlWithDtoExtensions(link.url, link.extensions)}"/>
                                    <a href="${fullLinkURL}" class="badge icon icon-share-square-o" target="_blank"></a>
                                    <c:if test="${not empty originalUrls[link.id]}">
                                        <span class="badge badge-alert">
                                            <mvc:message code="mailing.trackablelinks.url_changed" />
                                        </span>
                                    </c:if>
                                    <c:if test="${not empty link}">
                                        ${fullLinkURL}
                                    </c:if>
                                </span>
                            </display:column>
        
                            <display:column headerClass="js-table-sort" class="align-top" titleKey="Description" sortable="true" sortProperty="description">
                                <mvc:text path="links[${rowIndex}].shortname" cssClass="form-control"/>
                            </display:column>
        
                            <display:column class="align-top" titleKey="LinkTracking" sortable="false">
                                <c:choose>
                                    <c:when test="${fn:contains(link.url, '##')}">
                                        <mvc:message code="Text_and_HTML_Version" />
                                    </c:when>
                                    <c:otherwise>
                                        <mvc:select path="links[${rowIndex}].usage" cssClass="form-control">
                                            <mvc:option value="${TRACKABLE_NONE}"><mvc:message code="mailing.Not_Trackable" /></mvc:option>
                                            <mvc:option value="${TRACKABLE_ONLY_TEXT}"><mvc:message code="Only_Text_Version" /></mvc:option>
                                            <mvc:option value="${TRACKABLE_ONLY_HTML}"><mvc:message code="Only_HTML_Version" /></mvc:option>
                                            <mvc:option value="${TRACKABLE_TEXT_HTML}"><mvc:message code="Text_and_HTML_Version" /></mvc:option>
                                        </mvc:select>
                                    </c:otherwise>
                                </c:choose>
                            </display:column>
        
                            <display:column class="align-top" titleKey="action.Action" sortable="false">
                                <mvc:select path="links[${rowIndex}].action" cssClass="form-control js-select">
                                    <mvc:option value="0"><mvc:message code="settings.No_Action" /></mvc:option>
                                    <c:forEach var="action" items="${notFormActions}">
                                        <mvc:option value="${action.id}">${action.shortname}</mvc:option>
                                    </c:forEach>
                                </mvc:select>
                            </display:column>
        
                            <display:column class="js-checkable align-center" titleKey="AdminLink" sortable="false">
                                <label class="toggle">
                                    <mvc:checkbox path="links[${rowIndex}].admin"/>
                                    <div class="toggle-control"></div>
                                </label>
                            </display:column>
                            
                            <c:if test="${SHOW_CREATE_SUBSTITUTE_LINK}">
                                <display:column class="js-checkable align-center" titleKey="CreateSubstituteLink" sortable="false">
                                   <label class="toggle">
                                       <mvc:checkbox path="links[${rowIndex}].createSubstituteForAgnDynMulti"/>
                                       <div class="toggle-control"></div>
                                   </label>
                                </display:column>
                            </c:if>
                            
                            <display:column class="align-top" sortable="false" title="${deepTrackingTitle}">
                                <mvc:select path="links[${rowIndex}].deepTracking" cssClass="form-control">
                                    <mvc:option value="0"><mvc:message code="TrackableLink.deepTrack.non" /></mvc:option>
                                    <mvc:option value="1"><mvc:message code="TrackableLink.deepTrack.cookie" /></mvc:option>
                                </mvc:select>
                            </display:column>
        
                            <emm:ShowByPermission token="mailing.extend_trackable_links">
                                <display:column class="align-top" titleKey="mailing.extend_trackable_link" sortable="false">
                                    <c:set var="extensionCount" value="${not empty link.extensions ? link.extensions.size() : 0}"/>
                                    <c:choose>
                                        <c:when test="${extensionCount > 0}">
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
                                </display:column>
                            </emm:ShowByPermission>
        
                            <display:column class="hidden" headerClass="hidden">
                                <a href="<c:url value="/mailing/${mailingId}/trackablelink/${link.id}/view.action"/>" class="hidden js-row-show"></a>
                            </display:column>
                            <c:set var="rowIndex" value="${rowIndex + 1}"/>
                        </display:table>
                    </div>
                </div>
            </div>
            
            <div class="tile">
                <div class="tile-header">
                    <a href="#" class="headline" data-toggle-tile="#tile-trackableLinkEditAll">
                        <i class="tile-toggle icon icon-angle-up"></i>
                        <mvc:message code="default.settings" />
                    </a>
                </div>
        
                <div id="tile-trackableLinkEditAll" class="tile-content tile-content-forms">
                    <%@include file="fragments/settings/link-extensions.jspf" %>
        
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label">
                                <mvc:message code="EveryPositionLink" />
                                <button class="icon icon-help" data-help="help_${helplanguage}/mailing/trackable_links/TrackEveryPosition.xml" tabindex="-1" type="button"></button>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <button type="button" class="btn btn-regular btn-primary" data-form-submit
                                ${isTrackingOnEveryPositionAvailable ? '' : "disabled='disabled'"}
                                    data-form-url="<c:url value="/mailing/${mailingId}/trackablelink/activateTrackingLinksOnEveryPosition.action"/>">
                                <span><mvc:message code="button.Activate" /></span>
                            </button>
                        </div>
                    </div>
        
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label for="openActionId" class="control-label">
                                <mvc:message code="mailing.OpenAction" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <mvc:select path="openActionId" cssClass="form-control js-select" id="openActionId">
                                <mvc:option value="0"><mvc:message code="settings.No_Action" /></mvc:option>
                                <c:forEach var="action" items="${notFormActions}">
                                    <mvc:option value="${action.id}">${action.shortname}</mvc:option>
                                </c:forEach>
                            </mvc:select>
                        </div>
                    </div>
        
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label for="clickActionId" class="control-label">
                                <mvc:message code="mailing.ClickAction" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <mvc:select path="clickActionId" cssClass="form-control js-select" id="clickActionId">
                                <mvc:option value="0">
                                    <mvc:message code="settings.No_Action" />
                                </mvc:option>
                                <c:forEach var="action" items="${notFormActions}">
                                    <mvc:option value="${action.id}">${action.shortname}</mvc:option>
                                </c:forEach>
                            </mvc:select>
                        </div>
                    </div>
        
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="intelliAdIdString">
                                <mvc:message code="mailing.intelliad.enable" />
                            </label>
                        </div>
        
                        <div class="col-sm-8">
                            <label class="toggle">
                                <mvc:checkbox path="intelliAdEnabled" id="intelliAdEnabled" />
                                <div class="toggle-control"></div>
                            </label>
                        </div>
                    </div>
        
                    <div class="form-group" data-show-by-checkbox="#intelliAdEnabled">
                        <div class="col-sm-4">
                            <label class="control-label" for="intelliAdIdString">
                                <mvc:message code="mailing.intelliad.idstring" />
                            </label>
                        </div>
        
                        <div class="col-sm-8">
                            <mvc:text path="intelliAdIdString" maxlength="500" id="intelliAdIdString" cssClass="form-control" />
                        </div>
                    </div>
                </div>
            </div>
            </div>
            <c:if test="${isMailingGrid}">
                </div>
            </c:if>
        </tiles:put>
        
        <tiles:putList name="footerItems">
            <tiles:add>
                <button type="button" class="btn btn-large btn-primary pull-right"
                        data-form-target="#trackableLinkForm"
                        data-form-set="everyPositionLink: false"
                        data-action="save">
                    <span class="text"><mvc:message code="button.Save"/></span>
                </button>
            </tiles:add>
        </tiles:putList>
    </tiles:insert>
</mvc:form>
