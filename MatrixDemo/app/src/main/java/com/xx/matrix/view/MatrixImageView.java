package com.xx.matrix.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.widget.ImageView;

import com.xx.matrix.R;

/**
 * Created by wxx on 2016/11/3.
 */

public class MatrixImageView extends ImageView {

    private Bitmap mBitmap;
    private Matrix mMatrix;

    public MatrixImageView(Context context){
        super(context);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.p1);
        mMatrix = new Matrix();
    }

    @Override
    protected void onDraw(Canvas canvas){
        // 画出原图像
        canvas.drawBitmap(mBitmap, 0, 0, null);
        // 画出变换后的图像
        canvas.drawBitmap(mBitmap, mMatrix, null);
        super.onDraw(canvas);
    }

    @Override
    public void setImageMatrix(Matrix matrix){
        mMatrix.set(matrix);
        super.setImageMatrix(matrix);
    }

    public Bitmap getImageBitmap(){
        return mBitmap;
    }

}
