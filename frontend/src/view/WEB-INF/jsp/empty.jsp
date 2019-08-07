<%@ page language="java" import="org.springframework.web.context.support.WebApplicationContextUtils, com.agnitas.dao.LandingpageDao" pageEncoding="UTF-8" errorPage="/error.do"%>
<%
	LandingpageDao landingpageDao = (LandingpageDao) WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext()).getBean("LandingpageDao");
	String rdirectUrl = landingpageDao.getLandingPage(request.getRequestURL().toString());

	if (!"blank".equalsIgnoreCase(rdirectUrl)) {
		if (rdirectUrl != null && !rdirectUrl.startsWith("http")) {
			rdirectUrl = "http://" + rdirectUrl;
		}
%>
<meta http-equiv="refresh" content="0; URL=<%= rdirectUrl %>">
<%	} %>
