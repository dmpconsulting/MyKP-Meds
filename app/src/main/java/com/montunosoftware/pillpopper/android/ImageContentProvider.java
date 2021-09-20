package com.montunosoftware.pillpopper.android;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.montunosoftware.mymeds.BuildConfig;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageContentProvider extends ContentProvider {
	public static final String AUTHORITY = BuildConfig.AUTHORITY;
	public static final Uri CONTENT_URI = Uri.parse
			("content://" + AUTHORITY);

    private static final HashMap<String, String> MIME_TYPES =
           new HashMap<>();

    static {
        MIME_TYPES.put(".jpg", "image/jpeg");
        MIME_TYPES.put(".jpeg", "image/jpeg");
    }



	@Override
    public boolean onCreate() {

        try {
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
            return (true);
        } catch (Exception e) {
          LoggerUtils.exception("Exception", e);
            return false;
        }

    }

    @Override
    public String getType(Uri uri) {
		if (isAccessProvided(uri)) {
			String path = uri.toString();

			for (Map.Entry entry : MIME_TYPES.entrySet()) {
				if (path.endsWith(entry.getValue().toString())) {
					return (MIME_TYPES.get(entry.getValue().toString()));
				}
			}
		}
		return (null);
	}

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
    throws FileNotFoundException {
    	 if(isAccessProvided(uri)){
        File f = new File(getContext().getFilesDir(), "newImage.jpg");
    	if(!f.exists())
			try {
				if(!f.createNewFile()){
					PillpopperLog.say("Oops!, ImageContentProvider openFile - failed to create file");
				}
			} catch (IOException e) {
				PillpopperLog.say("Oops!, IOException" , e);
			} catch (Exception e) {
				PillpopperLog.say("Oops!, Exception" , e);
			}
	
            return (ParcelFileDescriptor.open(f,ParcelFileDescriptor.MODE_READ_WRITE));
    	 }else{
    		 throw new FileNotFoundException("File not found");
    	 }
    }

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return null;
	}

	 @Override
	    public Uri insert(Uri uri, ContentValues initialValues) {
	   	 return null;
	   	 }

	 @Override
	    public int delete(Uri uri, String where, String[] whereArgs) {
		 boolean status = false;
		 if(isAccessProvided(uri)){
	        File f = new File(getContext().getFilesDir(), "newImage.jpg");
	        if(f.exists()){
	        	try {
					status = f.delete();
				} catch (Exception e) {
					LoggerUtils.exception("Exception", e);
				}
	        }
	        getContext().getContentResolver().notifyChange(CONTENT_URI, null);
		 }
	        return status?1:0;
	    }

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
	
	public boolean isAccessProvided(Uri uri){
		 String queryParameter = uri.getQueryParameter("code");
		return queryParameter!=null && queryParameter.equalsIgnoreCase(RunTimeData.getInstance().getCpCode());
	}
}