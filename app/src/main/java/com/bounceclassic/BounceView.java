package com.bounceclassic;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

public class BounceView extends View {
    private final Paint p = new Paint(3);
    private final ArrayList<RectF> platforms = new ArrayList<>();
    private float ballX=90, ballY=0, vx=0, vy=0, floorY=0, scale=1;
    private boolean left=false, right=false, jump=false, started=false, won=false;
    private int rings=0;
    private long last=0;
    private final RectF leftBtn=new RectF(), rightBtn=new RectF(), jumpBtn=new RectF();
    public BounceView(Context c){ super(c); setLayerType(View.LAYER_TYPE_SOFTWARE,null); }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(), h=getHeight(); if(w==0||h==0)return;
        scale=Math.min(w/800f,h/450f);
        c.save(); c.scale(scale,scale);
        float W=w/scale,H=h/scale; floorY=H-66;
        p.setShader(new LinearGradient(0,0,0,H,0xffe9f5ec,0xffb8d8c1,Shader.TileMode.CLAMP)); c.drawRect(0,0,W,H,p); p.setShader(null);
        p.setColor(0xff9ac6a3); Path hills=new Path(); hills.moveTo(0,floorY); hills.cubicTo(150,250,220,360,370,floorY); hills.cubicTo(520,270,650,280,W,floorY); hills.close(); c.drawPath(hills,p);
        p.setColor(0xff355b43); c.drawRect(0,floorY,W,H,p);
        p.setColor(0xff5d9668); c.drawRect(0,floorY,W,floorY+8,p);
        platforms.clear();
        platforms.add(new RectF(175,floorY-85,290,floorY-68));
        platforms.add(new RectF(385,floorY-145,500,floorY-128));
        platforms.add(new RectF(600,floorY-95,720,floorY-78));
        p.setColor(0xff477d55); for(RectF r:platforms)c.drawRoundRect(r,7,7,p);
        p.setColor(0xfff5f0df); for(int x=320;x<370;x+=16){Path s=new Path();s.moveTo(x,floorY);s.lineTo(x+8,floorY-22);s.lineTo(x+16,floorY);s.close();c.drawPath(s,p);}
        for(int i=0;i<3;i++){float rx=225+i*190, ry=floorY-(i==0?125:i==1?185:135); if(rings<=i){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(6);p.setColor(0xffffc928);c.drawCircle(rx,ry,11,p);p.setStyle(Paint.Style.FILL);}}
        if(!started){ballY=floorY-22;}
        else if(!won){
            long now=System.nanoTime(); float dt=last==0?0.016f:Math.min(.033f,(now-last)/1e9f);last=now;
            if(left)vx-=700*dt; if(right)vx+=700*dt; if(!left&&!right)vx*=Math.pow(.04,dt);
            vx=Math.max(-260,Math.min(260,vx)); if(jump && ballY>=floorY-24)vy=-510;
            vy+=1150*dt; ballX+=vx*dt; ballY+=vy*dt;
            if(ballY>floorY-22){ballY=floorY-22;vy=-Math.min(430,Math.abs(vy)*.68f);}
            for(RectF r:platforms) if(ballX>r.left-15&&ballX<r.right+15&&ballY+20>r.top&&ballY+20<r.bottom+20&&vy>0){ballY=r.top-21;vy=-360;}
            if(ballX<20)ballX=20;if(ballX>W-20)ballX=W-20;
            for(int i=0;i<3;i++){float rx=225+i*190,ry=floorY-(i==0?125:i==1?185:135);if(rings==i&&Math.hypot(ballX-rx,ballY-ry)<28)rings++;}
            if(rings>=3&&ballX>W-75)won=true;
        }
        p.setShader(new RadialGradient(ballX-7,ballY-8,32,new int[]{0xffff8b80,0xffef332d,0xffa91f20},null,Shader.TileMode.CLAMP)); c.drawCircle(ballX,ballY,21,p);p.setShader(null);
        p.setColor(0x55ffffff);c.drawCircle(ballX-7,ballY-9,6,p);
        p.setColor(0xff173b27);p.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));p.setTextSize(24);c.drawText("BOUNCE CLASSIC",24,34,p);p.setTextSize(18);c.drawText("RINGS  "+rings+"/3",W-130,34,p);
        leftBtn.set(24,H-58,105,H-12);rightBtn.set(116,H-58,197,H-12);jumpBtn.set(W-130,H-70,W-24,H-12);
        drawButton(c,leftBtn,"◀");drawButton(c,rightBtn,"▶");drawButton(c,jumpBtn,"JUMP");
        if(!started||won){p.setColor(0xdd10291b);c.drawRoundRect(W/2-170,H/2-54,W/2+170,H/2+54,18,18,p);p.setColor(-1);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(27);c.drawText(won?"LEVEL CLEAR!":"TAP TO START",W/2,H/2+9,p);p.setTextAlign(Paint.Align.LEFT);}
        c.restore(); postInvalidateOnAnimation();
    }
    private void drawButton(Canvas c,RectF r,String t){p.setColor(0x99506f58);c.drawRoundRect(r,13,13,p);p.setColor(-1);p.setTextSize(t.equals("JUMP")?16:26);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);c.drawText(t,r.centerX(),r.centerY()+8,p);p.setTextAlign(Paint.Align.LEFT);}
    @Override public boolean onTouchEvent(MotionEvent e){
        float x=e.getX()/scale,y=e.getY()/scale;
        if(e.getAction()==MotionEvent.ACTION_DOWN){if(!started||won){started=true;won=false;ballX=90;ballY=floorY-22;vx=vy=0;rings=0;last=0;return true;}}
        if(e.getAction()==MotionEvent.ACTION_DOWN||e.getAction()==MotionEvent.ACTION_MOVE){left=leftBtn.contains(x,y);right=rightBtn.contains(x,y);jump=jumpBtn.contains(x,y);return true;}
        if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){left=right=jump=false;return true;} return true;
    }
}
