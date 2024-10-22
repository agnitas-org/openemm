<%@ page contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/errorRedesigned.action" %>
<%@page import="com.agnitas.emm.core.trackablelinks.common.LinkTrackingMode"%>
<%@ page import="org.agnitas.beans.BaseTrackableLink" %>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag" %>
<%@ taglib prefix="emm"        uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"        uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"         uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"          uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="SHOW_CREATE_SUBSTITUTE_LINK" type="java.lang.Boolean"--%>
<%--@elvariable id="isAutoDeeptrackingEnabled" type="java.lang.Boolean"--%>
<%--@elvariable id="isTrackingOnEveryPositionAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="originalUrls" type="java.util.Map<java.lang.Integer, java.lang.String>"--%>
<%--@elvariable id="notFormActions" type="java.util.List<org.agnitas.actions.EmmAction>"--%>
<%--@elvariable id="link" type="com.agnitas.emm.core.trackablelinks.form.TrackableLinkForm"--%>
<%--@elvariable id="trackableLinksForm" type="com.agnitas.emm.core.trackablelinks.form.TrackableLinksForm"--%>
<%--@elvariable id="defaultExtensions" type="java.util.List<com.agnitas.emm.core.trackablelinks.dto.ExtensionProperty>"--%>
<%--@elvariable id="paginatedTrackableLinks" type="org.agnitas.beans.impl.PaginatedListImpl<com.agnitas.emm.core.trackablelinks.form.TrackableLinkForm>"--%>

<c:set var="TRACKABLE_NONE"      value="<%= LinkTrackingMode.NONE.getMode() %>"/>
<c:set var="TRACKABLE_ONLY_TEXT" value="<%= LinkTrackingMode.TEXT_ONLY.getMode() %>"/>
<c:set var="TRACKABLE_ONLY_HTML" value="<%= LinkTrackingMode.HTML_ONLY.getMode() %>"/>
<c:set var="TRACKABLE_TEXT_HTML" value="<%= LinkTrackingMode.TEXT_AND_HTML.getMode() %>"/>
<c:set var="KEEP_UNCHANGED"      value="<%= BaseTrackableLink.KEEP_UNCHANGED %>"/>

<mvc:message var="descriptionMsg" code="Description" />

<c:set var="isSettingsReadonly" value="${param.settingsReadonly}"/>
<c:set var="controllerPath" value="${param.controllerPath}"/>

<c:set var="isExtensionsPermitted" value="${emm:permissionAllowed('mailing.extend_trackable_links', pageContext.request)}"/>
<c:set var="bulkActionsDisallowed" value="${isSettingsReadonly or not emm:permissionAllowed('mailing.extend_trackable_links', pageContext.request)}" />

<mvc:form servletRelativeAction="${controllerPath}/list.action" id="trackableLinksForm"
          cssClass="tiles-container"
          modelAttribute="trackableLinksForm"
          data-controller="mailing-trackable-links"
          data-initializer="mailing-trackable-links" data-editable-view="${agnEditViewKey}">

    <script id="config:mailing-trackable-links" type="application/json">
        {
            "KEEP_UNCHANGED": ${KEEP_UNCHANGED},
            "SAVE_ALL_URL": "<c:url value='${controllerPath}/saveAll.action'/>",
            "scrollToLinkId": ${emm:toJson(scrollToLinkId)}
        }
    </script>
            
    <div id="table-tile" class="tile" style="flex: 3" data-editable-tile="main">
        <div class="tile-body">
            <c:set var="includeDeleted" value="${not empty trackableLinksForm.includeDeleted ? trackableLinksForm.includeDeleted : false}"/>
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "trackable-links-overview": {
                        "rows-count": ${trackableLinksForm.numberOfRows},
                        "include-deleted": ${includeDeleted}
                    }
                }
            </script>
            <c:set var="deepTrackingTitle">
                <mvc:message code="stat.impression.retargeting"/>
                <a href="#" class="icon icon-question-circle" data-help="help_${helplanguage}/mailing/trackable_links/TrackingCookie.xml"></a>
            </c:set>
            <div class="table-wrapper">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <c:if test="${not bulkActionsDisallowed}">
                            <div class="bulk-actions hidden">
                                <p class="bulk-actions__selected">
                                    <span><%-- Updates by JS --%></span>
                                    <mvc:message code="default.list.entry.select" />
                                </p>
                                <div class="bulk-actions__controls">
                                    <c:url var="bulkActionsViewUrl" value='${controllerPath}/bulkActionsView.action'/>
                                    <a href="#" class="icon-btn text-primary" data-tooltip="<mvc:message code='TrackableLink.edit'/>" data-form-url="${bulkActionsViewUrl}" data-form-confirm>
                                        <i class="icon icon-pen"></i>
                                    </a>

                                    <c:url var="bulkClearExtensionsUrl" value='${controllerPath}/confirmBulkClearExtensions.action'/>
                                    <a href="#" class="icon-btn text-danger" data-tooltip="<mvc:message code='ClearAllProperties'/>" data-form-url="${bulkClearExtensionsUrl}" data-form-confirm>
                                        <i class="icon icon-unlink"></i>
                                    </a>
                                </div>
                            </div>
                        </c:if>

                        <c:if test="${param.deletedLinksAllowed}">
                            <div class="form-check form-switch">
                                <mvc:checkbox path="includeDeleted" id="show-deleted-links" cssClass="form-check-input" role="switch" data-form-change="" data-form-submit=""/>
                                <label class="form-label form-check-label" for="show-deleted-links"><mvc:message code="mailing-trackablelinks.show.deleted"/></label>
                            </div>
                        </c:if>

                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp">
                            <jsp:param name="totalEntries" value="${paginatedTrackableLinks.fullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                <agnDisplay:table class="table table-hover table--borderless js-table"
                               id="link"
                               name="paginatedTrackableLinks"
                               sort="external"
                               excludedParams="*"
                               requestURI="${controllerPath}/list.action"
                               partialList="false"
                               decorator="com.agnitas.emm.core.trackablelinks.web.TrackableLinkListDecorator">
                    <%@ include file="../../common/displaytag/displaytag-properties.jspf" %>
                    
                    <c:set var="rowIndex" value="${link_rowNum - 1}" />
                    
                    <agnDisplay:column title="<input type='checkbox' class='form-check-input' data-bulk-checkboxes ${bulkActionsDisallowed ? 'disabled' : ''}/>" headerClass="fit-content">
                        <mvc:hidden path="links[${rowIndex}].id"/>
                        <c:choose>
                            <c:when test="${link.deleted}">
                                <div class="flex-center">
                                    <i class="icon icon-trash-alt center-block"></i>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <mvc:checkbox path="bulkIds" cssClass='form-check-input' value="${link.id}" disabled="${bulkActionsDisallowed}" data-bulk-checkbox="" />
                            </c:otherwise>
                        </c:choose>
                    </agnDisplay:column>

                    <agnDisplay:column headerClass="js-table-sort" titleKey="URL" sortable="true" sortProperty="fullUrlWithExtensions">
                        <div class="d-flex gap-1 align-items-center overflow-wrap-anywhere">
                            <c:set var="fullLinkURL" value="${emm:getFullUrlWithDtoExtensions(link.url, link.extensions)}"/>
                            <c:if test="${not empty originalUrls[link.id]}">
                                <span class="badge badge--error">
                                    <mvc:message code="mailing.trackablelinks.url_changed" />
                                </span>
                            </c:if>
                            <c:if test="${not empty link}">
                                <span class="text-truncate-table">${fullLinkURL}</span>
                            </c:if>
                        </div>
                    </agnDisplay:column>

                    <agnDisplay:column headerClass="js-table-sort" title="${descriptionMsg}" sortable="true" sortProperty="description">
                        <mvc:text path="links[${rowIndex}].shortname" cssClass="form-control" disabled="${isSettingsReadonly}" placeholder="${descriptionMsg}" />
                    </agnDisplay:column>

                    <agnDisplay:column titleKey="LinkTracking" sortable="false">
                        <c:choose>
                            <c:when test="${fn:contains(link.url, '##')}">
                                <span><mvc:message code="Text_and_HTML_Version" /></span>
                            </c:when>
                            <c:otherwise>
                                <mvc:select path="links[${rowIndex}].usage" cssClass="form-control js-select" disabled="${isSettingsReadonly}">
                                    <mvc:option value="${TRACKABLE_NONE}"><mvc:message code="mailing.Not_Trackable" /></mvc:option>
                                    <mvc:option value="${TRACKABLE_ONLY_TEXT}"><mvc:message code="Only_Text_Version" /></mvc:option>
                                    <mvc:option value="${TRACKABLE_ONLY_HTML}"><mvc:message code="Only_HTML_Version" /></mvc:option>
                                    <mvc:option value="${TRACKABLE_TEXT_HTML}"><mvc:message code="Text_and_HTML_Version" /></mvc:option>
                                </mvc:select>
                            </c:otherwise>
                        </c:choose>
                    </agnDisplay:column>

                    <agnDisplay:column titleKey="action.Action" sortable="false" headerClass="fit-content">
                        <mvc:select path="links[${rowIndex}].action" cssClass="form-control js-select" disabled="${isSettingsReadonly}">
                            <mvc:option value="0"><mvc:message code="settings.No_Action" /></mvc:option>
                            <c:forEach var="action" items="${notFormActions}">
                                <mvc:option value="${action.id}">${action.shortname}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </agnDisplay:column>

                    <agnDisplay:column titleKey="AdminLink" sortable="false" headerClass="fit-content">
                        <div class="form-check form-switch">
                            <mvc:checkbox path="links[${rowIndex}].admin" disabled="${link.deleted or isSettingsReadonly}" cssClass="form-check-input" role="switch"/>
                        </div>
                    </agnDisplay:column>

                    <c:if test="${SHOW_CREATE_SUBSTITUTE_LINK}">
                        <agnDisplay:column titleKey="CreateSubstituteLink" sortable="false">
                            <div class="form-check form-switch">
                                <mvc:checkbox path="links[${rowIndex}].createSubstituteForAgnDynMulti" disabled="${link.deleted or isSettingsReadonly}" cssClass="form-check-input" role="switch"/>
                            </div>
                        </agnDisplay:column>
                    </c:if>

                    <agnDisplay:column sortable="false" title="${deepTrackingTitle}">
                        <mvc:select path="links[${rowIndex}].deepTracking" cssClass="form-control js-select" disabled="${isAutoDeeptrackingEnabled or isSettingsReadonly}">
                            <mvc:option value="0"><mvc:message code="TrackableLink.deepTrack.non" /></mvc:option>
                            <mvc:option value="1"><mvc:message code="TrackableLink.deepTrack.cookie" /></mvc:option>
                        </mvc:select>
                    </agnDisplay:column>

                    <c:if test="${isExtensionsPermitted}">
                        <agnDisplay:column titleKey="default.advanced" sortable="false" headerClass="fit-content">
                            <c:set var="extensionCount" value="${not empty link.extensions ? link.extensions.size() : 0}"/>
                            <div class="flex-center">
                                <c:choose>
                                    <c:when test="${extensionCount gt 0}">
                                        <span class="table-badge"><mvc:message code="default.Yes" />: ${extensionCount}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="table-badge"><mvc:message code="No" /></span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </agnDisplay:column>
                    </c:if>

                    <agnDisplay:column headerClass="fit-content">
                        <c:if test="${not link.deleted}">
                            <a href="<c:url value="${controllerPath}/${link.id}/view.action"/>" class="hidden" data-view-row></a>
                        </c:if>
                        <a href="${fullLinkURL}" class="icon-btn text-dark" target="_blank"><i class="icon icon-external-link-alt"></i></a>
                    </agnDisplay:column>
                </agnDisplay:table>
                </div>
            </div>
        </div>
    </div>

    <div id="settings-tile" class="tile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="Settings"/></h1>
        </div>
        <div class="tile-body form-column">
            <c:if test="${isExtensionsPermitted}">
                <div class="d-flex flex-column gap-1">
                    <div class="form-check form-switch">
                        <input type="checkbox" name="modifyAllLinksExtensions" id="settings_modifyLinkExtensions" class="form-check-input" role="switch" ${isSettingsReadonly ? 'disabled' : ''} />
                        <label class="form-label form-check-label" for="settings_modifyLinkExtensions"><mvc:message code="mailing.trackablelinks.extensions.add"/></label>
                    </div>
                    <div id="settingsExtensions" class="tile tile--sm" data-show-by-checkbox="#settings_modifyLinkExtensions">
                        <div class="tile-body" id="link-common-extensions" data-trackable-link-extensions>
                            <script data-config type="application/json">
                              {
                                  "data": ${emm:toJson(allLinksExtensions)},
                                  "defaultExtensions": ${emm:toJson(defaultExtensions)}
                              }
                            </script>
                            <div class="d-flex flex-column gap-2 pt-2">
                                <c:if test="${not empty defaultExtensions}">
                                    <a href="#" class="btn btn-inverse" data-add-default-extensions style="min-width: 0">
                                        <i class="icon icon-plus"></i>
                                        <span class="text-truncate"><mvc:message code="AddDefaultProperties"/></span>
                                    </a>
                                </c:if>
                                <a href="#" class="btn btn-danger" data-delete-all-extensions style="min-width: 0">
                                    <i class="icon icon-trash-alt"></i>
                                    <span class="text-truncate"><mvc:message code="mailing.trackablelinks.clearPropertiesTable"/></span>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>

            <div>
                <label for="openActionId" class="form-label"><mvc:message code="mailing.OpenAction" /></label>
                <mvc:select path="openActionId" cssClass="form-control js-select" id="openActionId" disabled="${isSettingsReadonly}">
                    <mvc:option value="0"><mvc:message code="settings.No_Action" /></mvc:option>
                    <c:forEach var="action" items="${notFormActions}">
                        <mvc:option value="${action.id}">${action.shortname}</mvc:option>
                    </c:forEach>
                </mvc:select>
            </div>

            <div>
                <label for="clickActionId" class="form-label"><mvc:message code="mailing.ClickAction" /></label>
                <mvc:select path="clickActionId" cssClass="form-control js-select" id="clickActionId" disabled="${isSettingsReadonly}">
                    <mvc:option value="0">
                        <mvc:message code="settings.No_Action" />
                    </mvc:option>
                    <c:forEach var="action" items="${notFormActions}">
                        <mvc:option value="${action.id}">${action.shortname}</mvc:option>
                    </c:forEach>
                </mvc:select>
            </div>

            <div class="form-check form-switch">
                <c:url var="trackingLinksOnEveryPositionUrl" value="${controllerPath}/activateTrackingLinksOnEveryPosition.action"/>
                <input type="checkbox" class="form-check-input" role="switch" 
                              data-form-url="${trackingLinksOnEveryPositionUrl}"
                              data-form-set="trackOnEveryPosition: true"
                              data-form-submit=""
                              ${not isTrackingOnEveryPositionAvailable ? 'checked' : ''}
                              ${not isTrackingOnEveryPositionAvailable or isSettingsReadonly ? 'disabled' : ''} />
                <label class="form-label form-check-label">
                    <mvc:message code="EveryPositionLink"/>
                    <a href="#" class="icon icon-question-circle" data-help="help_${helplanguage}/mailing/trackable_links/TrackEveryPosition.xml"></a>
                </label>
            </div>
    
            <emm:ShowByPermission token="settings.extended">
                <div>
                    <div class="form-check form-switch mb-1">
                        <mvc:checkbox path="intelliAdEnabled" id="intelliAdEnabled" cssClass="form-check-input" role="switch" disabled="${isSettingsReadonly}"/>
                        <label class="form-label form-check-label" for="intelliAdEnabled"><mvc:message code="mailing.intelliad.enable"/></label>
                    </div>
                    <mvc:message var="inteliAdIdStr" code="mailing.intelliad.idstring"/>
                    <mvc:text path="intelliAdIdString" maxlength="500" id="intelliAdIdString" cssClass="form-control"
                              data-show-by-checkbox="#intelliAdEnabled"
                              placeholder="${inteliAdIdStr}" disabled="${isSettingsReadonly}"/>
                </div>
            </emm:ShowByPermission>
            <emm:HideByPermission token="settings.extended">
                <mvc:hidden path="intelliAdEnabled"/>
                <mvc:hidden path="intelliAdIdString"/>
            </emm:HideByPermission>
        </div>
    </div>
</mvc:form>
