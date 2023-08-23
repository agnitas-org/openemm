####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
from	__future__ import annotations
from	enum import Enum
#
class MediaType (Enum):
	EMAIL = 0
	@classmethod
	def valid (cls, mediatype: int) -> bool:
		return mediatype in {_m.value for _m in cls.__members__.values ()}

class UserStatus (Enum):
	ACTIVE = 1
	BOUNCE = 2
	ADMOUT = 3
	OPTOUT = 4
	WAITCONFIRM = 5
	BLOCKLIST = 6
	SUSPEND = 7
	@classmethod
	def find_status (cls, name: str) -> UserStatus:
		return cls.__members__[name.upper ()]

class WorkStatus (Enum):
	New = 'mailing.status.new'
	Admin = 'mailing.status.admin'
	Active = 'mailing.status.active'
	Disable = 'mailing.status.disable'
	Cancel = 'mailing.status.canceled'
	CancelCopy = 'mailing.status.canceledAndCopied'
	Edit = 'mailing.status.edit'
	Generating = 'mailing.status.in-generation'
	Finished = 'mailing.status.generation-finished'
	NoRecipient = 'mailing.status.norecipients'
	Ready = 'mailing.status.ready'
	Scheduled = 'mailing.status.scheduled'
	Sending = 'mailing.status.sending'
	Sent = 'mailing.status.sent'
	Test = 'mailing.status.test'

