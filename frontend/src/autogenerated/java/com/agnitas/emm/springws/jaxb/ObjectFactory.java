/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.springws.jaxb;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.agnitas.emm.springws.jaxb package. 
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

    private final static QName _Error_QNAME = new QName("http://agnitas.com/ws/schemas", "Error");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.agnitas.emm.springws.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetMailingContentResponse }
     * 
     */
    public GetMailingContentResponse createGetMailingContentResponse() {
        return new GetMailingContentResponse();
    }

    /**
     * Create an instance of {@link GroupStatisticInfo }
     * 
     */
    public GroupStatisticInfo createGroupStatisticInfo() {
        return new GroupStatisticInfo();
    }

    /**
     * Create an instance of {@link SetSubscriberBindingWithActionRequest }
     * 
     */
    public SetSubscriberBindingWithActionRequest createSetSubscriberBindingWithActionRequest() {
        return new SetSubscriberBindingWithActionRequest();
    }

    /**
     * Create an instance of {@link SetSubscriberBindingWithActionResponse }
     * 
     */
    public SetSubscriberBindingWithActionResponse createSetSubscriberBindingWithActionResponse() {
        return new SetSubscriberBindingWithActionResponse();
    }

    /**
     * Create an instance of {@link GetMailingContentRequest }
     * 
     */
    public GetMailingContentRequest createGetMailingContentRequest() {
        return new GetMailingContentRequest();
    }

    /**
     * Create an instance of {@link GetMailingContentResponse.Items }
     * 
     */
    public GetMailingContentResponse.Items createGetMailingContentResponseItems() {
        return new GetMailingContentResponse.Items();
    }

    /**
     * Create an instance of {@link UpdateMailingContentRequest }
     * 
     */
    public UpdateMailingContentRequest createUpdateMailingContentRequest() {
        return new UpdateMailingContentRequest();
    }

    /**
     * Create an instance of {@link UpdateMailingContentResponse }
     * 
     */
    public UpdateMailingContentResponse createUpdateMailingContentResponse() {
        return new UpdateMailingContentResponse();
    }

    /**
     * Create an instance of {@link SendServiceMailRequest }
     * 
     */
    public SendServiceMailRequest createSendServiceMailRequest() {
        return new SendServiceMailRequest();
    }

    /**
     * Create an instance of {@link SendServiceMailResponse }
     * 
     */
    public SendServiceMailResponse createSendServiceMailResponse() {
        return new SendServiceMailResponse();
    }

    /**
     * Create an instance of {@link UpdateTargetGroupRequest }
     * 
     */
    public UpdateTargetGroupRequest createUpdateTargetGroupRequest() {
        return new UpdateTargetGroupRequest();
    }

    /**
     * Create an instance of {@link UpdateTargetGroupResponse }
     * 
     */
    public UpdateTargetGroupResponse createUpdateTargetGroupResponse() {
        return new UpdateTargetGroupResponse();
    }

    /**
     * Create an instance of {@link CopyMailingRequest }
     * 
     */
    public CopyMailingRequest createCopyMailingRequest() {
        return new CopyMailingRequest();
    }

    /**
     * Create an instance of {@link CopyMailingResponse }
     * 
     */
    public CopyMailingResponse createCopyMailingResponse() {
        return new CopyMailingResponse();
    }

    /**
     * Create an instance of {@link AddTargetGroupRequest }
     * 
     */
    public AddTargetGroupRequest createAddTargetGroupRequest() {
        return new AddTargetGroupRequest();
    }

    /**
     * Create an instance of {@link AddTargetGroupResponse }
     * 
     */
    public AddTargetGroupResponse createAddTargetGroupResponse() {
        return new AddTargetGroupResponse();
    }

    /**
     * Create an instance of {@link MapItem }
     * 
     */
    public MapItem createMapItem() {
        return new MapItem();
    }

    /**
     * Create an instance of {@link Map }
     * 
     */
    public Map createMap() {
        return new Map();
    }

    /**
     * Create an instance of {@link StatisticValue }
     * 
     */
    public StatisticValue createStatisticValue() {
        return new StatisticValue();
    }

    /**
     * Create an instance of {@link StatisticEntry }
     * 
     */
    public StatisticEntry createStatisticEntry() {
        return new StatisticEntry();
    }

    /**
     * Create an instance of {@link MailingContent }
     * 
     */
    public MailingContent createMailingContent() {
        return new MailingContent();
    }

    /**
     * Create an instance of {@link GroupStatisticInfo.Items }
     * 
     */
    public GroupStatisticInfo.Items createGroupStatisticInfoItems() {
        return new GroupStatisticInfo.Items();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://agnitas.com/ws/schemas", name = "Error")
    public JAXBElement<String> createError(String value) {
        return new JAXBElement<String>(_Error_QNAME, String.class, null, value);
    }

}
