<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="birtStatisticUrlWithoutFormat" type="java.lang.String"--%>

<div class="tiles-container">
    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="Statistics"/></h1>
        </div>
        <div class="tile-body js-scrollable">
            <iframe src="${birtStatisticUrlWithoutFormat}&__format=html" border="0" scrolling="auto" style="width: 100%" frameborder="0">
                Your Browser does not support IFRAMEs, please update!
            </iframe>
        </div>
    </div>
</div>
