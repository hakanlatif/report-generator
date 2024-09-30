package com.example.reportgenerator.helper;

import java.nio.ByteBuffer;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdGenerator {

    public static String generateId() {
        UUID uuid = UUID.randomUUID();
        long uuidLong = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
        return Long.toString(uuidLong, Character.MAX_RADIX);
    }

}
