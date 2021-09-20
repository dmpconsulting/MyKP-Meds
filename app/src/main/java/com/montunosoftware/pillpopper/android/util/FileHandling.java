package com.montunosoftware.pillpopper.android.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.KeySpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class FileHandling
{
	private static final String _tmpExtension = ".tmp";

	// this cipher construction is loosely based on the combination of two urls:
	// http://stackoverflow.com/questions/4275311/how-to-encrypt-and-decrypt-file-in-android
	// http://stackoverflow.com/questions/992019/java-256-bit-aes-password-based-encryption
	//
	// however, with some changes:
	// * salt effectively disabled because it doesn't make sense in this context
	// * iterations reduced from 64k to 16 because the phone isn't fast enough to do 64k
	// * cipher changed to AES from AES/CBC/PKCS5Padding because it complains about not
	//   having IV (initialization vector) initialized; fix was to change to regular AES
	//   as described here: http://stackoverflow.com/questions/13389870/android-4-2-broke-my-aes-encrypt-decrypt-code

    @SuppressLint("GetInstance")
	private static Cipher _getCipher(Context context, int opmode, String additionalPassword)
	{
		String password = UniqueDeviceId.getHardwareId(context);
		
		if (additionalPassword != null) {
			password = password + additionalPassword;
		}
		
		SecretKeyFactory factory;
		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), "no-salt-required".getBytes(), 16, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(opmode, secret);
			return cipher;
		} catch (Exception e) {
			PillpopperLog.say("Couldn't create cipher:", e);
			return null;
		}
	}
	
	// from http://stackoverflow.com/questions/11281010/how-can-i-get-external-sd-card-path-for-android-4-0
	
	// Commenting this method definition because of DE1566 [QC-4288]: [My KP Meds Android] : [Command Injection]. 
	// And we are not encouraging the external SdCard storage.
	public static List<String> _getExternalMounts()
	{
	    /*final List<String> out = new ArrayList<String>();
	    String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
	    String s = "";
	    try {
	        final Process process = new ProcessBuilder().command("mount")
	                .redirectErrorStream(true).start();
	        process.waitFor();
	        final InputStream is = process.getInputStream();
	        final byte[] buffer = new byte[1024];
	        while (is.read(buffer) != -1) {
	            s = s + new String(buffer);
	        }
	        is.close();
	    } catch (final Exception e) {
	    	return null;
	    }
	
	    // parse output
	    final String[] lines = s.split("\n");
	    for (String line : lines) {
	        if (!line.toLowerCase(Locale.US).contains("asec")) {
	            if (line.matches(reg)) {
	                String[] parts = line.split(" ");
	                for (String part : parts) {
	                    if (part.startsWith("/"))
	                        if (!part.toLowerCase(Locale.US).contains("vold"))
	                            out.add(part);
	                }
	            }
	        }
	    }*/
	    return null;
	}

	// Get the external directory (on the sd card) to which to write files.
	// Temporary files go in Android/data/<packagename>/files and get auto-deleted when the app is un-installed.
	// Durable files go in Android/Dosecast/<packagename>. These should stay durable across app installations.
	
	public enum StorageLocation {
		Internal,
		External_Temporary,
		External_Durable
	}

	private static File externalDurable = null;
	private static File externalTemporary = null;  
	
	public static File getExternalStorageDirectory(Context context, StorageLocation storageLocation) throws IOException
	{
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			throw new IOException();
		}
		
		if (externalDurable == null) {
			/*List<String> extDirs = _getExternalMounts();

			File baseDir;

			if (extDirs != null && !extDirs.isEmpty()) {
				baseDir = new File(extDirs.get(0));
			} else {
				baseDir = Environment.getExternalStorageDirectory();
			}*/
			File baseDir = Environment.getExternalStorageDirectory();
			externalDurable = new File(baseDir, "Dosecast");
			externalDurable = new File(baseDir, context.getPackageName());
			
			externalTemporary = new File(baseDir, "Android");
			externalTemporary = new File(externalTemporary, "data");
			externalTemporary = new File(externalTemporary, context.getPackageName());
			externalTemporary = new File(externalTemporary, "files");
		}
		
		File retval = null;
		
		if (storageLocation == StorageLocation.External_Durable) {
			retval = externalDurable;
		} else {
			retval = externalTemporary;
		}
		
		if (!retval.exists()) {
			retval.mkdirs();
		}
		
		return retval;
	}
	
	public static void copyStream(InputStream sourceStream, OutputStream destStream) throws IOException
	{
		byte[] buf = new byte[16384];
		int len;
		
		while ((len = sourceStream.read(buf)) > 0) {
			destStream.write(buf, 0, len);
		}
	}
	
	public static void copyFile(File source, File dest) throws IOException
	{
		InputStream sourceStream = new FileInputStream(source);
		OutputStream destStream = new FileOutputStream(dest);
		
		copyStream(sourceStream, destStream);
		try {
			sourceStream.close();
			destStream.close();
		} catch (Exception e){
			LoggerUtils.exception("Exception",e);
		} finally {
			Util.closeSilently(sourceStream);
			Util.closeSilently(destStream);
		}

	}

	// based on http://www.java-tips.org/java-se-tips/java.io/how-to-copy-a-directory-from-one-location-to-another-loc.html
	public static void copyDirectory(Context context, File source, File dest) throws IOException
	{
		if (source == null || dest == null) {
			return;
		}
		
		if (source.isDirectory()) {
			if(source.list()!=null) {
				for (String child : source.list()) {
					copyDirectory(context, new File(source, child), new File(dest, child));
				}
			}
		} else {
			PillpopperLog.say("Copying %s -> %s", source, dest);
			copyFile(source, dest);
		}
	}


	//// file writing
	public static class OutputStreamAndFilename
	{
		private OutputStream outputStream;
		private File outputDirectory;
		private File outputFilename;
		private FileDescriptor fd;

		public OutputStream getOutputStream() {
			return outputStream;
		}

		public void setOutputStream(OutputStream outputStream) {
			this.outputStream = outputStream;
		}

		public File getOutputDirectory() {
			return outputDirectory;
		}

		public void setOutputDirectory(File outputDirectory) {
			this.outputDirectory = outputDirectory;
		}

		public File getOutputFilename() {
			return outputFilename;
		}

		public void setOutputFilename(File outputFilename) {
			this.outputFilename = outputFilename;
		}

		public FileDescriptor getFd() {
			return fd;
		}

		public void setFd(FileDescriptor fd) {
			this.fd = fd;
		}
	}

	public static OutputStreamAndFilename openOutputStream(Context ctx,
			PillpopperAppContext pillpopperAppContext,
			String outputFilename,
			StorageLocation storageLocation,
			String attachment,
			int mode)
	{
		OutputStreamAndFilename retval = new OutputStreamAndFilename();
		Context context = ctx;
		FileOutputStream outputStream = null;
		try {
			if (storageLocation == StorageLocation.Internal) {
				outputStream = context.openFileOutput(outputFilename, mode);
				retval.setOutputDirectory(context.getFilesDir());
				retval.setOutputFilename(new File(context.getFilesDir(), outputFilename));
			} else {
				File extDir = getExternalStorageDirectory(context, storageLocation);

				if (extDir==null || !extDir.exists()) {
					PillpopperLog.say("Uh oh, no external storage directory!");
					throw new IOException();
				} else {
					retval.setOutputDirectory(extDir);
					retval.setOutputFilename(new File(retval.getOutputDirectory(), outputFilename));
					outputStream = new FileOutputStream(retval.getOutputFilename());
				}
			}

			retval.setFd(outputStream.getFD());

			if (mode != Context.MODE_WORLD_READABLE) {
				Cipher cipher = _getCipher(context, Cipher.ENCRYPT_MODE, null);

				if (cipher == null) {
					PillpopperLog.say("Couldn't create encryption cipher!");
					throw new IOException();
				}

				retval.setOutputStream(new CipherOutputStream(outputStream, cipher));
			} else {
				retval.setOutputStream(outputStream);
			}

			retval.getOutputStream().write(attachment.getBytes());

		} catch (IOException ex){
			LoggerUtils.exception("IOException",ex);
		} catch (Exception ex){
			LoggerUtils.exception("Exception",ex);
		} finally {
			Util.closeSilently(outputStream);
		}
		
		return retval;
	}

	// Tries to delete a file from the given storage location. Fails silently.
	public static void deleteFile(Context context, String filename, StorageLocation storageLocation)
	{
		File f;
		
		if (storageLocation == StorageLocation.Internal) {
			f = new File(context.getFilesDir(), filename);
		} else {
			File extDir;
			
			try {
				extDir = getExternalStorageDirectory(context, storageLocation);
			} catch (IOException e) {
				LoggerUtils.exception("IOException", e);
				// possibly no external storage mounted
				return;
			}
			f = new File(extDir, filename);
		}
		
		if(!f.delete()){
            PillpopperLog.say("Oops!, FileHandling - deleteFile Failed to delete file ");
        }
	}
}
