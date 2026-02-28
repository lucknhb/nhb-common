package com.nhb.common.dubbo.codec;

import org.apache.dubbo.common.serialize.ObjectOutput;
import org.apache.fory.Fory;
import org.apache.fory.io.BlockedStreamUtils;
import org.apache.fory.memory.MemoryBuffer;

import java.io.IOException;
import java.io.OutputStream;

public class ForyObjectOutput implements ObjectOutput {
    private final Fory fory;
    private final MemoryBuffer buffer;
    private final OutputStream output;

    public ForyObjectOutput(Fory fory, MemoryBuffer buffer, OutputStream output) {
        this.fory = fory;
        this.buffer = buffer;
        this.output = output;
    }

    public void writeObject(Object obj) {
        BlockedStreamUtils.serialize(fory, output, obj);
    }

    public void writeBool(boolean v) throws IOException {
        buffer.putBoolean(0, v);
        output.write(buffer.getHeapMemory(), 0, 1);
    }

    public void writeByte(byte v) throws IOException {
        buffer.putByte(0, v);
        output.write(buffer.getHeapMemory(), 0, 1);
    }

    public void writeShort(short v) throws IOException {
        buffer.putInt16(0, v);
        output.write(buffer.getHeapMemory(), 0, 2);
    }

    public void writeInt(int v) throws IOException {
        buffer.putInt32(0, v);
        output.write(buffer.getHeapMemory(), 0, 4);
    }

    public void writeLong(long v) throws IOException {
        buffer.putInt64(0, v);
        output.write(buffer.getHeapMemory(), 0, 8);
    }

    public void writeFloat(float v) throws IOException {
        buffer.putFloat32(0, v);
        output.write(buffer.getHeapMemory(), 0, 4);
    }

    public void writeDouble(double v) throws IOException {
        buffer.putFloat64(0, v);
        output.write(buffer.getHeapMemory(), 0, 8);
    }

    public void writeUTF(String v) throws IOException {
        // avoid `writeInt` overwrite sting data.
        buffer.writerIndex(4);
        if (v != null) {
            buffer.writeBoolean(true);
            fory.writeJavaString(buffer, v);
        } else {
            buffer.writeBoolean(false);
        }
        int size = buffer.writerIndex() - 4;
        writeInt(size);
        output.write(buffer.getHeapMemory(), 4, size);
    }

    public void writeBytes(byte[] v) throws IOException {
        writeInt(v.length);
        output.write(v);
    }

    public void writeBytes(byte[] v, int off, int len) throws IOException {
        writeInt(len);
        output.write(v, off, len);
    }

    public void flushBuffer() throws IOException {
        output.flush();
    }
}