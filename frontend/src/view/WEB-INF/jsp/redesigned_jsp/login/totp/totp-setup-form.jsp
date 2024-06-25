<%@ page language="java" pageEncoding="UTF-8" errorPage="/errorRedesigned.action"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring"%>

<%--@elvariable id="supportMailAddress" type="java.lang.String"--%>
<%--@elvariable id="adminMailAddress" type="java.lang.String"--%>
<%--@elvariable id="layoutdir" type="java.lang.String"--%>

<mvc:form servletRelativeAction="/logon/totp-setupRedesigned.action" data-form-focus="authenticationCode" modelAttribute="form" cssClass="row g-6">
	<div class="col-12">
		<p class="fs-1 fw-medium text-center">
			<mvc:message code="totp.scanCode" />
		</p>
	</div>

	<div class="col-12 flex-center">
		<img src="<c:url value="/logon/totp-qrcode.action" />" />
	</div>

	<div class="col-12 d-flex flex-column gap-3">
		<p class="fs-1 fw-medium">
			<mvc:message code="totp.enterCode" />
		</p>

		<mvc:text path="totp" cssClass="form-control" maxlength="6" placeholder="123456" />

		<button type="submit" class="btn btn-light btn-lg flex-grow-1">
			<span class="text"><mvc:message code="logon.totp.send" /></span>
			<i class="icon icon-caret-right"></i>
		</button>
	</div>
</mvc:form>
