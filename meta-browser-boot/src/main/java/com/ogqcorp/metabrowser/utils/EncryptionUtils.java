package com.ogqcorp.metabrowser.utils;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import org.apache.commons.codec.binary.Base64;

import java.nio.ByteBuffer;
import java.util.UUID;

public class EncryptionUtils {
    public static String generateUUID(){
        TimeBasedGenerator uuidV1Generator = Generators.timeBasedGenerator(EthernetAddress.fromInterface());
        UUID uuid = uuidV1Generator.generate();

        uuid = Generators.randomBasedGenerator().generate();

        ByteBuffer uuidBytes = ByteBuffer.wrap(new byte[16]);
        uuidBytes.putLong(uuid.getMostSignificantBits());
        uuidBytes.putLong(uuid.getLeastSignificantBits());
        return Base64.encodeBase64URLSafeString(uuidBytes.array());
    }

    public static String generateUrl62UUIDv4() {

        UUID uuid = Generators.randomBasedGenerator().generate();

        ByteBuffer uuidBytes = ByteBuffer.wrap(new byte[16]);
        uuidBytes.putLong(uuid.getMostSignificantBits());
        uuidBytes.putLong(uuid.getLeastSignificantBits());

        return Base62.encode(uuidBytes.array());
    }
}
