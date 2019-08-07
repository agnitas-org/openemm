<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<script type="text/javascript">

    // cross-browser function to add event listener
    function addEventListenerCustom(node, type, listener) {
        if (node.addEventListener) {
            node.addEventListener(type, listener, false);
            return true;
        } else if (node.attachEvent) {
            node['e' + type + listener] = listener;
            node[type + listener] = function() {
                node['e' + type + listener](window.event);
            };
            node.attachEvent('on' + type, node[type + listener]);
            return true;
        }
        return false;
    }

    // count the sum of menu points and set that width to menu
    // container (to avoid going menu points to the next line)
    fixMenuWidth = function() {
        var width = 0;
        var menuPoints = document.getElementsByClassName("top_menu_point");
        for (var i = 0; i < menuPoints.length; i++) {
            width += menuPoints[i].offsetWidth;
        }
        document.getElementById("top_navigation_container").setAttribute("style","width:" + width + "px");
    };

    addEventListenerCustom(window, "load", fixMenuWidth);

</script>

<div id="top_navigation_container">
    <ul class="top_navigation_level1" id="top_menu_container">
        <emm:ShowNavigation navigation="sidemenu"
                            highlightKey='<%= (String) request.getAttribute("sidemenu_active") %>'>
            <emm:ShowByPermission token="<%= _navigation_token %>">
                <%
                    String cssClassPostfix = _navigation_isHighlightKey.booleanValue() ? "_active" : "_no";
                    String styleClass = "top_navigation_level1" + cssClassPostfix;
                    if (_navigation_index.intValue() == 1) {
                        styleClass += "_round";
                    }
                %>
                <li class="top_menu_point">
                    <html:link page="<%= _navigation_href %>" styleClass="<%= styleClass %>">
                    	<c:if test="${empty _navigation_plugin}">
		                   	<bean:message key="<%= _navigation_navMsg %>"/>
		                </c:if>
		                <c:if test="${not empty _navigation_plugin}">
			                <emm:message key="${_navigation_navMsg}" plugin="${_navigation_plugin}"/>
		                </c:if>
                    </html:link>
                </li>
            </emm:ShowByPermission>
        </emm:ShowNavigation>
    </ul>
</div>
<emm:ShowNavigation navigation="sidemenu"
                    highlightKey='<%= (String) request.getAttribute("sidemenu_active") %>'>
    <emm:ShowByPermission token="<%= _navigation_token %>">

        <% if (_navigation_isHighlightKey.booleanValue()) { %>
        <div><br><br></div>
        <div class="float_left" style="width:1070px;">
            <ul class="top_navigation_level2">
                <emm:ShowNavigation navigation='<%= _navigation_navMsg+"Sub" %>'
                                    highlightKey="<%= (String) request.getAttribute(\"sidemenu_sub_active\") %>"
                                    prefix="_sub">
                    <emm:ShowByPermission token="<%= _sub_navigation_token %>">
                        <%
                            String subCssClassPostfix = _sub_navigation_isHighlightKey.booleanValue() ? "_active" : "_no";
                        %>
                        <li>
                            <%
                                if (_sub_navigation_index.intValue() != 1) {

                            %>
                            <label>|</label>
                            <% } %>
                            <html:link page="<%= _sub_navigation_href %>" styleClass="<%= \"top_navigation_level2\" + subCssClassPostfix %>">
                            	<c:if test="${empty _sub_navigation_plugin}">
	                            	<bean:message key="<%= _sub_navigation_navMsg %>"/>
	                            </c:if>
	                            <c:if test="${not empty _sub_navigation_plugin}">
	                            	<emm:message key="${_sub_navigation_navMsg}" plugin="${_sub_navigation_plugin}"/>
	                            </c:if>
                            </html:link>
                        </li>
                    </emm:ShowByPermission>
                </emm:ShowNavigation>
            </ul>
        </div>
        <% } %>
    </emm:ShowByPermission>
</emm:ShowNavigation>
