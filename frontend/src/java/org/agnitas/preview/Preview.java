/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.preview;

import java.util.Map;

public interface Preview {

	/**
	 * Preview size constants
	 */
	enum Size {
		DESKTOP(1, 1022, false, "predelivery.desktop"),
        MOBILE_PORTRAIT(2, 320, true, "mailing.PreviewSize.MobilePortrait"),
        MOBILE_LANDSCAPE(3, 356, true, "mailing.PreviewSize.MobileLandscape"),
        TABLET_PORTRAIT(4, 768, true, "mailing.PreviewSize.TabletPortrait"),
        TABLET_LANDSCAPE(5, 1024, false, "mailing.PreviewSize.TabletLandscape");

		private final int value;
		private final int width;
        private final boolean mediaQuery;
        private final String msgCode;

		/**
         * @param value preview size integer constant used in front-end
         * @param width default width of the device
         * @param mediaQuery media query
         * @param msgCode message code for i18n
         */
		Size(int value, int width, boolean mediaQuery, String msgCode) {
			this.value = value;
            this.width = width;
            this.mediaQuery = mediaQuery;
            this.msgCode = msgCode;
        }

		public static Size getSizeById(int previewSize) {
			for (Size size : values()) {
				if (size.getValue() == previewSize) {
					return size;
				}
			}
			return DESKTOP;
		}

		/**
		 * @return associated integer constant
		 */
		public int getValue() {
			return value;
		}

        public int getWidth() {
            return width;
        }

        public boolean isMediaQuery() {
            return mediaQuery;
        }

        public String getMsgCode() {
            return msgCode;
        }
    }

	/* Special IDs for identifing parts of message */
	/**
	 * The ID for the complete header
	 */
	String ID_HEAD = "__head__";
	/**
	 * The ID for a hashtable for individual header lines
	 */
	String ID_HDETAIL = "__head_detail__";
	/**
	 * The ID for the text part
	 */
	String ID_TEXT = "__text__";
	/**
	 * The ID for the HTML part
	 */
	String ID_HTML = "__html__";
	
	final String ID_SMS = "__sms__";
	
	/**
	 * The ID for an error, if one had occured
	 */
	String ID_ERROR = "__error__";

	/**
	 * done
	 * CLeanup code
	 */
	void done();

	int getMaxAge();

	void setMaxAge(int nMaxAge);

	int getMaxEntries();

	void setMaxEntries(int nMaxEntries);

	/**
	 * makePreview
	 * The main entrance for this class, a preview for all
	 * parts of the mail is generated into a hashtable for
	 * the given mailing and customer. If cachable is set
	 * to true, the result is cached for speed up future
	 * access.
	 *
	 * @param mailingID		 the mailing-id to create the preview for
	 * @param customerID		 the customer-id to create the preview for
	 * @param selector		 optional selector for selecting different version of cached page
	 * @param anon			 if we should anonymize the result
	 * @param convertEntities	 replace non ascii characters by ther HTML entity representation
	 * @param ecsUIDs		 if set we should use ecs (extended click statistics) style UIDs
	 * @param createAll		 if set create all displayable parts of the mailing
	 * @param cachable		 if the result should be cached
	 * @param targetIDs		 targetID is considered as true during text block creation for previewing
	 * @param isMobile		 parameter for preview purpose to resolve image url
	 * @param sendDate		 sendDate of original mailing as unix epoch
	 * @param onAnonPreserveLinks    if an anon preview is created, preserve the plain links instead of eliminating them
	 * @return the preview
	 */
	Page makePreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean createAll, boolean cachable, long[] targetIDs, boolean isMobile, long sendDate, boolean onAnonPreserveLinks);

	Page makePreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean createAll, boolean cachable);

	Page makePreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean cachable);

	Page makePreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean cachable);

	Page makePreview(long mailingID, long customerID, String selector, boolean anon, boolean cachable);

	Page makePreview(long mailingID, long customerID, String selector, boolean anon, boolean cachable, long sendDate);

	Page makePreview(long mailingID, long customerID, boolean cachable);

    Page makeAnonPreview(long mailingId, boolean mobile);

	Page makePreview(long mailingID, long customerID, boolean cachable, long sendDate);

	Page makePreview(long mailingID, long customerID, boolean cachable, boolean isMobile);

	Page makePreview(long mailingID, long customerID, long targetID);

	Page makePreview(long mailingID, long customerID, long targetID, boolean isMobile);

	String makePreview(long mailingID, long customerID, String text, boolean cachable);

	String makePreview(long mailingID, long customerID, String text);

	String makePreview(long mailingID, long customerID, String text, String proxy, boolean encode);

	String makePreview(long mailingID, long customerID, String text, String proxy);

	/* Wrapper for heatmap generation
	 * @param mailingID the mailing to generate the heatmap for
	 * @param customerID the customerID to generate the heatmap for
	 * @return the preview
	 */
	String makePreviewForHeatmap(long mailingID, long customerID);

	String makePreviewForHeatmap(long mailingID, long customerID, String proxy, boolean encode);

	String makePreviewForHeatmap(long mailingID, long customerID, String proxy);

	@Deprecated
	Map<String, Object> createPreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean createAll, boolean cachable);

	@Deprecated
	Map<String, Object> createPreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean cachable);

	@Deprecated
	Map<String, Object> createPreview(long mailingID, long customerID, String selector, boolean anon, boolean cachable);

}
