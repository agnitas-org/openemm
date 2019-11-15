<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--css--%>
<c:url var="jqueryUiUrl" value="/assets/core/styles/common/jquery-ui.css"/>
<c:url var="contextMenuUrl" value="/assets/core/styles/common/jquery.contextMenu.css"/>
<c:url var="campaignManagerUrl" value="/assets/core/styles/campaignManager/campaignManager.css"/>
<link rel="stylesheet" href="${jqueryUiUrl}" />
<link rel="stylesheet" href="${contextMenuUrl}" />
<link rel="stylesheet" href="${campaignManagerUrl}">

<%--libs--%>
<%--<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/common/jquery.jsPlumb-1.3.16-all.js"></script>--%>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/common/jquery.contextMenu.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/common/jquery.disableSelection.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-de.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-es.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-fr.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-nl.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-pt.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-tr.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-zh-TW.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-it.js"></script>
<!--<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-it.js"></script>-->

<%--our js files--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowManagerSettings.js"></script>--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowManagerNodes.js"></script>--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowManagerSelection.js"></script>--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowManagerToolbar.js"></script>--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowManagerContextMenu.js"></script>--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowManagerScale.js"></script>--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowManagerNodeFactory.js"></script>--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowManagerEditorsHelper.js"></script>--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowManagerIconsSet.js"></script>--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowManagerStatistics.js"></script>--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowManagerChainProcessor.js"></script>--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowManagerAutoLayout.js"></script>--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowManager.js"></script>--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowMailingEditorBase.js"></script>--%>
<%--<script type="text/javascript" src="js/lib/workflow/workflowMailingSelectorBase.js"></script>--%>
