package de.theholyexception.holyapi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipHandler {

	public static void zipFile(File source, File target) throws IOException {
		target.createNewFile();
		FileOutputStream fos = new FileOutputStream(target);
		ZipOutputStream zos = new ZipOutputStream(fos);
		FileInputStream fis = new FileInputStream(target);
		ZipEntry entry = new ZipEntry(source.getName());
		zos.putNextEntry(entry);
		byte[] buffer = new byte[1024];
		int l;
		while((l = fis.read()) != -1) {
			zos.write(buffer, 0, l);
		}
		zos.close();
		fis.close();
		fos.close();
	}
	
	public static void zipFolder(File sourceFolder, File target) throws IOException {
		target.createNewFile();
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(target));
		for (File files : sourceFolder.listFiles()) {
			zipFolderREC(sourceFolder, files, target, zos);
		}
		zos.close();
	}
	
	private static void zipFolderREC(File sourceFolder, File currentFolder, File target, ZipOutputStream zos) throws IOException {
		String path = currentFolder.getAbsolutePath().replace(sourceFolder.getAbsolutePath(), "");
		if (path.length() > 0) path = path.substring(1);
		if (currentFolder.isDirectory()) {
			path = path + (path.endsWith("/") ? "" : "/");
			zos.putNextEntry(new ZipEntry(path));
			zos.closeEntry();
			for (File files : currentFolder.listFiles()) {
				File s = new File(currentFolder, files.getName());
				zipFolderREC(sourceFolder, s, target, zos);
			}
		} else {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(currentFolder);
				zos.putNextEntry(new ZipEntry(path));				
				byte[] buffer = new byte[1024*1024];
				int l;
				while((l = fis.read(buffer)) > 0)
					zos.write(buffer, 0, l);
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (fis != null) fis.close();
			}
		}
	}
	
}
