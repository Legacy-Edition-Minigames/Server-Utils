package net.kyrptonaught.serverutils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class VelocityPacket {
    public static class ByteBufDataOutput extends OutputStream implements ByteArrayDataOutput {

        private final ByteBuf buf;
        private final DataOutputStream utf8out;

        public ByteBufDataOutput(ByteBuf buf) {
            this.buf = buf;
            this.utf8out = new DataOutputStream(this);
        }

        public ByteBuf getBuf() {
            return buf;
        }

        @Override
        public byte[] toByteArray() {
            return ByteBufUtil.getBytes(buf);
        }

        @Override
        public void write(int b) {
            buf.writeByte(b);
        }

        @Override
        public void write(byte[] b) {
            buf.writeBytes(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            buf.writeBytes(b, off, len);
        }

        @Override
        public void writeBoolean(boolean v) {
            buf.writeBoolean(v);
        }

        @Override
        public void writeByte(int v) {
            buf.writeByte(v);
        }

        @Override
        public void writeShort(int v) {
            buf.writeShort(v);
        }

        @Override
        public void writeChar(int v) {
            buf.writeChar(v);
        }

        @Override
        public void writeInt(int v) {
            buf.writeInt(v);
        }

        @Override
        public void writeLong(long v) {
            buf.writeLong(v);
        }

        @Override
        public void writeFloat(float v) {
            buf.writeFloat(v);
        }

        @Override
        public void writeDouble(double v) {
            buf.writeDouble(v);
        }

        @Override
        public void writeBytes(String s) {
            buf.writeCharSequence(s, StandardCharsets.US_ASCII);
        }

        @Override
        public void writeChars(String s) {
            for (char c : s.toCharArray()) {
                buf.writeChar(c);
            }
        }

        @Override
        public void writeUTF(String s) {
            try {
                this.utf8out.writeUTF(s);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void close() {
        }
    }

    public static class ByteBufDataInput implements ByteArrayDataInput {

        private final ByteBuf in;

        public ByteBufDataInput(ByteBuf buf) {
            this.in = buf;
        }

        public ByteBuf unwrap() {
            return in;
        }

        @Override
        public void readFully(byte[] b) {
            in.readBytes(b);
        }

        @Override
        public void readFully(byte[] b, int off, int len) {
            in.readBytes(b, off, len);
        }

        @Override
        public int skipBytes(int n) {
            in.skipBytes(n);
            return n;
        }

        @Override
        public boolean readBoolean() {
            return in.readBoolean();
        }

        @Override
        public byte readByte() {
            return in.readByte();
        }

        @Override
        public int readUnsignedByte() {
            return in.readUnsignedByte() & 0xFF;
        }

        @Override
        public short readShort() {
            return in.readShort();
        }

        @Override
        public int readUnsignedShort() {
            return in.readUnsignedShort();
        }

        @Override
        public char readChar() {
            return in.readChar();
        }

        @Override
        public int readInt() {
            return in.readInt();
        }

        @Override
        public long readLong() {
            return in.readLong();
        }

        @Override
        public float readFloat() {
            return in.readFloat();
        }

        @Override
        public double readDouble() {
            return in.readDouble();
        }

        @Override
        public String readLine() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String readUTF() {
            try {
                return DataInputStream.readUTF(this);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
