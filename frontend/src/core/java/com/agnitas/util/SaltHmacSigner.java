/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SaltHmacSigner {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final HexFormat HEX = HexFormat.of();

    private final DataEncryptor dataEncryptor;
    private volatile byte[] key;

    public SaltHmacSigner(DataEncryptor dataEncryptor) {
        this.dataEncryptor = dataEncryptor;
    }

    public String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(getKey(), HMAC_ALGORITHM));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(raw);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }

    public boolean verify(String payload, String token) {
        if (token == null || payload == null) {
            return false;
        }
        return constantTimeEquals(sign(payload), token);
    }

    private byte[] getKey() {
        byte[] result = key;
        if (result == null) {
            synchronized (this) {
                result = key;
                if (result == null) {
                    result = dataEncryptor.getHmacSigningKey();
                    key = result;
                }
            }
        }
        return result;
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
