package com.example.surfaceview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import java.util.ArrayList;
import static java.lang.Math.sqrt;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MySurfaceView(this));
    }
}
class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    Resources resources;
    Bitmap img, sprite_image; SurfaceThread thread;
    Paint paint;
    float currentx=0, currenty=0, stepx=0, stepy=0, touchx, touchy;
    float width, height;
    boolean touchevent=false;
    //Sprites sprite;
    ArrayList <Sprites> sprite = new ArrayList<Sprites>();

    public MySurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
        resources = getResources();
        img = BitmapFactory.decodeResource(resources, R.drawable.m);
        sprite_image = BitmapFactory.decodeResource(resources, R.drawable.sprites);
        setFocusable(true);
        sprite.add(0, new Sprites (this, sprite_image, currentx, currenty));
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new SurfaceThread(getHolder(), this);
        thread.start();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
    void checkWall(){
        if (currentx <=0 || currentx+img.getWidth()>=width) stepx=-stepx;
        if (currenty <=0 || currenty+img.getHeight()>=height) stepy=-stepy;
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry  = true; thread.runBool = false;
        while (retry){
            try {
                thread.join(); retry=false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) { //информация о касании event
        int j;
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            touchevent = true;
            touchx = event.getX();
            touchy = event.getY();
            for (j=0; j<sprite.size(); j++)
                sprite.get(j).setTarget(touchx, touchy);
            sprite.add(j, new Sprites(this, sprite_image, touchx, touchy));

        }

        return true;
    }
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        width = canvas.getWidth();
        height = canvas.getHeight();
        canvas.drawARGB(1, 255, 255, 255);

        for (int i=0; i<sprite.size(); i++){
            sprite.get(i).draw(canvas);   //direction 3 вверх 0 вниз 2вправо 1 влево
        }
    }

    public class Sprites {
        MySurfaceView surfaceview;
        Bitmap image;
        float currentx, currenty, targetx, targety, speedx = 0, speedy = 0;
        int columns = 3, rows = 4, width_1, height_1;
        Paint paint= new Paint();
        int currentFrame=0, direction=0;
        int cadrx, cadry;


        public Sprites (MySurfaceView surfaceview, Bitmap image, float x, float y) {
            this.surfaceview = surfaceview;
            this.image = image;
            currentx = x;
            currenty = y;
            width_1 = image.getWidth() / columns;
            height_1 = image.getHeight() / rows;
        }

        void draw(Canvas canvas) {
            currentx += speedx;
            currenty += speedy;
            checkWall();
            currentFrame = ++currentFrame % columns;
            cadrx = currentFrame * width_1;
            cadry = direction * height_1;
            Rect src = new Rect(cadrx, cadry, cadrx+width_1, cadry+height_1);
            Rect dst = new Rect((int) currentx, (int) currenty, (int) currentx + width_1, (int) currenty + height_1);
            canvas.drawBitmap(image, src, dst, paint);
        }

        //public void setTarget(float targetx, float targety) {
            //this.targetx = targetx;
            //this.targety = targety;
            //speedx = (targetx - currentx) / surfaceview.getWidth() * 50;
            // sqrt(x"+y")- длина вектора
            //speedy = (targety - currenty) / surfaceview.getWidth() * 50;
            //speedx = (float) ((targetx - currentx) / sqrt((targetx - currentx)*(targetx - currentx))
             //       +(targety - currenty)*(targety - currenty))/ surfaceview.getWidth() * 50;
            //speedy = (float) ((targety - currenty) / sqrt((targety - currenty)*(targety - currenty))
             //       +(targetx - currentx)*(targetx - currentx))/ surfaceview.getWidth() * 50;
            //if (speedy/speedx>1 ) {direction=3;}
            //else {if (speedy/speedx<1 & speedy/speedx>-1) direction=0;
            //else {
               // if (speedy/speedx<-1){direction=1;}
            //    else{direction=2;}
          //  }}
        //}
        public void setTarget(float targetx, float targety) {
            this.targetx = targetx;
            this.targety = targety;
            speedx = (targetx - currentx); /// surfaceview.getWidth() * 50;
            speedy = (targety - currenty); /// surfaceview.getWidth() * 50;
            float norm = (float)Math.sqrt(speedx*speedx+speedy*speedy);
            speedx =  speedx/norm;
            speedy =  speedy/norm;
            speedx *=5;
            speedy *=5;
            if (speedx>0 ) {direction=0;}
            else direction=1;
        }

        void checkWall(){
            if (currentx <=0 || currentx+width_1>=surfaceview.getWidth()) speedx=-speedx;
            if (currenty <=0 || currenty+height_1>=surfaceview.getHeight()) speedy=-speedy;
        }
    }

}


class SurfaceThread extends Thread {
    SurfaceHolder Holder;
    MySurfaceView view;
    long Time;
    boolean runBool = true;

    public SurfaceThread(SurfaceHolder holder, MySurfaceView view) { //constructor
        Holder = holder;
        this.view = view; //remember полученные переменные to local
        Time = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while (runBool) {
            //super.run();
            Canvas canvas = null;
            long Time0 = System.currentTimeMillis();
            long Time_ = Time0 - Time;

            if (Time_ > 30) {
                Time = Time0;
                canvas = Holder.lockCanvas();
                synchronized (Holder) {
                    this.view.draw(canvas);

                }

                if (canvas != null) Holder.unlockCanvasAndPost(canvas);
            }
        }
    }




}