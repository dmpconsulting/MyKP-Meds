package com.montunosoftware.pillpopper.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.Drug;


public class DrugImageView extends RelativeLayout
{
	private ImageView _imgView;

	private ImageView _customImageView;

	public DrugImageView(Context context)
	{
		super(context);
		_init(context);
	}

	public DrugImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		_init(context);
	}

	private void _init(Context context)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.drugimage_view, this);

		_imgView = this.findViewById(R.id.drugimage_text);
		_customImageView = this.findViewById(R.id.cross_img);
		updateView(null);
	}
	public static ImageView scaleImage(ImageView view, int boundBoxInDp,Drawable drawable)
	{
	    // Get the ImageView and its bitmap
	    Drawable drawing = drawable;
	    Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

	    if (null != bitmap) {
	    	 // Get current dimensions
		    int width = bitmap.getWidth();
		    int height = bitmap.getHeight();

		    // Determine how much to scale: the dimension requiring less scaling is
		    // closer to the its side. This way the image always stays inside your
		    // bounding box AND either x/y axis touches it.
		    float xScale = ((float) boundBoxInDp) / width;
		    float yScale = ((float) boundBoxInDp) / height;
		    float scale = (xScale <= yScale) ? xScale : yScale;

		    // Create a matrix for the scaling and add the scaling data
		    Matrix matrix = new Matrix();
		    matrix.postScale(scale, scale);

		    // Create a new bitmap and convert it to a format understood by the ImageView
		    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		    BitmapDrawable result = new BitmapDrawable(scaledBitmap);
		    width = scaledBitmap.getWidth();
		    height = scaledBitmap.getHeight();

		    // Apply the scaled bitmap
		    view.setBackgroundDrawable(result);

		    // Now change ImageView's dimensions to match the scaled image
		    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
		    params.width = width;
		    params.height = height;
		    view.setLayoutParams(params);
		    return view;
		}
		return view;
	   
	}
	public void updateView(Drug drug)
	{
		Drawable drawable = drug == null ? null : drug.getImageDrawable(this.getContext());

		if(null!=drawable){
			_imgView=scaleImage(_imgView,380,drawable);//.setbasetBackgroundDrawable(drawable);
			_imgView.setImageDrawable(null);

		}else{
			_imgView.setBackgroundColor(Util.getColorWrapper(getContext(), R.color.light_grey));
			_imgView.setImageResource(R.drawable.camera);
		}
	}
}
