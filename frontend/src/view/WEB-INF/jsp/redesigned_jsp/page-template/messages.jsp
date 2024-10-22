<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="mvc"     uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<emm:messagesPresent type="success">
    <script type="text/javascript" data-message="">
        <emm:messages var="msg" type="success">
            AGN.Lib.Messages.successText(`${emm:escapeJs(msg)}`);
        </emm:messages>
    </script>
</emm:messagesPresent>

<emm:messagesPresent type="info">
    <script type="text/javascript" data-message="">
        <emm:messages var="msg" type="info">
            AGN.Lib.Messages.infoText(`${emm:escapeJs(msg)}`);
        </emm:messages>
    </script>
</emm:messagesPresent>

<emm:messagesPresent type="warning">
    <script type="text/javascript" data-message>
      <emm:messages var="msg" type="warning">
           AGN.Lib.Messages.warnText(`${emm:escapeJs(msg)}`);
      </emm:messages>
    </script>
</emm:messagesPresent>

<emm:messagesPresent type="error">
    <script type="text/javascript" data-message>
      <c:choose>
          <c:when test="${fn:length(errorReport) gt 0}">
              <c:set var="popupErrorMsgText">
                  <emm:messagesPresent type="error">
                    <emm:messages var="msg" type="error">${msg}<br/></emm:messages>
                  </emm:messagesPresent>

                <display:table id="errorMessageReportRow" name="errorReport">
                    <display:column sortable="false" titleKey="mailing.tag">
                        <c:choose>
                            <c:when test='${not empty errorMessageReportRow[1]}'>
                                ${errorMessageReportRow[1]}
                            </c:when>
                            <c:otherwise>
                                ${errorMessageReportRow[2]}
                            </c:otherwise>
                        </c:choose>
                    </display:column>
                </display:table>
              </c:set>

              AGN.Lib.Messages.alertText(`${fn:replace(popupErrorMsgText, "'", "\\'")}`);
          </c:when>
          <c:otherwise>
            <emm:messages var="msg" type="error">
                AGN.Lib.Messages.alertText(`${emm:escapeJs(msg)}`);
            </emm:messages>
          </c:otherwise>
      </c:choose>
    </script>
</emm:messagesPresent>

<emm:messagesPresent type="error" formField="true">
    <emm:fieldMessages var="msg" type="error" fieldNameVar="fieldName">
        <script type="text/html" data-message="${fieldName}">
            ${fn:replace(msg, "'", "\\'")}
        </script>
    </emm:fieldMessages>
</emm:messagesPresent>
