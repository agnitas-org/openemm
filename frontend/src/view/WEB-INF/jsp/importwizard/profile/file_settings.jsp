<%@ page language="java"
         import="org.agnitas.web.ImportProfileAction"
         contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="org.agnitas.web.forms.ImportProfileForm" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common" prefix="emm" %>

  <div class="tile">
      <div class="tile-header">
          <a href="#" class="headline" data-toggle-tile="#recipient-import-format">
              <i class="tile-toggle icon icon-angle-up"></i>
              <bean:message key="import.profile.file.settings"/>
          </a>
      </div>
      <div id="recipient-import-format" class="tile-content tile-content-forms" data-field="toggle-vis">
			<%@ include file="import_profile_datatype_setting.jspf" %>
          <div class="form-group" id="separator-input-group">
              <div class="col-sm-4">
                  <label class="control-label">
                      <label for="recipient-import-format-separator"><bean:message key="import.Separator"/></label>
                      <button class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/Separator.xml" tabindex="-1" type="button"></button>
                  </label>
              </div>
              <div class="col-sm-8">
                  <html:select styleId="recipient-import-format-separator" styleClass="form-control" property="profile.separator">
					<html:option value="0">;</html:option>
					<html:option value="1">,</html:option>
					<html:option value="2">|</html:option>
					<html:option value="3">Tab</html:option>
					<html:option value="4">^</html:option>
                  </html:select>
              </div>
          </div>
          <div class="form-group">
              <div class="col-sm-4">
                  <label class="control-label">
                      <label for="recipient-import-format-charset"><bean:message key="mailing.Charset"/></label>
                      <button class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/Charset.xml" tabindex="-1" type="button"></button>
                  </label>
              </div>
              <div class="col-sm-8">
                  <html:select styleId="recipient-import-format-charset" styleClass="form-control" property="profile.charset">
                      <c:forEach var="charset"
                                 items="${importProfileForm.charsets}">
                          <html:option value="${charset.intValue}">${charset.charsetName}</html:option>
                      </c:forEach>
                  </html:select>
              </div>
          </div>

          <div class="form-group" id="text-recognition-input-group">
              <div class="col-sm-4">
                  <label class="control-label">
                      <label for="recipient-import-format-delimiter"><bean:message key="import.Delimiter"/></label>
                      <button class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/Delimiter.xml" tabindex="-1" type="button"></button>
                  </label>
              </div>
              <div class="col-sm-8">
                  <html:select styleId="recipient-import-format-delimiter" styleClass="form-control" property="profile.textRecognitionChar">
                      <c:forEach var="delimiter"
                                 items="${importProfileForm.delimiters}">
                          <html:option value="${delimiter.intValue}">
                              <bean:message key="${delimiter.publicValue}"/>
                          </html:option>
                      </c:forEach>
                  </html:select>
              </div>
          </div>

          <div class="form-group">
              <div class="col-sm-4">
                  <label class="control-label">
                      <label for="recipient-import-format-dateformat"><bean:message key="import.dateFormat"/></label>
                      <button class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/DateFormat.xml" tabindex="-1" type="button"></button>
                  </label>
              </div>
              <div class="col-sm-8">
                  <html:select styleId="recipient-import-format-dateformat" styleClass="form-control" property="profile.dateFormat">
                      <c:forEach var="dateFormat"
                                 items="${importProfileForm.dateFormats}">
                          <html:option value="${dateFormat.intValue}">
                              <bean:message key="${dateFormat.publicValue}"/>
                          </html:option>
                      </c:forEach>
                  </html:select>
              </div>
          </div>

		<div class="form-group">
			<div class="col-sm-4">
				<label class="control-label">
					<label for="import_processaction"><bean:message key="csv.DecimalSeparator" /></label>
					<button class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/DecimalSeparator.xml" tabindex="-1" type="button"></button>
				</label>
			</div>
			<div class="col-sm-8">
				<html:select styleId="import_decimalseparator" styleClass="form-control" property="profile.decimalSeparator">
					<html:option value=".">.</html:option>
					<html:option value=",">,</html:option>
				</html:select>
			</div>
		</div>

		<%@include file="/WEB-INF/jsp/importwizard/profile/file_settings-nocsvheaders.jspf" %>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><bean:message key="import.zipped" />
                    <button class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/ImportZipped.xml" tabindex="-1" type="button"></button>
                </label>
            </div>
            <div class="col-sm-8">
                <html:hidden property="__STRUTS_CHECKBOX_profile.zipped" value="false"/>
  					<label class="toggle">
  						<html:checkbox styleId="zipPasswordCheckbox" property="profile.zipped" onclick="toggleZipPasswordVisibility();"/>
                          <div class="toggle-control"></div>
  					</label>
            </div>
        </div>
        
           <div class="form-group" id="zipPasswordGroup">
               <div class="col-sm-4">
                   <label for="" class="control-label"><bean:message key="import.zipPassword"/>
                   	<button class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_1/ImportZipped.xml" tabindex="-1" type="button"></button>
                   </label>
               </div>
               <div class="col-sm-8">
                   <html:text styleId="mailing_name" styleClass="form-control" property="profile.zipPassword" maxlength="99" />
               </div>
           </div>
	</div>
  </div>

<script type="text/javascript">
	$(document).ready(function() {
		toggleZipPasswordVisibility();
	}) 
	
	function toggleZipPasswordVisibility() {
		if (document.getElementById("zipPasswordCheckbox").checked) {
		    jQuery('#zipPasswordGroup').show();
		} else {
		    jQuery('#zipPasswordGroup').hide();
		}
	}
</script>
