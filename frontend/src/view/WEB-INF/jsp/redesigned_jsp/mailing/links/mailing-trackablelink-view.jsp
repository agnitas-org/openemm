<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="emm"   uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="linkId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="usage" type="java.lang.Boolean"--%>
<%--@elvariable id="isSettingsReadonly" type="java.lang.Boolean"--%>
<%--@elvariable id="urlModified" type="java.lang.Boolean"--%>
<%--@elvariable id="isUrlEditingAllowed" type="java.lang.Boolean"--%>
<%--@elvariable id="SHOW_CREATE_SUBSTITUTE_LINK" type="java.lang.Boolean"--%>
<%--@elvariable id="altText" type="java.lang.String"--%>
<%--@elvariable id="originalUrl" type="java.lang.String"--%>
<%--@elvariable id="workflowForwardParams" type="java.lang.String"--%>
<%--@elvariable id="notFormActions" type="java.util.List<org.agnitas.actions.EmmAction>"--%>
<%--@elvariable id="trackableLinkForm" type="com.agnitas.emm.core.trackablelinks.form.TrackableLinkForm"--%>

<c:set var="isMailingGrid" value="${not empty gridTemplateId and gridTemplateId gt 0}" scope="request"/>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-xl modal-dialog-centered">
        <mvc:form servletRelativeAction="/mailing/${mailingId}/trackablelink/${linkId}/save.action" 
                  cssClass="modal-content"
                  id="trackableLinkForm"
                  data-controller="mailing-trackable-links"
                  modelAttribute="trackableLinkForm"
                  data-form="resource">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="TrackableLink.editLink"/></h1>
                <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body grid gap-3" style="--bs-columns: 1">
                <div>
                    <label class="form-label" for="linkUrl"><mvc:message code="URL" /></label>
                    <c:choose>
                        <c:when test="${isUrlEditingAllowed}">
                            <mvc:text path="url" id="linkUrl" cssClass="form-control" disabled="${isSettingsReadonly}"/>
                        </c:when>
                        <c:otherwise>
                            <mvc:text path="" value="${trackableLinkForm.url}" id="linkUrl" cssClass="form-control" readonly="true"/>
                            <mvc:hidden path="url"/>
                        </c:otherwise>
                    </c:choose>
                </div>
    
                <c:if test="${not empty originalUrl}">
                    <div>
                        <label class="form-label" for="originalUrl"><mvc:message code="mailing.trackablelinks.original_url" /></label>
                        <input type="text" value="${originalUrl}" id="originalUrl" readonly class="form-control">
                        <input type="text" value="${originalUrl}" id="originalUrl" readonly class="form-control">
                    </div>
                </c:if>
    
                <div>
                    <label class="form-label" for="linkName"><mvc:message code="Description" /></label>
                    <mvc:text path="shortname" id="linkName" cssClass="form-control" disabled="${isSettingsReadonly}"/>
                </div>
    
                <c:if test="${not empty altText}">
                    <div>
                        <label class="form-label" for="altText"><mvc:message code="Title" /></label>
                        <mvc:text path="" value="${altText}" id="altText" cssClass="form-control" readonly="true"/>
                    </div>
                </c:if>
    
                <div>
                    <label class="form-label" for="usage"><mvc:message code="LinkTracking" /></label>
                    <c:choose>
                        <c:when test="${fn:contains(trackableLinkForm.url, '##')}">
                            <input type="text" class="form-control" value='<mvc:message code="Text_and_HTML_Version"/>' readonly/>
                        </c:when>
                        <c:otherwise>
                            <mvc:select path="usage" id="usage" cssClass="form-control" data-action="link-details-trackable" disabled="${isSettingsReadonly}">
                                <mvc:option value="0"><mvc:message code="mailing.Not_Trackable"/></mvc:option>
                                <mvc:option value="1"><mvc:message code="Only_Text_Version"/></mvc:option>
                                <mvc:option value="2"><mvc:message code="Only_HTML_Version"/></mvc:option>
                                <mvc:option value="3"><mvc:message code="Text_and_HTML_Version"/></mvc:option>
                            </mvc:select>
                        </c:otherwise>
                    </c:choose>
                </div>
    
                <div>
                    <label class="form-label" for="linkAction"><mvc:message code="action.Action" /></label>
                    <mvc:select path="action" size="1" id="linkAction" cssClass="form-control" disabled="${isSettingsReadonly}">
                        <mvc:option value="0"><mvc:message code="settings.No_Action" /></mvc:option>
                        <c:forEach var="action" items="${notFormActions}">
                            <mvc:option value="${action.id}">${action.shortname}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
    
                <%@include file="fragments/mailing-trackablelink-view-revenue.jspf" %>
                <%@include file="fragments/mailing-trackablelink-static-link.jsp" %>
    
                <div class="form-check form-switch">
                    <mvc:checkbox path="admin" id="administrativeCheckbox" cssClass="form-check-input" role="switch" disabled="${isSettingsReadonly}"/>
                    <label class="form-label form-check-label text-capitalize" for="administrativeCheckbox"><mvc:message code="report.adminLinks"/></label>
                </div>
                    
                <c:if test="${SHOW_CREATE_SUBSTITUTE_LINK}">
                    <div class="form-check form-switch">
                        <mvc:checkbox path="createSubstituteForAgnDynMulti" id="createSubstituteLink" cssClass="form-check-input" role="switch" disabled="${isSettingsReadonly}"/>
                        <label class="form-label form-check-label text-capitalize" for="createSubstituteLink"><mvc:message code="CreateSubstituteLink"/></label>
                    </div>
                </c:if>		
                
                <emm:ShowByPermission token="mailing.extend_trackable_links">
                    <div class="d-flex flex-column">
                        <label class="form-label" for="createSubstituteLink"><mvc:message code="LinkExtensions" /></label>
                        <div class="tile">
                            <div class="row p-2 pb-0">
                                <span class="col fw-semibold"><mvc:message code="Name"/></span>
                                <span class="col fw-semibold"><mvc:message code="Value"/></span>
                                <c:if test="${not isSettingsReadonly}">
                                    <div class="col-auto" style="width: 50px;"></div>
                                </c:if>
                            </div>
                            <div class="tile-body p-2 pt-1" id='individual-extensions' data-trackable-link-extensions>
                                <script data-config type="application/json">
                                  {
                                      "data": ${emm:toJson(trackableLinkForm.extensions)},
                                      "defaultExtensions": ${emm:toJson(defaultExtensions)},
                                      "readonly": ${isSettingsReadonly}
                                  }
                                </script>
                                <div class="row pt-2 g-2">
                                    <c:if test="${not empty defaultExtensions}">
                                        <div class="col d-flex">
                                            <a href="#" class="col flex-grow-1 btn btn-inverse text-nowrap px-1 ${isSettingsReadonly ? 'disabled' : ''}" ${isSettingsReadonly ? '' : 'data-add-default-extensions'}>
                                                <i class="icon icon-plus"></i>
                                                <mvc:message code="AddDefaultProperties"/>
                                            </a>
                                        </div>
                                    </c:if>
                                    <div class="col d-flex">
                                        <a href="#" class="col flex-grow-1 btn btn-danger text-nowrap px-1 ${isSettingsReadonly ? 'disabled' : ''}" ${isSettingsReadonly ? '' : 'data-delete-all-extensions'}>
                                            <i class="icon icon-trash-alt"></i>
                                            <mvc:message code="mailing.trackablelinks.clearPropertiesTable"/>
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </emm:ShowByPermission>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary flex-grow-1" ${isSettingsReadonly ? 'disabled' : 'data-action="save-individual"'}>
                    <i class="icon icon-save"></i>
                    <mvc:message code="button.Save"/>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
