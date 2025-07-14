/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.sender;

import java.util.Date;

/**
 * Sender for webhook messages.
 */
/*
 * Top-level structure:
 * 
 * {
 * 	 "event_count" : <number of events>,
 *   "event_type"  : <type of event>,
 *   "events"      : [
 *                     <N-times the basic message format, all of same event type>
 *   			     ]
 * }
 * 
 * 
 * Basic message format:
 * 
 * {
 *   "event_id"        : <unique numeric value>,
 *   "event_timestamp" : <timestamp of event in UTC>,
 *   "event_data"      : <event-specific data>
 * }
 * 
 * 
 * Example for "Mailing opened":
 * {
 * 	 "event_count" : 1,
 *   "event_type"  : 'mailing_opened',
 *	 "events"      : [
 * 					   {
 *   					 "event_id"        : 12345678,
 *   					 "event_timestamp" : "2021-02-18T13:06:45Z",
 *   					 "event_data"      : {
 *   						                   mailing_id: 123456,
 *   						                   recipient_id: 4567
 *   										 }
 *   				   }
 *				     ]
 * }
 * 
 * 
 * Example for "Link clicked":
 * {
 * 	 "event_count" : 1,
 *   "event_type"  : 'link_clicked',
 *	 "events"      : [
 * 					   {
 *   					 "event_id"        : 12345690,
 *   				  	 "event_timestamp" : "2021-02-18T13:08:21Z",
 *   					 "event_data" : {
 *   						              mailing_id: 123459,
 *   						              recipient_id: 5678,
 *   						              link_id: 23456789012
 *   				                    }
 *                     }
 *				    ]
 * }
 */
public interface WebhookMessageSender {

	/**
	 * Sends a block of webhook messages of same type and company ID.
	 * 
	 * @return <code>true</code> if at least one message was found in queue to send, <code>false</code> if queue was empty
	 */
	public boolean sendNextMessagePackage(final Date limitDate);

}
