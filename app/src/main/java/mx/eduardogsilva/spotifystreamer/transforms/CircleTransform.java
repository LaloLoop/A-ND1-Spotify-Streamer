package mx.eduardogsilva.spotifystreamer.transforms;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

/**
 * Picasso transform to show rounded images
 * Created by Lalo on 28/06/15.
 */
public class CircleTransform implements Transformation{
    @Override
    public Bitmap transform(Bitmap source) {
        // Get min circle size
        int size = Math.min(source.getWidth(), source.getHeight());

        // Get new origin for image
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        // "Cut" source to new size
        Bitmap squareBitmap = Bitmap.createBitmap(source, x, y, size, size);

        if(squareBitmap != source){
            // We no longer need the source. Recycle it.
            source.recycle();
        }

        // Create a new blank bitmap
        Bitmap circleBitmap = Bitmap.createBitmap(size, size, source.getConfig());

        // Draw it all together
        Canvas canvas = new Canvas(circleBitmap);
        Paint paint = new Paint();
        // Create shader to draw circle with image cropped
        BitmapShader shader = new BitmapShader(squareBitmap, BitmapShader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        // Get radius
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        squareBitmap.recycle();

        return circleBitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}
