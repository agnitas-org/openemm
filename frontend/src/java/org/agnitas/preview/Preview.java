/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
    enum Size{
        DESKTOP(1),
        MOBILE_PORTRAIT(2),
        MOBILE_LANDSCAPE(3),
        TABLET_PORTRAIT(4),
        TABLET_LANDSCAPE(5);

        private final int value;

        /**
         * @param value preview size integer constant used in front-end
         */
        Size(int value) {
            this.value = value;
        }
    
        public static Size getSizeById(int previewSize) {
            for (Size size: values()) {
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
    }

    /* Special IDs for identifing parts of message */
    /** The ID for the complete header */
    String ID_HEAD = "__head__";
    /** The ID for a hashtable for individual header lines */
    String ID_HDETAIL = "__head_detail__";
    /** The ID for the text part */
    String ID_TEXT = "__text__";
    /** The ID for the HTML part */
    String ID_HTML = "__html__";
    /** The ID for an error, if one had occured */
    String ID_ERROR = "__error__";

    /** done
     * CLeanup code
     */
    void done();

    int getMaxAge();

    void setMaxAge(int nMaxAge);

    int getMaxEntries();

    void setMaxEntries(int nMaxEntries);

    /** makePreview
     * The main entrance for this class, a preview for all
     * parts of the mail is generated into a hashtable for
     * the given mailing and customer. If cachable is set
     * to true, the result is cached for speed up future
     * access.
     * @param mailingID the mailing-id to create the preview for
     * @param customerID the customer-id to create the preview for
     * @param selector optional selector for selecting different version of cached page
     * @param anon if we should anonymize the result
     * @param convertEntities replace non ascii characters by ther HTML entity representation
     * @param ecsUIDs if set we should use ecs (extended click statistics) style UIDs
     * @param createAll if set create all displayable parts of the mailing
     * @param cachable if the result should be cached
     * @param each targetID is considered as true during text block creation for previewing
     * @return the preview
     */
    Page makePreview (long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean createAll, boolean cachable, long[] targetIDs);
    Page makePreview (long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean createAll, boolean cachable);
    Page makePreview (long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean cachable);
    Page makePreview (long mailingID, long customerID, String selector, String text, boolean anon, boolean cachable);
    Page makePreview (long mailingID, long customerID, String selector, boolean anon, boolean cachable);
    Page makePreview (long mailingID, long customerID, boolean cachable);
    Page makePreview (long mailingID, long customerID, long targetID);
    /* Wrapper for heatmap generation
     * @param mailingID the mailing to generate the heatmap for
     * @param customerID the customerID to generate the heatmap for
     * @return the preview
     */
    String makePreviewForHeatmap (long mailingID, long customerID);

    @Deprecated
    Map <String, Object> createPreview (long mailingID,
            long customerID, String selector, String text, boolean anon,
            boolean convertEntities, boolean ecsUIDs, boolean createAll,
            boolean cachable);
    @Deprecated
    Map <String, Object> createPreview (long mailingID,
            long customerID, String selector, String text, boolean anon,
            boolean convertEntities, boolean ecsUIDs, boolean cachable);
    @Deprecated
    Map<String, Object> createPreview(long mailingID,
            long customerID, String selector, String text, boolean anon,
            boolean cachable);
    @Deprecated
    Map<String, Object> createPreview(long mailingID,
            long customerID, String selector, boolean anon,
            boolean cachable);
    @Deprecated
    Map<String, Object> createPreview(long mailingID,
            long customerID,
            boolean cachable);

    /**
     * Get header-, text- or HTML-part from hashtable created by
     * createPreview as byte stream
     */
    @Deprecated
    byte[] getHeaderPart(Map<String, Object> output,
            String charset, boolean escape);

    @Deprecated
    byte[] getHeaderPart(Map<String, Object> output,
            String charset);

    @Deprecated
    byte[] getTextPart(Map<String, Object> output,
            String charset, boolean escape);

    @Deprecated
    byte[] getTextPart(Map<String, Object> output,
            String charset);

    @Deprecated
    byte[] getHTMLPart(Map<String, Object> output,
            String charset, boolean escape);

    @Deprecated
    byte[] getHTMLPart(Map<String, Object> output,
            String charset);

    /**
     * Get header-, text- or HTML-part as strings
     */
    @Deprecated
    String getHeader(Map<String, Object> output,
            boolean escape);

    @Deprecated
    String getHeader(Map<String, Object> output);

    @Deprecated
    String getText(Map<String, Object> output,
            boolean escape);

    @Deprecated
    String getText(Map<String, Object> output);

    @Deprecated
    String getHTML(Map<String, Object> output,
            boolean escape);

    @Deprecated
    String getHTML(Map<String, Object> output);

    /**
     * Get individual lines from the header
     */
    @Deprecated
    String[] getHeaderField(Map<String, Object> output,
            String field);

    @Deprecated
    String getPartOfHeader(Map<String, Object> output,
            boolean escape, String headerKeyword);

    /**
     * Get attachment names and content
     */
    @Deprecated
    String[] getAttachmentNames (Map <String, Object> output);

    @Deprecated
    byte[] getAttachment (Map <String, Object> output, String name);

    // well, we could create a global Hashmap containing all the values for this preview
    // but the part-Method is called not very often, so its more efficient to parse
    // the header if we need it.
    // As parameter give the "Keyword" you will get then the appropriate return String.
    // Possible Values for the Header are:
    // "Return-Path", "Received", "Message-ID", "Date", "From", "To", "Subject", "X-Mailer", "MIME-Version"
    // warning! We do a "startswith" comparison, that means, if you give "Re" as parameter, you will
    // get either "Return-Path" or "Received", depending on what comes at last.
    @Deprecated
    String getPartOfHeader(Map<String, Object> output, String charset, boolean forHTML, String headerKeyword) throws Exception;
}
