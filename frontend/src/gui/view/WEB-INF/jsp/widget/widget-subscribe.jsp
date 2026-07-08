<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.util.importvalues.Gender" %>
<%@ page import="com.agnitas.util.importvalues.MailType" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.widget.form.SubscribeWidgetForm"--%>

<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Subscribe form</title>
    <link rel="stylesheet" href="<c:url value="/assets/widget/subscribe.css" />">
</head>
<body>
<mvc:form modelAttribute="form" servletRelativeAction="/widget/subscribe.action" accept-charset="UTF-8">

    <mvc:hidden path="token" />

    <label for="gender"><mvc:message code="recipient.Salutation"/></label>
    <mvc:select id="gender" path="gender">
        <c:forEach var="gender" items="${[Gender.UNKNOWN, Gender.FEMALE, Gender.MALE]}">
            <mvc:option value="${gender}"/>
        </c:forEach>
    </mvc:select>

    <label for="firstName"><mvc:message code="Firstname" /></label>
    <mvc:text id="firstName" path="firstName" />

    <label for="lastName"><mvc:message code="Lastname" /></label>
    <mvc:text id="lastName" path="lastName" />

    <%@ include file="fragments/sms-number-field.jspf" %>

    <c:forEach var="mailType" items="${[MailType.HTML, MailType.TEXT]}">
        <div>
            <label for="mailtype"><mvc:message code="${mailType.messageKey}" /></label>
            <mvc:radiobutton id="mailtype" path="mailType" value="${mailType}" />
        </div>
    </c:forEach>

    <label for="email"><mvc:message code="mailing.MediaType.0" /></label>
    <mvc:text id="email" path="email" />

    <div>
        <label for="trackingVeto"><mvc:message code="recipient.trackingVeto" /></label>
        <mvc:checkbox id="trackingVeto" path="trackingVeto" />
    </div>

    <button type="submit">Submit</button>
</mvc:form>

<c:if test="${not empty successMsg}">
    <div class="widget-message widget-message--success">${successMsg}</div>
</c:if>

<c:if test="${not empty errorMsg}">
    <div class="widget-message widget-message--error">${errorMsg}</div>
</c:if>

<c:if test="${not empty successMsg or not empty errorMsg}">
    <script>
      setTimeout(() => {
        document.querySelector('.widget-message').style.display = 'none';
      }, 3000);
    </script>
</c:if>

</body>
</html>
