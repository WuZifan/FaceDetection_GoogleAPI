package com.wzf.face.facedetection1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;


public class FaceActivity extends AppCompatActivity {
    private Bitmap bitmap;
    private static final int MAXIMUM_FACE = 10;
    private static FaceDetector.Face[] faces = new Face[MAXIMUM_FACE];
    private ImageView imageView_show;
    /**
     * handler处理子线程发过来的消息
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            int face_count = (Integer) msg.obj;
            drawCircleOnFace(face_count);
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 0. 调用父类的方法
        super.onCreate(savedInstanceState);
        // 1. 加载相应布局
        setContentView(R.layout.activity_face);
        // 2. 得到开启本Activity的intent
        Intent intent = this.getIntent();
        // 3. 得到数据
        Uri uri = (Uri) intent.getParcelableExtra("image");
        // 4. 得到显示控件1
        imageView_show = (ImageView) this.findViewById(R.id.iv_show);
        // 5. 压缩图片
        bitmap = ImageUtils.scaleToAndroidImage(uri, FaceActivity.this);
        // 8. 显示原始图片
        imageView_show.setImageBitmap(bitmap);

    }


    private void initFaceActivity(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. 加载相应布局
        setContentView(R.layout.activity_face);
        // 2. 得到开启本Activity的intent
        Intent intent = this.getIntent();
        // 3. 得到数据
        Uri uri = (Uri) intent.getParcelableExtra("image");
        // 4. 得到显示控件
        imageView_show = (ImageView) this.findViewById(R.id.iv_show);
        // 5. 压缩图片
        bitmap = ImageUtils.scaleToAndroidImage(uri, FaceActivity.this);
        // 8. 显示图片
        imageView_show.setImageBitmap(bitmap);
    }

    /**
     * 单击，找到人脸
     *
     * @param view
     */
    public void click_findFace(View view) {
        findFaceByOrgAPI();
    }

    /**
     * 利用google原生api来进行人脸识别
     */
    private void findFaceByOrgAPI() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 1. 得到人脸检测者
                FaceDetector faceDetector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), MAXIMUM_FACE);
                // 2. 得到需要检测对象的备份，设置编码为565
                Bitmap detectImage = bitmap.copy(Bitmap.Config.RGB_565, true);
                // 3. 查找人脸
                // 4. 猜测这应该是一个耗时的操作，被放在一个新线程中执行，所以有后面会空指针
                int face_Count = faceDetector.findFaces(detectImage, faces);
                // 4. 发送查找到多少张人脸
                Message message = Message.obtain();
                message.obj = face_Count;
                handler.sendMessage(message);
            }
        }).start();
    }

    /**
     * 在人脸周围画圈
     *
     * @param face_Count
     */
    private void drawCircleOnFace(int face_Count) {
        // 0. 得到图片的副本
        Bitmap copyBitmap = ImageUtils.copyOfImage(bitmap);
        if (face_Count > 0) {
            for (int i = 0; i < face_Count; i++) {
                Face face = this.faces[i];
                PointF pointF = new PointF();
                // 5. 拿到眉心位置
                face.getMidPoint(pointF);
                int x = (int) pointF.x;
                int y = (int) pointF.y;
                // 6. 拿到眼间距
                int eyeDistance = (int) face.eyesDistance();
                int radio = eyeDistance * 9 / 5;
                for (int xc = -radio; xc <= radio; xc++) {
                    for (int yc = -radio; yc <= radio; yc++) {
                        if (Math.sqrt(xc * xc + yc * yc) <= radio && Math.sqrt(xc * xc + yc * yc) >= 0.95 * radio) {
                            try {
                                copyBitmap.setPixel(x + xc, y + yc, Color.RED);
                            } catch (Exception exception) {
                                System.out.println("Out of bound");
                            }
                        }
                    }
                }

            }
            this.imageView_show.setImageBitmap(copyBitmap);
        }
    }
}
