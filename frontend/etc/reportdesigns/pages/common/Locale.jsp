<%@ page import="com.agnitas.reporting.birt.external.utils.ComBirtResources" %>
<%@ page import="java.util.Locale" %>

<%
    Locale locale = request.getLocale();
%>
<%-- Map Java resource messages to Javascript constants --%>
<script type="text/javascript">
    // <![CDATA[
    // Error msgs
    // It is important to use the same message keys as BirtResources contains to keep backward compatible.
    Constants.error.invalidPageRange = '<%= ComBirtResources.getJavaScriptMessage( "birt.viewer.dialog.page.error.invalidpagerange", locale )%>';
    Constants.error.parameterRequired = '<%= ComBirtResources.getJavaScriptMessage( "birt.viewer.error.parameterrequired", locale )%>';
    Constants.error.parameterNotAllowBlank = '<%= ComBirtResources.getJavaScriptMessage( "birt.viewer.error.parameternotallowblank", locale )%>';
    Constants.error.parameterNotSelected = '<%= ComBirtResources.getJavaScriptMessage( "birt.viewer.error.parameternotselected", locale )%>';
    Constants.error.invalidPageNumber = '<%= ComBirtResources.getJavaScriptMessage( "birt.viewer.navbar.error.blankpagenum", locale )%>';
    Constants.error.invalidCopiesNumber = '<%= ComBirtResources.getJavaScriptMessage( "birt.viewer.error.copiesnumber", locale )%>';
    Constants.error.unknownError = '<%= ComBirtResources.getJavaScriptMessage( "birt.viewer.error.unknownerror", locale )%>';
    Constants.error.generateReportFirst = '<%= ComBirtResources.getJavaScriptMessage( "birt.viewer.error.generatereportfirst", locale )%>';
    Constants.error.columnRequired = '<%= ComBirtResources.getJavaScriptMessage( "birt.viewer.error.columnrequired", locale )%>';
    Constants.error.printPreviewAlreadyOpen = '<%= ComBirtResources.getJavaScriptMessage( "birt.viewer.dialog.print.printpreviewalreadyopen", locale )%>';
    Constants.error.confirmCancelTask = '<%= ComBirtResources.getJavaScriptMessage( "birt.viewer.progressbar.confirmcanceltask", locale )%>';
    // ]]>
</script>