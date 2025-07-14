<%@ page contentType="text/html; charset=utf-8" %>
<%@ page session="false" buffer="none" %>
<%@ page import="org.eclipse.birt.report.presentation.aggregation.IFragment,
				 org.eclipse.birt.report.utility.ParameterAccessor,
				 org.eclipse.birt.report.context.BaseAttributeBean,
				 org.eclipse.birt.report.engine.api.DataExtractionFormatInfo" %>
<%@ page import="com.agnitas.reporting.birt.external.utils.EmmBirtResources" %>
<%@ page import="java.util.Locale" %>


<%-----------------------------------------------------------------------------
	Expected java beans
-----------------------------------------------------------------------------%>
<jsp:useBean id="fragment" type="org.eclipse.birt.report.presentation.aggregation.IFragment" scope="request" />
<jsp:useBean id="attributeBean" type="org.eclipse.birt.report.context.BaseAttributeBean" scope="request" />

<%
    DataExtractionFormatInfo[] dataExtractInfos = ParameterAccessor.supportedDataExtractions;
    Locale locale = request.getLocale();
%>
<%-----------------------------------------------------------------------------
	Export data dialog fragment
-----------------------------------------------------------------------------%>
<TABLE ID="simpleExportDialogBody" CELLSPACING="2" CELLPADDING="2" CLASS="birtviewer_dialog_body">
    <TR HEIGHT="5px"><TD></TD></TR>
    <TR>
        <TD>
            <LABEL FOR="resultsets"><%= EmmBirtResources.getMessage( "birt.viewer.dialog.exportdata.resultsets", locale )%>
            </LABEL>
        </TD>
    </TR>
    <TR>
        <TD COLSPAN="4">
            <SELECT ID="resultsets" CLASS="birtviewer_exportdata_dialog_single_select">
            </SELECT>
        </TD>
    </TR>
    <TR HEIGHT="5px"><TD></TD></TR>
    <TR>
        <TD VALIGN="top">
            <TABLE STYLE="font-size:8pt;">
                <TR><TD>
                    <LABEL FOR="availableColumnSelect"><%= EmmBirtResources.getMessage( "birt.viewer.dialog.exportdata.availablecolumn", locale )%></LABEL>
                </TD></TR>
                <TR><TD>
                    <SELECT ID="availableColumnSelect" MULTIPLE="true" SIZE="10" CLASS="birtviewer_exportdata_dialog_select">
                    </SELECT>
                </TD></TR>
            </TABLE>
        </TD>
        <TD VALIGN="middle">
            <TABLE HEIGHT="100%">
                <TR>
                    <TD>
                        <TABLE VALIGN="middle">
                            <TR><TD>
                                <INPUT TYPE="image" NAME="Addall"
                                    <%
									if( !attributeBean.isRtl())
									{
									%>
                                       SRC="birt/images/AddAll.gif"
                                    <%
									}
									else
									{
									%>
                                       SRC="birt/images/AddAll_rtl.gif"
                                    <%
									}
									%>
                                       ALT="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.exportdata.addall", locale )%>"
                                       TITLE="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.exportdata.addall", locale )%>"
                                       CLASS="birtviewer_exportdata_dialog_button">
                            </TD></TR>
                            <TR height="2px"><TD></TD></TR>
                            <TR><TD>
                                <INPUT TYPE="image" NAME="Add"
                                    <%
									if( !attributeBean.isRtl())
									{
									%>
                                       SRC="birt/images/Add.gif"
                                    <%
									}
									else
									{
									%>
                                       SRC="birt/images/Add_rtl.gif"
                                    <%
									}
									%>
                                       ALT="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.exportdata.add", locale )%>"
                                       TITLE="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.exportdata.add", locale )%>"
                                       CLASS="birtviewer_exportdata_dialog_button">
                            </TD></TR>
                            <TR height="2px"><TD></TD></TR>
                            <TR><TD>
                                <INPUT TYPE="image" NAME="Remove"
                                    <%
									if( !attributeBean.isRtl())
									{
									%>
                                       SRC="birt/images/Remove_disabled.gif"
                                    <%
									}
									else
									{
									%>
                                       SRC="birt/images/Remove_disabled_rtl.gif"
                                    <%
									}
									%>
                                       ALT="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.exportdata.remove", locale )%>"
                                       TITLE="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.exportdata.remove", locale )%>"
                                       CLASS="birtviewer_exportdata_dialog_button">
                            </TD></TR>
                            <TR height="2px"><TD></TD></TR>
                            <TR><TD>
                                <INPUT TYPE="image" NAME="Removeall"
                                    <%
									if( !attributeBean.isRtl())
									{
									%>
                                       SRC="birt/images/RemoveAll_disabled.gif"
                                    <%
									}
									else
									{
									%>
                                       SRC="birt/images/RemoveAll_disabled_rtl.gif"
                                    <%
									}
									%>
                                       ALT="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.exportdata.removeall", locale )%>"
                                       TITLE="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.exportdata.removeall", locale )%>"
                                       CLASS="birtviewer_exportdata_dialog_button">
                            </TD></TR>
                        </TABLE>
                    </TD>
                </TR>
            </TABLE>
        </TD>
        <TD VALIGN="middle">
            <TABLE HEIGHT="100%">
                <TR>
                    <TD>
                        <TABLE VALIGN="middle">
                            <TR><TD>
                                <INPUT TYPE="image" NAME="Up" SRC="birt/images/Up_disabled.gif"
                                       ALT="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.exportdata.up", locale )%>"
                                       TITLE="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.exportdata.up", locale )%>"
                                       CLASS="birtviewer_exportdata_dialog_button">
                            </TD></TR>
                            <TR height="2px"><TD></TD></TR>
                            <TR><TD>
                                <INPUT TYPE="image" NAME="Down" SRC="birt/images/Down_disabled.gif"
                                       ALT="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.exportdata.down", locale )%>"
                                       TITLE="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.dialog.exportdata.down", locale )%>"
                                       CLASS="birtviewer_exportdata_dialog_button">
                            </TD></TR>
                        </TABLE>
                    </TD>
                </TR>
            </TABLE>
        </TD>
        <TD >
            <TABLE STYLE="font-size:8pt;">
                <TR><TD>
                    <LABEL FOR="selectedColumnSelect"><%= EmmBirtResources.getMessage( "birt.viewer.dialog.exportdata.selectedcolumn", locale )%></LABEL>
                </TD></TR>
                <TR><TD>
                    <SELECT ID="selectedColumnSelect" MULTIPLE="true" SIZE="10" CLASS="birtviewer_exportdata_dialog_select">
                    </SELECT>
                </TD></TR>
            </TABLE>
        </TD>
    </TR>
    <TR HEIGHT="5px"><TD></TD></TR>
    <TR>
        <TD COLSPAN="4">
            <DIV>
                <label for="exportDataExtension"><%= EmmBirtResources.getMessage( "birt.viewer.dialog.exportdata.extension", locale )%></label>
                <SELECT ID="exportDataExtension" CLASS="birtviewer_exportdata_dialog_select">
                    <%
                        for ( int i = 0; i < dataExtractInfos.length; i++ )
                        {
                            DataExtractionFormatInfo extensionInfo  = dataExtractInfos[i];
                            if( extensionInfo.getId() == null
                                    || extensionInfo.getFormat() == null
                                    || ( extensionInfo.isHidden() != null && extensionInfo.isHidden().booleanValue() ) )
                                continue;

                            String extensionName = extensionInfo.getName( );
                            if( extensionName == null )
                                extensionName = "";
                    %>
                    <OPTION VALUE="<%= extensionInfo.getId() %>"><%= extensionName %>(*.<%= extensionInfo.getFormat() %>)</OPTION>
                    <%
                        }
                    %>
                </SELECT>
            </DIV>
            <BR/>
            <DIV ID="exportDataEncodingSetting">
                <TABLE>
                    <TR>
                        <TD><label for="exportDataEncoding_UTF8"><%= EmmBirtResources.getMessage( "birt.viewer.dialog.exportdata.encoding", locale )%></label></TD>
                        <TD>
                            <INPUT TYPE="radio" NAME="exportDataEncoding" ID="exportDataEncoding_UTF8" CHECKED value="UTF-8">
                            <label for="exportDataEncoding_UTF8">UTF-8</label>
                        </TD>
                    </TR>
                    <TR>
                        <TD></TD>
                        <TD>
                            <TABLE cellpadding="0" cellspacing="0"><TR>
                                <TD valign="TOP">
                                    <INPUT TYPE="radio" NAME="exportDataEncoding" ID="exportDataEncoding_other">
                                </TD>
                                <TD>
                                    <label for="exportDataEncoding_other"><%= EmmBirtResources.getMessage( "birt.viewer.dialog.exportdata.encoding.other", locale )%></label>
                                    <INPUT TYPE="text" NAME="exportDataOtherEncoding" ID="exportDataOtherEncoding_input" CLASS="birtviewer_exportdata_dialog_input" DISABLED="true">
                                    <%= EmmBirtResources.getMessage( "birt.viewer.dialog.exportdata.encoding.comment", locale )%>
                                </TD>
                            </TR></TABLE>
                        </TD>
                    </TR>
                </TABLE>
            </DIV>
            <BR/>
            <DIV>
                <label for="exportDataCSVSeparator"><%= EmmBirtResources.getMessage( "birt.viewer.dialog.exportdata.separator", locale )%></label>
                <SELECT ID="exportDataCSVSeparator" CLASS="birtviewer_exportdata_dialog_select">
                    <OPTION VALUE="0" SELECTED><%= EmmBirtResources.getMessage( "birt.viewer.sep.0", locale )%></OPTION>
                    <OPTION VALUE="1"><%= EmmBirtResources.getMessage( "birt.viewer.sep.1")%></OPTION>
                    <OPTION VALUE="2"><%= EmmBirtResources.getMessage( "birt.viewer.sep.2")%></OPTION>
                    <OPTION VALUE="3"><%= EmmBirtResources.getMessage( "birt.viewer.sep.3")%></OPTION>
                    <OPTION VALUE="4"><%= EmmBirtResources.getMessage( "birt.viewer.sep.4")%></OPTION>
                </SELECT>
            </DIV>
            <BR/>
            <DIV>
                <TABLE cellpadding="0" cellspacing="0">
                    <TR >
                        <TD><INPUT TYPE="checkbox" ID="exportColumnDataType"></TD>
                        <TD>
                            <label for="exportColumnDataType"><%= EmmBirtResources.getMessage( "birt.viewer.dialog.exportdata.datatype", locale )%></label>
                        </TD>
                        <TD style="padding-left:20px;" ><INPUT TYPE="checkbox" ID="exportColumnLocaleNeutral"></TD>
                        <TD rowspan="2" valign="top" style="padding-top:3px;">
                            <label for="exportColumnLocaleNeutral"><%= EmmBirtResources.getMessage( "birt.viewer.dialog.exportdata.localeneutral", locale )%></label>
                        </TD>
                    </TR>
                    <TR>
                        <TD><INPUT TYPE="checkbox" ID="exportDataWithCR"></TD>
                        <TD nowrap="nowrap">
                            <label for="exportDataWithCR"><%= EmmBirtResources.getMessage( "birt.viewer.dialog.exportdata.carriage_return", locale )%></label>
                        </TD>
                    </TR>
                </TABLE>
            </DIV>
        </TD>
    </TR>
    <TR HEIGHT="5px"><TD></TD></TR>
</TABLE>