<%@ page import="org.agnitas.beans.BaseTrackableLink" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="notFormActions" type="java.util.List<org.agnitas.actions.EmmAction>"--%>

<c:set var="KEEP_UNCHANGED" value="<%= BaseTrackableLink.KEEP_UNCHANGED %>"/>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-xl">
        <div class="modal-content">
            <mvc:form id="bulkActionsForm" onsubmit="return false;">
                <div class="modal-header">
                    <h1 class="modal-title"><mvc:message code="bulkAction"/></h1>
                    <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
                <div class="modal-body grid" style="--bs-columns: 1">
                    <div>
                        <div class="form-check form-switch">
                            <input type="checkbox" name="bulkModifyDescription" id="modifyBulkDescription" class="form-check-input" role="switch"/>
                            <label class="form-label form-check-label" for="modifyBulkDescription"><mvc:message code="default.description.edit"/></label>
                        </div>
                        <div class="mt-1" data-show-by-checkbox="#modifyBulkDescription">
                            <input type="text" name="bulkDescription" id="bulkDescription" class="form-control" placeholder="<mvc:message code="Description"/>"/>
                        </div>
                    </div>

                    <div>
                        <label for="globalUsage" class="form-label"><mvc:message code="LinkTracking" /></label>
                        <select name="bulkUsage" id="globalUsage" class="form-control js-select">
                            <option value="${KEEP_UNCHANGED}"><mvc:message code="KeepUnchanged"/></option>
                            <option value="0"><mvc:message code="mailing.Not_Trackable" /></option>
                            <option value="1"><mvc:message code="Only_Text_Version" /></option>
                            <option value="2"><mvc:message code="Only_HTML_Version" /></option>
                            <option value="3"><mvc:message code="Text_and_HTML_Version" /></option>
                        </select>
                    </div>

                    <div>
                        <label for="linkAction" class="form-label"><mvc:message code="mailing.DefaultAction" /></label>
                        <select name="bulkAction" class="form-control js-select" id="linkAction">
                            <option value="${KEEP_UNCHANGED}"><mvc:message code="KeepUnchanged" /></option>
                            <option value="0"><mvc:message code="settings.No_Action" /></option>
                            <c:forEach var="action" items="${notFormActions}">
                                <option value="${action.id}">${action.shortname}</option>
                            </c:forEach>
                        </select>
                    </div>
                    
                    <%@include file="fragments/bulk-edit/link-list-revenue-field.jspf" %>
                    
                    <div>
                        <label class="form-label" for="bulkLinkStatic"><mvc:message code="TrackableLink.staticLink" /></label>
                        <select name="bulkStatic" id="bulkLinkStatic" class="form-control js-select">
                            <option value="${KEEP_UNCHANGED}"><mvc:message code="KeepUnchanged"/></option>
                            <option value="0"><mvc:message code="default.No" /></option>
                            <option value="1"><mvc:message code="default.Yes" /></option>
                        </select>
                    </div>
                    
                    <emm:ShowByPermission token="mailing.extend_trackable_links">
                        <div class="d-flex flex-column gap-1">
                            <div class="form-check form-switch">
                                <input type="checkbox" name="modifyBulkLinksExtensions" id="bulkActions_modifyLinkExtensions" class="form-check-input" role="switch"/>
                                <label class="form-label form-check-label" for="bulkActions_modifyLinkExtensions"><mvc:message code="mailing.links.extension.bulk.change"/></label>
                            </div>
                            <div class="tile tile--sm" data-show-by-checkbox="#bulkActions_modifyLinkExtensions">
                                <div id="bulkActionExtensions" class="tile-body" data-trackable-link-extensions>
                                    <script data-config type="application/json">
                                      {
                                          "data": ${emm:toJson(commonExtensions)}
                                      }
                                    </script>
                                </div>
                            </div>
                        </div>
                    </emm:ShowByPermission>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" data-form-target="#trackableLinksForm" data-form-set="everyPositionLink: false" data-action="save-bulk-actions">
                        <mvc:message code="button.Save"/>
                    </button>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
