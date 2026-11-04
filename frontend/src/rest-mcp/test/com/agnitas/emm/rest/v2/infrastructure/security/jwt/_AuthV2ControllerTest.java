/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.security.jwt;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import com.agnitas.emm.rest.v2.infrastructure.security.jwt.dto.JwtResponse;
import com.agnitas.emm.rest.v2.infrastructure.security.jwt.service.JwtRestService;
import com.agnitas.testsupport.annotation.MockBean;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

class _AuthV2ControllerTest extends _AuthV2ControllerTestSupport {

    @MockBean
    private JwtRestService jwtRestServiceMock;

    @Test
    void shouldReturn200_onGeneratedJwtToken() throws Exception {
        JwtResponse jwt = new JwtResponse("test.jwt", Instant.parse("2026-06-15T19:56:04Z"));
        expect(jwtRestServiceMock.generateToken(eq(adminMock), anyObject(HttpServletRequest.class))).andReturn(jwt);

        authenticated(post("/auth/jwt")).andExpectAll(
                status().isOk(),
                content().json("""
                        {
                            "jwt": "test.jwt",
                            "validity": "2026-06-15T19:56:04Z"
                        }
                        """
                )
        ).andDo(documentCreateJwt());
    }

    @Test
    void shouldReturn400_whenAlreadyAuthenticated() throws Exception {
        authenticated(post("/auth/jwt").header("Authorization", "Bearer test.jwt")).andExpectAll(
                status().isBadRequest(),
                content().json("""
                    {
                      "detail": "Already authenticated. Use basic auth to create new JWT token."
                    }
                """
            )
        );
    }
}
