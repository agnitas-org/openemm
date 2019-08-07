<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>

<%--@elvariable id="pluginUploadForm" type="com.agnitas.emm.core.pluginmanager.form.PluginUploadForm"--%>

<mvc:form servletRelativeAction="/administration/pluginmanager/plugin/install.action" modelAttribute="pluginUploadForm"
          enctype="multipart/form-data"
          class="form-vertical"
          data-form="static">

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="pluginmanager.install"/></h2>
        </div>

        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <label class="control-label" for="uploadPluginFile">
                    <bean:message key="pluginmanager.installer.select_file"/>
                </label>
                <input type="file" id="uploadPluginFile" name="uploadPluginFile" class="form-control" data-upload>
            </div>
            <div class="form-group">
                <button type="button" class="btn btn-regular btn-primary" data-form-submit>
                    <i class="icon icon-cloud-upload"></i>
                    <span class="text"><bean:message key="button.Upload"/></span>
                </button>
            </div>
        </div>

    </div>

</mvc:form>
