package de.theholyexception.holyapi.util;

import java.io.*;

public class DataUtils {

    public static byte[] readAllBytes(InputStream is) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(8192)) {
            byte[] buf = new byte[8192];

            int l;
            while ((l = is.read(buf)) != -1) {
                baos.write(buf, 0, l);
            }

            return baos.toByteArray();
        }
    }

    public static byte[] loadBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            long l = file.length();
            if ((l & -2147483648L) != 0L) {
                throw new IOException("The file is too big! loadBytes() can only handle files up to 2GB");
            } else {
                byte[] data = new byte[(int)l];
                int off = readToArray(fis, data);
                if (off != data.length) {
                    throw new IOException("File read was incomplete, the stream was closed too early");
                } else {
                    return data;
                }
            }
        }
    }

    public static int readToArray(InputStream is, byte[] out) throws IOException {
        int off = 0;
        int len;
        while(out.length - off > 0 && (len = is.read(out, off, out.length - off)) != -1){
            off += len;
        }
        return off;
    }

}
