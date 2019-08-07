<%@ page contentType="text/html; charset=utf-8" %>
<%@ page session="false" buffer="none" %>
<%@ page import="org.eclipse.birt.core.exception.BirtException,
				 org.eclipse.birt.report.utility.ParameterAccessor,
				 java.io.PrintWriter" %>
<%@ page import="com.agnitas.reporting.birt.external.utils.ComBirtResources" %>

<%-----------------------------------------------------------------------------
	Expected java beans
-----------------------------------------------------------------------------%>
<jsp:useBean id="error" type="java.lang.Exception" scope="request" />

<%-----------------------------------------------------------------------------
	Error content
-----------------------------------------------------------------------------%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
    <TITLE>
        <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
        <%= ComBirtResources.getMessage("birt.viewer.title.error", request.getLocale())%>
    </TITLE>
    <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
    <LINK REL="stylesheet" HREF="<%= request.getContextPath( ) + "/webcontent/birt/styles/style.css" %>" TYPE="text/css">
</HEAD>
<BODY>
<TABLE CLASS="BirtViewer_Highlight_Label">
    <TR><TD NOWRAP>
        <%
            if ( error != null )
            {
                if ( error.getMessage( ) != null )
                {
                    out.println( ParameterAccessor.htmlEncode( new String( error.getMessage( ).getBytes( "ISO-8859-1" ),"UTF-8" ) ) );
                }
                else
                {
                    PrintWriter writer = new PrintWriter( out );
                    error.printStackTrace( writer );
                }
            }
        %>
    </TD></TR>
</TABLE>
</BODY>
</HTML>