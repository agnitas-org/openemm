
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="isSettingsReadonly" type="java.lang.Boolean"--%>

<jsp:include page="/WEB-INF/jsp/common/trackablelink/trackablelink-view.jsp">
    <jsp:param name="controllerPath" value="/mailing/${mailingId}/trackablelink" />
    <jsp:param name="settingsReadonly" value="${isSettingsReadonly}" />
</jsp:include>
