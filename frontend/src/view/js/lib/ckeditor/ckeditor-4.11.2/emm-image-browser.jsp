<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="com.agnitas.beans.ComAdmin" %>
<%@ page import="org.agnitas.beans.Company" %>
<%@ page import="org.agnitas.emm.core.commons.util.ConfigService" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page import="org.apache.commons.lang3.math.NumberUtils" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:setAbsolutePath var="absoluteCssPath" path="${emmLayoutBase.cssURL}"/>
<emm:Permission token="mailing.components.show"/>

<%--@elvariable id="emmLayoutBase" type="org.agnitas.beans.impl.EmmLayoutBaseImpl"--%>

<%
    int tmpMailingID = NumberUtils.toInt(request.getParameter("mailingID"));
    Company company = ((ComAdmin) session.getAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN)).getCompany();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <link type="text/css" rel="stylesheet" href="${absoluteCssPath}/style.css">
    <link type="text/css" rel="stylesheet" href="${absoluteCssPath}/structure.css">
    <link type="text/css" rel="stylesheet" href="${absoluteCssPath}/displaytag.css">
</head>

<script type="text/javascript">

    function updateImg() {
        var imageNameValue = document.selform.imgsel.value;
        if (!imageNameValue || !imageNameValue.length){
            document.theimage.style.display = 'none';
            document.getElementById("no_image_message").style.display = '';
        } else {
            document.theimage.src = normalizeName(imageNameValue);
        }
        return 1;
    }

    function getUrlParam(paramName) {
        var reParam = new RegExp('(?:[\?&]|&amp;)' + paramName + '=([^&]+)', 'i');
        var match = window.location.search.match(reParam);
        return (match && match.length > 1) ? match[1] : '';
    }

    function submit_image() {
        var funcNum = getUrlParam('CKEditorFuncNum');
        window.opener.CKEDITOR.tools.callFunction(funcNum, normalizeName(document.selform.imgsel.value));
        window.close();
    }

    function normalizeName(fname) {
        if (fname.substr(0, 4).toLowerCase() != 'http') {
            fname = '<%= company.getRdirDomain() %>/image?ci=<%= company.getId() %>&mi=<%= tmpMailingID %>&name=' + fname;
        }
        return fname;
    }
</script>

<body onload="updateImg()">

<%
    String imageSrcExpression = "CONCAT(co.rdir_domain, '/image?ci=', cmp.company_id, '&mi=', cmp.mailing_id, '&name=', cmp.compname)";
    if (ConfigService.isOracleDB()) {
        imageSrcExpression = "co.rdir_domain||'/image?ci='||cmp.company_id||'&mi='||cmp.mailing_id||'&name='||cmp.compname";
    }
    String query = "select filename, image_src from (" +
        "  select cmp.compname filename, " +
        "      case when (cmp.comptype=1) then cmp.compname else " + imageSrcExpression + " end image_src" +
        "  from component_tbl cmp, company_tbl co where co.company_id = cmp.company_id and (cmp.comptype=1 or cmp.comptype=5) and cmp.mailing_id=" + tmpMailingID +
        "  and cmp.company_id=" + company.getId() +
        "  union all" +
        "  select upl.filename filename, co.rdir_domain||'/image?upi='||upl.upload_id image_src from upload_tbl upl, company_tbl co " +
        "  where co.company_id = upl.company_id " +
        "  and (lower(upl.filename) like '%.jpg' or lower(upl.filename) like '%.jpeg' or lower(upl.filename) like '%.png' or lower(upl.filename) like '%.gif') " +
        "  and upl.company_id = " + company.getId() +
        "  and upl.filename not in (select compname from component_tbl " +
        "  where (comptype=1 or comptype=5) and mailing_id=" + tmpMailingID +" and company_id="+ company.getId() +")) subsel1 order by filename";
%>
<form name="selform" id="selform" action="">
    <div>
        <div class="float_left fckeditor_select_panel">
            <div class="float_left">
                <bean:message key="mailing.Graphics_Component"/>:&nbsp;
                <select name="imgsel" id="imgsel" onchange="updateImg()" size="1">
                    <emm:ShowTable id="comp" sqlStatement="<%= query %>">
                        <option value='<%= (String)pageContext.getAttribute("_comp_image_src") %>'><%= pageContext.getAttribute("_comp_filename") %></option>
                    </emm:ShowTable>
                </select>
            </div>
            <div class="maildetail_button add_button">
                <a href="#"
                   onclick="submit_image(); return false;">
                    <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="button.Select"/></span>
                        </button>
                </a>
            </div>
        </div>
        <br>
        <div class="dotted_line fckeditor_dotted_line"></div>
        <br>
    </div>
</form>

<table border="0" cellspacing="0" cellpadding="0" width="100%" height="85%">
    <tr width="100%">
        <td align="center" valign="center">
            <img src="ckeditor-3.6.3/images/spacer.gif" name="theimage" border="1">
            <div id="no_image_message" style="display:none;"><bean:message key="mailing.Graphics_Component.NoImage"/></div>
        </td>
    </tr>
</table>
</body>
</html>
