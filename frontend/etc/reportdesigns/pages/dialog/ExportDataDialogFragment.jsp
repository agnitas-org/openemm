<%@ page contentType="text/html; charset=utf-8" %>
<%@ page session="false" buffer="none" %>
<%@ page import="org.eclipse.birt.report.presentation.aggregation.IFragment" %>
<%@page import="com.agnitas.reporting.birt.external.utils.ComBirtResources" %>
<%@ page import="java.util.Locale" %>

<%-----------------------------------------------------------------------------
	Expected java beans
-----------------------------------------------------------------------------%>
<jsp:useBean id="fragment" type="org.eclipse.birt.report.presentation.aggregation.IFragment" scope="request" />

<%-----------------------------------------------------------------------------
	Export data dialog fragment
-----------------------------------------------------------------------------%>
<%
    Locale locale = request.getLocale();
%>
<DIV ID="dialog_content">
    <TABLE CELLSPACING="0" CELLPADDING="0" STYLE="width:100%">
        <TR>
            <TD>
                <TABLE ID="tabs" CELLSPACING="0" CELLPADDING="2">
                    <TR HEIGHT="20px">
                        <TD CLASS="birtviewer_dialog_tab_selected" NOWRAP>
                            <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                            <%=
                            ComBirtResources.getMessage( "birt.viewer.dialog.exportdata.tab.field", locale )
                            %>
                        </TD>
                        <TD CLASS="birtviewer_dialog_tab_normal">
                            <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                            <%= ComBirtResources.getMessage( "birt.viewer.dialog.exportdata.tab.filter", locale )%>
                        </TD>
                    </TR>
                </TABLE>
            </TD>
        </TR>
        <TR>
            <TD>
                <DIV ID="aaacontent">
                    <DIV >
                        <TABLE CELLSPACING="2" CELLPADDING="2" CLASS="birtviewer_dialog_body">
                            <TR HEIGHT="5px"><TD></TD></TR>
                            <TR>
                                <TD>
                                    <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                                    <%= ComBirtResources.getMessage( "birt.viewer.dialog.exportdata.availablecolumn", locale )%>
                                </TD>
                                <TD></TD>
                                <TD>
                                    <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                                    <%= ComBirtResources.getMessage( "birt.viewer.dialog.exportdata.selectedcolumn", locale )%>
                                </TD>
                            </TR>
                            <TR>
                                <TD VALIGN="top">
                                    <TABLE>
                                        <TR><TD>
                                            <SELECT ID="availableColumnSelect" SIZE="10" CLASS="birtviewer_exportdata_dialog_select">
                                            </SELECT>
                                        </TD></TR>
                                    </TABLE>
                                </TD>
                                <TD VALIGN="top">
                                    <TABLE HEIGHT="100%">
                                        <TR><TD>&nbsp;</TD></TR>
                                        <TR><TD>
                                            <TABLE VALIGN="top">
                                                <TR><TD>
                                                    <INPUT TYPE="button" VALUE=">>" CLASS="birtviewer_exportdata_dialog_button">
                                                </TD></TR>
                                                <TR><TD>
                                                    <INPUT TYPE="button" VALUE=">" CLASS="birtviewer_exportdata_dialog_button">
                                                </TD></TR>
                                                <TR><TD>
                                                    <INPUT TYPE="button" VALUE="<" CLASS="birtviewer_exportdata_dialog_button">
                                                </TD></TR>
                                                <TR><TD>
                                                    <INPUT TYPE="button" VALUE="<<" CLASS="birtviewer_exportdata_dialog_button">
                                                </TD></TR>
                                            </TABLE>
                                        </TD></TR>
                                    </TABLE>
                                </TD>
                                <TD>
                                    <TABLE>
                                        <TR><TD>
                                            <SELECT ID="selectedColumnSelect" SIZE="10" CLASS="birtviewer_exportdata_dialog_select">
                                            </SELECT>
                                        </TD></TR>
                                    </TABLE>
                                </TD>
                            </TR>
                            <TR HEIGHT="5px"><TD></TD></TR>
                            <TR>
                                <TD COLSPAN="3" STYLE="font-size:7pt">
                                    <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                                    <%= ComBirtResources.getMessage( "birt.viewer.dialog.exportdata.format", locale )%>
                                </TD>
                            </TR>
                            <TR HEIGHT="5px"><TD></TD></TR>
                        </TABLE>
                    </DIV>
                    <DIV STYLE="display:none">
                        <IMG NAME="add" SRC="birt/images/AddFilter.gif" TITLE="add" CLASS="birtviewer_clickable">
                        <TABLE ID="ExportCriteriaTable" CELLSPACING="2" CELLPADDING="2" CLASS="birtviewer_dialog_body">
                            <TBODY ID="ExportCriteriaTBODY">
                            </TBODY>
                        </TABLE>
                    </DIV>
                </DIV>
            </TD>
        </TR>
    </TABLE>
</DIV>