<%@ page contentType="text/html; charset=utf-8" %>
<%@ page session="false" buffer="none" %>
<%@ page import="org.eclipse.birt.report.presentation.aggregation.IFragment,
				 org.eclipse.birt.report.context.BaseAttributeBean" %>
<%@ page import="com.agnitas.reporting.birt.external.utils.EmmBirtResources" %>
<%@ page import="java.util.Locale" %>

<%-----------------------------------------------------------------------------
	Expected java beans
-----------------------------------------------------------------------------%>
<jsp:useBean id="fragment" type="org.eclipse.birt.report.presentation.aggregation.IFragment" scope="request" />
<jsp:useBean id="attributeBean" type="org.eclipse.birt.report.context.BaseAttributeBean" scope="request" />

<%-----------------------------------------------------------------------------
	Navigation bar fragment
-----------------------------------------------------------------------------%>
<%
    Locale locale = request.getLocale();
%>
<TR
        <%
            String imagesPath = "birt/images/";

            if( attributeBean.isShowNavigationbar( ) )
            {
        %>
        HEIGHT="25px"
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
    <TD>
        <DIV id="navigationBar">
            <TABLE CELLSPACING="0" CELLPADDING="0" WIDTH="100%" HEIGHT="25px" CLASS="birtviewer_navbar">
                <TR><TD></TD></TR>
                <TR>
                    <TD WIDTH="6px">&nbsp;</TD>
                    <TD WIDTH="100%" NOWRAP>
                        <B>
                            <%
                                if ( attributeBean.getBookmark( ) != null )
                                {
                            %>
                            <%=
                            EmmBirtResources.getMessage( "birt.viewer.navbar.prompt.one", locale )
                            %>&nbsp;
                            <SPAN ID='pageNumber'></SPAN>&nbsp;
                            <%= EmmBirtResources.getMessage( "birt.viewer.navbar.prompt.two", locale )%>&nbsp;
                            <SPAN ID='totalPage'></SPAN>
                            <%
                            }
                            else
                            {
                            %>
                            <%= EmmBirtResources.getMessage( "birt.viewer.navbar.prompt.one", locale )%>&nbsp;
                            <SPAN ID='pageNumber'><%= ""+attributeBean.getReportPage( ) %></SPAN>&nbsp;
                            <%= EmmBirtResources.getMessage( "birt.viewer.navbar.prompt.two", locale )%>&nbsp;
                            <SPAN ID='totalPage'></SPAN>
                            <%
                                }
                            %>
                        </B>
                    </TD>
                    <TD WIDTH="15px">
                        <INPUT TYPE="image" SRC="<%= imagesPath + (attributeBean.isRtl()?"LastPage":"FirstPage") + "_disabled.gif" %>" NAME='first'
                               ALT="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.navbar.first", locale )%>"
                               TITLE="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.navbar.first", locale )%>" CLASS="birtviewer_clickable">
                    </TD>
                    <TD WIDTH="2px">&nbsp;</TD>
                    <TD WIDTH="15px">
                        <INPUT TYPE="image" SRC="<%= imagesPath + (attributeBean.isRtl()?"NextPage":"PreviousPage") + "_disabled.gif" %>" NAME='previous'
                               ALT="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.navbar.previous", locale )%>"
                               TITLE="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.navbar.previous", locale )%>" CLASS="birtviewer_clickable">
                    </TD>
                    <TD WIDTH="2px">&nbsp;</TD>
                    <TD WIDTH="15px">
                        <INPUT TYPE="image" SRC="<%= imagesPath + (attributeBean.isRtl()?"PreviousPage":"NextPage") + "_disabled.gif" %>" NAME='next'
                               ALT="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.navbar.next", locale )%>"
                               TITLE="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.navbar.next", locale )%>" CLASS="birtviewer_clickable">
                    </TD>
                    <TD WIDTH="2px">&nbsp;</TD>
                    <TD WIDTH="15px">
                        <INPUT TYPE="image" SRC="<%= imagesPath + (attributeBean.isRtl()?"FirstPage":"LastPage") + "_disabled.gif" %>" NAME='last'
                               ALT="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.navbar.last", locale )%>"
                               TITLE="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.navbar.last", locale )%>" CLASS="birtviewer_clickable">
                    </TD>

                    <TD WIDTH="8px">&nbsp;&nbsp;</TD>

                    <TD ALIGN="right" NOWRAP><LABEL for="gotoPage"><b><%= EmmBirtResources.getMessage( "birt.viewer.navbar.lable.goto", locale )%></b></LABEL></TD>
                    <TD WIDTH="2px">&nbsp;</TD>
                    <TD ALIGN="right" WIDTH="50px">
                        <INPUT ID='gotoPage' TYPE='text' VALUE='' MAXLENGTH="8" SIZE='5' CLASS="birtviewer_navbar_input">
                    </TD>
                    <TD WIDTH="4px">&nbsp;</TD>
                    <TD ALIGN="right" WIDTH="10px">
                        <INPUT TYPE="image" SRC="<%= imagesPath + (attributeBean.isRtl()?"Go_rtl.gif":"Go.gif") %>" NAME='goto'
                               ALT="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.navbar.goto", locale )%>"
                               TITLE="<%= EmmBirtResources.getHtmlMessage( "birt.viewer.navbar.goto", locale )%>" CLASS="birtviewer_clickable">
                    </TD>
                    <TD WIDTH="6px">&nbsp;</TD>
                </TR>
            </TABLE>
        </DIV>
    </TD>
</TR>