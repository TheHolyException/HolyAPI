package de.theholyexception.holyapi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;

public class ResourceUtilities {

    private ResourceUtilities() {}

    private static final Logger logger = LoggerFactory.getLogger("ResourceUtilities");

    public static File getResourceFile(String path) {
        try {
            URL url = ResourceUtilities.class.getClassLoader().getResource(path);
            if (url != null) {
                return new File(url.toURI());
            } else {
                return new File(path);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }

    public static InputStream getResourceStream(String path) {
        try {
            InputStream stream = ResourceUtilities.class.getClassLoader().getResourceAsStream(path);
            if (stream == null) {
                File f = new File(path);
                if(f.exists()) stream = new FileInputStream(f);
            }
            return stream;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }

    public static byte[] getResourceFileBytes(String path) throws IOException {
        InputStream is = getResourceStream(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int l;
        byte[] buf = new byte[4096];
        while((l = is.read(buf)) != -1){
            baos.write(buf, 0, l);
        }
        return baos.toByteArray();
    }


}