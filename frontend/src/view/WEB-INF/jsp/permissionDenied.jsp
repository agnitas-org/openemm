<%@ page isErrorPage="true" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>

<div class="login_page_root_container">
    <div class="login_page_top_spacer"></div>
    <div class="loginbox_container">

        <div class="loginbox_top"></div>

        <div class="loginbox_content">
            <img src="${emmLayoutBase.imagesURL}/facelift/agnitas-emm-logo.png" border="0" class="logon_image">
            <br>
            <span class="logon_page_emm_title"><bean:message key="permission.denied.title"/></span>
            <div class="loginbox_row">
                <bean:message key="permission.denied.message"/>
            </div>
        </div>

        <div class="loginbox_bottom"></div>
    </div>
</div>
