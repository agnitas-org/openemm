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
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/common/jsplumb.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/common/panzoom-9.2.5.min.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/common/jquery.contextMenu.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/common/jquery.disableSelection.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-de.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-es.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-fr.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-nl.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-pt.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-tr.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-zh-TW.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-it.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/grid/jquery.imagesloaded.min.js"></script>
<!--<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/common/jquery-datepicker-i18n/jquery.ui.datepicker-it.js"></script>-->
