<%@ page import="com.agnitas.reporting.birt.external.utils.EmmBirtResources" %>
<%@ page import="java.util.Locale" %>

<%
    Locale locale = request.getLocale();
%>
<%-- Map Java resource messages to Javascript constants --%>
<script type="text/javascript">
    // <![CDATA[
    // Error msgs
    // It is important to use the same message keys as BirtResources contains to keep backward compatible.
    Constants.error.invalidPageRange = '<%= EmmBirtResources.getJavaScriptMessage( "birt.viewer.dialog.page.error.invalidpagerange", locale )%>';
    Constants.error.parameterRequired = '<%= EmmBirtResources.getJavaScriptMessage( "birt.viewer.error.parameterrequired", locale )%>';
    Constants.error.parameterNotAllowBlank = '<%= EmmBirtResources.getJavaScriptMessage( "birt.viewer.error.parameternotallowblank", locale )%>';
    Constants.error.parameterNotSelected = '<%= EmmBirtResources.getJavaScriptMessage( "birt.viewer.error.parameternotselected", locale )%>';
    Constants.error.invalidPageNumber = '<%= EmmBirtResources.getJavaScriptMessage( "birt.viewer.navbar.error.blankpagenum", locale )%>';
    Constants.error.invalidCopiesNumber = '<%= EmmBirtResources.getJavaScriptMessage( "birt.viewer.error.copiesnumber", locale )%>';
    Constants.error.unknownError = '<%= EmmBirtResources.getJavaScriptMessage( "birt.viewer.error.unknownerror", locale )%>';
    Constants.error.generateReportFirst = '<%= EmmBirtResources.getJavaScriptMessage( "birt.viewer.error.generatereportfirst", locale )%>';
    Constants.error.columnRequired = '<%= EmmBirtResources.getJavaScriptMessage( "birt.viewer.error.columnrequired", locale )%>';
    Constants.error.printPreviewAlreadyOpen = '<%= EmmBirtResources.getJavaScriptMessage( "birt.viewer.dialog.print.printpreviewalreadyopen", locale )%>';
    Constants.error.confirmCancelTask = '<%= EmmBirtResources.getJavaScriptMessage( "birt.viewer.progressbar.confirmcanceltask", locale )%>';
    // ]]>
</script>