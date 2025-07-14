<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="c"			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" 			uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="emm" 		uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="display" 	uri="http://displaytag.sf.net" %>
<%@ taglib prefix="mvc" 		uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<emm:ShowByPermission token="webhooks.enable">
	<mvc:form servletRelativeAction="/webhooks/enableInterface.action" modelAttribute="enableInterfaceForm"   method="POST">
		<div class="tile">
			<div class="tile-header">
	        	<h2 class="headline">
		            <mvc:message code="webhooks.interface"/>
		        </h2>
	
				<ul class="tile-header-actions">
					<li>
					<%--
						<button type="button" class="btn btn-regular btn-primary" data-form-url="${saveUrl}" data-form-set="showStatistic: true" data-form-submit="">
							<i class="icon icon-refresh"></i> <span class="text"><mvc:message
									code="button.save.evaluate" /></span>
						</button>
						--%>
						<button type="submit" class="btn btn-regular btn-primary">
							<i class="icon icon-refresh"></i> <span class="text">
							<mvc:message code="button.Save" /></span>
						</button>
					</li>
				</ul>
			</div>
			<div class="tile-content">
				<div class="form-group">
					<div class="col-sm-6 col-xs-8">
						<label class="control-label pull-right checkbox-control-label" for="enableWebhooksInterface">
							<mvc:message code="webhooks.enable"/>
						</label>
					</div>
					<div class="col-sm-6 col-xs-4">
						<label class="toggle">
							<mvc:checkbox id="enableWebhooksInterface" path="enable" value="true" />
							<div class="toggle-control"></div>
						</label>
					</div>
				</div>
			</div>
		</div>
	</mvc:form>
</emm:ShowByPermission>

<div class="tile">
	<div class="tile-header">
		<h2 class="headline">
			<mvc:message code="default.Overview" />
		</h2>
	</div>
	<div class="tile-content">
		<div class="table-wrapper">
			<display:table
				class="table table-bordered table-striped table-hover js-table"
				id="webhook" name="WEBHOOKS" pagesize="${targetForm.numberOfRows}"
				sort="list" requestURI="/webhooks.action" excludedParams="*">

				<c:url var="VIEW_URL" value="/webhooks/${webhook.eventType}/view.action" />

				<display:column headerClass="js-table-sort squeeze-column" titleKey="webhooks.event">
					<a href="${VIEW_URL}"> 
						<mvc:message code="webhooks.event.${webhook.eventType}.label" />
					</a>
				</display:column>
				<display:column headerClass="js-table-sort" titleKey="webhooks.url">
					<a href="${VIEW_URL}">${webhook.url}</a>
				</display:column>
				<display:column headerClass="js-table-sort"	titleKey="webhooks.profilefields">
					<a href="${VIEW_URL}">${webhook.profilefieldsAsString}</a>
				</display:column>
			</display:table>
		</div>
	</div>
</div>
