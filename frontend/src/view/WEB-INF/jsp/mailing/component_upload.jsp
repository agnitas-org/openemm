<%@ page language="java" import="org.agnitas.util.*, java.net.*, java.util.*," contentType="text/html; charset=utf-8"  errorPage="/error.do" %>

<html>
<head>
<script type="text/javascript">
<!--
   window.parent.OnUploadCompleted(0, '<%= request.getAttribute("file_path") %>', 'test.jpg', 'Alles ok!');
-->
</script>
</head>
</html>
