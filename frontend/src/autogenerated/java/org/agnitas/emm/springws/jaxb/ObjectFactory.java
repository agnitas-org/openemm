/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.emm.springws.jaxb;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.agnitas.emm.springws.jaxb package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
@SuppressWarnings("all")
public class ObjectFactory {

    private final static QName _Error_QNAME = new QName("http://agnitas.org/ws/schemas", "Error");
    private final static QName _GetAttachmentResponse_QNAME = new QName("http://agnitas.org/ws/schemas", "GetAttachmentResponse");
    private final static QName _AttachmentDateTimeDefault_QNAME = new QName("http://agnitas.org/ws/schemas", "AttachmentDateTimeDefault");
    private final static QName _AttachmentDateTimeISO_QNAME = new QName("http://agnitas.org/ws/schemas", "AttachmentDateTimeISO");
    private final static QName _GetMailingResponse_QNAME = new QName("http://agnitas.org/ws/schemas", "GetMailingResponse");
    private final static QName _GetMailinglistResponse_QNAME = new QName("http://agnitas.org/ws/schemas", "GetMailinglistResponse");
    private final static QName _GetTemplateResponse_QNAME = new QName("http://agnitas.org/ws/schemas", "GetTemplateResponse");
    private final static QName _GetSubscriberBindingResponse_QNAME = new QName("http://agnitas.org/ws/schemas", "GetSubscriberBindingResponse");
    private final static QName _BindingDateTimeDefault_QNAME = new QName("http://agnitas.org/ws/schemas", "BindingDateTimeDefault");
    private final static QName _BindingDateTimeISO_QNAME = new QName("http://agnitas.org/ws/schemas", "BindingDateTimeISO");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.agnitas.emm.springws.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AddMailingRequest }
     * 
     */
    public AddMailingRequest createAddMailingRequest() {
        return new AddMailingRequest();
    }

    /**
     * Create an instance of {@link AddTemplateRequest }
     * 
     */
    public AddTemplateRequest createAddTemplateRequest() {
        return new AddTemplateRequest();
    }

    /**
     * Create an instance of {@link GetSubscriberRequest }
     * 
     */
    public GetSubscriberRequest createGetSubscriberRequest() {
        return new GetSubscriberRequest();
    }

    /**
     * Create an instance of {@link ListSubscriberMailingsResponse }
     * 
     */
    public ListSubscriberMailingsResponse createListSubscriberMailingsResponse() {
        return new ListSubscriberMailingsResponse();
    }

    /**
     * Create an instance of {@link ListContentBlockNamesResponse }
     * 
     */
    public ListContentBlockNamesResponse createListContentBlockNamesResponse() {
        return new ListContentBlockNamesResponse();
    }

    /**
     * Create an instance of {@link ListContentBlocksResponse }
     * 
     */
    public ListContentBlocksResponse createListContentBlocksResponse() {
        return new ListContentBlocksResponse();
    }

    /**
     * Create an instance of {@link ListMailingsRequest }
     * 
     */
    public ListMailingsRequest createListMailingsRequest() {
        return new ListMailingsRequest();
    }

    /**
     * Create an instance of {@link UpdateMailingRequest }
     * 
     */
    public UpdateMailingRequest createUpdateMailingRequest() {
        return new UpdateMailingRequest();
    }

    /**
     * Create an instance of {@link UpdateTemplateRequest }
     * 
     */
    public UpdateTemplateRequest createUpdateTemplateRequest() {
        return new UpdateTemplateRequest();
    }

    /**
     * Create an instance of {@link ListTrackableLinksResponse }
     * 
     */
    public ListTrackableLinksResponse createListTrackableLinksResponse() {
        return new ListTrackableLinksResponse();
    }

    /**
     * Create an instance of {@link GetTrackableLinkSettingsResponse }
     * 
     */
    public GetTrackableLinkSettingsResponse createGetTrackableLinkSettingsResponse() {
        return new GetTrackableLinkSettingsResponse();
    }

    /**
     * Create an instance of {@link UpdateTrackableLinkSettingsRequest }
     * 
     */
    public UpdateTrackableLinkSettingsRequest createUpdateTrackableLinkSettingsRequest() {
        return new UpdateTrackableLinkSettingsRequest();
    }

    /**
     * Create an instance of {@link ListTargetgroupsResponse }
     * 
     */
    public ListTargetgroupsResponse createListTargetgroupsResponse() {
        return new ListTargetgroupsResponse();
    }

    /**
     * Create an instance of {@link UpdateTrackableLinkSettingsRequest.LinkExtensions }
     * 
     */
    public UpdateTrackableLinkSettingsRequest.LinkExtensions createUpdateTrackableLinkSettingsRequestLinkExtensions() {
        return new UpdateTrackableLinkSettingsRequest.LinkExtensions();
    }

    /**
     * Create an instance of {@link GetTrackableLinkSettingsResponse.LinkExtensions }
     * 
     */
    public GetTrackableLinkSettingsResponse.LinkExtensions createGetTrackableLinkSettingsResponseLinkExtensions() {
        return new GetTrackableLinkSettingsResponse.LinkExtensions();
    }

    /**
     * Create an instance of {@link ListMailingsRequest.Filter }
     * 
     */
    public ListMailingsRequest.Filter createListMailingsRequestFilter() {
        return new ListMailingsRequest.Filter();
    }

    /**
     * Create an instance of {@link Template }
     * 
     */
    public Template createTemplate() {
        return new Template();
    }

    /**
     * Create an instance of {@link Mailing }
     * 
     */
    public Mailing createMailing() {
        return new Mailing();
    }

    /**
     * Create an instance of {@link AddAttachmentRequest }
     * 
     */
    public AddAttachmentRequest createAddAttachmentRequest() {
        return new AddAttachmentRequest();
    }

    /**
     * Create an instance of {@link AddAttachmentResponse }
     * 
     */
    public AddAttachmentResponse createAddAttachmentResponse() {
        return new AddAttachmentResponse();
    }

    /**
     * Create an instance of {@link AddBlacklistRequest }
     * 
     */
    public AddBlacklistRequest createAddBlacklistRequest() {
        return new AddBlacklistRequest();
    }

    /**
     * Create an instance of {@link AddBlacklistResponse }
     * 
     */
    public AddBlacklistResponse createAddBlacklistResponse() {
        return new AddBlacklistResponse();
    }

    /**
     * Create an instance of {@link AddContentBlockRequest }
     * 
     */
    public AddContentBlockRequest createAddContentBlockRequest() {
        return new AddContentBlockRequest();
    }

    /**
     * Create an instance of {@link AddContentBlockResponse }
     * 
     */
    public AddContentBlockResponse createAddContentBlockResponse() {
        return new AddContentBlockResponse();
    }

    /**
     * Create an instance of {@link AddMailinglistRequest }
     * 
     */
    public AddMailinglistRequest createAddMailinglistRequest() {
        return new AddMailinglistRequest();
    }

    /**
     * Create an instance of {@link AddMailinglistResponse }
     * 
     */
    public AddMailinglistResponse createAddMailinglistResponse() {
        return new AddMailinglistResponse();
    }

    /**
     * Create an instance of {@link AddMailingFromTemplateRequest }
     * 
     */
    public AddMailingFromTemplateRequest createAddMailingFromTemplateRequest() {
        return new AddMailingFromTemplateRequest();
    }

    /**
     * Create an instance of {@link AddMailingFromTemplateResponse }
     * 
     */
    public AddMailingFromTemplateResponse createAddMailingFromTemplateResponse() {
        return new AddMailingFromTemplateResponse();
    }

    /**
     * Create an instance of {@link AddMailingRequest.TargetIDList }
     * 
     */
    public AddMailingRequest.TargetIDList createAddMailingRequestTargetIDList() {
        return new AddMailingRequest.TargetIDList();
    }

    /**
     * Create an instance of {@link AddMailingResponse }
     * 
     */
    public AddMailingResponse createAddMailingResponse() {
        return new AddMailingResponse();
    }

    /**
     * Create an instance of {@link AddSubscriberRequest }
     * 
     */
    public AddSubscriberRequest createAddSubscriberRequest() {
        return new AddSubscriberRequest();
    }

    /**
     * Create an instance of {@link Map }
     * 
     */
    public Map createMap() {
        return new Map();
    }

    /**
     * Create an instance of {@link AddSubscriberResponse }
     * 
     */
    public AddSubscriberResponse createAddSubscriberResponse() {
        return new AddSubscriberResponse();
    }

    /**
     * Create an instance of {@link AddTemplateRequest.TargetIDList }
     * 
     */
    public AddTemplateRequest.TargetIDList createAddTemplateRequestTargetIDList() {
        return new AddTemplateRequest.TargetIDList();
    }

    /**
     * Create an instance of {@link AddTemplateResponse }
     * 
     */
    public AddTemplateResponse createAddTemplateResponse() {
        return new AddTemplateResponse();
    }

    /**
     * Create an instance of {@link CheckBlacklistRequest }
     * 
     */
    public CheckBlacklistRequest createCheckBlacklistRequest() {
        return new CheckBlacklistRequest();
    }

    /**
     * Create an instance of {@link CheckBlacklistResponse }
     * 
     */
    public CheckBlacklistResponse createCheckBlacklistResponse() {
        return new CheckBlacklistResponse();
    }

    /**
     * Create an instance of {@link DeleteAttachmentRequest }
     * 
     */
    public DeleteAttachmentRequest createDeleteAttachmentRequest() {
        return new DeleteAttachmentRequest();
    }

    /**
     * Create an instance of {@link DeleteAttachmentResponse }
     * 
     */
    public DeleteAttachmentResponse createDeleteAttachmentResponse() {
        return new DeleteAttachmentResponse();
    }

    /**
     * Create an instance of {@link DeleteBlacklistRequest }
     * 
     */
    public DeleteBlacklistRequest createDeleteBlacklistRequest() {
        return new DeleteBlacklistRequest();
    }

    /**
     * Create an instance of {@link DeleteBlacklistResponse }
     * 
     */
    public DeleteBlacklistResponse createDeleteBlacklistResponse() {
        return new DeleteBlacklistResponse();
    }

    /**
     * Create an instance of {@link DeleteContentBlockRequest }
     * 
     */
    public DeleteContentBlockRequest createDeleteContentBlockRequest() {
        return new DeleteContentBlockRequest();
    }

    /**
     * Create an instance of {@link DeleteContentBlockResponse }
     * 
     */
    public DeleteContentBlockResponse createDeleteContentBlockResponse() {
        return new DeleteContentBlockResponse();
    }

    /**
     * Create an instance of {@link DeleteMailinglistRequest }
     * 
     */
    public DeleteMailinglistRequest createDeleteMailinglistRequest() {
        return new DeleteMailinglistRequest();
    }

    /**
     * Create an instance of {@link DeleteMailinglistResponse }
     * 
     */
    public DeleteMailinglistResponse createDeleteMailinglistResponse() {
        return new DeleteMailinglistResponse();
    }

    /**
     * Create an instance of {@link DeleteMailingRequest }
     * 
     */
    public DeleteMailingRequest createDeleteMailingRequest() {
        return new DeleteMailingRequest();
    }

    /**
     * Create an instance of {@link DeleteMailingResponse }
     * 
     */
    public DeleteMailingResponse createDeleteMailingResponse() {
        return new DeleteMailingResponse();
    }

    /**
     * Create an instance of {@link DeleteSubscriberBindingRequest }
     * 
     */
    public DeleteSubscriberBindingRequest createDeleteSubscriberBindingRequest() {
        return new DeleteSubscriberBindingRequest();
    }

    /**
     * Create an instance of {@link DeleteSubscriberBindingResponse }
     * 
     */
    public DeleteSubscriberBindingResponse createDeleteSubscriberBindingResponse() {
        return new DeleteSubscriberBindingResponse();
    }

    /**
     * Create an instance of {@link DeleteSubscriberRequest }
     * 
     */
    public DeleteSubscriberRequest createDeleteSubscriberRequest() {
        return new DeleteSubscriberRequest();
    }

    /**
     * Create an instance of {@link DeleteSubscriberResponse }
     * 
     */
    public DeleteSubscriberResponse createDeleteSubscriberResponse() {
        return new DeleteSubscriberResponse();
    }

    /**
     * Create an instance of {@link DeleteTemplateRequest }
     * 
     */
    public DeleteTemplateRequest createDeleteTemplateRequest() {
        return new DeleteTemplateRequest();
    }

    /**
     * Create an instance of {@link DeleteTemplateResponse }
     * 
     */
    public DeleteTemplateResponse createDeleteTemplateResponse() {
        return new DeleteTemplateResponse();
    }

    /**
     * Create an instance of {@link FindSubscriberRequest }
     * 
     */
    public FindSubscriberRequest createFindSubscriberRequest() {
        return new FindSubscriberRequest();
    }

    /**
     * Create an instance of {@link FindSubscriberResponse }
     * 
     */
    public FindSubscriberResponse createFindSubscriberResponse() {
        return new FindSubscriberResponse();
    }

    /**
     * Create an instance of {@link GetAttachmentRequest }
     * 
     */
    public GetAttachmentRequest createGetAttachmentRequest() {
        return new GetAttachmentRequest();
    }

    /**
     * Create an instance of {@link AttachmentDateTimeDefault }
     * 
     */
    public AttachmentDateTimeDefault createAttachmentDateTimeDefault() {
        return new AttachmentDateTimeDefault();
    }

    /**
     * Create an instance of {@link AttachmentDateTimeISO }
     * 
     */
    public AttachmentDateTimeISO createAttachmentDateTimeISO() {
        return new AttachmentDateTimeISO();
    }

    /**
     * Create an instance of {@link GetBlacklistItemsRequest }
     * 
     */
    public GetBlacklistItemsRequest createGetBlacklistItemsRequest() {
        return new GetBlacklistItemsRequest();
    }

    /**
     * Create an instance of {@link GetBlacklistItemsResponse }
     * 
     */
    public GetBlacklistItemsResponse createGetBlacklistItemsResponse() {
        return new GetBlacklistItemsResponse();
    }

    /**
     * Create an instance of {@link GetContentBlockRequest }
     * 
     */
    public GetContentBlockRequest createGetContentBlockRequest() {
        return new GetContentBlockRequest();
    }

    /**
     * Create an instance of {@link GetContentBlockResponse }
     * 
     */
    public GetContentBlockResponse createGetContentBlockResponse() {
        return new GetContentBlockResponse();
    }

    /**
     * Create an instance of {@link GetMailingRequest }
     * 
     */
    public GetMailingRequest createGetMailingRequest() {
        return new GetMailingRequest();
    }

    /**
     * Create an instance of {@link GetMailinglistRequest }
     * 
     */
    public GetMailinglistRequest createGetMailinglistRequest() {
        return new GetMailinglistRequest();
    }

    /**
     * Create an instance of {@link Mailinglist }
     * 
     */
    public Mailinglist createMailinglist() {
        return new Mailinglist();
    }

    /**
     * Create an instance of {@link GetTemplateRequest }
     * 
     */
    public GetTemplateRequest createGetTemplateRequest() {
        return new GetTemplateRequest();
    }

    /**
     * Create an instance of {@link GetSubscriberRequest.Profilefields }
     * 
     */
    public GetSubscriberRequest.Profilefields createGetSubscriberRequestProfilefields() {
        return new GetSubscriberRequest.Profilefields();
    }

    /**
     * Create an instance of {@link GetSubscriberResponse }
     * 
     */
    public GetSubscriberResponse createGetSubscriberResponse() {
        return new GetSubscriberResponse();
    }

    /**
     * Create an instance of {@link ListSubscribersRequest }
     * 
     */
    public ListSubscribersRequest createListSubscribersRequest() {
        return new ListSubscribersRequest();
    }

    /**
     * Create an instance of {@link Criteria }
     * 
     */
    public Criteria createCriteria() {
        return new Criteria();
    }

    /**
     * Create an instance of {@link ListSubscribersResponse }
     * 
     */
    public ListSubscribersResponse createListSubscribersResponse() {
        return new ListSubscribersResponse();
    }

    /**
     * Create an instance of {@link GetSubscriberBindingRequest }
     * 
     */
    public GetSubscriberBindingRequest createGetSubscriberBindingRequest() {
        return new GetSubscriberBindingRequest();
    }

    /**
     * Create an instance of {@link BindingDateTimeDefault }
     * 
     */
    public BindingDateTimeDefault createBindingDateTimeDefault() {
        return new BindingDateTimeDefault();
    }

    /**
     * Create an instance of {@link BindingDateTimeISO }
     * 
     */
    public BindingDateTimeISO createBindingDateTimeISO() {
        return new BindingDateTimeISO();
    }

    /**
     * Create an instance of {@link ListSubscriberMailingsRequest }
     * 
     */
    public ListSubscriberMailingsRequest createListSubscriberMailingsRequest() {
        return new ListSubscriberMailingsRequest();
    }

    /**
     * Create an instance of {@link ListSubscriberMailingsResponse.Items }
     * 
     */
    public ListSubscriberMailingsResponse.Items createListSubscriberMailingsResponseItems() {
        return new ListSubscriberMailingsResponse.Items();
    }

    /**
     * Create an instance of {@link ListAttachmentsRequest }
     * 
     */
    public ListAttachmentsRequest createListAttachmentsRequest() {
        return new ListAttachmentsRequest();
    }

    /**
     * Create an instance of {@link ListAttachmentsResponse }
     * 
     */
    public ListAttachmentsResponse createListAttachmentsResponse() {
        return new ListAttachmentsResponse();
    }

    /**
     * Create an instance of {@link ListContentBlockNamesRequest }
     * 
     */
    public ListContentBlockNamesRequest createListContentBlockNamesRequest() {
        return new ListContentBlockNamesRequest();
    }

    /**
     * Create an instance of {@link ListContentBlockNamesResponse.ContentBlockName }
     * 
     */
    public ListContentBlockNamesResponse.ContentBlockName createListContentBlockNamesResponseContentBlockName() {
        return new ListContentBlockNamesResponse.ContentBlockName();
    }

    /**
     * Create an instance of {@link ListContentBlocksRequest }
     * 
     */
    public ListContentBlocksRequest createListContentBlocksRequest() {
        return new ListContentBlocksRequest();
    }

    /**
     * Create an instance of {@link ListContentBlocksResponse.ContentBlock }
     * 
     */
    public ListContentBlocksResponse.ContentBlock createListContentBlocksResponseContentBlock() {
        return new ListContentBlocksResponse.ContentBlock();
    }

    /**
     * Create an instance of {@link ListMailinglistsRequest }
     * 
     */
    public ListMailinglistsRequest createListMailinglistsRequest() {
        return new ListMailinglistsRequest();
    }

    /**
     * Create an instance of {@link ListMailinglistsResponse }
     * 
     */
    public ListMailinglistsResponse createListMailinglistsResponse() {
        return new ListMailinglistsResponse();
    }

    /**
     * Create an instance of {@link ListMailingsInMailinglistRequest }
     * 
     */
    public ListMailingsInMailinglistRequest createListMailingsInMailinglistRequest() {
        return new ListMailingsInMailinglistRequest();
    }

    /**
     * Create an instance of {@link ListMailingsInMailinglistResponse }
     * 
     */
    public ListMailingsInMailinglistResponse createListMailingsInMailinglistResponse() {
        return new ListMailingsInMailinglistResponse();
    }

    /**
     * Create an instance of {@link ListMailingsResponse }
     * 
     */
    public ListMailingsResponse createListMailingsResponse() {
        return new ListMailingsResponse();
    }

    /**
     * Create an instance of {@link ListTemplatesRequest }
     * 
     */
    public ListTemplatesRequest createListTemplatesRequest() {
        return new ListTemplatesRequest();
    }

    /**
     * Create an instance of {@link ListTemplatesResponse }
     * 
     */
    public ListTemplatesResponse createListTemplatesResponse() {
        return new ListTemplatesResponse();
    }

    /**
     * Create an instance of {@link ListSubscriberBindingRequest }
     * 
     */
    public ListSubscriberBindingRequest createListSubscriberBindingRequest() {
        return new ListSubscriberBindingRequest();
    }

    /**
     * Create an instance of {@link ListSubscriberBindingResponse }
     * 
     */
    public ListSubscriberBindingResponse createListSubscriberBindingResponse() {
        return new ListSubscriberBindingResponse();
    }

    /**
     * Create an instance of {@link SendMailingRequest }
     * 
     */
    public SendMailingRequest createSendMailingRequest() {
        return new SendMailingRequest();
    }

    /**
     * Create an instance of {@link SendMailingResponse }
     * 
     */
    public SendMailingResponse createSendMailingResponse() {
        return new SendMailingResponse();
    }

    /**
     * Create an instance of {@link SetSubscriberBindingRequest }
     * 
     */
    public SetSubscriberBindingRequest createSetSubscriberBindingRequest() {
        return new SetSubscriberBindingRequest();
    }

    /**
     * Create an instance of {@link SetSubscriberBindingResponse }
     * 
     */
    public SetSubscriberBindingResponse createSetSubscriberBindingResponse() {
        return new SetSubscriberBindingResponse();
    }

    /**
     * Create an instance of {@link UpdateAttachmentRequest }
     * 
     */
    public UpdateAttachmentRequest createUpdateAttachmentRequest() {
        return new UpdateAttachmentRequest();
    }

    /**
     * Create an instance of {@link UpdateAttachmentResponse }
     * 
     */
    public UpdateAttachmentResponse createUpdateAttachmentResponse() {
        return new UpdateAttachmentResponse();
    }

    /**
     * Create an instance of {@link UpdateContentBlockRequest }
     * 
     */
    public UpdateContentBlockRequest createUpdateContentBlockRequest() {
        return new UpdateContentBlockRequest();
    }

    /**
     * Create an instance of {@link UpdateContentBlockResponse }
     * 
     */
    public UpdateContentBlockResponse createUpdateContentBlockResponse() {
        return new UpdateContentBlockResponse();
    }

    /**
     * Create an instance of {@link UpdateMailingRequest.TargetIDList }
     * 
     */
    public UpdateMailingRequest.TargetIDList createUpdateMailingRequestTargetIDList() {
        return new UpdateMailingRequest.TargetIDList();
    }

    /**
     * Create an instance of {@link UpdateMailingResponse }
     * 
     */
    public UpdateMailingResponse createUpdateMailingResponse() {
        return new UpdateMailingResponse();
    }

    /**
     * Create an instance of {@link UpdateMailinglistRequest }
     * 
     */
    public UpdateMailinglistRequest createUpdateMailinglistRequest() {
        return new UpdateMailinglistRequest();
    }

    /**
     * Create an instance of {@link UpdateMailinglistResponse }
     * 
     */
    public UpdateMailinglistResponse createUpdateMailinglistResponse() {
        return new UpdateMailinglistResponse();
    }

    /**
     * Create an instance of {@link UpdateSubscriberRequest }
     * 
     */
    public UpdateSubscriberRequest createUpdateSubscriberRequest() {
        return new UpdateSubscriberRequest();
    }

    /**
     * Create an instance of {@link UpdateSubscriberResponse }
     * 
     */
    public UpdateSubscriberResponse createUpdateSubscriberResponse() {
        return new UpdateSubscriberResponse();
    }

    /**
     * Create an instance of {@link UpdateTemplateRequest.TargetIDList }
     * 
     */
    public UpdateTemplateRequest.TargetIDList createUpdateTemplateRequestTargetIDList() {
        return new UpdateTemplateRequest.TargetIDList();
    }

    /**
     * Create an instance of {@link UpdateTemplateResponse }
     * 
     */
    public UpdateTemplateResponse createUpdateTemplateResponse() {
        return new UpdateTemplateResponse();
    }

    /**
     * Create an instance of {@link DecryptLinkDataRequest }
     * 
     */
    public DecryptLinkDataRequest createDecryptLinkDataRequest() {
        return new DecryptLinkDataRequest();
    }

    /**
     * Create an instance of {@link DecryptLinkDataResponse }
     * 
     */
    public DecryptLinkDataResponse createDecryptLinkDataResponse() {
        return new DecryptLinkDataResponse();
    }

    /**
     * Create an instance of {@link ListTrackableLinksRequest }
     * 
     */
    public ListTrackableLinksRequest createListTrackableLinksRequest() {
        return new ListTrackableLinksRequest();
    }

    /**
     * Create an instance of {@link ListTrackableLinksResponse.TrackableLink }
     * 
     */
    public ListTrackableLinksResponse.TrackableLink createListTrackableLinksResponseTrackableLink() {
        return new ListTrackableLinksResponse.TrackableLink();
    }

    /**
     * Create an instance of {@link GetTrackableLinkSettingsRequest }
     * 
     */
    public GetTrackableLinkSettingsRequest createGetTrackableLinkSettingsRequest() {
        return new GetTrackableLinkSettingsRequest();
    }

    /**
     * Create an instance of {@link UpdateTrackableLinkSettingsResponse }
     * 
     */
    public UpdateTrackableLinkSettingsResponse createUpdateTrackableLinkSettingsResponse() {
        return new UpdateTrackableLinkSettingsResponse();
    }

    /**
     * Create an instance of {@link ListTargetgroupsRequest }
     * 
     */
    public ListTargetgroupsRequest createListTargetgroupsRequest() {
        return new ListTargetgroupsRequest();
    }

    /**
     * Create an instance of {@link ListTargetgroupsResponse.Item }
     * 
     */
    public ListTargetgroupsResponse.Item createListTargetgroupsResponseItem() {
        return new ListTargetgroupsResponse.Item();
    }

    /**
     * Create an instance of {@link AddMailingImageRequest }
     * 
     */
    public AddMailingImageRequest createAddMailingImageRequest() {
        return new AddMailingImageRequest();
    }

    /**
     * Create an instance of {@link AddMailingImageResponse }
     * 
     */
    public AddMailingImageResponse createAddMailingImageResponse() {
        return new AddMailingImageResponse();
    }

    /**
     * Create an instance of {@link GetMailingStatusRequest }
     * 
     */
    public GetMailingStatusRequest createGetMailingStatusRequest() {
        return new GetMailingStatusRequest();
    }

    /**
     * Create an instance of {@link GetMailingStatusResponse }
     * 
     */
    public GetMailingStatusResponse createGetMailingStatusResponse() {
        return new GetMailingStatusResponse();
    }

    /**
     * Create an instance of {@link MapItem }
     * 
     */
    public MapItem createMapItem() {
        return new MapItem();
    }

    /**
     * Create an instance of {@link Equals }
     * 
     */
    public Equals createEquals() {
        return new Equals();
    }

    /**
     * Create an instance of {@link UpdateTrackableLinkSettingsRequest.LinkExtensions.LinkExtension }
     * 
     */
    public UpdateTrackableLinkSettingsRequest.LinkExtensions.LinkExtension createUpdateTrackableLinkSettingsRequestLinkExtensionsLinkExtension() {
        return new UpdateTrackableLinkSettingsRequest.LinkExtensions.LinkExtension();
    }

    /**
     * Create an instance of {@link GetTrackableLinkSettingsResponse.LinkExtensions.LinkExtension }
     * 
     */
    public GetTrackableLinkSettingsResponse.LinkExtensions.LinkExtension createGetTrackableLinkSettingsResponseLinkExtensionsLinkExtension() {
        return new GetTrackableLinkSettingsResponse.LinkExtensions.LinkExtension();
    }

    /**
     * Create an instance of {@link ListMailingsRequest.Filter.SentBefore }
     * 
     */
    public ListMailingsRequest.Filter.SentBefore createListMailingsRequestFilterSentBefore() {
        return new ListMailingsRequest.Filter.SentBefore();
    }

    /**
     * Create an instance of {@link ListMailingsRequest.Filter.SentAfter }
     * 
     */
    public ListMailingsRequest.Filter.SentAfter createListMailingsRequestFilterSentAfter() {
        return new ListMailingsRequest.Filter.SentAfter();
    }

    /**
     * Create an instance of {@link Template.TargetIDList }
     * 
     */
    public Template.TargetIDList createTemplateTargetIDList() {
        return new Template.TargetIDList();
    }

    /**
     * Create an instance of {@link Template.Formats }
     * 
     */
    public Template.Formats createTemplateFormats() {
        return new Template.Formats();
    }

    /**
     * Create an instance of {@link Mailing.TargetIDList }
     * 
     */
    public Mailing.TargetIDList createMailingTargetIDList() {
        return new Mailing.TargetIDList();
    }

    /**
     * Create an instance of {@link Mailing.Formats }
     * 
     */
    public Mailing.Formats createMailingFormats() {
        return new Mailing.Formats();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://agnitas.org/ws/schemas", name = "Error")
    public JAXBElement<String> createError(String value) {
        return new JAXBElement<String>(_Error_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Attachment }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Attachment }{@code >}
     */
    @XmlElementDecl(namespace = "http://agnitas.org/ws/schemas", name = "GetAttachmentResponse")
    public JAXBElement<Attachment> createGetAttachmentResponse(Attachment value) {
        return new JAXBElement<Attachment>(_GetAttachmentResponse_QNAME, Attachment.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AttachmentDateTimeDefault }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AttachmentDateTimeDefault }{@code >}
     */
    @XmlElementDecl(namespace = "http://agnitas.org/ws/schemas", name = "AttachmentDateTimeDefault", substitutionHeadNamespace = "http://agnitas.org/ws/schemas", substitutionHeadName = "GetAttachmentResponse")
    public JAXBElement<AttachmentDateTimeDefault> createAttachmentDateTimeDefault(AttachmentDateTimeDefault value) {
        return new JAXBElement<AttachmentDateTimeDefault>(_AttachmentDateTimeDefault_QNAME, AttachmentDateTimeDefault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AttachmentDateTimeISO }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AttachmentDateTimeISO }{@code >}
     */
    @XmlElementDecl(namespace = "http://agnitas.org/ws/schemas", name = "AttachmentDateTimeISO", substitutionHeadNamespace = "http://agnitas.org/ws/schemas", substitutionHeadName = "GetAttachmentResponse")
    public JAXBElement<AttachmentDateTimeISO> createAttachmentDateTimeISO(AttachmentDateTimeISO value) {
        return new JAXBElement<AttachmentDateTimeISO>(_AttachmentDateTimeISO_QNAME, AttachmentDateTimeISO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Mailing }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Mailing }{@code >}
     */
    @XmlElementDecl(namespace = "http://agnitas.org/ws/schemas", name = "GetMailingResponse")
    public JAXBElement<Mailing> createGetMailingResponse(Mailing value) {
        return new JAXBElement<Mailing>(_GetMailingResponse_QNAME, Mailing.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Mailinglist }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Mailinglist }{@code >}
     */
    @XmlElementDecl(namespace = "http://agnitas.org/ws/schemas", name = "GetMailinglistResponse")
    public JAXBElement<Mailinglist> createGetMailinglistResponse(Mailinglist value) {
        return new JAXBElement<Mailinglist>(_GetMailinglistResponse_QNAME, Mailinglist.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Template }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Template }{@code >}
     */
    @XmlElementDecl(namespace = "http://agnitas.org/ws/schemas", name = "GetTemplateResponse")
    public JAXBElement<Template> createGetTemplateResponse(Template value) {
        return new JAXBElement<Template>(_GetTemplateResponse_QNAME, Template.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Binding }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Binding }{@code >}
     */
    @XmlElementDecl(namespace = "http://agnitas.org/ws/schemas", name = "GetSubscriberBindingResponse")
    public JAXBElement<Binding> createGetSubscriberBindingResponse(Binding value) {
        return new JAXBElement<Binding>(_GetSubscriberBindingResponse_QNAME, Binding.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BindingDateTimeDefault }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BindingDateTimeDefault }{@code >}
     */
    @XmlElementDecl(namespace = "http://agnitas.org/ws/schemas", name = "BindingDateTimeDefault", substitutionHeadNamespace = "http://agnitas.org/ws/schemas", substitutionHeadName = "GetSubscriberBindingResponse")
    public JAXBElement<BindingDateTimeDefault> createBindingDateTimeDefault(BindingDateTimeDefault value) {
        return new JAXBElement<BindingDateTimeDefault>(_BindingDateTimeDefault_QNAME, BindingDateTimeDefault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BindingDateTimeISO }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BindingDateTimeISO }{@code >}
     */
    @XmlElementDecl(namespace = "http://agnitas.org/ws/schemas", name = "BindingDateTimeISO", substitutionHeadNamespace = "http://agnitas.org/ws/schemas", substitutionHeadName = "GetSubscriberBindingResponse")
    public JAXBElement<BindingDateTimeISO> createBindingDateTimeISO(BindingDateTimeISO value) {
        return new JAXBElement<BindingDateTimeISO>(_BindingDateTimeISO_QNAME, BindingDateTimeISO.class, null, value);
    }

}
