<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://displaytag.sf.net"               prefix="display" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags"   prefix="agn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>


<%--@elvariable id="isSearchEnabled" type="java.lang.Boolean"--%>

<mvc:form servletRelativeAction="/target/list.action" modelAttribute="targetForm" data-form="search">

    <c:if test="${isSearchEnabled}">
        <div class="tile">
            <div class="tile-header">
                <%@ include file="jspf/list-search-header.jspf" %>
            </div>
            <div class="tile-content tile-content-forms form-vertical" id="tile-targetSearch">
                <%@ include file="jspf/list-search-content.jspf" %>
            </div>
        </div>
    </c:if>

    <div class="tile">
        <div class="tile-header">
            <%@ include file="jspf/list-overview-header.jspf" %>
        </div>
        <div class="tile-content" data-form-content="">
            <%@ include file="jspf/list-overview-content.jspf" %>
        </div>
    </div>
</mvc:form>
