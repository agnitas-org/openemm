<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.*"  errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComRecipientAction" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://ajaxanywhere.sourceforge.net/" prefix="aa" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common" prefix="emm" %>

<script>

    function go() {
        document.getElementsByName('recipientForm')[0].submit();
    }

    ajaxAnywhere.getZonesToReload = function () {
        return "loading"
    };

    ajaxAnywhere.onAfterResponseProcessing = function () {
        if (! ${recipientForm.error })
            window.setTimeout("go();", ${recipientForm.refreshMillis});
    }
    ajaxAnywhere.showLoadingMessage = function() {
    };

    ajaxAnywhere.onAfterResponseProcessing();
</script>


<%
    RecipientForm recipient = (RecipientForm) session.getAttribute("recipientForm");
    recipient.setAction(ComRecipientAction.ACTION_SOCIAL_UPDATE);
%>

<aa:zone name="loading">

    <html:form action="/recipient">
        <html:hidden property="action"/>
        <html:hidden property="error"/>
        <div class="loading_container">
            <table border="0" cellspacing="0" cellpadding="0" width="400">
                <tr>
                    <td>
                        <b>&nbsp;<b>
                    </td>
                </tr>
                <tr>
                    <td>
                        <emm:setAbsolutePath var="absoluteImagePath" path="${emmLayoutBase.imagesURL}"/>
                        <logic:equal value="false" name="recipientForm" property="error">
                            <img border="0" width="44" height="48"
                                 src="${absoluteImagePath}/wait.gif"/>
                        </logic:equal>
                        <logic:equal value="true" name="recipientForm" property="error">
                            <img border="0" width="29" height="30"
                                 src="${absoluteImagePath}/warning.gif"/>
                        </logic:equal>
                    </td>
                </tr>
                <tr>
                    <td>
                        <b>&nbsp;<b>
                    </td>
                </tr>
                <tr>
                    <td>
                        <b>
                            <logic:equal value="false" name="recipientForm" property="error">
                                <bean:message key="default.loading"/>
                            </logic:equal>
                            <logic:equal value="true" name="recipientForm" property="error">
                                <bean:message key="default.loading.stopped"/>
                            </logic:equal>

                            <b>
                    </td>
                </tr>
                <tr>
                    <td>
                        <b>&nbsp;<b>
                    </td>
                </tr>

            </table>
        </div>
    </html:form>
</aa:zone>
