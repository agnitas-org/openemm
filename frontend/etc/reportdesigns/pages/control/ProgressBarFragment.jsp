<%@ page contentType="text/html; charset=utf-8" %>
<%@ page session="false" buffer="none" %>
<%@ page import="org.eclipse.birt.report.presentation.aggregation.IFragment"%>
<%@ page import="com.agnitas.reporting.birt.external.utils.EmmBirtResources"%>
<%@ page import="java.util.Locale" %>

<%-----------------------------------------------------------------------------
	Expected java beans
-----------------------------------------------------------------------------%>
<jsp:useBean id="fragment" type="org.eclipse.birt.report.presentation.aggregation.IFragment" scope="request" />
<jsp:useBean id="attributeBean" type="org.eclipse.birt.report.context.BaseAttributeBean" scope="request" />
<%-----------------------------------------------------------------------------
	Progress bar fragment
-----------------------------------------------------------------------------%>
<%
    Locale locale = request.getLocale();
%>
<DIV ID="progressBar" STYLE="display:none;position:absolute;z-index:310">
    <TABLE WIDTH="250px" CLASS="birtviewer_progressbar" CELLSPACING="10px">
        <TR>
            <TD ALIGN="center">
                <B>
                    <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                    <%= EmmBirtResources.getMessage("statistic.viewer.progressbar.prompt", locale)%>
                </B>
            </TD>
        </TR>
        <TR>
            <TD ALIGN="center">
                <IMG SRC="<%= "birt/images/" + (attributeBean.isRtl()?"Loading_rtl":"Loading") + ".gif" %>" ALT="Progress Bar Image"/>
            </TD>
        </TR>
        <TR>
            <TD ALIGN="center">
                <DIV ID="cancelTaskButton" STYLE="display:none">
                    <TABLE WIDTH="100%">
                        <TR>
                            <TD ALIGN="center">
                                <%--It is important to use the same message keys as BirtResources contains to keep backward compatible.--%>
                                <INPUT TYPE="BUTTON" VALUE="<%= EmmBirtResources.getHtmlMessage("button.Cancel", locale)%>"
                                       TITLE="<%= EmmBirtResources.getHtmlMessage("button.Cancel", locale)%>"
                                       CLASS="birtviewer_progressbar_button"/>
                            </TD>
                        </TR>
                    </TABLE>
                </DIV>
            </TD>
        </TR>
        <INPUT TYPE="HIDDEN" ID="taskid" VALUE=""/>
    </TABLE>
</DIV>