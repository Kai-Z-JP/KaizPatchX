package jp.ngt.ngtlib.io;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class ByteCodeObject extends SimpleJavaFileObject {
    protected final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private Class<?> clazz = null;

    public ByteCodeObject(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return this.bos;
    }

    public byte[] getBytes() {
        return this.bos.toByteArray();
    }

    public void setDefinedClass(Class<?> c) {
        this.clazz = c;
    }

    public Class<?> getDefinedClass() {
        return this.clazz;
    }
}