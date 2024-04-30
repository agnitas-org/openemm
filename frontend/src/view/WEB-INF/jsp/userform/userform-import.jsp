<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:url var="importAction" value="/webform/importUserForm.action" />
<mvc:form method="post" action="${importAction}" enctype="multipart/form-data" data-form="resource">
	<div class="tile">
		<div class="tile-header">
			<h2 class="headline">
				<bean:message key="forms.import"/>
			</h2>
		</div>
		<div class="tile-content">
			<div class="tile-content-forms form-vertical">
				<div class="form-group">
					<div class="input-group">
						<div class="input-group-controls">
							<input type="file" name="uploadFile" class="form-control">
						</div>
						<div class="input-group-btn">
							<button type="button" class="btn btn-regular btn-primary" data-form-submit>
								<i class="icon icon-cloud-upload"></i>
								<span class="text">
									<bean:message key="forms.import"/>
								</span>
							</button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</mvc:form>
