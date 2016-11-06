package com.xx.matrix;

import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.xx.matrix.view.MatrixImageView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private MatrixImageView mMatrixImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mMatrixImageView = new MatrixImageView(this);
        mMatrixImageView.setScaleType(ImageView.ScaleType.MATRIX);
        mMatrixImageView.setOnTouchListener(this);

        setContentView(mMatrixImageView);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                Matrix matrix = new Matrix();
                //平移 沿x轴移动图片的宽度距离，沿y轴移动图片的高度距离
//                matrix.postTranslate(mMatrixImageView.getImageBitmap().getWidth(), mMatrixImageView.getImageBitmap().getHeight());
//                mMatrixImageView.setImageMatrix(matrix);

                //缩放 以图片中心点缩小一倍
//                matrix.postScale(0.5f, 0.5f,
//                        mMatrixImageView.getImageBitmap().getWidth() / 2,
//                        mMatrixImageView.getImageBitmap().getHeight() / 2);
//                mMatrixImageView.setImageMatrix(matrix);

                //旋转 绕图片右下角的点旋转180度
//                matrix.postRotate(180,
//                        mMatrixImageView.getImageBitmap().getWidth(),
//                        mMatrixImageView.getImageBitmap().getHeight());
//                mMatrixImageView.setImageMatrix(matrix);

                // 错切
//                matrix.postSkew(0, 1);//垂直错切
//                matrix.postSkew(1, 0);//水平错切
//                matrix.postSkew(1, 2);//复合错切
//                mMatrixImageView.setImageMatrix(matrix);

                float[] src = {
                        0, 0,                                              // 左上
                        mMatrixImageView.getImageBitmap().getWidth(), 0, // 右上
                        mMatrixImageView.getImageBitmap().getWidth(), mMatrixImageView.getImageBitmap().getHeight(),        // 右下
                        0, mMatrixImageView.getImageBitmap().getHeight()};                        // 左下

                float[] dst = {
                        88, 88,                                    // 左上
                        mMatrixImageView.getImageBitmap().getWidth(), 255,                        // 右上
                        mMatrixImageView.getImageBitmap().getWidth(), mMatrixImageView.getImageBitmap().getHeight() - 256,  // 右下
                        0, mMatrixImageView.getImageBitmap().getHeight()};                        // 左下

                matrix.setPolyToPoly(src, 0, dst, 0, 4);
                mMatrixImageView.setImageMatrix(matrix);
                break;

        }
        mMatrixImageView.invalidate();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
