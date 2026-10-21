package com.glass26.app;

import android.content.*;
import android.graphics.*;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Status-indicator artwork INSIDE Glass26 screens only.
 * Never modifies the actual Android/Samsung status bar or system-wide indicators.
 * Uses real system clock, battery level and connected transports. The cellular bars
 * are a connectivity icon, not an assertion of precise radio signal strength.
 */
public final class GlassStatusBar extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF tmp=new RectF();
    private final SimpleDateFormat time=new SimpleDateFormat("HH:mm",Locale.getDefault());
    private float density;
    private long lastUpdate=0;
    private int battery=0;
    private boolean wifi=false;
    private boolean cellular=false;
    private boolean charging=false;
    public GlassStatusBar(Context context){
        super(context);
        density=context.getResources().getDisplayMetrics().density;
        setContentDescription("Indicadores de relógio, conexão e bateria do Glass26");
    }
    private float d(float v){return v*density;}
    private void update(){
        if(System.currentTimeMillis()-lastUpdate<1000)return;
        lastUpdate=System.currentTimeMillis();
        Intent status=getContext().registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if(status!=null){
            int level=status.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
            int scale=status.getIntExtra(BatteryManager.EXTRA_SCALE,100);
            battery=scale>0?Math.round(level*100f/scale):0;
            int power=status.getIntExtra(BatteryManager.EXTRA_PLUGGED,0);
            charging=power!=0;
        }
        wifi=false;cellular=false;
        try{
            ConnectivityManager conn=(ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network=conn.getActiveNetwork();
            NetworkCapabilities caps=network==null?null:conn.getNetworkCapabilities(network);
            if(caps!=null){
                wifi=caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                cellular=caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            }
        }catch(Exception ignored){}
        setContentDescription("Hora "+time.format(new Date())+
            ". Wi-Fi "+(wifi?"conectado":"não conectado")+
            ". Rede móvel "+(cellular?"ativa":"sem dados ativos")+
            ". Bateria "+battery+" por cento.");
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);update();
        float w=getWidth(),h=getHeight();
        paint.clearShadowLayer();
        paint.setColor(Color.WHITE);paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
        paint.setTextSize(d(15));
        Paint.FontMetrics fm=paint.getFontMetrics();
        float baseline=(h-(fm.bottom+fm.top))/2f;
        canvas.drawText(time.format(new Date()),d(8),baseline,paint);
        // Camera-area capsule: a visual element within Glass26, not Android's SystemUI.
        paint.setColor(0xFF050910);
        tmp.set(w/2-d(62),h/2-d(15),w/2+d(62),h/2+d(15));
        canvas.drawRoundRect(tmp,d(17),d(17),paint);
        paint.setColor(0xFF13263D);
        canvas.drawCircle(w/2+d(45),h/2,d(4.5f),paint);
        paint.setColor(Color.WHITE);
        float right=w-d(8);
        float cx=right-d(14),cy=h/2f;
        // Battery outline and real fill percentage.
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(d(1.4f));
        paint.setStrokeJoin(Paint.Join.ROUND);
        tmp.set(cx-d(10),cy-d(5),cx+d(11),cy+d(5));
        canvas.drawRoundRect(tmp,d(2.6f),d(2.6f),paint);
        paint.setStyle(Paint.Style.FILL);
        tmp.set(cx+d(12),cy-d(2),cx+d(14),cy+d(2));
        canvas.drawRoundRect(tmp,d(1),d(1),paint);
        float fill=Math.max(0.05f,battery/100f);
        tmp.set(cx-d(8),cy-d(3),cx-d(8)+d(17)*fill,cy+d(3));
        canvas.drawRoundRect(tmp,d(1.2f),d(1.2f),paint);
        if(charging){
            paint.setTextSize(d(10));paint.setTypeface(Typeface.DEFAULT_BOLD);
            canvas.drawText("ϟ",cx-d(2),cy+d(3),paint);
        }
        // Wi-Fi waves are only displayed when Wi-Fi is the active transport.
        float wx=cx-d(37);
        if(wifi){
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(d(2.1f));
            paint.setStrokeCap(Paint.Cap.ROUND);
            for(int i=0;i<3;i++){
                float radius=d(5+i*4);
                tmp.set(wx-radius,cy-radius*.75f,wx+radius,cy+radius*.75f);
                canvas.drawArc(tmp,207,126,false,paint);
            }
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(wx,cy+d(5),d(2),paint);
        }
        // Mobile connectivity glyph: not a measured signal-strength claim.
        float bx=wifi?cx-d(72):cx-d(42);
        if(cellular){
            for(int i=0;i<4;i++){
                float height=d(3+i*2);
                canvas.drawRoundRect(bx+i*d(4),cy+d(6)-height,bx+i*d(4)+d(2.5f),cy+d(6),d(.5f),d(.5f),paint);
            }
        }
        paint.setStyle(Paint.Style.FILL);
        postInvalidateDelayed(15000);
    }
}
