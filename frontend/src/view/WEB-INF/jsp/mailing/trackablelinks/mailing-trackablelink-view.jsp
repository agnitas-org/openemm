<%@ page language="java" contentType="text/html; charset=utf-8" import="com.agnitas.web.*" errorPage="/error.do" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="emm"   uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="linkId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="usage" type="java.lang.Boolean"--%>
<%--@elvariable id="urlModified" type="java.lang.Boolean"--%>
<%--@elvariable id="isUrlEditingAllowed" type="java.lang.Boolean"--%>
<%--@elvariable id="SHOW_CREATE_SUBSTITUTE_LINK" type="java.lang.Boolean"--%>
<%--@elvariable id="altText" type="java.lang.String"--%>
<%--@elvariable id="originalUrl" type="java.lang.String"--%>
<%--@elvariable id="workflowForwardParams" type="java.lang.String"--%>
<%--@elvariable id="notFormActions" type="java.util.List<org.agnitas.actions.EmmAction>"--%>
<%--@elvariable id="trackableLinkForm" type="com.agnitas.emm.core.trackablelinks.form.TrackableLinkForm"--%>

<c:set var="isMailingGrid" value="${not empty gridTemplateId and gridTemplateId gt 0}" scope="request"/>

<tiles:insert page="/WEB-INF/jsp/mailing/template.jsp">
    <tiles:put name="header" type="string">
        <h2 class="headline"><mvc:message code="TrackableLink.editLink"/></h2>

        <ul class="tile-header-nav">
            <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
        </ul>
    </tiles:put>
    
    <tiles:put name="content" type="string">
        <c:if test="${not isMailingGrid}">
            <div class="row">
        </c:if>

        <div class="${isMailingGrid ? '' : 'col-xs-12 row-1-1'}" data-controller="trackable-link">
            <mvc:form servletRelativeAction="/mailing/${mailingId}/trackablelink/${linkId}/save.action" 
                      id="trackableLinkForm" 
                      modelAttribute="trackableLinkForm"
                      data-form="resource">

                <c:if test="${not isMailingGrid}">
                    <div class="tile">
                        <div class="tile-header">
                            <h2 class="headline">
                                <mvc:message code="TrackableLink.editLink"/>
                            </h2>
                        </div>
                </c:if>

                <div class="${isMailingGrid ? '' : 'tile-content'} tile-content-forms">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="linkUrl">
                                <mvc:message code="URL" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <c:choose>
                                <c:when test="${isUrlEditingAllowed}">
                                    <mvc:text path="url" id="linkUrl" cssClass="form-control"/>
                                </c:when>
                                <c:otherwise>
                                    <mvc:text path="" value="${trackableLinkForm.url}" id="linkUrl" cssClass="form-control" readonly="true"/>
                                    <mvc:hidden path="url"/>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <c:if test="${not empty originalUrl}">
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label class="control-label" for="originalUrl">
                                    <mvc:message code="mailing.trackablelinks.original_url" />
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <input type="text" value="${originalUrl}" id="originalUrl" readonly class="form-control">
                            </div>
                        </div>
                    </c:if>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="linkName">
                                <mvc:message code="Description" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <mvc:text path="shortname" id="linkName" cssClass="form-control" />
                        </div>
                    </div>

                    <c:if test="${not empty altText}">
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label class="control-label" for="altText">
                                    <mvc:message code="Title" />
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <mvc:text path="" value="${altText}" id="altText" cssClass="form-control" readonly="true"/>
                            </div>
                        </div>
                    </c:if>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="usage">
                                <mvc:message code="LinkTracking" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <c:choose>
                                <c:when test="${fn:contains(trackableLinkForm.url, '##')}">
                                    <mvc:message code="Text_and_HTML_Version" />
                                </c:when>
                                <c:otherwise>
                                    <mvc:select path="usage" id="usage" cssClass="form-control" data-action="link-details-trackable">
                                        <mvc:option value="0"><mvc:message code="mailing.Not_Trackable" /></mvc:option>
                                        <mvc:option value="1"><mvc:message code="Only_Text_Version" /></mvc:option>
                                        <mvc:option value="2"><mvc:message code="Only_HTML_Version" /></mvc:option>
                                        <mvc:option value="3"><mvc:message code="Text_and_HTML_Version" /></mvc:option>
                                    </mvc:select>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="linkAction">
                                <mvc:message code="action.Action" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <mvc:select path="action" size="1" id="linkAction" cssClass="form-control">
                                <mvc:option value="0"><mvc:message code="settings.No_Action" /></mvc:option>
                                <c:forEach var="action" items="${notFormActions}">
                                    <mvc:option value="${action.id}">${action.shortname}</mvc:option>
                                </c:forEach>
                            </mvc:select>
                        </div>
                    </div>

                    <%@include file="fragments/mailing-trackablelink-view-revenue.jspf" %>
                    <%@include file="fragments/mailing-trackablelink-static-link.jsp" %>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="administrativeCheckbox"> 
                                <mvc:message code="report.adminLinks" />
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <mvc:checkbox path="admin" id="administrativeCheckbox"/>
                        </div>
                    </div>
                        
                    <c:if test="${SHOW_CREATE_SUBSTITUTE_LINK}">
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label class="control-label" for="createSubstituteLink"> 
                                    <mvc:message code="CreateSubstituteLink" />
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <mvc:checkbox path="createSubstituteForAgnDynMulti" styleId="createSubstituteLink"/>
                            </div>
                        </div>
                    </c:if>								
                    <emm:ShowByPermission token="mailing.extend_trackable_links">
                        <div id="extensions" data-initializer="trackable-link-extensions">
                            <script id="config:trackable-link-extensions" type="application/json">
                                {
                                    "extensions": ${emm:toJson(trackableLinkForm.extensions)}
                                }
                            </script>
                            <%@ include file="fragments/extensions-table.jspf" %>
                        </div>
                    </emm:ShowByPermission>
                </div>
                <c:if test="${not isMailingGrid}">
                    </div>
                </c:if>
            </mvc:form>
        </div>
        <c:if test="${not isMailingGrid}">
            </div>
        </c:if>
    </tiles:put>
    
    <tiles:putList name="footerItems">
        <tiles:add>
            <c:url var="backLink" value="/mailing/${mailingId}/trackablelink/list.action">
                <c:param name="scrollToLinkId" value="${linkId}"/>
                <c:if test="${not empty workflowForwardParams}">
                    <c:param name="forwardParams" value="${workflowForwardParams}"/>
                </c:if>
            </c:url>
            <a id="buttonBack" class="btn btn-large pull-left" href="${backLink}">
                <span class="text">
                    <mvc:message code="button.Back"/>
                </span>
            </a>
        </tiles:add>
        <tiles:add>
            <button type="button" class="btn btn-large btn-primary pull-right" data-form-target='#trackableLinkForm' data-action="save">
                <span class="text">
                    <mvc:message code="button.Save"/>
                </span>
            </button>
        </tiles:add>
    </tiles:putList>
</tiles:insert>
