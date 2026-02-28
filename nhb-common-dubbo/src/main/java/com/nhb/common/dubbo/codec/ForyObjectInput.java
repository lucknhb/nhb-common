package com.nhb.common.dubbo.codec;

import org.apache.dubbo.common.serialize.ObjectInput;
import org.apache.fory.Fory;
import org.apache.fory.io.BlockedStreamUtils;
import org.apache.fory.memory.MemoryBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

@SuppressWarnings("unchecked")
public class ForyObjectInput implements ObjectInput {
    private final Fory fory;
    private final MemoryBuffer buffer;
    private final InputStream input;

    public ForyObjectInput(Fory fory, MemoryBuffer buffer, InputStream input) {
        this.fory = fory;
        this.buffer = buffer;
        this.input = input;
    }

    @Override
    public Object readObject() {
        return BlockedStreamUtils.deserialize(fory, input);
    }

    @Override
    public <T> T readObject(Class<T> cls) {
        return (T) readObject();
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) {
        return (T) readObject();
    }

    @Override
    public boolean readBool() throws IOException {
        readBytes(buffer.getHeapMemory(), 1);
        return buffer.getBoolean(0);
    }

    @Override
    public byte readByte() throws IOException {
        readBytes(buffer.getHeapMemory(), 1);
        return buffer.getByte(0);
    }

    @Override
    public short readShort() throws IOException {
        readBytes(buffer.getHeapMemory(), 2);
        return buffer.getInt16(0);
    }

    @Override
    public int readInt() throws IOException {
        readBytes(buffer.getHeapMemory(), 4);
        return buffer.getInt32(0);
    }

    @Override
    public long readLong() throws IOException {
        readBytes(buffer.getHeapMemory(), 8);
        return buffer.getInt64(0);
    }

    @Override
    public float readFloat() throws IOException {
        readBytes(buffer.getHeapMemory(), 4);
        return buffer.getFloat32(0);
    }

    @Override
    public double readDouble() throws IOException {
        readBytes(buffer.getHeapMemory(), 8);
        return buffer.getFloat64(0);
    }

    @Override
    public String readUTF() throws IOException {
        int size = readInt();
        buffer.readerIndex(0);
        buffer.ensure(size);
        readBytes(buffer.getHeapMemory(), size);
        if (buffer.readBoolean()) {
            return fory.readJavaString(buffer);
        } else {
            return null;
        }
    }

    @Override
    public byte[] readBytes() throws IOException {
        int size = readInt();
        byte[] bytes = new byte[size];
        readBytes(bytes, size);
        return bytes;
    }

    private void readBytes(byte[] bytes, int size) throws IOException {
        int off = 0;
        while (off != size) {
            off += input.read(bytes, off, size - off);
        }
    }
}