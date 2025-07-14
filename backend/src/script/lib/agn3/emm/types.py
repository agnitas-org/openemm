####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
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
__all__ = ['MediaType', 'MailType', 'UserType', 'UserStatus', 'MailingType', 'ComponentType', 'WorkStatus']
#
class MediaType (Enum):
	EMAIL = 0
	@classmethod
	def valid (cls, mediatype: int) -> bool:
		return mediatype in {_m.value for _m in cls.__members__.values ()}

class MailType (Enum):
	Text = 0
	HTML = 1
	OfflineHTML = 2

class UserType (Enum):
	ADMIN = 'A'
	TEST = 'T'
	TEST_VIP = 't'
	WORLD = 'W'
	WORLD_VIP = 'w'

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

class MailingType (Enum):
	#
	#	in maildrop_status_tbl.status_field
	WORLD = 'W'
	TEST = 'T'
	ADMIN = 'A'
	DATE_BASED = 'R'
	ACTION_BASED = 'E'
	ON_DEMAND = 'D'
	#
	#	in mailing_tbl.mailing_type
	MT_WORLD = 0
	MT_EVENT = 1
	MT_DATE = 2
	MT_FOLLOWUP = 3
	MT_INTERVAL = 4

class ComponentType (Enum):
	Template = 0
	Image = 1
	Attachment = 3
	PersonalizedAttachment = 4
	HostedImage = 5
	Font = 6
	PrecodedAttachment = 7
	ThumbnailImage = 8

class ComponentName (Enum):
	Head = 'agnHead'
	Text = 'agnText'
	HTML = 'agnHtml'
	PreHeader = 'agnPreheader'
	
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
	InsufficientVouchers = 'mailing.status.insufficient-vouchers'
	NoRecipient = 'mailing.status.norecipients'
	Ready = 'mailing.status.ready'
	Scheduled = 'mailing.status.scheduled'
	Sending = 'mailing.status.sending'
	Sent = 'mailing.status.sent'
	Test = 'mailing.status.test'
	@classmethod
	def by_name (cls, name: str) -> WorkStatus:
		try:
			return [_v for _v in cls.__members__.values () if _v.value == name][0]
		except IndexError:
			raise KeyError (name)

