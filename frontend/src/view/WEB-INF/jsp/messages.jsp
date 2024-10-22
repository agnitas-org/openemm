<%--  TODO: EMMGUI-714: remove after old design will be removed  --%>
<%--
********************************************************************************
   IMPORTANT NOTE
********************************************************************************

Your message / error message is not shown?

DO NOT POKE AROUND IN THIS FILE!!

First check, that your message / error message is in the right property!
To few your message at the classic location (near top of page), add your message or error message by

errors.add( ActionMessages.GLOBAL_MESSAGE, ...)
or
errors.add( ActionErrors.GLOBAL_MESSAGE, ...)

Using any other keys only makes sense with an additional <html:message property="..." /> tag at the form element where you
want to show your message / error message!
--%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<emm:messagesPresent type="success">
    <script type="text/javascript" data-message>
        <emm:messages var="msg" type="success" >
            AGN.Lib.Messages('<mvc:message code="default.Success" />', '${emm:escapeJs(msg)}', 'success');
        </emm:messages>
    </script>
</emm:messagesPresent>

<emm:messagesPresent type="info">
    <script type="text/javascript" data-message="">
        <emm:messages var="msg" type="info">
            AGN.Lib.Messages('<mvc:message code="Info"/>', '${emm:escapeJs(msg)}', 'info');
        </emm:messages>
    </script>
</emm:messagesPresent>

<emm:messagesPresent type="warning">
    <script type="text/javascript" data-message>
        <emm:messages var="msg" type="warning" >
            AGN.Lib.Messages('<mvc:message code="warning" />', '${emm:escapeJs(msg)}', 'warning');
        </emm:messages>
    </script>
</emm:messagesPresent>

<emm:messagesPresent type="error">
    <script type="text/javascript" data-message>
        <emm:messages var="msg" type="error">
            <c:set var="myText">
                ${msg}<br>
                <c:if test='${not empty errorReport }'>
                    <display:table name='errorReport' id='reportRow' class='errorTable' >
                        <display:column  headerClass='head_name' class='name'  sortable='false' titleKey='mailing.tag'>
                            <c:choose>
                                <c:when test='${not empty reportRow[1] }'>
                                    ${reportRow[1]}
                                </c:when>
                                <c:otherwise>
                                    ${reportRow[2]}
                                </c:otherwise>
                            </c:choose>
                        </display:column>
                    </display:table>
                </c:if>
            </c:set>

            AGN.Lib.Messages('<mvc:message code="Error" />', '${fn:replace(myText, "'", "\\'")}${fn:replace(errorTable, newLineChar, "")}', 'alert');
        </emm:messages>
    </script>
</emm:messagesPresent>

<emm:messagesPresent type="error" formField="true">
    <emm:fieldMessages var="msg" type="error" fieldNameVar="fieldName">
        <script type="text/html" data-message="${fieldName}">
            ${msg}
        </script>
    </emm:fieldMessages>
</emm:messagesPresent>
