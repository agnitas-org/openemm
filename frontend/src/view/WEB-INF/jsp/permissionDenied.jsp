<%@page import="org.agnitas.util.AgnUtils"%>
<%@ page isErrorPage="true" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<emm:setAbsolutePath var="absoluteImagePath" path="${emmLayoutBase.imagesURL}"/>

<div class="login_page_root_container">
	<div class="login_page_top_spacer"></div>
	<div class="col-sm-12">
		<div class="col-sm-4"></div>
		<div class="col-sm-4">
			<div class="tile">
				<div class="tile-header">
					<p class="headline">
						<i class="icon-fa5 icon-fa5-ban"></i>
						<mvc:message code="permission.denied.title"/>
					</p>
				</div>
				<div class="tile-content tile-content-forms">
					<p class="well no-border">
						<mvc:message code="permission.denied.message"/>
						<br/>
						<mvc:message code="permission.denied.message.extended"/> (<%= AgnUtils.getAdmin(request).getFirstName() %> <%= AgnUtils.getAdmin(request).getFullname() %>, <a href="mailto:<%= AgnUtils.getAdmin(request).getEmail() %>"><%= AgnUtils.getAdmin(request).getEmail() %></a>)
					</p>
				</div>
			</div>
		</div>
		<div class="col-sm-4"></div>
	</div>
</div>
