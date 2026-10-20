package com.glass26.app;

import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public final class IslandService extends Service {
    private WindowManager manager;
    private View island;
    private TextView display;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private int d(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    private final Runnable tick=new Runnable(){
        @Override public void run(){
            if(display!=null)display.setText("●   "+new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date())+"   "+battery()+"%");
            handler.postDelayed(this,20000);
        }
    };
    private int battery(){
        Intent i=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if(i==null)return 0;
        int level=i.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
        int scale=i.getIntExtra(BatteryManager.EXTRA_SCALE,100);
        return scale>0?Math.round(level*100f/scale):0;
    }
    @Override public void onCreate(){
        super.onCreate();
        if(!Settings.canDrawOverlays(this)){stopSelf();return;}
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel=new NotificationChannel("island","Ilha Dinâmica Glass26",NotificationManager.IMPORTANCE_LOW);
        nm.createNotificationChannel(channel);
        Notification notice=new Notification.Builder(this,"island")
            .setSmallIcon(android.R.drawable.ic_menu_info_details).setContentTitle("Glass26")
            .setContentText("Ilha Dinâmica ativa").setOngoing(true).build();
        try{startForeground(26,notice);}catch(Exception e){stopSelf();return;}
        manager=(WindowManager)getSystemService(WINDOW_SERVICE);
        display=new TextView(this);display.setTextColor(-1);display.setGravity(Gravity.CENTER);
        display.setTextSize(14);
        GradientDrawable bg=new GradientDrawable();bg.setColor(0xF5090E1B);bg.setCornerRadius(d(40));
        bg.setStroke(d(1),0x778B99B3);display.setBackground(bg);
        WindowManager.LayoutParams params=new WindowManager.LayoutParams(d(211),d(42),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;params.y=d(35);
        try{manager.addView(display,params);island=display;}catch(Exception e){stopSelf();return;}
        display.setOnClickListener(v->{
            params.width=params.width==d(211)?d(260):d(211);
            try{manager.updateViewLayout(display,params);}catch(Exception ignored){}
        });
        handler.post(tick);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){return START_STICKY;}
    @Override public void onDestroy(){
        handler.removeCallbacks(tick);
        if(island!=null&&manager!=null)try{manager.removeView(island);}catch(Exception ignored){}
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent){return null;}
}
