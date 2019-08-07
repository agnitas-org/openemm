<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.ecs.EcsGlobals" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<c:set var="isMailingGrid" value="${ecsForm.isMailingGrid}" scope="request"/>
<c:set var="GROSS_CLICKS" value="<%= EcsGlobals.MODE_GROSS_CLICKS %>"/>
<c:set var="NET_CLICKS" value="<%= EcsGlobals.MODE_NET_CLICKS %>"/>
<c:set var="PURE_MAILING" value="<%= EcsGlobals.MODE_PURE_MAILING %>"/>


<agn:agnForm action="/ecs_stat">
    <html:hidden property="method" value="view"/>
    <html:hidden property="mailingID"/>

    <c:set var="tileHeaderActions" scope="page">
        <c:if test="${ecsForm.selectedRecipient > 0}">
            <li>
                <a href="#" class="link" data-tooltip="<bean:message key='export.message.pdf'/>" data-prevent-load data-form-set="method: export" data-form-submit-static>
                    <i class="icon icon-cloud-download"></i>
                    <bean:message key="Export"/>
                </a>
            </li>
        </c:if>

        <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                <i class="icon icon-eye"></i>
                <span class="text"><bean:message key="default.View"/></span>
                <i class="icon icon-caret-down"></i>
            </a>
            <ul class="dropdown-menu">
                <li class="dropdown-header"><bean:message key="ecs.ViewMode"/></li>
                <li>
                    <label class="label">
                        <agn:agnRadio property="viewMode" value="${GROSS_CLICKS}"/>
                        <span class="label-text"><bean:message key="statistic.Clicks"/></span>
                    </label>
                </li>
                <li>
                    <label class="label">
                        <agn:agnRadio property="viewMode" value="${NET_CLICKS}"/>
                        <span class="label-text"><bean:message key="statistic.clicker"/></span>
                    </label>
                </li>
                <li>
                    <label class="label">
                        <agn:agnRadio property="viewMode" value="${PURE_MAILING}"/>
                        <span class="label-text"><bean:message key="ecs.PureMailing"/></span>
                    </label>
                </li>
                <li class="divider"></li>

                <li class="dropdown-header"><bean:message key="default.Size"/></li>
                <li>
                    <label class="label">
                        <agn:agnRadio property="previewSize" value="1"/>
                        <span class="label-text"><bean:message key="predelivery.desktop"/></span>
                    </label>
                </li>
                <li>
                    <label class="label">
                        <agn:agnRadio property="previewSize" value="2"/>
                        <span class="label-text"><bean:message key="mailing.PreviewSize.MobilePortrait"/></span>
                    </label>
                </li>
                <li>
                    <label class="label">
                        <agn:agnRadio property="previewSize" value="3"/>
                        <span class="label-text"><bean:message key="mailing.PreviewSize.MobileLandscape"/></span>
                    </label>
                </li>
                <li>
                    <label class="label">
                        <agn:agnRadio property="previewSize" value="4"/>
                        <span class="label-text"><bean:message key="mailing.PreviewSize.TabletPortrait"/></span>
                    </label>
                </li>
                <li>
                    <label class="label">
                        <agn:agnRadio property="previewSize" value="5"/>
                        <span class="label-text"><bean:message key="mailing.PreviewSize.TabletLandscape"/></span>
                    </label>
                </li>

                <li class="divider"></li>
                <li>
                    <p>
                        <button type="button" class="btn btn-block btn-primary btn-regular" data-form-submit><i class="icon icon-refresh"></i> <bean:message key="button.Refresh"/></button>
                    </p>
                </li>
            </ul>
        </li>
    </c:set>

    <tiles:insert page="/WEB-INF/jsp/mailing/template.jsp">
            <%-- There're no footer items --%>

        <c:if test="${isMailingGrid}">
            <tiles:put name="header" type="string">
                <ul class="tile-header-nav">
                    <!-- Tabs BEGIN -->
                    <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
                    <!-- Tabs END -->
                </ul>

                <ul class="tile-header-actions">${tileHeaderActions}</ul>
            </tiles:put>
        </c:if>

        <tiles:put name="content" type="string">

            <c:if test="${not isMailingGrid}">
            <div class="tile">
                <div class="tile-header">

                    <h2 class="headline">${ecsForm.shortname}</h2>
                    <ul class="tile-header-actions">${tileHeaderActions}</ul>

                </div>
                <div class="tile-content">
            </c:if>

                    <div class="mailing-preview-header">
                        <div class="form-group">
                            <div class="col-sm-3 col-xs-12">
                                <label for="select" class="control-label"><bean:message key="Recipient"/></label>
                            </div>
                            <div class="col-sm-9 col-xs-12">
                                <agn:agnSelect property="selectedRecipient" styleClass="form-control" data-form-submit="">
                                    <c:forEach var="recipient" items="${ecsForm.testRecipients}">
                                        <agn:agnOption value="${recipient.key}">
                                            ${recipient.value}
                                        </agn:agnOption>
                                    </c:forEach>
                                </agn:agnSelect>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-3 col-xs-12">
                                <label for="colorDescription" class="control-label"><bean:message key="ecs.ColorCoding"/></label>
                            </div>

                            <div id="colorDescription" class="col-sm-9 col-xs-12">
                                <div class="form-control form-control-unstyled">
                                    <ul class="list-floated list-spaced">
                                        <c:forEach var="color" items="${ecsForm.rangeColors}" varStatus="rowCounter">
                                            <li>
                                                <i class="icon icon-circle" style="color:#${color.color};"></i> <bean:message key="Heatmap.max"/>&nbsp;${color.rangeEnd}%
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>

                    <c:if test="${ecsForm.selectedRecipient > 0}">
                        <div class="${isMailingGrid ? 'tile-content-padded' : 'mailing-preview-wrapper'}">
                            <div>
                                <c:url var="heatmapURL" value="${ecsForm.statServerUrl}/ecs_view;jsessionid=${pageContext.request.session.id}">
                                    <c:param name="mailingID" value="${ecsForm.mailingID}"/>
                                    <c:param name="recipientId" value="${ecsForm.selectedRecipient}" />
                                    <c:param name="viewMode" value="${ecsForm.viewMode}" />
                                </c:url>
                                <div class="mailing-preview-scroller center-block">
                                    <iframe src="${heatmapURL}" id="ecs_frame" class="mailing-preview-frame js-simple-iframe" data-height-extra="20" data-max-width="${ecsForm.previewWidth}" style="width: ${ecsForm.previewWidth}px" ></iframe>
                                </div>
                            </div>
                        </div>
                    </c:if>

                    <div class="tile-content-padded">
                        <p><bean:message key="ecs.Heatmap.description"/></p>
                    </div>
        <c:if test="${not isMailingGrid}">
                </div>
            </div>
        </c:if>
        </tiles:put>
    </tiles:insert>

</agn:agnForm>
