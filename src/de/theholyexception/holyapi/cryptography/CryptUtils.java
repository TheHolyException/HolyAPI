package de.theholyexception.holyapi.cryptography;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class CryptUtils {

	public static KeyPair readKeyPair(File privateKeyFile, File publicKeyFile, String algorithm) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		FileInputStream fis;
		
		fis = new FileInputStream(privateKeyFile);
		byte[] keyPrivate = fis.readAllBytes();
		fis.close();
		
		fis = new FileInputStream(publicKeyFile);
		byte[] keyPublic = fis.readAllBytes();
		fis.close();
		
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
	    EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyPrivate);
	    PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

	    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyPublic);
	    PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
	    return new KeyPair(publicKey, privateKey);
	}
	
	public static PublicKey getPublic(byte[] data, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(data);
	    return keyFactory.generatePublic(publicKeySpec);
	}
	
	public static PrivateKey getPrivate(byte[] data, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
	    EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(data);
	    return keyFactory.generatePrivate(privateKeySpec);
	}
	
}
