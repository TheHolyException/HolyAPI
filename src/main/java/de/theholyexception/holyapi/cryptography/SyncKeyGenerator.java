package de.theholyexception.holyapi.cryptography;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class SyncKeyGenerator {

	private KeyPair keyPair;
    
    @SuppressWarnings("unused")
	private SecretKey aesKey;

    public SyncKeyGenerator(int keylength) {
        try {
        	KeyGenerator generator = KeyGenerator.getInstance("AES");
        	generator.init(256);
        	SecretKey secKey = generator.generateKey();
        	aesKey = secKey;        	
        	
        	KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keylength);
            keyPair = keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
        	ex.printStackTrace();
        }
    }
    
	public void generateKeyFiles(File privateKeyFile, File publicKeyFile) throws IOException {
		try (FileOutputStream fos1 = new FileOutputStream(privateKeyFile);
		     FileOutputStream fos2 = new FileOutputStream(publicKeyFile)) {
			fos1.write(keyPair.getPrivate().getEncoded());
			fos2.write(keyPair.getPublic().getEncoded());
		}
	}
    
    public KeyPair getKeyPair() {
		return keyPair;
	}
}
