<%@ page contentType="text/html; charset=utf-8" %>
<%@ page session="false" buffer="none" %>
<%@ page import="org.eclipse.birt.report.presentation.aggregation.IFragment,
				 org.eclipse.birt.report.resource.ResourceConstants"  %>
<%@page import="com.agnitas.reporting.birt.external.utils.ComBirtResources" %>
<%@ page import="java.util.Locale" %>

<%-----------------------------------------------------------------------------
	Expected java beans
-----------------------------------------------------------------------------%>
<jsp:useBean id="fragment" type="org.eclipse.birt.report.presentation.aggregation.IFragment" scope="request" />

<%-----------------------------------------------------------------------------
	Exception dialog fragment
-----------------------------------------------------------------------------%>
<%
    Locale locale = request.getLocale();
%>
<TABLE CELLSPACING="2" CELLPADDING="2" CLASS="birtviewer_dialog_body">
    <TR>
        <TD CLASS="birtviewer_exception_dialog">
            <TABLE CELLSPACING="2" CELLPADDING="2">
                <TR>
                    <TD VALIGN="top"><IMG SRC="birt/images/Error.gif" /></TD>

                    <TD>

                        <TABLE CELLSPACING="2" CELLPADDING="4" CLASS="birtviewer_exception_dialog_container" >
                            <TR>
                                <TD>
                                    <DIV ID="faultStringContainer" CLASS="birtviewer_exception_dialog_message">
                                        <B><SPAN ID='faultstring'></SPAN></B>
                                    </DIV>
                                </TD>
                            </TR>
                            <TR>
                                <TD>
                                    <DIV ID="showTraceLabel" CLASS="birtviewer_exception_dialog_label">
                                        <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                                        <%= ComBirtResources.getMessage( ResourceConstants.EXCEPTION_DIALOG_SHOW_STACK_TRACE, locale ) %>
                                    </DIV>
                                    <DIV ID="hideTraceLabel" CLASS="birtviewer_exception_dialog_label" STYLE="display:none">
                                        <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                                        <%= ComBirtResources.getMessage( ResourceConstants.EXCEPTION_DIALOG_HIDE_STACK_TRACE, locale ) %>
                                    </DIV>
                                </TD>
                            </TR>
                            <TR>
                                <TD>
                                    <DIV ID="exceptionTraceContainer" STYLE="display:none">
                                        <TABLE WIDTH="100%">
                                            <TR>
                                                <TD>
                                                    <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                                                    <%=
                                                    ComBirtResources.getMessage( ResourceConstants.EXCEPTION_DIALOG_STACK_TRACE, locale )
                                                    %><BR>
                                                </TD>
                                            </TR>
                                            <TR>
                                                <TD>
                                                    <DIV CLASS="birtviewer_exception_dialog_detail">
                                                        <SPAN ID='faultdetail'></SPAN>
                                                    </DIV>
                                                </TD>
                                            </TR>
                                        </TABLE>
                                    </DIV>
                                </TD>
                            </TR>
                        </TABLE>

                    </TD>

                </TR>
            </TABLE>
        </TD>
    </TR>
</TABLE>