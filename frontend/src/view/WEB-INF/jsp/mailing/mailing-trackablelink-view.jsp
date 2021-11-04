<%@ page language="java" contentType="text/html; charset=utf-8" import="com.agnitas.web.*" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="notFormActions" type="java.util.List<org.agnitas.actions.EmmAction>"--%>

<c:set var="ACTION_LIST" value="<%= ComTrackableLinkAction.ACTION_LIST %>"/>
<c:set var="PROPERTY_NAME_PREFIX" value="<%= ComMailingContentForm.PROPERTY_NAME_PREFIX %>"/>
<c:set var="PROPERTY_VALUE_PREFIX" value="<%= ComMailingContentForm.PROPERTY_VALUE_PREFIX %>"/>

<tiles:insert page="template.jsp">
    <tiles:put name="header" type="string">
        <h2 class="headline">
            <bean:message key="TrackableLink.editLink"/>
        </h2>

        <ul class="tile-header-nav">
            <!-- Tabs BEGIN -->
            <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
            <!-- Tabs END -->
        </ul>
    </tiles:put>

    <tiles:putList name="footerItems">
        <tiles:add>
            <c:choose>
                <c:when test="${not empty workflowForwardParams}">
                    <c:url var="backLink" value="/tracklink.do?action=${ACTION_LIST}&forwardParams=${workflowForwardParams}&mailingID=${trackableLinkForm.mailingID}&scrollToLinkId=${trackableLinkForm.linkID}"/>
                </c:when>
                <c:otherwise>
                    <c:url var="backLink" value="/tracklink.do?action=${ACTION_LIST}&mailingID=${trackableLinkForm.mailingID}&scrollToLinkId=${trackableLinkForm.linkID}"/>
                </c:otherwise>
            </c:choose>
            <a id="buttonBack" class="btn btn-large pull-left" href="${backLink}">
                <span class="text">
                    <bean:message key="button.Back"/>
                </span>
                    <%--<i class="icon icon-reply"></i>--%>
            </a>
        </tiles:add>
        <tiles:add>
            <button type="button" class="btn btn-large btn-primary pull-right" data-form-target='#trackableLinkForm' data-form-submit>
                <span class="text">
                    <bean:message key="button.Save"/>
                </span>
                    <%--<i class="icon icon-save"></i>--%>
            </button>
        </tiles:add>
    </tiles:putList>

    <tiles:put name="content" type="string">
        <c:if test="${not isMailingGrid}">
        <div class="row">
        </c:if>

            <div class="${isMailingGrid ? '' : 'col-xs-12 row-1-1'}" data-controller="trackable-link">
                <agn:agnForm action="/tracklink" id="trackableLinkForm" data-form="resource">
                    <html:hidden property="mailingID" />
                    <html:hidden property="linkID" />
                    <html:hidden property="action" />

                    <c:if test="${not isMailingGrid}">
                    <div class="tile">
                        <div class="tile-header">
                            <h2 class="headline">
                                <bean:message key="TrackableLink.editLink"/>
                            </h2>
                        </div>
                    </c:if>

                        <div class="${isMailingGrid ? '' : 'tile-content'} tile-content-forms">
                            <div class="form-group">
                                <div class="col-sm-4">
                                    <label class="control-label" for="linkUrl">
                                        <bean:message key="URL" />
                                    </label>
                                </div>
                                <div class="col-sm-8">
                                    <c:choose>
                                        <c:when test="${CAN_EDIT_URL}">
                                            <html:text property="linkUrl" styleId="linkUrl" styleClass="form-control"/>
                                        </c:when>
                                        <c:otherwise>
                                            <html:text property="" value="${trackableLinkForm.linkUrl}" styleId="linkUrl" styleClass="form-control" readonly="true"/>
                                            <html:hidden property="linkUrl" value=""/>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <c:if test="${trackableLinkForm.linkToView.urlModified}">
                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label class="control-label">
                                            <bean:message key="mailing.trackablelinks.original_url" />
                                        </label>
                                    </div>
                                    <div class="col-sm-8">
                                        <input type="text" value="${trackableLinkForm.linkToView.originalUrl}" readonly class="form-control">
                                    </div>
                                </div>
                            </c:if>

                            <div class="form-group">
                                <div class="col-sm-4">
                                    <label class="control-label" for="linkName">
                                        <bean:message key="Description" />
                                    </label>
                                </div>
                                <div class="col-sm-8">
                                    <html:text property="linkName" styleId="linkName" styleClass="form-control" />
                                </div>
                            </div>

                            <c:if test="${not empty trackableLinkForm.altText}">
                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label class="control-label" for="altText">
                                            <bean:message key="Title" />
                                        </label>
                                    </div>
                                    <div class="col-sm-8">
                                        <html:text property="" value="${trackableLinkForm.altText}" styleId="altText" styleClass="form-control" readonly="true"/>
                                    </div>
                                </div>
                            </c:if>

                            <div class="form-group">
                                <div class="col-sm-4">
                                    <label class="control-label" for="trackable">
                                        <bean:message key="Trackable" />
                                    </label>
                                </div>
                                <div class="col-sm-8">
                                    <c:choose>
                                        <c:when test="${trackableLinkForm.linkToView.usage >= 0}">
		                                    <agn:agnSelect property="trackable" styleId="trackable" styleClass="form-control" data-action="link-details-trackable">
		                                        <html:option value="0"><bean:message key="mailing.Not_Trackable" /></html:option>
		                                        <html:option value="1"><bean:message key="Only_Text_Version" /></html:option>
		                                        <html:option value="2"><bean:message key="Only_HTML_Version" /></html:option>
		                                        <html:option value="3"><bean:message key="Text_and_HTML_Version" /></html:option>
		                                    </agn:agnSelect>
                                        </c:when>
                                       	<c:otherwise>
                                       		<bean:message key="Text_and_HTML_Version" />
                                       	</c:otherwise>
                                   	</c:choose>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="col-sm-4">
                                    <label class="control-label" for="linkAction">
                                        <bean:message key="action.Action" />
                                    </label>
                                </div>
                                <div class="col-sm-8">
                                    <html:select property="linkAction" size="1" styleId="linkAction" styleClass="form-control">
                                        <html:option value="0"><bean:message key="settings.No_Action" /></html:option>
                                        <c:forEach var="action" items="${notFormActions}">
                                            <html:option value="${action.id}">${action.shortname}</html:option>
                                        </c:forEach>
                                    </html:select>
                                </div>
                            </div>

							<%@include file="mailing-trackablelink-view-revenue.jspf" %>
							<%@include file="mailing-trackablelink-static-link.jsp" %>

							<div class="form-group">
									<div class="col-sm-4">
										<label class="control-label" for="administrativeLink"> <bean:message
												key="report.adminLinks" />
										</label>
									</div>
									<div class="col-sm-8">
										<html:checkbox property="administrativeLink" styleId="administrativeLink"/>
									</div>
								</div>
								
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
                                                    <c:set var="properties" value="${trackableLinkForm.linkToView.properties}"/>
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
                                                                <td>
                                                                    <a href="#" class="btn btn-regular btn-alert" data-tooltip="<bean:message key="button.Delete" />" data-action="link-details-delete-link" data-link-id="${propertyIndex}">
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
                                                    <div class="col-sm-12 col-md-4">
                                                        <a href="#" class="btn btn-regular btn-block btn-primary" data-action="link-details-add-extension">
                                                            <i class="icon icon-plus"></i>
                                                            <span class="text"><bean:message key="AddProperty" /></span>
                                                        </a>
                                                    </div>

                                                    <div class="col-sm-12 col-md-4">
                                                        <a href="#" class="btn btn-regular btn-block btn-alert" data-action="link-details-delete-all-links">
                                                            <i class="icon icon-trash-o"></i>
                                                            <span class="text"><bean:message key="ClearAllProperties"/></span>
                                                        </a>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                        	</emm:ShowByPermission>
                        </div>
                        <!-- Tile Content END -->

                    <c:if test="${not isMailingGrid}">
                    </div>
                    </c:if>
                    <!-- Tile END -->

                </agn:agnForm>
            </div>

        <c:if test="${not isMailingGrid}">
        </div>
        </c:if>
    </tiles:put>
</tiles:insert>

<script id="link-table-row" type="text/x-mustache-template">
    <tr id="linkProperty_{{= count }}" data-link-id="{{= count }}" >
        <td>
            <input class="form-control" type="text" id="${PROPERTY_NAME_PREFIX}{{= count }}" name="${PROPERTY_NAME_PREFIX}{{= count }}" value="{{= linkName }}"/>
        </td>
        <td>
            <input class="form-control" type="text" id="${PROPERTY_VALUE_PREFIX}{{= count }}" name="${PROPERTY_VALUE_PREFIX}{{= count }}" value="{{= linkValue }}"/>
        </td>
        <td>
            <a href="#" class="btn btn-regular btn-alert" data-tooltip="<bean:message key="button.Delete" />" data-action="link-details-delete-link" data-link-id="{{= count }}">
                <i class="icon icon-trash-o"></i>
            </a>
        </td>
    </tr>
</script>
