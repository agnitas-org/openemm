<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:url var="action" value="/import/execute.action"/>
<mvc:form action="${action}" id="mailingImport" enctype="multipart/form-data" data-form="resource">
	<div class="tile">
		<div class="tile-header">
			<h2 class="headline">
				<mvc:message code="mailing.import" />
			</h2>
		</div>
		<div class="tile-content">
			<div class="tile-content-forms form-vertical">
				<div class="form-group">
					<div class="input-group">
						<div class="input-group-controls">
							<input type="file" id="uploadFile" name="uploadFile" class="form-control" />
						</div>
						<div class="input-group-btn">
							<button type="button" class="btn btn-regular btn-primary" data-form-submit>
								<i class="icon icon-cloud-upload"></i>
								<span class="text">
									<mvc:message code="mailing.import"/>
								</span>
							</button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</mvc:form>
