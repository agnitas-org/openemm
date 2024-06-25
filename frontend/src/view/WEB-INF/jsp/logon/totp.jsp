<%@ page language="java" pageEncoding="UTF-8" errorPage="/error.action"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<%--@elvariable id="supportMailAddress" type="java.lang.String"--%>
<%--@elvariable id="adminMailAddress" type="java.lang.String"--%>
<%--@elvariable id="layoutdir" type="java.lang.String"--%>

<s:message var="title" code="logon.title" />

<c:url var="editionLogoSrc" value="/layout/0/edition_logo.png" />
<c:url var="agnitasEmmLogoSvgSrc" value="/layout/0/logo.svg" />
<c:url var="agnitasEmmLogoPngSrc" value="/layout/0/logo.png" />

<c:if test="${not empty layoutdir and layoutdir != 'assets/core'}">
	<%-- Use custom title and edition logo --%>
	<s:message var="title"
		code="logon.title.${fn:substringAfter(layoutdir, 'assets/')}"
		text="${title}" />
</c:if>

<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<sec:csrfMetaTags />

<title>${title}</title>

<link rel="shortcut icon" href="<c:url value="/favicon.ico"/>">

<tiles:insertTemplate template="/WEB-INF/jsp/assets.jsp"/>
</head>
<body>
<div style="padding: 4vw;">
	<mvc:form servletRelativeAction="/logon/totp.action" data-form-focus="authenticationCode" modelAttribute="form">
		<div style="line-height:1.8">
			<div class="col-md-12">
				<div class="tile">
					<div class="tile-header">
						<div class="headline"><mvc:message code="totp.headline" /></div>
					</div>
					<div class="tile-content tile-content-forms" style="padding-bottom: 15px;">
						<div class="form-group">
							<div class="col-md-3">
								<div class="form-group">
									<div class="col-md-12">
										<p style="font-size: 16px; font-weight: bold;"><mvc:message code="totp.scanCode" /></p>
									</div>
								</div>
								<div class="col-sm-8 col-md-12 col-lg-8">
									<div class="form-group">
										<mvc:text path="totp" cssClass="form-control" maxlength="6" placeholder="123456" />
									</div>
								</div>
								
								<div class="col-sm-4 col-md-12 col-lg-4">
									<div class="form-group">
										<button type="submit" class="btn btn-primary btn-regular full-width whitespace_normal">
											<mvc:message code="logon.totp.send" />
										</button>
									</div>
								</div>

			                    <c:if test="${TOTP_TRUST_DEVICE_ENABLED}">
									<div class="col-sm-4 col-md-12 col-lg-4">
										<div class="form-group">
	                        					<label class="toggle">
												<mvc:checkbox path="trustDevice" id="trustDevice" value="true"/>
	                        						<div class="toggle-control"></div>
	                        					</label>
	               				            <label class="control-label checkbox-control-label" for="trustedDevice"><mvc:message code="logon.hostauth.trustDevice"/></label>
										</div>
									</div>
								</c:if>
								
								<div class="form-group">
									<div class="col-md-12">
										<div id="notifications-container" style="position: initial; width: auto;">
											<script type="text/javascript" data-message="">
												<emm:messages var="msg" type="error">
													AGN.Lib.Messages('<mvc:message code="Error"/>', '${emm:escapeJs(msg)}', 'alert');
												</emm:messages>
												<emm:messages var="msg" type="warning">
													AGN.Lib.Messages('<mvc:message code="warning"/>', '${emm:escapeJs(msg)}', 'warning');
												</emm:messages>
											</script>
										</div>
									</div>
								</div>
							</div>
							<div class="col-md-9">
								<div class="tile">
									<div class="tile-header">
										<div class="headline">
											<mvc:message code="totp.setup.info.headline.twoFactor" />
										</div>
									</div>
									<div class="tile-content tile-content-forms-small" style="padding: 15px;">
											<p class="well no-border"><mvc:message code="totp.setup.info.headline.twoFactor" /></p>
									</div>
								</div>
								<div class="tile">
									<div class="tile-header">
										<div class="headline">
											<mvc:message code="totp.info.headline.availableApps" />
										</div>
									</div>
									<div class="tile-content tile-content-forms-small" style="padding: 15px;">
											<p class="well no-border"><mvc:message code="totp.info.content.availableApps" /></p>
											<br/>
											<p class="well no-border"><mvc:message code="totp.info.content.availableApps.listHeadline" /></p>
											<ul class="list-group">
												<li class="list-group-item no-border">
													<label class="item-styled item-style-md bullet-style">
														<mvc:message code="totp.info.content.availableApps.listContent.1" />
													</label>
												</li>
												<li class="list-group-item no-border">
													<label class="item-styled item-style-md bullet-style">
														<mvc:message code="totp.info.content.availableApps.listContent.2" />
													</label>
												</li>
												<li class="list-group-item no-border">
													<label class="item-styled item-style-md bullet-style">
														<mvc:message code="totp.info.content.availableApps.listContent.3" />
													</label>
												</li>
											</ul>
									</div>
								</div>
								<div class="tile">
									<div class="tile-header">
										<a class="headline" href="#" data-toggle-tile="#tile-issues">
											<i class="icon tile-toggle icon-angle-up"></i>
											<mvc:message code="totp.info.headline.loginProblems" />
										</a>
									</div>
									<div class="tile-content tile-content-forms-small hidden" id="tile-issues" style="padding: 15px;">
											<p class="well no-border"><mvc:message code="totp.info.content.loginProblems.1" /></p>
											<br/>
											<p class="well no-border"><b><mvc:message code="totp.info.content.loginProblems.listHeadline.1" /></b></p>
											<ul class="list-group">
												<li class="list-group-item no-border">
													<label class="item-styled item-style-md bullet-style">
														<mvc:message code="totp.info.content.loginProblems.listContent.1" />
													</label>
												</li>
											</ul>
											<p class="well no-border"><b><mvc:message code="totp.info.content.loginProblems.listHeadline.2" /></b></p>
											<ul class="list-group">
												<li class="list-group-item no-border">
													<label class="item-styled item-style-md bullet-style">
														<mvc:message code="totp.info.content.loginProblems.listContent.2" />
													</label>
												</li>
											</ul>
											<p class="well no-border"><b><mvc:message code="totp.info.content.loginProblems.listHeadline.3" /></b></p>
											<ul class="list-group">
												<li class="list-group-item no-border">
													<label class="item-styled item-style-md bullet-style">
														<mvc:message code="totp.info.content.loginProblems.listContent.3" />
													</label>
												</li>
											</ul>
											<p class="well no-border"><b><mvc:message code="totp.info.content.loginProblems.listHeadline.4" /></b></p>
											<ul class="list-group">
												<li class="list-group-item no-border">
													<label class="item-styled item-style-md bullet-style">
														<mvc:message code="totp.info.content.loginProblems.listContent.4" />
													</label>
												</li>
											</ul>
											<p class="well no-border"><b><mvc:message code="totp.info.content.loginProblems.2" /></b></p>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</mvc:form>
</div>

	<%@include file="/WEB-INF/jsp/additional.jsp"%>
</body>
</html>
