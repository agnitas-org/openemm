<%@ page contentType="text/html; charset=utf-8"%>
<%@ page session="false" buffer="none"%>
<%@ page import="org.eclipse.birt.report.presentation.aggregation.IFragment,
				 org.eclipse.birt.report.IBirtConstants,
				 java.util.ArrayList,
				 java.util.Map,
				 org.eclipse.birt.report.utility.Printer,
				 org.eclipse.birt.report.utility.DataUtil,
				 org.eclipse.birt.report.utility.PrintUtility,
				 org.eclipse.birt.report.utility.ParameterAccessor"%>
<%@ page import="com.agnitas.reporting.birt.external.utils.ComBirtResources" %>
<%@ page import="java.util.Locale" %>

<%-----------------------------------------------------------------------------
	Expected java beans
-----------------------------------------------------------------------------%>
<jsp:useBean id="fragment" type="org.eclipse.birt.report.presentation.aggregation.IFragment" scope="request" />

<%
    Locale locale = request.getLocale();
%>

<SCRIPT LANGUAGE="javascript">var index = 0;</SCRIPT>
<%
    boolean enable = ParameterAccessor.isSupportedPrintOnServer;
    if( enable )
    {
        String[] supportedFormats = ParameterAccessor.supportedFormats;
        for( int i=0; i<supportedFormats.length; i++ )
        {
            if( IBirtConstants.POSTSCRIPT_RENDER_FORMAT.equalsIgnoreCase( supportedFormats[i] ) )
            {
                enable = true;
                break;
            }
        }
    }

    if( enable )
    {
        ArrayList printers = (ArrayList)PrintUtility.findPrinters();
        for( int i=0; i<printers.size( ); i++ )
        {
            Printer bean = (Printer)printers.get( i );
            String name = PrintUtility.handleSlash( bean.getName( ) );
            String status = null;

            if ( bean.getStatus() == Printer.STATUS_ACCEPTING_JOBS )
            {
                status = ComBirtResources.getMessage( "birt.viewer.dialog.printserver.status.acceptingjobs", locale ); // TODO: localized key
            }
            else
            {
                status = ComBirtResources.getMessage( "birt.viewer.dialog.printserver.status.notacceptingjobs", locale ); // TODO: localized key
            }
            status = DataUtil.trimString( status );

            String model = DataUtil.trimString( bean.getModel( ) );
            String info = DataUtil.trimString( bean.getInfo( ) );
            String copies = "" + bean.getCopies( );
            String mode = "" + bean.getMode( );
            String duplex = "" + bean.getDuplex( );
            String mediaSize = DataUtil.trimString( bean.getMediaSize( ) );
            Map map = bean.getMediaSizeNames( );
            Object[] mediaSizeNames = map.keySet( ).toArray( );
%>
<SCRIPT LANGUAGE="javascript">
    var printer = new Printer( );
    printer.setName( "<%= name %>" );
    printer.setStatus( "<%= status %>" );
    printer.setModel( "<%= model %>" );
    printer.setInfo( "<%= info %>" );

    // Copies attribute
    <%
    if( bean.isCopiesSupported() )
    {
    %>
    printer.setCopiesSupported( true );
    printer.setCopies( "<%= copies %>" );
    <%
    }
    else
    {
    %>
    printer.setCopiesSupported( false );
    <%
    }
    %>

    // Collate attribute
    <%
    if( bean.isCollateSupported() )
    {
    %>
    printer.setCollateSupported( true );
    <%
    if( bean.isCollate( ) )
    {
    %>
    printer.setCollate( true );
    <%
    }
    else
    {
    %>
    printer.setCollate( false );
    <%
        }
    }
    else
    {
    %>
    printer.setCopiesSupported( false );
    <%
    }
    %>

    // Mode attribute
    <%
    if( bean.isModeSupported( ) )
    {
    %>
    printer.setModeSupported( true );
    printer.setMode( "<%= mode %>" );
    <%
    }
    else
    {
    %>
    printer.setModeSupported( false );
    <%
    }
    %>

    // Duplex attribute
    <%
    if( bean.isDuplexSupported( ) )
    {
    %>
    printer.setDuplexSupported( true );
    printer.setDuplex( "<%= duplex %>" );
    <%
    }
    else
    {
    %>
    printer.setDuplexSupported( false );
    <%
    }
    %>

    // Media attribute
    <%
    if( bean.isMediaSupported( ) )
    {
    %>
    printer.setMediaSupported( true );
    printer.setMediaSize( "<%= mediaSize %>" );
    <%
    for( int j=0; j<mediaSizeNames.length; j++ )
    {
        String mediaSizeName = DataUtil.trimString( (String)mediaSizeNames[j] );
        mediaSizeName = ParameterAccessor.htmlEncode( mediaSizeName );
    %>
    printer.addMediaSizeName( "<%= mediaSizeName %>" );
    <%
        }
    }
    else
    {
    %>
    printer.setMediaSupported( false );
    <%
    }
    %>

    if( !printers[index] )
        printers[index] = {};

    printers[index].name = printer.getName( );
    printers[index].value = printer;

    index++;

</SCRIPT>
<%
        }
    }
%>
<%-----------------------------------------------------------------------------
	Print report on the server dialog fragment
-----------------------------------------------------------------------------%>
<TABLE CELLSPACING="2" CELLPADDING="2" CLASS="birtviewer_dialog_body">
    <TR HEIGHT="5px"><TD></TD></TR>
    <TR>
        <TD>
            <INPUT TYPE="checkbox" ID="print_onserver" <%if( !enable ) { %>DISABLED="true"<%}%>/>
            <%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.onserver", locale )%>
        </TD>
    </TR>
    <TR HEIGHT="5px"><TD></TD></TR>
    <TR>
        <TD>
            <TABLE WIDTH="100%" ID="printer_general">
                <TR>
                    <TD WIDTH="80px"><%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.printer", locale )%></TD>
                    <TD>
                        <SELECT ID="printer" CLASS="birtviewer_printreportserver_dialog_select"></SELECT>
                    </TD>
                </TR>
                <TR>
                    <TD><%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.status", locale )%></TD>
                    <TD><LABEL ID="printer_status"></LABEL></TD>
                </TR>
                <TR>
                    <TD><%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.model", locale )%></TD>
                    <TD><LABEL ID="printer_model"></LABEL></TD>
                </TR>
                <TR>
                    <TD><%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.description", locale )%></TD>
                    <TD><LABEL ID="printer_description"></LABEL></TD>
                </TR>
            </TABLE>
        </TD>
    </TR>
    <TR HEIGHT="5px"><TD><HR/></TD></TR>
    <TR>
        <TD><%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.settings", locale )%></TD>
    </TR>
    <TR>
        <TD>
            <TABLE WIDTH="100%" ID="printer_config">
                <TR>
                    <TD WIDTH="100px">
                        <%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.settings.copies", locale )%>
                    </TD>
                    <TD>
                        <INPUT TYPE="text" CLASS="birtviewer_printreportserver_dialog_input_short" ID="printer_copies"/>
                        &nbsp;&nbsp;<%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.settings.collate", locale )%>&nbsp;&nbsp;<INPUT TYPE="checkbox" ID="printer_collate"/>
                    </TD>
                </TR>
                <TR>
                    <TD>
                        <%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.settings.duplex", locale )%>
                    </TD>
                    <TD>
                        <INPUT TYPE="radio" ID="printer_duplexSimplex" NAME="printerDuplex"/><%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.settings.duplex.simplex", locale )%>
                        &nbsp;&nbsp;<INPUT TYPE="radio" ID="printer_duplexHorz" NAME="printerDuplex"/><%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.settings.duplex.horizontal", locale )%>
                        &nbsp;&nbsp;<INPUT TYPE="radio" ID="printer_duplexVert" NAME="printerDuplex"/><%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.settings.duplex.vertical", locale )%>
                    </TD>
                </TR>
                <TR>
                    <TD>
                        <%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.settings.mode", locale )%>
                    </TD>
                    <TD>
                        <INPUT TYPE="radio" ID="printer_modeBW" NAME="printerMode"/><%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.settings.mode.bw", locale )%>
                        &nbsp;&nbsp;<INPUT TYPE="radio" ID="printer_modeColor" NAME="printerMode"/><%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.settings.mode.color", locale )%>
                    </TD>
                </TR>
                <TR>
                    <TD>
                        <%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.settings.pagesize", locale )%>
                    </TD>
                    <TD>
                        <SELECT ID="printer_mediasize" CLASS="birtviewer_printreportserver_dialog_select"></SELECT>
                    </TD>
                </TR>
            </TABLE>
        </TD>
    </TR>
    <TR HEIGHT="5px"><TD><HR/></TD></TR>
    <TR>
        <TD>
            <DIV ID="printServerPageSetting">
                <TABLE>
                    <TR>
                        <TD><%=ComBirtResources.getMessage( "birt.viewer.dialog.printserver.settings.print", locale )%></TD>
                        <TD STYLE="padding-left:5px">
                            <INPUT TYPE="radio" ID="printServerPageAll" NAME="printServerPages" CHECKED/><%=ComBirtResources.getMessage( "birt.viewer.dialog.page.all", locale )%>
                        </TD>
                        <TD STYLE="padding-left:5px">
                            <INPUT TYPE="radio" ID="printServerPageCurrent" NAME="printServerPages"/><%=ComBirtResources.getMessage( "birt.viewer.dialog.page.current", locale )%>
                        </TD>
                        <TD STYLE="padding-left:5px">
                            <INPUT TYPE="radio" ID="printServerPageRange" NAME="printServerPages"/><%=ComBirtResources.getMessage( "birt.viewer.dialog.page.range", locale )%>
                            <INPUT TYPE="text" CLASS="birtviewer_printreportserver_dialog_input" ID="printServerPageRange_input"/>
                        </TD>
                    </TR>
                </TABLE>
            </DIV>
        </TD>
    </TR>
    <TR>
        <TD>&nbsp;&nbsp;<%=ComBirtResources.getMessage( "birt.viewer.dialog.page.range.description", locale )%></TD>
    </TR>
    <TR HEIGHT="5px"><TD><HR/></TD></TR>
    <TR>
        <TD>
            <DIV ID="printServerFitSetting">
                <TABLE>
                    <TR>
                        <TD>
                            <INPUT TYPE="radio" ID="printServerFitToAuto" NAME="printServerFit" CHECKED/><%=ComBirtResources.getHtmlMessage( "birt.viewer.dialog.export.pdf.fittoauto", locale )%>
                        </TD>
                        <TD>
                            <INPUT TYPE="radio" ID="printServerFitToActual" NAME="printServerFit"/><%=ComBirtResources.getMessage( "birt.viewer.dialog.export.pdf.fittoactual", locale )%>
                        </TD>
                        <TD STYLE="padding-left:5px">
                            <INPUT TYPE="radio" ID="printServerFitToWhole" NAME="printServerFit"/><%=ComBirtResources.getMessage( "birt.viewer.dialog.export.pdf.fittowhole", locale )%>
                        </TD>
                    <TR>
                </TABLE>
            </DIV>
        </TD>
    </TR>
    <TR HEIGHT="5px"><TD></TD></TR>
</TABLE>