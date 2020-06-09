/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.mailing.web;

import java.util.List;
import java.util.Objects;

import com.agnitas.web.dto.BooleanResponseDto;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.mailing.service.MailingService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@RequestMapping("/mailing/ajax")
public final class MailingAjaxController {
	
	private static final Logger LOGGER = Logger.getLogger(MailingAjaxController.class);
	
	private final MailingService mailingService;
	
	public MailingAjaxController(final MailingService mailingService) {
		this.mailingService = Objects.requireNonNull(mailingService, "Mailing service is null");
	}
	
	@RequestMapping(value="/listActionBasedForMailinglist.action", produces="application/json")
	public final ResponseEntity<String> listAllActionBasedMailingsForMailinglist(final ComAdmin admin, @RequestParam(value="mailinglist") final int mailinglistID) {
		try {
			final List<LightweightMailing> list = mailingService.listAllActionBasedMailingsForMailinglist(admin.getCompanyID(), mailinglistID);
			
			final JSONObject root = new JSONObject();
			final JSONArray mailings = new JSONArray();
			
			for(final LightweightMailing mailing : list) {
				final JSONObject mailingJson = new JSONObject();
				
				mailingJson.put("id", mailing.getMailingID());
				mailingJson.put("shortname", mailing.getShortname());
				
				mailings.add(mailingJson);
			}
			
			root.put("mailings", mailings);
			
			return ResponseEntity.ok(root.toString());
		} catch(final Exception e) {
			LOGGER.error(String.format("Error listing action based mailings for mailing list %d", mailinglistID), e);
			
			final JSONObject json = new JSONObject();
			json.put("error", "Internal server error");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(json.toString());
		}
	}

	@PostMapping("{mailingId:\\d+}/lock.action")
	public ResponseEntity<BooleanResponseDto> tryToLock(ComAdmin admin, @PathVariable int mailingId) {
		try {
			// Start or prolong locking unless other admin is holding it.
			return ResponseEntity.ok(new BooleanResponseDto(mailingService.tryToLock(admin, mailingId)));
		} catch (MailingNotExistException e) {
			return ResponseEntity.notFound().build();
		}
	}
}
