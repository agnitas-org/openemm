<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ page import="com.agnitas.util.LinkUtils" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.trackablelinks.form.FormTrackableLinkForm"--%>
<%--@elvariable id="userFormId" type="java.lang.Integer"--%>
<%--@elvariable id="defaultExtensions" type="java.util.List"--%>

<c:set var="TRACKABLE_NO" value="<%= LinkUtils.TRACKABLE_NO %>"/>
<c:set var="TRACKABLE_YES" value="<%= LinkUtils.TRACKABLE_YES %>"/>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form cssClass="modal-content" servletRelativeAction="/webform/${userFormId}/trackablelink/save.action" method="post"
                  id="userFormTrackableLinkForm"
                  data-form="resource" modelAttribute="form"
                  data-controller="form-trackable-links"
                  data-action="save-individual">
            <mvc:hidden path="id"/>

            <div class="modal-header">
                <h1 class="tile-title text-truncate"><mvc:message code="TrackableLink.editLink"/></h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body grid" style="--bs-columns: 1">
                <div>
                    <label class="form-label" for="link-url"><mvc:message code="URL" /></label>
                    <input type="text" name="url" id="link-url" class="form-control" readonly="readonly" value="${form.url}"/>
                </div>

                <div>
                    <label class="form-label" for="link-name"><mvc:message code="Description" /></label>
                    <mvc:text path="name" id="link-name" cssClass="form-control"/>
                </div>

                <div>
                    <label class="form-label" for="link-trackable"><mvc:message code="LinkTracking" /></label>
                    <mvc:select path="trackable" id="link-trackable" cssClass="form-control">
                        <mvc:option value="${TRACKABLE_NO}"><mvc:message code="NotTrackedLink"/></mvc:option>
                        <mvc:option value="${TRACKABLE_YES}"><mvc:message code="TrackedLink"/></mvc:option>
                    </mvc:select>
                </div>

                <emm:ShowByPermission token="mailing.extend_trackable_links">
                    <div class="tile">
                        <div class="tile-header p-2 border-bottom">
                            <span class="text-dark fw-medium"><mvc:message code="mailing.trackablelinks.extensions.add"/></span>
                        </div>
                        <div class="tile-body p-2">
                            <div id="link-extensions" data-trackable-link-extensions>
                                <script data-config type="application/json">
                                    {
                                        "data": ${emm:toJson(form.extensions)},
                                        "defaultExtensions": ${emm:toJson(defaultExtensions)}
                                    }
                                </script>
                                <div class="row pt-2 g-2">
                                    <c:if test="${not empty defaultExtensions}">
                                        <div class="col d-flex">
                                            <a href="#"
                                               data-add-default-extensions
                                               class="col flex-grow-1 btn btn-inverse text-nowrap px-1">
                                                <i class="icon icon-plus"></i>
                                                <mvc:message code="AddDefaultProperties"/>
                                            </a>
                                        </div>
                                    </c:if>
                                    <div class="col d-flex">
                                        <a href="#"
                                           class="col flex-grow-1 btn btn-danger text-nowrap px-1"
                                           data-delete-all-extensions>
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
                <button type="button" class="btn btn-primary" data-form-submit-event>
                    <i class="icon icon-save"></i>
                    <mvc:message code="button.Save"/>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
