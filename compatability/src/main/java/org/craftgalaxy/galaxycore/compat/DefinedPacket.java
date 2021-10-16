package org.craftgalaxy.galaxycore.compat;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.craftgalaxy.galaxycore.compat.exception.BadPacketException;
import org.craftgalaxy.galaxycore.compat.exception.OverflowPacketException;

import java.util.*;

public abstract class DefinedPacket {

    private static final BadPacketException OVERSIZED_VAR_INT_EXCEPTION = new BadPacketException("VarInt too big!");
    private static final BadPacketException NO_MORE_BYTES_EXCEPTION = new BadPacketException("No more bytes reading VarInt");

    public static void writeString(ByteBuf output, String s) {
        DefinedPacket.writeString(output, s, Short.MAX_VALUE);
    }

    public static void writeString(ByteBuf output, String s, int maxLength) {
        if (s.length() > maxLength) {
            throw new OverflowPacketException(String.format("Cannot send String longer than %s (got %s characters)", maxLength, s.length()));
        }

        byte[] bytes = s.getBytes(CharsetUtil.UTF_8);
        DefinedPacket.writeVarInt(output, bytes.length);
        output.writeBytes(bytes);
    }

    public static String readString(ByteBuf input) {
        return DefinedPacket.readString(input, Short.MAX_VALUE);
    }

    public static String readString(ByteBuf input, int maxLength) {
        int length = DefinedPacket.readVarInt(input);
        if (length > maxLength * 4) {
            throw new OverflowPacketException(String.format("Cannot read String longer than %s (got %s bytes)", maxLength * 4, length));
        }

        byte[] bytes = new byte[length];
        input.readBytes(bytes);
        String result = new String(bytes, CharsetUtil.UTF_8);
        if (result.length() > maxLength) {
            throw new OverflowPacketException(String.format("Cannot read String longer than %s (got %s characters)", maxLength, result.length()));
        }

        return result;
    }

    public static void writeVarInt(ByteBuf output, int value) {
        do {
            int part = value & 127;
            value >>>= 7;
            if (value != 0) {
                part |= 128;
            }

            output.writeByte(part);
        } while(value != 0);
    }

    public static int readVarInt(ByteBuf input) {
        return DefinedPacket.readVarInt(input, 5);
    }

    public static int readVarInt(ByteBuf input, int maxBytes) {
        int out = 0;
        int bytes = 0;

        while(input.readableBytes() != 0) {
            byte in = input.readByte();
            out |= (in & 127) << bytes++ * 7;
            if (bytes > maxBytes) {
                throw OVERSIZED_VAR_INT_EXCEPTION;
            }

            if ((in & 128) != 128) {
                return out;
            }
        }

        throw NO_MORE_BYTES_EXCEPTION;
    }

    public static List<Integer> readVarIntList(ByteBuf input) {
        int length = DefinedPacket.readVarInt(input);
        List<Integer> list = new ArrayList<>(length);
        for(int i = 0; i < length; ++i) {
            list.add(DefinedPacket.readVarInt(input));
        }

        return list;
    }

    public static int[] readVarIntArray(ByteBuf input) {
        int length = DefinedPacket.readVarInt(input);
        int[] array = new int[length];
        for(int i = 0; i < length; ++i) {
            array[i] = DefinedPacket.readVarInt(input);
        }

        return array;
    }

    public static void writeVarIntArray(ByteBuf output, Collection<Integer> array) {
        DefinedPacket.writeVarInt(output, array.size());
        for (int i : array) {
            DefinedPacket.writeVarInt(output, i);
        }
    }

    public static void writeVarIntArray(ByteBuf output, int[] array) {
        DefinedPacket.writeVarInt(output, array.length);
        for (int i : array) {
            DefinedPacket.writeVarInt(output, i);
        }
    }

    public static void writeStringArray(ByteBuf output, Collection<String> array) {
        DefinedPacket.writeVarInt(output, array.size());
        for (String s : array) {
            DefinedPacket.writeString(output, s);
        }
    }

    public static List<String> readStringArray(ByteBuf input) {
        int length = DefinedPacket.readVarInt(input);
        List<String> list = new ArrayList<>(length);
        for(int i = 0; i < length; ++i) {
            list.add(DefinedPacket.readString(input));
        }

        return list;
    }

    public static void writeUUID(ByteBuf output, UUID value) {
        output.writeLong(value.getMostSignificantBits());
        output.writeLong(value.getLeastSignificantBits());
    }

    public static UUID readUUID(ByteBuf input) {
        return new UUID(input.readLong(), input.readLong());
    }

    public void read(ByteBuf buf) {
        throw new UnsupportedOperationException("Packet does not implement read method");
    }

    public void read(ByteBuf buf, Protocol.Direction direction) {
        this.read(buf);
    }

    public void write(ByteBuf buf) {
        throw new UnsupportedOperationException("Packet does not implement write method");
    }

    public void write(ByteBuf buf, Protocol.Direction direction) {
        this.write(buf);
    }

    public abstract void handle(AbstractPacketHandler handler) throws Exception;
}
