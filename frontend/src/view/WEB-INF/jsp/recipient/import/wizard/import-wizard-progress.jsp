<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div class="col-md-10 col-md-push-1 col-lg-8 col-lg-push-2">
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><i class="icon icon-file-o"></i> <mvc:message code="import.Wizard"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div data-load="<c:url value='/recipient/import/wizard/run.action'/>" data-load-target="#import-result" data-load-interval="5000" data-prevent-load=''></div>
        </div>
    </div>
</div>
