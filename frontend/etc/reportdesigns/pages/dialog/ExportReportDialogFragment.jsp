<%@ page contentType="text/html; charset=utf-8"%>
<%@ page session="false" buffer="none"%>
<%@ page import="org.eclipse.birt.report.presentation.aggregation.IFragment,
				 org.eclipse.birt.report.utility.ParameterAccessor"%>
<%@page import="com.agnitas.reporting.birt.external.utils.EmmBirtResources" %>
<%@ page import="java.util.Locale" %>

<%-----------------------------------------------------------------------------
	Expected java beans
-----------------------------------------------------------------------------%>
<jsp:useBean id="fragment" type="org.eclipse.birt.report.presentation.aggregation.IFragment" scope="request" />

<%
    String[] supportedFormats = ParameterAccessor.supportedFormats;
    Locale locale = request.getLocale();
%>
<%-----------------------------------------------------------------------------
	Export report dialog fragment
-----------------------------------------------------------------------------%>
<TABLE CELLSPACING="2" CELLPADDING="2" CLASS="birtviewer_dialog_body">
    <TR HEIGHT="5px"><TD></TD></TR>
    <TR>
        <TD>
            <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
            <label for="exportFormat"><%=EmmBirtResources.getMessage( "birt.viewer.dialog.export.format", locale )%></label>
            <SELECT	ID="exportFormat" NAME="format" CLASS="birtviewer_exportreport_dialog_select">
                <%
                    ParameterAccessor.sortSupportedFormatsByDisplayName(supportedFormats);

                    for ( int i = 0; i < supportedFormats.length; i++ )
                    {
                        if ( !ParameterAccessor.PARAM_FORMAT_HTML.equalsIgnoreCase( supportedFormats[i] ) )
                        {
                %>
                <OPTION VALUE="<%= supportedFormats[i] %>"><%= ParameterAccessor.getOutputFormatLabel( supportedFormats[i] ) %></OPTION>
                <%
                        }
                    }
                %>
            </SELECT>
        </TD>
    </TR>
    <TR HEIGHT="5px"><TD></TD></TR>
    <TR HEIGHT="5px"><TD><HR/></TD></TR>
    <TR>
        <TD>
            <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
            <label for="exportPages"><%=EmmBirtResources.getMessage( "birt.viewer.dialog.page", locale )%></label>
        </TD>
    </TR>
    <TR>
        <TD>
            <DIV ID="exportPageSetting">
                <TABLE>
                    <TR>
                        <TD>
                            <INPUT TYPE="radio" ID="exportPageAll" NAME="exportPages" CHECKED/>
                            <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                            <label for="exportPageAll"><%=EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.page.all", locale )%></label>
                        </TD>
                        <TD STYLE="padding-left:5px">
                            <INPUT TYPE="radio" ID="exportPageCurrent" NAME="exportPages"/>
                            <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                            <label for="exportPageCurrent"><%=EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.page.current", locale )%></label>
                        </TD>
                        <TD STYLE="padding-left:5px">
                            <INPUT TYPE="radio" ID="exportPageRange" NAME="exportPages"/>
                            <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                            <label for="exportPageRange"><%=EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.page.range", locale )%></label>
                            <INPUT TYPE="text" CLASS="birtviewer_exportreport_dialog_input" ID="exportPageRange_input" DISABLED="true"/>
                        </TD>
                    </TR>
                </TABLE>
            </DIV>
        </TD>
    </TR>
    <TR>
        <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
        <TD>&nbsp;&nbsp;<%=EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.page.range.description", locale )%></TD>
    </TR>
    <TR HEIGHT="5px"><TD><HR/></TD></TR>
    <TR>
        <TD>
            <DIV ID="exportFitSetting">
                <TABLE>
                    <TR>
                        <TD>
                            <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                            <label for="exportFitToAuto"><%=EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.export.pdf.fitto", locale )%></label>
                        </TD>
                    </TR>
                    <TR>
                        <TD>
                            <INPUT TYPE="radio" ID="exportFitToAuto" NAME="exportFit" CHECKED/>
                            <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                            <label for="exportFitToAuto"><%=EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.export.pdf.fittoauto", locale )%></label>
                        </TD>
                        <TD>
                            <INPUT TYPE="radio" ID="exportFitToActual" NAME="exportFit"/>
                            <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                            <label for="exportFitToActual"><%=EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.export.pdf.fittoactual", locale )%></label>
                        </TD>
                        <TD STYLE="padding-left:5px">
                            <INPUT TYPE="radio" ID="exportFitToWhole" NAME="exportFit"/>
                            <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                            <label for="exportFitToWhole"><%=EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.export.pdf.fittowhole", locale )%></label>
                        </TD>
                    </TR>
                </TABLE>
            </DIV>
        </TD>
    </TR>
    <TR HEIGHT="5px"><TD></TD></TR>
</TABLE>