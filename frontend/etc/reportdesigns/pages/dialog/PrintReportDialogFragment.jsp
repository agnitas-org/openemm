<%@ page contentType="text/html; charset=utf-8"%>
<%@ page session="false" buffer="none"%>
<%@ page import="org.eclipse.birt.report.presentation.aggregation.IFragment"%>
<%@ page import="com.agnitas.reporting.birt.external.utils.ComBirtResources" %>
<%@ page import="java.util.Locale" %>

<%-----------------------------------------------------------------------------
	Expected java beans
-----------------------------------------------------------------------------%>
<jsp:useBean id="fragment" type="org.eclipse.birt.report.presentation.aggregation.IFragment" scope="request" />

<%-----------------------------------------------------------------------------
	Print report dialog fragment
-----------------------------------------------------------------------------%>
<%
    Locale locale = request.getLocale();
%>
<%--It is important to use the same message keys as BirtResources contains to keep backward compatible!--%>
<TABLE CELLSPACING="2" CELLPADDING="2" CLASS="birtviewer_dialog_body">
    <TR HEIGHT="5px"><TD></TD></TR>
    <TR>
        <TD>
            <DIV ID="printFormatSetting">
                <DIV><label for="printAsHTML"><%=ComBirtResources.getMessage( "birt.viewer.dialog.print.format", locale )%></label></DIV>
                <br/>
                <DIV>
                    <INPUT TYPE="radio" ID="printAsHTML" name="printFormat" CHECKED />
                    <label for="printAsHTML"><%=ComBirtResources.getMessage( "birt.viewer.dialog.print.format.html", locale )%></label>
                </DIV>
                <DIV>
                    <INPUT TYPE="radio" ID="printAsPDF" name="printFormat" />
                    <label for="printAsPDF"><%=ComBirtResources.getMessage( "birt.viewer.dialog.print.format.pdf", locale )%></label>
                    &nbsp;&nbsp;
                    <SELECT	ID="printFitSetting" CLASS="birtviewer_printreport_dialog_select" DISABLED="true">
                        <option value="0" selected><%=ComBirtResources.getMessage( "birt.viewer.dialog.export.pdf.fittoauto", locale )%></option>
                        <option value="1"><%=ComBirtResources.getMessage( "birt.viewer.dialog.export.pdf.fittoactual", locale )%></option>
                        <option value="2"><%=ComBirtResources.getMessage( "birt.viewer.dialog.export.pdf.fittowhole", locale )%></option>
                    </SELECT>
                </DIV>
            </DIV>
        </TD>
    </TR>
    <TR HEIGHT="5px"><TD><HR/></TD></TR>
    <TR>
        <TD>
            <DIV ID="printPageSetting">
                <TABLE>
                    <TR>
                        <TD>
                            <label for="exportPages"><%=ComBirtResources.getMessage( "birt.viewer.dialog.page", locale )%></label>
                        </TD>
                    </TR>
                    <TR>
                        <TD>
                            <INPUT TYPE="radio" ID="printPageAll" NAME="printPages" CHECKED/>
                            <label for="printPageAll"><%=ComBirtResources.getMessage( "birt.viewer.dialog.page.all", locale )%></label>
                        </TD>
                        <TD STYLE="padding-left:5px">
                            <INPUT TYPE="radio" ID="printPageCurrent" NAME="printPages"/>
                            <label for="printPageCurrent"><%=ComBirtResources.getMessage( "birt.viewer.dialog.page.current", locale )%></label>
                        </TD>
                        <TD STYLE="padding-left:5px">
                            <INPUT TYPE="radio" ID="printPageRange" NAME="printPages"/>
                            <label for="printPageRange"><%=ComBirtResources.getMessage( "birt.viewer.dialog.page.range", locale )%></label>
                            <INPUT TYPE="text" CLASS="birtviewer_printreport_dialog_input" ID="printPageRange_input" DISABLED="true"/>
                        </TD>
                    </TR>
                </TABLE>
            </DIV>
        </TD>
    </TR>
    <TR>
        <TD>&nbsp;&nbsp;<%=ComBirtResources.getMessage( "birt.viewer.dialog.page.range.description", locale )%></TD>
    </TR>
    <TR HEIGHT="5px"><TD></TD></TR>
</TABLE>