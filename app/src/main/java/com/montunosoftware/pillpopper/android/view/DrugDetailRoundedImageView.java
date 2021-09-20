package com.montunosoftware.pillpopper.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.model.Drug;

public class DrugDetailRoundedImageView extends RelativeLayout
{
	private ImageView _imgView;

	private int defaultImage = R.drawable.pill_default;

	public DrugDetailRoundedImageView(Context context)
	{
		super(context);
		_init(context);
	}

	public DrugDetailRoundedImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		_init(context);
	}

	private void _init(Context context)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.detail_drug_round_image_view, this);

		_imgView = this.findViewById(R.id.drugimage_round_text);
		updateView(null);
	}

	public void updateView(Drug drug)
	{
		Drawable drawable = drug == null ? null : drug.getImageDrawable(this.getContext());

		if (drawable == null) {
			_imgView.setImageResource(defaultImage);
		} else {
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			if (bitmap != null) {
				setBitmap(bitmap);
			} else {
				_imgView.setImageResource(defaultImage);
			}
		}

	}

	public void setDrawable(Drawable drawable) {
		if (drawable == null) {
			_imgView.setImageResource(defaultImage);
		} else {
			_imgView.setImageDrawable(drawable);
		}
	}

	public void setBitmap(Bitmap bitmap) {
		if(bitmap!=null) {
			setRoundedShape(bitmap);
		}else{
			_imgView.setImageResource(defaultImage);
		}
	}

	public void setImageDrawable(Context context, Drawable drawable) {
		if (drawable == null) {
			_imgView.setBackground(ContextCompat.getDrawable(context, defaultImage));
		} else {
			_imgView.setImageDrawable(drawable);
		}
	}

	public Drawable getImage(){
		return _imgView.getDrawable();
	}

	public void setRoundedShape(Bitmap scaleBitmapImage) {

		Bitmap source = maintainRatio(scaleBitmapImage, scaleBitmapImage.getWidth(), scaleBitmapImage.getHeight());

		int size = Math.min(source.getWidth(), source.getHeight());
		int width= (source.getWidth() - size) / 2;
		int height = (source.getHeight() - size) / 2;

		Bitmap squaredBitMap = Bitmap.createBitmap(source, width, height, size, size);
		Bitmap finalBitmap = Bitmap.createBitmap(size, size, source.getConfig());

		Canvas canvas = new Canvas(finalBitmap);
		BitmapShader shader = new BitmapShader(squaredBitMap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setShader(shader);
		float radius = size / 2f;

		canvas.drawCircle(radius, radius, radius, paint);

		_imgView.setImageBitmap(finalBitmap);
	}

	private Bitmap maintainRatio(Bitmap scaleBitmapImage, int width, int height) {
		int destinationWidth = _imgView.getLayoutParams().width;
		int destinationHeight = _imgView.getLayoutParams().height;

		float widthRatio = (float) destinationWidth / (float) width;
		float heightRatio = (float) destinationHeight / (float) height;

		int finalWidth = (int) Math.floor(width * widthRatio);
		int finalHeight = (int) Math.floor(height * widthRatio);
		if (finalWidth > destinationWidth || finalHeight > destinationHeight) {
			finalWidth = (int) Math.floor(width * widthRatio);
			finalHeight = (int) Math.floor(height * heightRatio);
		}

		scaleBitmapImage = Bitmap.createScaledBitmap(scaleBitmapImage, finalWidth, finalHeight, true);
		Bitmap scaledImage = Bitmap.createBitmap(destinationWidth, destinationHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(scaledImage);

		Paint paint = new Paint();
		paint.setColor(scaleBitmapImage.getPixel(5, 5));
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

		float ratioBitmap = (float) finalWidth / (float) finalHeight;
		float destinationRatio = (float) destinationWidth / (float) destinationHeight;
		float left = ratioBitmap >= destinationRatio ? 0 : (float) (destinationWidth - finalWidth) / 2;
		float top = ratioBitmap < destinationRatio ? 0 : (float) (destinationHeight - finalHeight) / 2;
		canvas.drawBitmap(scaleBitmapImage, left, top, null);

		return scaledImage;
	}


	public void setDefaultImage(int defaultImage) {
		this.defaultImage = defaultImage;
	}

	public ImageView get_imgView() {
		return _imgView;
	}

}
