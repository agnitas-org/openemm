<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>

<agn:agnForm action="/newimportwizard">
    <html:hidden property="action"/>
    <html:errors/>
    <table border="0" cellspacing="0" cellpadding="0" width="100%">
        <tr>
            <td colspan=3>
                <div class="tooltiphelp" id="subscribelists">
                    <br><span class="head3"><bean:message key="import.SubscribeLists"/>:</span></div>
                <script type="text/javascript">
                    var hb1 = new HelpBalloon({
                        dataURL: 'help_${helplanguage}/importwizard/step_6/SubscribeLists.xml'
                    });
                    $('subscribelists').appendChild(hb1.icon);
                </script>
            </td>
        </tr>
        <tr>
            <td height="12px"/>
        </tr>

        <c:forEach var="mlist" items="${newImportWizardForm.allMailingLists}">
            <tr>
                <td width="20px"><input type="checkbox" name="agn_mlid_${mlist.id}"/></td>
                <td>
                        ${mlist.shortname}
                </td>
            </tr>
        </c:forEach>

        <tr>
            <td colspan="3">
                <hr>
                <html:image src="button?msg=button.Proceed" border="0"/>
            </td>
        </tr>

    </table>

</agn:agnForm>
