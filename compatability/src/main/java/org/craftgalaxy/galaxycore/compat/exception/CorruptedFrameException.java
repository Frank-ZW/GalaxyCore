package org.craftgalaxy.galaxycore.compat.exception;

import io.netty.handler.codec.DecoderException;

public class CorruptedFrameException extends DecoderException {

    public CorruptedFrameException(String message) {
        super(message);
    }
}
