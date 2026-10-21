package com.glass26.app;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

public final class GlassVisual {
    private GlassVisual(){}
    public static int dp(Context context,float amount) {
        return Math.round(amount*context.getResources().getDisplayMetrics().density);
    }
    public static GradientDrawable panel(Context context,float radius,boolean dark) {
        GradientDrawable drawable=new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            dark?new int[]{0x886F90BA,0x587A9EB5,0x637887B6}
                :new int[]{0xC7EEF9FF,0x9CD6F5FA,0xACC9DDF9});
        drawable.setCornerRadius(dp(context,radius));
        drawable.setStroke(Math.max(1,dp(context,1)),dark?0x8EC0EEFF:0xE1FFFFFF);
        return drawable;
    }
    public static GradientDrawable accent(Context context,float radius,int top,int bottom){
        GradientDrawable drawable=new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,new int[]{top,bottom});
        drawable.setCornerRadius(dp(context,radius));
        drawable.setStroke(Math.max(1,dp(context,1)),0x66FFFFFF);
        return drawable;
    }
    public static GradientDrawable island(Context context,float radius){
        GradientDrawable d=new GradientDrawable();
        d.setColor(0xFF090B13);d.setCornerRadius(dp(context,radius));
        d.setStroke(Math.max(1,dp(context,.6f)),0x50576B88);
        return d;
    }
    public static final class Wallpaper extends View {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        public Wallpaper(Context context){super(context);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            float w=getWidth(),h=getHeight();
            if(w<=0||h<=0)return;
            p.setShader(new LinearGradient(0,0,w*.8f,h,
                new int[]{0xFF102B5D,0xFF1683AD,0xFF43C4C3,0xFF07245D},
                new float[]{0f,.34f,.64f,1f},Shader.TileMode.CLAMP));
            canvas.drawRect(0,0,w,h,p);
            p.setShader(new RadialGradient(w*.9f,h*.25f,w*.8f,
                new int[]{0xBB80FCF0,0x446DDCD1,0x0027A9BB},null,Shader.TileMode.CLAMP));
            canvas.drawRect(0,0,w,h,p);
            p.setShader(new RadialGradient(w*.13f,h*.9f,w*.74f,
                new int[]{0xEF19D0C7,0x7744D3CD,0x001EC5C5},null,Shader.TileMode.CLAMP));
            canvas.drawRect(0,0,w,h,p);
            p.setShader(new LinearGradient(0,h*.63f,0,h,
                new int[]{0x00000000,0xA8001742},null,Shader.TileMode.CLAMP));
            canvas.drawRect(0,0,w,h,p);
            p.setShader(null);
        }
    }
}
