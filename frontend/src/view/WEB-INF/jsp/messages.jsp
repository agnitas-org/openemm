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
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<logic:messagesPresent property="org.apache.struts.action.GLOBAL_MESSAGE" message="true">
    <script type="text/javascript" data-message>
        <html:messages id="msg" property="org.apache.struts.action.GLOBAL_MESSAGE" message="true" >
            AGN.Lib.Messages('<bean:message key="default.Success" />', '${emm:escapeJs(msg)}', 'success');
        </html:messages>
    </script>
</logic:messagesPresent>

<logic:messagesPresent property="de.agnitas.GLOBAL_WARNING" message="true">
    <script type="text/javascript" data-message>
        <html:messages id="msg" property="de.agnitas.GLOBAL_WARNING" message="true" >
            AGN.Lib.Messages('<bean:message key="warning" />', '${emm:escapeJs(msg)}', 'warning');
        </html:messages>
    </script>
</logic:messagesPresent>

<logic:messagesPresent property="de.agnitas.GLOBAL_WARNING_PERMANENT" message="true">
    <script type="text/javascript" data-message>
        <html:messages id="msg" property="de.agnitas.GLOBAL_WARNING_PERMANENT" message="true" >
            AGN.Lib.Messages('<bean:message key="warning" />', '${emm:escapeJs(msg)}', 'warning_permanent');
        </html:messages>
    </script>
</logic:messagesPresent>

<logic:messagesPresent message="false">
    <script type="text/javascript" data-message>
        <html:messages id="msg" message="false">
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
            <%--AGN.Lib.Messages('<bean:message key="Error" />', '${myText}', 'alert');--%>
            AGN.Lib.Messages('<bean:message key="Error" />', '${fn:replace(myText, "'", "\\'")}${fn:replace(errorTable, newLineChar, "")}', 'alert');
        </html:messages>
    </script>
</logic:messagesPresent>

<c:if test="${POPUPS_FIELDS_ERRORS ne null}">
    <c:forEach var="fieldError" items="${POPUPS_FIELDS_ERRORS}">
        <script type="text/html" data-message="${fieldError.fieldName}">
                <mvc:message code="${fieldError.message.code}" arguments="${fieldError.argumentsStr}"/>
        </script>
    </c:forEach>
</c:if>
