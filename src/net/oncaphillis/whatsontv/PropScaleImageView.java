package net.oncaphillis.whatsontv;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PropScaleImageView extends ImageView {

	private boolean _fitV = false;
	private boolean _fitH   = true;
	
    public PropScaleImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final Drawable d = this.getDrawable();
 
        if (d != null && _fitH) {
        	final int width = MeasureSpec.getSize(widthMeasureSpec);
        	final int height = (int) Math.ceil(width * (float) d.getIntrinsicHeight() / d.getIntrinsicWidth());
            this.setMeasuredDimension(width, height);
        } else if (d != null && _fitV) {
        	final int height = MeasureSpec.getSize(heightMeasureSpec);
        	final int width  = (int) Math.ceil(height * (float) d.getIntrinsicWidth() / d.getIntrinsicHeight());
            this.setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}