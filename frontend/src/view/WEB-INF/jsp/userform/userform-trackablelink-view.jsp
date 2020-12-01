<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>

<%@ page import="com.agnitas.util.LinkUtils" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.trackablelinks.form.FormTrackableLinkForm"--%>
<%--@elvariable id="userFormId" type="java.lang.Integer"--%>
<%--@elvariable id="defaultExtensions" type="java.util.List"--%>

<c:set var="TRACKABLE_NO" value="<%= LinkUtils.TRACKABLE_NO %>"/>
<c:set var="TRACKABLE_YES" value="<%= LinkUtils.TRACKABLE_YES %>"/>

<div class="row">
    <div class="col-xs-12 row-1-1">

        <mvc:form servletRelativeAction="/webform/${userFormId}/trackablelink/save.action" method="post"
                  id="userFormTrackableLinkForm"
                  data-form="resource" modelAttribute="form"
                  data-controller="form-trackable-link"
                  data-action="save">
            <mvc:hidden path="id"/>

            <div class="tile">
                <div class="tile-header">
                    <h2 class="headline">
                        <mvc:message code="TrackableLink.editLink"/>
                    </h2>
                </div>
                <div class="tile-content tile-content-forms">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="link-url">
                                <mvc:message code="URL" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <input type="text" name="url" id="link-url" class="form-control" readonly="readonly" value="${form.url}"/>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="link-name">
                                <mvc:message code="Description" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <mvc:text path="name" id="link-name" cssClass="form-control"/>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="link-trackable">
                                <mvc:message code="LinkTracking" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <mvc:select path="trackable" id="link-trackable" cssClass="form-control">
                                <mvc:option value="${TRACKABLE_NO}"><mvc:message code="NotTrackedLink"/></mvc:option>
                                <mvc:option value="${TRACKABLE_YES}"><mvc:message code="TrackedLink"/></mvc:option>
                            </mvc:select>
                        </div>
                    </div>

                    <emm:ShowByPermission token="mailing.extend_trackable_links">
                        <div class="form-group" data-initializer="trackable-link-extensions">

                            <script id="config:trackable-link-extensions" type="application/json">
                            {
                                "extensions": ${emm:toJson(form.extensions)}
                            }
                            </script>
                            <div class="col-sm-4">
                                <label class="control-label">
                                    <mvc:message code="LinkExtensions"/>
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <div id="link-extensions" class="table-responsive">
                                    <table class="table table-bordered table-striped" id="extensions-table">
                                        <thead>
                                        <th><mvc:message code="Name"/></th>
                                        <th><mvc:message code="Value"/></th>
                                        <th></th>
                                        </thead>
                                        <tbody>
                                        <%-- this block load by JS--%>
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
                                            <a href="#"
                                               class="btn btn-regular btn-block btn-primary"
                                               data-action="add-extension">
                                                <i class="icon icon-plus"></i>
                                                <span class="text"><mvc:message code="AddProperty"/></span>
                                            </a>
                                        </div>

                                        <div class="col-sm-12 col-md-4">
                                            <a href="#"
                                               class="btn btn-regular btn-block btn-alert"
                                               data-action="delete-all-extensions">
                                                <i class="icon icon-trash-o"></i>
                                                <span class="text"><mvc:message code="mailing.trackablelinks.clearPropertiesTable"/></span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </emm:ShowByPermission>
                </div>
            </div>
        </mvc:form>
    </div>
</div>

<%@ include file="extension-row-template.jspf" %>
