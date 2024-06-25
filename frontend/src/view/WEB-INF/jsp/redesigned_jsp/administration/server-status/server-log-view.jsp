<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%--@elvariable id="logFileContent" type="java.lang.String"--%>

<div class="tiles-container">
    <div class="tile">
        <div class="tile-body js-scrollable">
            <pre>${logFileContent}</pre>
        </div>
    </div>
</div>
