<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<agn:agnForm action="/importwizard" enctype="multipart/form-data" data-form="static">
    <html:hidden property="action" value="4"/>

    <div class="col-md-10 col-md-push-1 col-lg-8 col-lg-push-2">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><i class="icon icon-file-o"></i> <bean:message key="ImportClassic"/></h2>
                <ul class="tile-header-actions">
                    <li class="">
                        <ul class="pagination">
                            <li>
                                <a href="#" data-form-action="previous">
                                    <i class="icon icon-angle-left"></i>
                                    <bean:message key="button.Back" />
                                </a>
                            </li>
                            <li class="active"><span>1</span></li>
                            <li class="disabled"><span>2</span></li>
                            <li class="disabled"><span>3</span></li>
                            <li class="disabled"><span>4</span></li>
                            <li class="disabled"><span>5</span></li>
                            <li class="disabled"><span>6</span></li>
                            <li class="disabled"><span>7</span></li>
                            <li>
                                <a href="javascript:void(0);" data-form-submit>
                                    <bean:message key="button.Proceed" />
                                    <i class="icon icon-angle-right"></i>
                                </a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label">
                            <bean:message key="settings.FileName"/>
                            <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/FileName.xml"></button>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:file styleClass="form-control" property="csvFile" styleId="csvFile"/>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label">
                            <bean:message key="import.wizard.uploadCsvFile"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <label class="toggle">
                            <input type="checkbox" id="useCsvUpload" name="useCsvUpload" data-toggle-usability="#attachment_csv_file_id, #csvFile" />
                            <div class="toggle-control"></div>
                        </label>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-4 col-sm-8">
                        <html:select styleClass="form-control js-select" property="attachmentCsvFileID" size="1" styleId="attachment_csv_file_id" disabled="true">
                            <c:forEach var="csvFile" items="${csvFiles}">
                                ${csvFile.uploadID}
                                <html:option value="${csvFile.uploadID}">${csvFile.filename}</html:option>
                            </c:forEach>
                        </html:select>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label">
                            <bean:message key="Separator"/>
                            <button class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/Separator.xml"></button>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:select styleClass="form-control js-select" property="status.separator" size="1">
                            <html:option value=";">;</html:option>
                            <html:option value=",">,</html:option>
                            <html:option value="|">|</html:option>
                            <html:option value="t">Tab</html:option>
                            <html:option value="^">^</html:option>
                        </html:select>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label">
                            <bean:message key="Delimiter"/>
                            <button class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/Delimiter.xml"></button>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:select styleClass="form-control js-select" property="status.delimiter" size="1">
                            <html:option value=""><bean:message key="delimiter.none"/></html:option>
                            <html:option value="&#34;"><bean:message key="delimiter.doublequote"/></html:option>
                            <html:option value="'"><bean:message key="delimiter.singlequote"/></html:option>
                        </html:select>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label">
                            <bean:message key="mailing.Charset"/>
                            <button class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/Charset.xml"></button>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:select styleClass="form-control js-select" property="status.charset" size="1">
							<emm:ShowByPermission token="charset.use.iso_8859_15">
								<html:option value="ISO-8859-15"><bean:message key="mailing.iso-8859-15" /></html:option>
							</emm:ShowByPermission>
							<emm:ShowByPermission token="charset.use.utf_8">
								<html:option value="UTF-8"><bean:message key="mailing.utf-8" /></html:option>
							</emm:ShowByPermission>

							<%@include file="/WEB-INF/jsp/importwizard_classic/importwizard_classic-start-premium_charsets.jspf" %>

                        </html:select>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="import.dateFormat"/></label>
                    </div>
                    <div class="col-sm-8">
                        <html:select styleClass="form-control js-select" property="dateFormat" size="1">
                            <html:option value="dd.MM.yyyy HH:mm">dd.MM.yyyy HH:mm</html:option>
                            <html:option value="dd.MM.yyyy">dd.MM.yyyy</html:option>
                            <html:option value="yyyyMMdd">yyyyMMdd</html:option>
                            <html:option value="yyyyMMdd HH:mm">yyyyMMdd HH:mm</html:option>
                            <html:option value="yyyy-MM-dd HH:mm:ss">yyyy-MM-dd HH:mm:ss</html:option>
                            <html:option value="dd.MM.yyyy HH:mm:ss">dd.MM.yyyy HH:mm:ss</html:option>
                            <html:option value="dd.MM.yy">dd.MM.yy</html:option>
                        </html:select>
                    </div>
                </div>
            </div>
            <div class="tile-footer">
                <a href="#" class="btn btn-large pull-left" data-form-action="previous">
                    <i class="icon icon-angle-left"></i>
                    <span class="text"><bean:message key="button.Back"/></span>
                </a>
                <a class="btn btn-large btn-primary pull-right" data-form-submit>
                    <span class="text"><bean:message key="button.Proceed"/></span>
                    <i class="icon icon-angle-right"></i>
                </a>
                <span class="clearfix"></span>
            </div>
        </div>
    </div>

</agn:agnForm>
