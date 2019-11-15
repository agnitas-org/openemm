<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.agnitas.dao.TagDao" %>
<%@ page import="com.agnitas.dao.ComTitleDao" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<meta content="noindex, nofollow" name="robots">

<script type="text/javascript">

    function getResultValue() {
        var sel = document.getElementById('tagsel');
        return buildTagParameters(sel.value);
    }

    function hideAll() {
        document.getElementById('param1').style.visibility = 'hidden';
        document.getElementById('param2').style.visibility = 'hidden';
        document.getElementById('param3').style.visibility = 'hidden';

        document.getElementById('paramlabel1').style.visibility = 'hidden';
        document.getElementById('paramlabel2').style.visibility = 'hidden';
        document.getElementById('paramlabel3').style.visibility = 'hidden';
    }

    function updateTagParameters(select) {
        hideAll();
        var input = select.value;
        console.log(select.options + '-- options');
        console.log(select.selectedIndex + '-- index');
        var text = select.options[select.selectedIndex].text;
        var parameters = getTagParameters(input);
        if (parameters) {
            for (i = 0; i < parameters.length; ++i) {
                var parname = 'param' + (i + 1);
                var parlabel = 'paramlabel' + (i + 1);
                if (parameters[i] == 'column') {
                    document.getElementById(parname).innerHTML = document.getElementById('colsel').outerHTML;
                } else if (parameters[i] == 'type') {
                    document.getElementById(parname).innerHTML = document.getElementById('typesel').outerHTML;
                } else if (parameters[i] == 'base' && text == 'agnDATE') {
                        document.getElementById(parname).innerHTML = document.getElementById('basesel').outerHTML;
                } else {
                    document.getElementById(parname).innerHTML =
                        ' <input type="text" id="par' + (i+1) + '" name="par' + (i+1) + '" size="23">';
                }

                document.getElementById(parname).style.visibility = 'visible';
                document.getElementById(parlabel).firstChild.data = parameters[i] + ":";
                document.getElementById(parlabel).style.visibility = 'visible';
            }
        }
        return true;
    }

    function buildTagParameters(input) {
        var parameters = getTagParameters(input);
        var params = '';
        if (parameters) {
            for (i = 0; i < parameters.length; ++i) {
                var parname = 'par' + (i + 1);
                if (parameters[i] == 'column') {
                    parname = 'colsel';
                }
                if (parameters[i] == 'type') {
                    parname = 'typesel';
                }
                if (parameters[i] == 'base') {
                    parname = 'basesel';
                }

                var parvalue = document.getElementById(parname).value;
                params = params + " " + parameters[i] + '="' + parvalue + '"';
            }
        }

        return '[' + document.getElementById('tagsel').options[document.getElementById('tagsel').selectedIndex].text + params + ']';
    }

    function getTagParameters(input) {
        var parameters = input.match(/\{[^}]*\}/g);
        if (parameters) {
            for (i = 0; i < parameters.length; ++i) {
                parameters[i] = parameters[i].substr(1, parameters[i].length - 2);
            }
        }
        return parameters;
    }

</script>

<style type="text/css">
    body {
        background:  transparent;
    }

    body * {
        font-family: Tahoma, sans-serif;
        font-size: 11px;
    }
</style>

</head>

<%
	ApplicationContext aContext = WebApplicationContextUtils.getWebApplicationContext(application);
    TagDao tagDao = aContext.getBean("TagDao", TagDao.class);
    ComTitleDao titleDao = (ComTitleDao) aContext.getBean("TitleDao");
    List<org.agnitas.beans.Title> titles = titleDao.getTitles(AgnUtils.getCompanyID(request));
    pageContext.setAttribute("titles",titles);
    List<Map<String, String>> tags = tagDao.getTags(AgnUtils.getCompanyID(request));
    pageContext.setAttribute("tags",tags);
%>

<body>
<table cellSpacing="3" cellPadding="2" width="100%" border="0">
    <tr>
        <td noWrap><label for="tagsel"><bean:message key="htmled.tag"/>:</label>
        </td>
        <td width="100%">
            <select name="tagsel" id="tagsel"
                    onchange="updateTagParameters(document.getElementById('tagsel'))" size="1" style="width:180px">
                <c:forEach var="tag" items="${tags}">
                    <c:forEach var="tagM" items="${tag}">
                        <option value='<c:out value="${tagM.value}"/>'>${tagM.key}</option>
                    </c:forEach>
                </c:forEach>
            </select>
        </td>
    </tr>
    <tr>
        <td vAlign="top" nowrap>
            <div id="paramlabel1" style="position:relative; top:0; left:0; visibility:hidden">
            </div>
        </td>
        <td vAlign="top">
                <div id="param1" style="position:relative; top:0; left:0; visibility:hidden">
                </div>
        </td>
    </tr>

    <tr>
        <td vAlign="top" nowrap>
            <div id="paramlabel2" style="position:relative; top:0; left:0; visibility:hidden">
            </div>
        </td>
        <td vAlign="top">
            <div id="param2" style="position:relative; top:0; left:0; visibility:hidden">
            </div>
        </td>
    </tr>

    <tr>
        <td vAlign="top" nowrap>
            <div id="paramlabel3" style="position:relative; top:0; left:0; visibility:hidden">
            </div>
        </td>
        <td vAlign="top">
            <div id="param3" style="position:relative; top:0; left:0; visibility:hidden">
            </div>
        </td>
    </tr>

    <tr>
        <td vAlign="top" nowrap>
            <div id="paramlabel4" style="position:relative; top:0; left:0; visibility:hidden">
            </div>
        </td>
        <td vAlign="top">
            <div id="param4" style="position:relative; top:0; left:0; visibility:hidden">
            </div>
        </td>
    </tr>

</table>

<script type="text/javascript">
    updateTagParameters(document.getElementById('tagsel'));
</script>



<div hidden="true">
    <select id="colsel" class="tags_dialog_select" name="colsel" size="1" style="width:180px">
        <emm:ShowColumnInfo id="colsel" table="<%= AgnUtils.getCompanyID(request) %>">
            <option value=<%= pageContext.getAttribute("_colsel_column_name") %>><%= pageContext.getAttribute("_colsel_shortname") %>
            </option>
        </emm:ShowColumnInfo>
    </select>
</div>

<div hidden="true">
    <select id="typesel" class="tags_dialog_select" name="typesel" size="1" style="width:180px">
        <c:forEach var="title" items="${titles}">
            <option value="${title.id}">${title.description}</option>
        </c:forEach>
    </select>
</div>

<div hidden="true">
    <select id="basesel" class="tags_dialog_select" name="typesel" size="1" style="width:180px">
            <option value="now">Current time</option>
            <option value="senddate">Send date</option>
    </select>
</div>
</body>
</html>
