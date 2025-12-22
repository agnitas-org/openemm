/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonWriter;

public class JsonRequestResponse extends BaseRequestResponse {

	private static final Logger logger = LogManager.getLogger(JsonRequestResponse.class);
	
	private JsonNode jsonDataNode;

	@Override
	public String getString() throws Exception {
		String responseJsonString;
		
		if (responseState == State.EXPORTED_TO_STREAM) {
			// nothing more to do
			responseJsonString = null;
		} else if (responseState == State.OK) {
			if (jsonDataNode != null) {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				try (JsonWriter jsonWriter = new JsonWriter(outputStream)) {
					jsonWriter.setUglify(true);
					if (jsonDataNode.isJsonObject()) {
						jsonWriter.add((JsonObject) jsonDataNode.getValue());
					} else if (jsonDataNode.isJsonArray()) {
						jsonWriter.add((JsonArray) jsonDataNode.getValue());
					} else {
						jsonWriter.addSimpleValue(jsonDataNode.getValue());
					}
				}
				responseJsonString = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
			} else {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				try (JsonWriter jsonWriter = new JsonWriter(outputStream)) {
					jsonWriter.openJsonObject();
					jsonWriter.openJsonObjectProperty("error");
					jsonWriter.addSimpleJsonObjectPropertyValue("Invalid end state");
					jsonWriter.closeJsonObject();
				}
				responseJsonString = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
			}
		} else {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			try (JsonWriter jsonWriter = new JsonWriter(outputStream)) {
				jsonWriter.openJsonObject();
				jsonWriter.openJsonObjectProperty("error");
				jsonWriter.addSimpleJsonObjectPropertyValue(error.getMessage());
				if (errorCode != null) {
					jsonWriter.openJsonObjectProperty("errorCode");
					jsonWriter.addSimpleJsonObjectPropertyValue(errorCode.getCode());
				}
				jsonWriter.openJsonObjectProperty("errorTime");
				jsonWriter.addSimpleJsonObjectPropertyValue(new Date());
				jsonWriter.closeJsonObject();
			}
			responseJsonString = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
		}

		logger.debug(responseJsonString);
		return responseJsonString;
	}

	@Override
	public String getMimeType() {
		return "application/json";
	}
	
	public void setJsonResponseData(JsonNode jsonDataNode) {
		this.jsonDataNode = jsonDataNode;
	}
}
