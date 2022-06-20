<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComMailingContentAction" %>
<%@ page import="com.agnitas.web.ComMailingSendActionBasic" %>
<%@ page import="org.agnitas.beans.EmmLayoutBase" %>
<%@ page import="com.agnitas.beans.Mailing" %>
<%@ page import="com.agnitas.emm.core.mailing.web.MailingPreviewHelper" %>

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingContentForm" type="com.agnitas.web.ComMailingContentForm"--%>
<%--@elvariable id="IS_MAILING_EDITABLE" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingExclusiveLockingAcquired" type="java.lang.Boolean"--%>
<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>

<c:set var="ACTION_VIEW" value="<%=ComMailingContentAction.ACTION_VIEW%>"/>
<c:set var="ACTION_PREVIEW_SELECT" value="<%=ComMailingSendActionBasic.ACTION_PREVIEW_SELECT%>"/>
<c:set var="ACTION_PREVIEW" value="<%=ComMailingSendActionBasic.ACTION_PREVIEW%>"/>
<c:set var="ACTION_VIEW_CONTENT" value="<%= ComMailingContentAction.ACTION_VIEW_CONTENT %>"/>
<c:set var="ACTION_IMPORT_CONTENT" value="<%= ComMailingContentAction.ACTION_IMPORT_CONTENT %>"/>
<c:set var="ACTION_GENERATE_TEXT_FROM_HTML"	value="<%= ComMailingContentAction.ACTION_GENERATE_TEXT_FROM_HTML_CONFIRM %>"/>

<c:set var="LIVEPREVIEW_POSITION_BOTTOM" value="<%= EmmLayoutBase.LIVEPREVIEW_POSITION_BOTTOM %>"/>
<c:set var="LIVEPREVIEW_POSITION_DEACTIVATE" value="<%= EmmLayoutBase.LIVEPREVIEW_POSITION_DEACTIVATE %>"/>
<c:set var="LIVEPREVIEW_POSITION_RIGHT" value="<%= EmmLayoutBase.LIVEPREVIEW_POSITION_RIGHT %>"/>
<c:set var="MENU_POSITION_LEFT" value="<%= EmmLayoutBase.MENU_POSITION_LEFT %>"/>
<c:set var="MENU_POSITION_TOP" value="<%= EmmLayoutBase.MENU_POSITION_TOP %>"/>
<c:set var="PREVIEW_FORMAT_HTML" value="<%= MailingPreviewHelper.INPUT_TYPE_HTML %>"/>
<c:set var="PREVIEW_FORMAT_TEXT" value="<%= MailingPreviewHelper.INPUT_TYPE_HTML %>"/>

<c:set var="isMailingGrid" value="${mailingContentForm.gridTemplateId > 0}" scope="request"/>

<emm:HideByPermission token="mailing.editor.hide">
    <jsp:include page="/${emm:ckEditorPath(pageContext.request)}/ckeditor-emm-helper.jsp">
        <jsp:param name="toolbarType" value="${emm:isCKEditorTrimmed(pageContext.request) ? 'Trimmed' : 'EMM'}"/>
    </jsp:include>
</emm:HideByPermission>                                    

<tiles:insert page="template.jsp">
    <tiles:put name="header" type="string">
        <ul class="tile-header-nav">
            <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
        </ul>
    </tiles:put>

    <tiles:put name="content" type="string">
        <c:if test="${isMailingGrid}">
            <div class="tile-content-padded">
        </c:if>
		<c:choose>
			<c:when test="${not isPostMailing}">
				<div class="row">
					<div class="col-xs-12" data-view-split="col-md-6 split-1-1" data-view-block="col-xs-12" data-view-hidden="col-xs-12">
						<agn:agnForm action="/mailingcontent"  data-form="resource" data-controller="mailing-content-controller">
							<html:hidden property="mailingID"/>
							<html:hidden property="showDateSettings"/>
							<html:hidden property="action" value=""/>

							<div class="tile">
								<div class="tile-header">
									<h2 class="headline"><bean:message key="mailing.TextModules"/></h2>
								</div>

								<div class="tile-content" data-initializer="mailing-content-overview">
									<div class="tile-content-forms">
										<%@include file="mailing-content-list-contentsource-list.jsp"  %>

										<c:if test="${mailingContentForm.enableTextGeneration and not mailingContentForm.worldMailingSend}">
											<div class="form-group">
												<div class="col-sm-4">
													<label class="control-label">
														<bean:message key="mailing.option"/>
													</label>
												</div>

												<div class="col-sm-8">
													<agn:agnLink page="/mailingcontent.do?action=${ACTION_GENERATE_TEXT_FROM_HTML}&mailingID=${mailingContentForm.mailingID}"
																 tabindex="-1" styleClass="btn btn-regular" data-controls-group="editing"
																 data-confirm="">
														<i class="icon icon-exchange"></i>
														<span class="text"><bean:message key="mailing.GenerateText"/></span>
													</agn:agnLink>
												</div>
											</div>
										</c:if>

										<%@include file="mailing-content-list-contentsource-datelimit.jsp" %>
									</div>

									<table class="table table-bordered table-striped table-hover js-table" id="contentList"
										   data-controls-group="editing">
										<thead>
											<tr>
												<th><bean:message key="Text_Module"/></th>
												<th><bean:message key="Target"/></th>
												<th><bean:message key="default.Content"/></th>
											</tr>
										</thead>
										<tbody id="table_body">
										</tbody>
									</table>

									<%--todo: mailingContentForm.tags contains extra information.--%>
									<%--todo: Please create DTO for that entity during migration --%>
									<script data-initializer="mailing-content-initializer" type="application/json">
										{
											"targetGroupList": ${emm:toJson(mailingContentForm.availableTargetGroups)},
											"interestGroupList": ${emm:toJson(mailingContentForm.availableInterestGroups)},
											"dynTagNames": ${emm:toJson(mailingContentForm.dynTagNames)},
											"dynTagsMap": ${emm:toJson(mailingContentForm.tags)},
											"isMailingExclusiveLockingAcquired": ${isMailingExclusiveLockingAcquired},
											"isEditableMailing": ${IS_MAILING_EDITABLE}
										}
									</script>

									<script id="config:mailing-content-overview" type="application/json">
										{
											"mailingId": ${mailingContentForm.mailingID},
											"isMailingExclusiveLockingAcquired": ${isMailingExclusiveLockingAcquired},
											"anotherLockingUserName": "${mailingContentForm.anotherLockingUserName}",
											"isEditableMailing": ${IS_MAILING_EDITABLE}
										}
									</script>
								</div>
								<!-- Tile Content END -->
							</div>
							<!-- Tile END -->
						</agn:agnForm>
					</div>
					<!-- col END -->

					<emm:ShowByPermission token="mailing.send.show">
						<c:url var="previewLink" value="/mailingsend.do">
							<c:param name="action" value="${ACTION_PREVIEW_SELECT}"/>
							<c:param name="mailingID" value="${mailingContentForm.mailingID}"/>
							<c:param name="previewForm.pure" value="true"/>
							<c:param name="previewForm.format" value="${mailingContentForm.showHTMLEditor ? PREVIEW_FORMAT_HTML : PREVIEW_FORMAT_TEXT}"/>
						</c:url>

						<div class="hidden" data-view-split="col-md-6" data-view-block="col-xs-12" data-view-hidden="hidden">
							<div data-load="${previewLink}" data-load-target="#preview"></div>
						</div>
					</emm:ShowByPermission>
				</div>
			</c:when>
			<c:otherwise>
				<%@include file="mailing-type-post.jspf" %>
			</c:otherwise>
		</c:choose>
        <c:if test="${isMailingGrid}">
            </div>
        </c:if>
    </tiles:put>
</tiles:insert>

<c:set var="mailingId" value="${mailingContentForm.mailingID}"/>

<%@ include file="fragments/content-editor-template.jspf"  %>

<%@ include file="fragments/enlarged-content-editor-template.jspf"  %>

<%@ include file="fragments/mailing-content-table-entry-template.jspf"  %>
