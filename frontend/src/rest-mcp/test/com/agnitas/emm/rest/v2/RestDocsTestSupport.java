/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static com.epages.restdocs.apispec.SimpleType.INTEGER;
import static com.epages.restdocs.apispec.SimpleType.NUMBER;
import static com.epages.restdocs.apispec.SimpleType.STRING;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class RestDocsTestSupport extends RestTestSupport {

    protected static final String PATCH_DEFAULT_DESCRIPTION =
            """
            ℹ️ <strong>Partial update</strong>. Only the fields included in the request are updated. \
            Fields omitted from the request remain unchanged \
            and are <strong>not</strong> reset to empty values or their default values.
            """;

    protected static final Schema BULK_DELETE_REQUEST_SCHEMA = new Schema("BulkDeleteRequest");
    protected static final Schema BULK_DELETE_RESPONSE_SCHEMA = new Schema("BulkDeleteRequest");

    private static final FieldDescriptor[] PAGINATION_FIELDS = {
            requiredField("page.number", "Current page number").type(NUMBER),
            requiredField("page.size", "Size of the page").type(NUMBER),
            requiredField("page.totalCount", "Total number of records").type(NUMBER),
            requiredField("list", "An array of found items").type(ARRAY),
    };

    private static final FieldDescriptor[] ERROR_FIELDS = {
            optionalField("field", "Field that caused the error").type(STRING),
            requiredField("message", "Error message details").type(STRING),
    };

    private static final FieldDescriptor[] BULK_DELETE_RESULT_ENTRY = fields(
            requiredField("id", "Recipient ID").type(NUMBER),
            requiredField("status", "Status of the operation").type(STRING),
            Stream.of(applyPrefix("errors[]", ERROR_FIELDS))
                    .map(FieldDescriptor::optional)
                    .toArray(FieldDescriptor[]::new)
    );

    protected static final FieldDescriptor[] BULK_DELETE_RESULT_FIELDS = fields(
            requiredField("total", "Total number of IDs processed"),
            requiredField("succeeded", "Number of successful deletions"),
            requiredField("failed", "Number of failed deletions"),
            optionalField("results", "Detailed results for each ID"),
            applyPrefix("results[]", BULK_DELETE_RESULT_ENTRY)
    );

    protected FieldDescriptor[] paginationFields(FieldDescriptor[] itemFields) {
        return fields(PAGINATION_FIELDS, applyPrefix("list[]", itemFields));
    }

    protected Schema schema(String name) {
        return new Schema(StringUtils.deleteWhitespace(getDocsTag() + name));
    }

    protected ResultHandler document(String id, ResourceSnippetParametersBuilder snippetBuilder) {
        return document(id, snippetBuilder, false);
    }

    protected ResultHandler documentExtendedScope(String id, ResourceSnippetParametersBuilder snippetBuilder) {
        return document(id, snippetBuilder, true);
    }

    protected ResultHandler document(
            String id,
            ResourceSnippetParametersBuilder snippetBuilder,
            Snippet... additionalSnippets
    ) {
        return document(id, snippetBuilder, false, additionalSnippets);
    }

    private ResultHandler document(
            String id,
            ResourceSnippetParametersBuilder snippetBuilder,
            boolean extended,
            Snippet... additionalSnippets
    ) {
        if (extended) {
            snippetBuilder.summary(snippetBuilder.getSummary() + " [EMM_ONLY]").tags();
        }
        List<Snippet> allSnippets = new ArrayList<>();
        allSnippets.add(resource(snippetBuilder.tag(getDocsTag()).build()));

        if (additionalSnippets != null) {
            allSnippets.addAll(Arrays.asList(additionalSnippets));
        }

        return MockMvcRestDocumentationWrapper.document(
                getDocsIdentifier(id),
                allSnippets.toArray(new Snippet[0])
        );
    }

    protected final ParameterDescriptorWithType getIdParam() {
        return intParam("id", getDocsTag() + " ID");
    }

    protected ResourceSnippetParametersBuilder builderWithIdParam() {
        return builder().pathParameters(getIdParam());
    }

    protected abstract String getDocsIdentifierPrefix();

    protected abstract String getDocsTag();

    protected String getDocsTagLc() {
        return getDocsTag().toLowerCase();
    }

    private String getDocsIdentifier(String id) {
        return getDocsIdentifierPrefix() + "-" + id;
    }

    public static FieldDescriptor[] applyPrefix(String prefix, FieldDescriptor... descriptors) {
        return java.util.Arrays.stream(descriptors)
                .map(d -> {
                    FieldDescriptor res = fieldWithPath(prefix + "." + d.getPath())
                            .type(d.getType())
                            .description(d.getDescription());
                    if (d.isOptional()) {
                        res.optional();
                    }
                    if (d.isIgnored()) {
                        res.ignored();
                    }
                    d.getAttributes().forEach((k, v) -> res.attributes(key(k).value(v)));
                    return res;
                })
                .toArray(FieldDescriptor[]::new);
    }

    protected ResultHandler documentPagination(
            FieldDescriptor[] singleItemResponseFields,
            ParameterDescriptorWithType... additionalQueryParameters

    ) {
        return document("list", pagination(singleItemResponseFields, additionalQueryParameters));
    }

    protected ResultHandler documentPagination(
            String description,
            FieldDescriptor[] singleItemResponseFields,
            ParameterDescriptorWithType... additionalQueryParameters

    ) {
        return document(
                "list",
                pagination(singleItemResponseFields, additionalQueryParameters)
                        .description(description)
        );
    }

    protected ResourceSnippetParametersBuilder pagination(
            FieldDescriptor[] singleItemResponseFields,
            ParameterDescriptorWithType... additionalQueryParameters
    ) {
        ParameterDescriptorWithType[] pageParams = {
                optionalParam("page", "Page number to retrieve"),
                optionalParam("size", "Number of items per page")
        };
        Map<String, ParameterDescriptorWithType> merged = new LinkedHashMap<>();
        Stream.concat(Stream.of(pageParams), Stream.of(additionalQueryParameters))
                .forEach(p -> merged.put(p.getName(), p));

        ParameterDescriptorWithType[] allParams = merged.values()
                .toArray(new ParameterDescriptorWithType[0]);

        return builder()
                .summary("Get paginated list of the " + getDocsIdentifierPrefix())
                .queryParameters(allParams)
                .responseSchema(schema("PaginatedListResponse"))
                .responseFields(paginationFields(singleItemResponseFields));
    }

    protected ResultHandler documentPatch(ResourceSnippetParametersBuilder builder) {
        return documentPatch("patch", builder);
    }

    protected ResultHandler documentPatch(String id, ResourceSnippetParametersBuilder builder) {
        return document(id, builder.description(PATCH_DEFAULT_DESCRIPTION));
    }

    protected static FieldDescriptor optionalField(String name, String description) {
        return fieldWithPath(name).description(description).optional();
    }

    protected static FieldDescriptor requiredField(String name, String description) {
        return fieldWithPath(name).description(description);
    }

    protected static FieldDescriptor requiredField(ConstrainedFields constraints, String name, String description) {
        return constraints.withPath(name).description(description);
    }

    protected static FieldDescriptor optionalField(ConstrainedFields constraints, String name, String description) {
        return constraints.withPath(name).description(description).optional();
    }

    protected static ParameterDescriptorWithType formParam(String name, String description) {
        return optionalParam(name, description).type(SimpleType.STRING);
    }

    protected static ParameterDescriptorWithType optionalParam(String name, String description) {
        return param(name, description).optional();
    }

    protected static ParameterDescriptorWithType intParam(String name, String description) {
        return param(name, description).type(INTEGER);
    }

    protected static ParameterDescriptorWithType stringParam(String name, String description) {
        return param(name, description).type(SimpleType.STRING);
    }

    protected static ParameterDescriptorWithType param(String name, String description) {
        return parameterWithName(name).description(description);
    }

    protected static ParameterDescriptorWithType fileParam(String name, String description) {
        return formParam(name, description).attributes(key("format").value("binary"));
    }

    protected static ParameterDescriptorWithType required(ParameterDescriptorWithType param) {
        return param.attributes(key("multipartRequired").value(true));
    }

    protected static ParameterDescriptorWithType notBlank(ParameterDescriptorWithType param) {
        return param.attributes(key("notBlank").value(true));
    }

    protected static ParameterDescriptorWithType positive(ParameterDescriptorWithType param) {
        return param.attributes(key("positive").value(true));
    }

    public static FieldDescriptor[] fields(Object... parts) {
        return Arrays.stream(parts)
                .flatMap(part -> {
                    if (part instanceof FieldDescriptor fd) {
                        return Stream.of(fd);
                    }
                    if (part instanceof FieldDescriptor[] arr) {
                        return Arrays.stream(arr);
                    }
                    throw new IllegalArgumentException(
                            "Expected FieldDescriptor or FieldDescriptor[], got: "
                                    + (part == null ? "null" : part.getClass().getName()));
                })
                .toArray(FieldDescriptor[]::new);
    }
}
