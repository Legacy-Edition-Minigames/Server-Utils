package net.kyrptonaught.serverutils.velocityserverswitch;

import net.minecraft.network.PacketByteBuf;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ByteBufDataOutput extends OutputStream {
    private final PacketByteBuf packetByteBuf;
    private final DataOutputStream dataOutputStream;

    public ByteBufDataOutput(PacketByteBuf buf) {
        this.packetByteBuf = buf;
        this.dataOutputStream = new DataOutputStream(this);
    }

    public PacketByteBuf getBuf() {
        return packetByteBuf;
    }

    @Override
    public void write(int b) {
        packetByteBuf.writeByte(b);
    }

    public void writeUTF(String s) {
        try {
            this.dataOutputStream.writeUTF(s);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
    }
}