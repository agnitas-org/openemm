/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mailing.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.antlr.v4.runtime.misc.Nullable;

import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.report.enums.DatabaseField;
import com.agnitas.emm.core.report.enums.DatabaseFieldUtils;

public class MailingModel {
	public interface AddGroup {
    	// do nothing
    }
	
	public interface AddFromTemplateGroup {
    	// do nothing
    }
	
	public interface GetGroup {
    	// do nothing
    }
	
	public interface UpdateGroup {
    	// do nothing
    }
	
	public interface SendGroup {
    	// do nothing
    }
	
	public interface CompanyGroup {
    	// do nothing
    }
	
	public interface GetForMLID {
    	// do nothing
    }

	public static enum MailingType {
		REGULAR("regular", 0), ACTION_BASED("action-based", 1), RULE_BASED("rule-based", 2);

		private final String name;
		private final int value;

		private MailingType(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public int getValue() {
			return value;
		}
	}

	public final static Map<String, MailingType> mailingTypeMap;
	static {
		mailingTypeMap = new HashMap<>(3);
		mailingTypeMap.put(MailingType.REGULAR.getName(), MailingType.REGULAR);
		mailingTypeMap.put(MailingType.ACTION_BASED.getName(), MailingType.ACTION_BASED);
		mailingTypeMap.put(MailingType.RULE_BASED.getName(), MailingType.RULE_BASED);
	}

	public final static Map<Integer, MailingType> mailingTypeValueMap;
	static {
		mailingTypeValueMap = new HashMap<>(3);
		mailingTypeValueMap.put(MailingType.REGULAR.getValue(), MailingType.REGULAR);
		mailingTypeValueMap.put(MailingType.ACTION_BASED.getValue(), MailingType.ACTION_BASED);
		mailingTypeValueMap.put(MailingType.RULE_BASED.getValue(), MailingType.RULE_BASED);
	}

	public static MailingType getMailingType(String mailingTypeString) {
		mailingTypeString = mailingTypeString != null ? mailingTypeString.toLowerCase() : null;
		MailingType mailingType = mailingTypeMap.get(mailingTypeString);
		if (mailingType == null) {
			throw new RuntimeException("Invalid mailType");
		}
		return mailingType;
	}

	public static MailingType getMailingType(Integer mailingTypeInt) {
		MailingType mailingType = mailingTypeValueMap.get(mailingTypeInt);
		if (mailingType == null) {
			throw new RuntimeException("Invalid mailType");
		}
		return mailingType;
	}

	public enum Format implements DatabaseField<Integer, Format> {
		TEXT(0, "text", "MailType.0"),
		ONLINE_HTML(1, "online-html", "HTML"),
		OFFLINE_HTML(2, "offline-html", "MailType.2"),;

		private final String name;
		private final int value;
		private final String translationKey;

		Format(int value, String name, String translationKey) {
			this.name = name;
			this.value = value;
			this.translationKey = translationKey;
		}

		/**
		 * Same as {@link Format#getReadableName()}.
		 * Need just for backward compatible.
		 *
		 * @return readable name.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Same as {@link Format#getCode()}.
		 * Need just for backward compatible.
		 *
		 * @return code name of field. value from DB.
		 */
		public int getValue() {
			return value;
		}

		@Nullable
		public static Format getByCode(final int code) {
			return (Format) DatabaseFieldUtils.getByCode(code, values());
		}

		@Nullable
		public static Format getByName(final String readableName) {
			return (Format) DatabaseFieldUtils.getByName(readableName, values());
		}

		@Nullable
		public static String getTranslationKeyByCode(final int code) {
			return DatabaseFieldUtils.getTranslationKeyByCode(code, values());
		}

		public static boolean isContainsCode(final int code) {
			return DatabaseFieldUtils.isContainsCode(code, values());
		}

		@Override
		public Integer getCode() {
			return value;
		}

		@Override
		public String getReadableName() {
			return name;
		}

		@Override
		public String getTranslationKey() {
			return translationKey;
		}
	}

	public final static Map<String, Format> formatMap;
	static {
		formatMap = new HashMap<>(3);
		formatMap.put(Format.TEXT.getName(), Format.TEXT);
		formatMap.put(Format.ONLINE_HTML.getName(), Format.ONLINE_HTML);
		formatMap.put(Format.OFFLINE_HTML.getName(), Format.OFFLINE_HTML);
	}

	public final static Map<Integer, Format> formatValueMap;
	static {
		formatValueMap = new LinkedHashMap<>(3);
		formatValueMap.put(Format.TEXT.getValue(), Format.TEXT);
		formatValueMap.put(Format.ONLINE_HTML.getValue(), Format.ONLINE_HTML);
		formatValueMap.put(Format.OFFLINE_HTML.getValue(), Format.OFFLINE_HTML);
	}

	public static Format getFormat(String formatString) {
		formatString = formatString != null ? formatString.toLowerCase() : null;
		Format format = formatMap.get(formatString);
		if (format == null) {
			throw new RuntimeException("Invalid format");
		}
		return format;
	}

	public static List<Format> getFormatList(Integer formatInt) {
		// The exception for historical reason: When the mailformat parameter
		// has the value 2, it is interpreted as value 3.
		if (formatInt == 2) {
			formatInt = 3;
		}
		List<Format> formatList = new ArrayList<>(1);
		formatList.add(Format.TEXT);
		for (Entry<Integer, MailingModel.Format> entry : formatValueMap.entrySet()) {
			if ((entry.getKey() & formatInt) != 0) {
				formatList.add(entry.getValue());
			}
		}
		return formatList;
	}

	public static enum OnePixel {
		TOP("top"), BOTTOM("bottom"), NONE("none");

		private final String name;

		private OnePixel(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public final static Map<String, OnePixel> onePixelMap;
	static {
		onePixelMap = new HashMap<>(3);
		onePixelMap.put(OnePixel.TOP.getName(), OnePixel.TOP);
		onePixelMap.put(OnePixel.BOTTOM.getName(), OnePixel.BOTTOM);
		onePixelMap.put(OnePixel.NONE.getName(), OnePixel.NONE);
	}

	public static OnePixel getOnePixel(String onePixelString) {
		onePixelString = onePixelString != null ? onePixelString.toLowerCase() : null;
		OnePixel onePixel = onePixelMap.get(onePixelString);
		if (onePixel == null) {
			throw new RuntimeException("Invalid onepixel");
		}
		return onePixel;
	}

	public static enum TargetMode {
		OR("one", 0), AND("all", 1);

		private final String name;
		private final int value;

		private TargetMode(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public int getValue() {
			return value;
		}
	}

	public final static Map<String, TargetMode> targetModeMap;
	static {
		targetModeMap = new HashMap<>(2);
		targetModeMap.put(TargetMode.OR.getName(), TargetMode.OR);
		targetModeMap.put(TargetMode.AND.getName(), TargetMode.AND);
	}

	public final static Map<Integer, TargetMode> targetModeValueMap;
	static {
		targetModeValueMap = new HashMap<>(2);
		targetModeValueMap.put(TargetMode.OR.getValue(), TargetMode.OR);
		targetModeValueMap.put(TargetMode.AND.getValue(), TargetMode.AND);
	}

	public static TargetMode getTargetMode(String targetModeString) {
		targetModeString = targetModeString != null ? targetModeString.toLowerCase() : null;
		TargetMode targetMode = targetModeMap.get(targetModeString);
		if (targetMode == null) {
			throw new RuntimeException("Invalid targetMode");
		}
		return targetMode;
	}

	public static TargetMode getTargetMode(Integer targetModeInt) {
		TargetMode targetMode = targetModeValueMap.get(targetModeInt);
		if (targetMode == null) {
			throw new RuntimeException("Invalid targetMode");
		}
		return targetMode;
	}

	public static MaildropStatus getMaildropStatus(String maildropStatusString) throws Exception {
		maildropStatusString = maildropStatusString != null ? maildropStatusString.toLowerCase() : null;
		MaildropStatus maildropStatus = MaildropStatus.fromName(maildropStatusString);
		if (maildropStatus == null) {
			throw new RuntimeException("Invalid targetMode");
		}
		return maildropStatus;
	}

	private int companyId;
	private String shortname;
	private String description;
	private int mailinglistId;
	private List<Integer> targetIDList;
	private String mailingTypeString;
	private MailingType mailingType;
	private String subject;
	private String senderName;
	private String senderAddress;
	private String replyToName;
	private String replyToAddress;
	private String charset;
	private int linefeed;
	private String formatString;
	private Format format;
	private String onePixelString;
	private OnePixel onePixel;
	private boolean template;
	private int templateId;
	private boolean autoUpdate;
	private int mailingId;
	private String targetModeString;
	private TargetMode targetMode;
	
	private String maildropStatusString;
	private MaildropStatus maildropStatus;
	private Date sendDate;
	private int stepping;
	private int blocksize;

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(@VelocityCheck int companyId) {
		this.companyId = companyId;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getMailinglistId() {
		return mailinglistId;
	}

	public void setMailinglistId(int mailinglistId) {
		this.mailinglistId = mailinglistId;
	}

	public List<Integer> getTargetIDList() {
		return targetIDList;
	}

	public void setTargetIDList(List<Integer> targetIDList) {
		this.targetIDList = targetIDList;
	}

	public String getMailingTypeString() {
		return mailingTypeString;
	}

	public void setMailingType(String mailingTypeString) {
		this.mailingTypeString = mailingTypeString;
		mailingType = null;
	}

	public MailingType getMailingType() {
		if (mailingType == null && mailingTypeString != null) {
			mailingType = getMailingType(mailingTypeString);
		}
		return mailingType;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getSenderAddress() {
		return senderAddress;
	}

	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}

	public String getReplyToName() {
		return replyToName;
	}

	public void setReplyToName(String replyToName) {
		this.replyToName = replyToName;
	}

	public String getReplyToAddress() {
		return replyToAddress;
	}

	public void setReplyToAddress(String replyToAddress) {
		this.replyToAddress = replyToAddress;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public int getLinefeed() {
		return linefeed;
	}

	public void setLinefeed(int linefeed) {
		this.linefeed = linefeed;
	}

	public String getFormatString() {
		return formatString;
	}

	public void setFormatString(String formatString) {
		this.formatString = formatString;
		format = null;
	}

	public Format getFormat() {
		if (format == null && formatString != null) {
			format = getFormat(formatString);
		}
		return format;
	}

	public String getOnePixelString() {
		return onePixelString;
	}

	public void setOnePixelString(String onePixelString) {
		this.onePixelString = onePixelString;
		onePixel = null;
	}

	public OnePixel getOnePixel() {
		if (onePixel == null && onePixelString != null) {
			onePixel = getOnePixel(onePixelString);
		}
		return onePixel;
	}

	public boolean isTemplate() {
		return template;
	}

	public void setTemplate(boolean template) {
		this.template = template;
	}

	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public int getTemplateId() {
		return templateId;
	}

	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}

	public int getMailingId() {
		return mailingId;
	}

	public void setMailingId(int mailingId) {
		this.mailingId = mailingId;
	}

	public String getTargetModeString() {
		return targetModeString;
	}

	public void setTargetMode(String targetModeString) {
		this.targetModeString = targetModeString;
		targetMode = null;
	}

	public TargetMode getTargetMode() {
		if (targetMode == null && targetModeString != null) {
			targetMode = getTargetMode(targetModeString);
		}
		return targetMode;
	}

	public String getMaildropStatusString() {
		return maildropStatusString;
	}

	public MaildropStatus getMaildropStatus() throws Exception {
		if (maildropStatus == null && maildropStatusString != null) {
			maildropStatus = getMaildropStatus(maildropStatusString);
		}
		return maildropStatus;
	}

	public void setMaildropStatus(String maildropStatusString) {
		this.maildropStatusString = maildropStatusString;
		this.maildropStatus = null;
	}

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public int getStepping() {
		return stepping;
	}

	public void setStepping(int stepping) {
		this.stepping = stepping;
	}

	public int getBlocksize() {
		return blocksize;
	}

	public void setBlocksize(int blocksize) {
		this.blocksize = blocksize;
	}

}
