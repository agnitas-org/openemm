<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/errorRedesigned.action" %>
<%@page import="com.agnitas.emm.core.trackablelinks.common.LinkTrackingMode"%>
<%@ page import="org.agnitas.beans.BaseTrackableLink" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="tiles"   uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="SHOW_CREATE_SUBSTITUTE_LINK" type="java.lang.Boolean"--%>
<%--@elvariable id="isSettingsReadonly" type="java.lang.Boolean"--%>
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
<c:set var="isMailingGrid"       value="${not empty gridTemplateId and gridTemplateId gt 0}" scope="request"/>

<c:set var="isExtensionsPermitted" value="false"/>
<emm:ShowByPermission token="mailing.extend_trackable_links">
    <c:set var="isExtensionsPermitted" value="true"/>
</emm:ShowByPermission>

<mvc:form servletRelativeAction="/mailing/${mailingId}/trackablelink/list.action" id="trackableLinksForm" 
          cssClass="tiles-container d-flex hidden"
          modelAttribute="trackableLinksForm"
          data-controller="mailing-trackable-links"
          data-initializer="mailing-trackable-links" data-editable-view="${agnEditViewKey}">

    <script id="config:mailing-trackable-links" type="application/json">
        {
            "KEEP_UNCHANGED": ${KEEP_UNCHANGED},
            "SAVE_ALL_URL": "<c:url value='/mailing/${mailingId}/trackablelink/saveAll.action'/>"
        }
    </script>
            
    <div id="table-tile" class="tile" style="flex: 3" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview"/></h1>
            <div class="tile-controls">
                <div class="form-check form-switch">
                    <mvc:checkbox path="includeDeleted" id="show-deleted-links" cssClass="form-check-input" role="switch" data-form-change="" data-form-submit=""/>
                    <label class="form-label form-check-label fw-semibold" for="show-deleted-links"><mvc:message code="mailing-trackablelinks.show.deleted"/></label>
                </div>
            </div>
        </div>

        <div class="tile-body">
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "trackable-links-overview": {
                        "rows-count": ${trackableLinksForm.numberOfRows},
                        "include-deleted": ${trackableLinksForm.includeDeleted}
                    }
                }
            </script>
            <c:set var="deepTrackingTitle">
                <mvc:message code="stat.impression.retargeting"/>
                <a href="#" class="icon icon-question-circle" data-help="help_${helplanguage}/mailing/trackable_links/TrackingCookie.xml"></a>
            </c:set>
            <div class="table-box">
                <div class="table-scrollable">
                <display:table class="table table-hover table-rounded js-table"
                               id="link"
                               name="paginatedTrackableLinks"
                               sort="external"
                               excludedParams="*"
                               requestURI="/mailing/${mailingId}/trackablelink/list.action"
                               partialList="false"
                               decorator="com.agnitas.emm.core.trackablelinks.web.TrackableLinkListDecorator">
                    <%@ include file="../../displaytag/displaytag-properties.jspf" %>
                    
                    <c:set var="rowIndex" value="${link_rowNum - 1}" />
                    
                    <display:column title="<input type='checkbox' class='form-check-input' data-form-bulk='bulkIds' ${isSettingsReadonly ? 'disabled' : ''}/>" class="js-checkable" sortable="false" headerClass="fit-content">
                        <mvc:hidden path="links[${rowIndex}].id"/>
                        <c:choose>
                            <c:when test="${link.deleted}">
                                <i class="icon icon-trash-alt center-block"></i>
                            </c:when>
                            <c:otherwise>
                                <mvc:checkbox path="bulkIds" cssClass='form-check-input' value="${link.id}" disabled="${isSettingsReadonly}"/>
                            </c:otherwise>
                        </c:choose>
                    </display:column>

                    <display:column headerClass="js-table-sort" titleKey="URL" sortable="true" sortProperty="fullUrlWithExtensions">
                        <div class="d-flex gap-1 align-items-center">
                            <c:set var="fullLinkURL" value="${emm:getFullUrlWithDtoExtensions(link.url, link.extensions)}"/>
                            <c:if test="${not empty originalUrls[link.id]}">
                                <span class="badge badge--error">
                                    <mvc:message code="mailing.trackablelinks.url_changed" />
                                </span>
                            </c:if>
                            <c:if test="${not empty link}">
                                <span class="text-truncate">${fullLinkURL}</span>
                            </c:if>
                        </div>
                    </display:column>

                    <display:column headerClass="js-table-sort" titleKey="Description" sortable="true" sortProperty="description">
                        <mvc:text path="links[${rowIndex}].shortname" cssClass="form-control" disabled="${isSettingsReadonly}"/>
                    </display:column>

                    <display:column titleKey="LinkTracking" sortable="false">
                        <c:choose>
                            <c:when test="${fn:contains(link.url, '##')}">
                                <mvc:message code="Text_and_HTML_Version" />
                            </c:when>
                            <c:otherwise>
                                <mvc:select path="links[${rowIndex}].usage" cssClass="form-control" disabled="${isSettingsReadonly}">
                                    <mvc:option value="${TRACKABLE_NONE}"><mvc:message code="mailing.Not_Trackable" /></mvc:option>
                                    <mvc:option value="${TRACKABLE_ONLY_TEXT}"><mvc:message code="Only_Text_Version" /></mvc:option>
                                    <mvc:option value="${TRACKABLE_ONLY_HTML}"><mvc:message code="Only_HTML_Version" /></mvc:option>
                                    <mvc:option value="${TRACKABLE_TEXT_HTML}"><mvc:message code="Text_and_HTML_Version" /></mvc:option>
                                </mvc:select>
                            </c:otherwise>
                        </c:choose>
                    </display:column>

                    <display:column titleKey="action.Action" sortable="false" headerClass="fit-content">
                        <mvc:select path="links[${rowIndex}].action" cssClass="form-control js-select" disabled="${isSettingsReadonly}">
                            <mvc:option value="0"><mvc:message code="settings.No_Action" /></mvc:option>
                            <c:forEach var="action" items="${notFormActions}">
                                <mvc:option value="${action.id}">${action.shortname}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </display:column>

                    <display:column titleKey="AdminLink" sortable="false" headerClass="fit-content">
                        <div class="form-check form-switch">
                            <mvc:checkbox path="links[${rowIndex}].admin" disabled="${link.deleted or isSettingsReadonly}" cssClass="form-check-input" role="switch"/>
                        </div>
                    </display:column>

                    <c:if test="${SHOW_CREATE_SUBSTITUTE_LINK}">
                        <display:column titleKey="CreateSubstituteLink" sortable="false">
                            <div class="form-check form-switch">
                                <mvc:checkbox path="links[${rowIndex}].createSubstituteForAgnDynMulti" disabled="${link.deleted or isSettingsReadonly}" cssClass="form-check-input" role="switch"/>
                            </div>
                        </display:column>
                    </c:if>

                    <display:column sortable="false" title="${deepTrackingTitle}">
                        <mvc:select path="links[${rowIndex}].deepTracking" cssClass="form-control" disabled="${isAutoDeeptrackingEnabled or isSettingsReadonly}">
                            <mvc:option value="0"><mvc:message code="TrackableLink.deepTrack.non" /></mvc:option>
                            <mvc:option value="1"><mvc:message code="TrackableLink.deepTrack.cookie" /></mvc:option>
                        </mvc:select>
                    </display:column>

                    <c:if test="${isExtensionsPermitted}">
                        <display:column titleKey="default.advanced" sortable="false" headerClass="fit-content">
                            <c:set var="extensionCount" value="${not empty link.extensions ? link.extensions.size() : 0}"/>
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
                        </display:column>
                    </c:if>

                    <display:column headerClass="fit-content">
                        <c:if test="${not link.deleted}">
                            <a href="<c:url value="/mailing/${mailingId}/trackablelink/${link.id}/view.action"/>" class="hidden" data-view-row></a>
                        </c:if>
                        <a href="${fullLinkURL}" class="btn btn-icon-sm btn-inverse" target="_blank"><i class="icon icon-external-link-alt"></i></a>
                    </display:column>
                </display:table>
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
                <div class="d-flex flex-column gap-1">
                    <div class="form-check form-switch">
                        <input type="checkbox" name="modifyAllLinksExtensions" id="settings_modifyLinkExtensions" class="form-check-input" role="switch" ${isSettingsReadonly ? 'disabled' : ''} />
                        <label class="form-label form-check-label text-capitalize" for="settings_modifyLinkExtensions"><mvc:message code="mailing.trackablelinks.extensions.add"/></label>
                    </div>
                    <div id="settingsExtensions" class="tile" data-show-by-checkbox="#settings_modifyLinkExtensions">
                        <div class="row p-2 pb-0">
                            <span class="col fw-semibold"><mvc:message code="Name"/></span>
                            <span class="col fw-semibold"><mvc:message code="Value"/></span>
                            <div class="col-auto" style="width: 50px;"></div>
                        </div>
                        <div class="tile-body p-2 pt-1" id='link-common-extensions' data-trackable-link-extensions>
                            <script data-config type="application/json">
                              {
                                  "data": ${emm:toJson(allLinksExtensions)},
                                  "defaultExtensions": ${emm:toJson(defaultExtensions)}
                              }
                            </script>
                            <div class="row pt-2 g-2">
                                <c:if test="${not empty defaultExtensions}">
                                    <div class="col d-flex">
                                        <a href="#" data-add-default-extensions class="col flex-grow-1 btn btn-inverse text-nowrap px-1">
                                            <i class="icon icon-plus"></i>
                                            <mvc:message code="AddDefaultProperties"/>
                                        </a>
                                    </div>
                                </c:if>
                                <div class="col d-flex">
                                    <a href="#" class="col flex-grow-1 btn btn-danger text-nowrap px-1" data-delete-all-extensions>
                                        <i class="icon icon-trash-alt"></i>
                                        <mvc:message code="mailing.trackablelinks.clearPropertiesTable"/>
                                    </a>
                                </div>
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
                <c:url var="trackingLinksOnEveryPositionUrl" value="/mailing/${mailingId}/trackablelink/activateTrackingLinksOnEveryPosition.action"/>
                <input type="checkbox" class="form-check-input" role="switch"
                              data-form-url="${trackingLinksOnEveryPositionUrl}"
                              data-form-submit=""
                              ${not isTrackingOnEveryPositionAvailable ? 'checked' : ''}
                              ${not isTrackingOnEveryPositionAvailable or isSettingsReadonly ? 'disabled' : ''} />
                <label class="form-label form-check-label text-capitalize"><mvc:message code="EveryPositionLink"/></label>
                <a href="#" class="icon icon-question-circle" data-help="help_${helplanguage}/mailing/trackable_links/TrackEveryPosition.xml"></a>
            </div>
    
            <emm:ShowByPermission token="settings.extended">
                <div>
                    <div class="form-check form-switch mb-1">
                        <mvc:checkbox path="intelliAdEnabled" id="intelliAdEnabled" cssClass="form-check-input" role="switch" disabled="${isSettingsReadonly}"/>
                        <label class="form-label form-check-label text-capitalize" for="intelliAdEnabled"><mvc:message code="mailing.intelliad.enable"/></label>
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
