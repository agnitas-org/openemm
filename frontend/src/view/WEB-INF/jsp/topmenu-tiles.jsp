<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

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
                    <a href="<c:url value="${_navigation_href}" />" class="${styleClass}">
                        <mvc:message code="<%= _navigation_navMsg %>"/>
                    </a>
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
                            <a href="<c:url value="${_sub_navigation_href}" />" class="<%= \"top_navigation_level2\" + subCssClassPostfix %>">
                                <mvc:message code="<%= _sub_navigation_navMsg %>"/>
                            </a>
                        </li>
                    </emm:ShowByPermission>
                </emm:ShowNavigation>
            </ul>
        </div>
        <% } %>
    </emm:ShowByPermission>
</emm:ShowNavigation>
