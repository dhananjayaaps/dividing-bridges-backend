package org.penpal.auth.tokens;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class TempPasswordGenerator {
    public String generateTempPwd() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "").substring(0, 9);
    }
}
