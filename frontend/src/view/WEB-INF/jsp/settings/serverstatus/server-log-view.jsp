<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="logFileContent" type="java.lang.String"--%>

<div class="col-sm-12">
    <div class="tile">
        <div class="tile-content">
	       <div style="white-space: normal; display: block; border: 0px;">
		       <pre style="font-size: 12px;">${logFileContent}</pre>
		   </div>
        </div>
    </div>
</div>
