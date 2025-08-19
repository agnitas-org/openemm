<%@ page language="java" pageEncoding="UTF-8" errorPage="/errorRedesigned.action"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring"%>

<mvc:form servletRelativeAction="/logon/totpRedesigned.action" data-form-focus="totp" modelAttribute="form" cssClass="row g-3">
	<div class="col-12">
		<p class="fs-1 fw-medium"><mvc:message code="totp.enterCode" /></p>
	</div>

	<div class="col-12">
		<mvc:text id="totp" path="totp" cssClass="form-control" maxlength="6" placeholder="123456" />
	</div>

	<c:if test="${TOTP_TRUST_DEVICE_ENABLED}">
		<div class="col-12">
			<div class="form-check form-switch">
				<mvc:checkbox path="trustDevice" id="trustDevice" value="true" cssClass="form-check-input" role="switch"/>
				<label class="form-label form-check-label" for="trustDevice">
					<mvc:message code="logon.hostauth.trustDevice"/>
				</label>
			</div>
		</div>
	</c:if>

	<div class="col-12">
		<button data-form-submit-static class="btn btn-light btn-lg w-100">
			<span class="text"><mvc:message code="logon.totp.send" /></span>
			<i class="icon icon-caret-right"></i>
		</button>
	</div>
</mvc:form>
