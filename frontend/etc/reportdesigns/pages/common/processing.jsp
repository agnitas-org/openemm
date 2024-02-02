<%@ page import="com.agnitas.reporting.birt.external.utils.ComBirtResources" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page session="false" buffer="none" %>

<%-----------------------------------------------------------------------------
	Progress page
-----------------------------------------------------------------------------%>
<%
    boolean rtl = false;
    String rtlParam = request.getParameter("__rtl");
    if ( rtlParam != null )
    {
        rtl = Boolean.getBoolean(rtlParam);
    }
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
    <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
    <LINK REL="stylesheet" HREF="<%= request.getContextPath( ) + "/webcontent/birt/styles/style.css" %>" TYPE="text/css">
</HEAD>
<BODY STYLE="background-color: #ECE9D8;">
<DIV ID="progressBar" ALIGN="center">
    <TABLE WIDTH="250px" CLASS="birtviewer_progresspage" CELLSPACING="10px">
        <TR>
            <TD ALIGN="center">
                <B>
                    <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                    <%= ComBirtResources.getMessage("birt.viewer.progressbar.promt", request.getLocale())%>
                </B>
            </TD>
        </TR>
        <TR>
            <TD ALIGN="center">
                <IMG SRC="<%= request.getContextPath( ) + "/webcontent/birt/images/" + (rtl?"Loading_rtl":"Loading") + ".gif" %>" ALT="Progress Bar Image"/>
            </TD>
        </TR>
    </TABLE>
</DIV>
</BODY>
</HTML>