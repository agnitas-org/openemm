<%@ page contentType="text/html; charset=utf-8" %>
<%@ page session="false" buffer="none" %>
<%@ page import="com.agnitas.reporting.birt.external.utils.ComBirtResources,
				 org.eclipse.birt.report.utility.ParameterAccessor,
				 org.eclipse.birt.report.presentation.aggregation.IFragment,
				 org.eclipse.birt.report.context.BaseAttributeBean" %>
<%@ page import="java.util.Locale" %>

<%-----------------------------------------------------------------------------
	Expected java beans
-----------------------------------------------------------------------------%>
<jsp:useBean id="fragment" type="org.eclipse.birt.report.presentation.aggregation.IFragment" scope="request" />
<jsp:useBean id="attributeBean" type="org.eclipse.birt.report.context.BaseAttributeBean" scope="request" />

<%-----------------------------------------------------------------------------
	Toolbar fragment
-----------------------------------------------------------------------------%>
<%
    Locale locale = request.getLocale();
%>
<TR
        <%
            if( attributeBean.isShowToolbar( ) )
            {
        %>
        HEIGHT="20px"
        <%
        }
        else
        {
        %>
        style="display:none"
        <%
            }
        %>
        >
    <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
    <TD COLSPAN='2'>
        <DIV ID="toolbar">
            <TABLE CELLSPACING="1px" CELLPADDING="1px" WIDTH="100%" CLASS="birtviewer_toolbar">
                <TR><TD></TD></TR>
                <TR>
                    <TD WIDTH="6px"/>
                    <TD WIDTH="15px">
                        <INPUT TYPE="image" NAME='toc' SRC="birt/images/Toc.gif"
                               TITLE="<%= ComBirtResources.getHtmlMessage( "birt.viewer.toolbar.toc", locale )%>"
                               ALT="<%= ComBirtResources.getHtmlMessage( "birt.viewer.toolbar.toc", locale )%>" CLASS="birtviewer_clickable">
                    </TD>
                    <TD WIDTH="6px"/>
                    <TD WIDTH="15px">
                        <INPUT TYPE="image" NAME='parameter' SRC="birt/images/Report_parameters.gif"
                               TITLE="<%= ComBirtResources.getHtmlMessage( "birt.viewer.toolbar.parameter", locale )%>"
                               ALT="<%= ComBirtResources.getHtmlMessage( "birt.viewer.toolbar.parameter", locale )%>" CLASS="birtviewer_clickable">
                    </TD>
                    <TD WIDTH="6px"/>
                    <TD WIDTH="15px">
                        <INPUT TYPE="image" NAME='export' SRC="birt/images/Export.gif"
                               TITLE="<%= ComBirtResources.getHtmlMessage( "birt.viewer.toolbar.export", locale )%>"
                               ALT="<%= ComBirtResources.getHtmlMessage( "birt.viewer.toolbar.export", locale )%>" CLASS="birtviewer_clickable">
                    </TD>
                    <TD WIDTH="6px"/>
                    <TD WIDTH="15px">
                        <INPUT TYPE="image" NAME='exportReport' SRC="birt/images/ExportReport.gif"
                               TITLE="<%= ComBirtResources.getHtmlMessage( "birt.viewer.toolbar.exportreport", locale )%>"
                               ALT="<%= ComBirtResources.getHtmlMessage( "birt.viewer.toolbar.exportreport", locale )%>" CLASS="birtviewer_clickable">
                    </TD>
                    <TD WIDTH="6px"/>
                    <TD WIDTH="15px">
                        <INPUT TYPE="image" NAME='print' SRC="birt/images/Print.gif"
                               TITLE="<%= ComBirtResources.getHtmlMessage( "birt.viewer.toolbar.print", locale )%>"
                               ALT="<%= ComBirtResources.getHtmlMessage( "birt.viewer.toolbar.print", locale )%>" CLASS="birtviewer_clickable">
                    </TD>
                    <%
                        if( ParameterAccessor.isSupportedPrintOnServer )
                        {
                    %>
                    <TD WIDTH="6px"/>
                    <TD WIDTH="15px">
                        <INPUT TYPE="image" NAME='printServer' SRC="birt/images/PrintServer.gif"
                               TITLE="<%= ComBirtResources.getHtmlMessage( "birt.viewer.toolbar.printserver", locale )%>"
                               ALT="<%= ComBirtResources.getHtmlMessage( "birt.viewer.toolbar.printserver", locale )%>" CLASS="birtviewer_clickable">
                    </TD>
                    <%
                        }
                    %>
                    <TD ALIGN='right'>
                    </TD>
                    <TD WIDTH="6px"/>
                </TR>
            </TABLE>
        </DIV>
    </TD>
</TR>