package com.glass26.app;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.*;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import java.util.*;

public final class IslandService extends Service {
    private WindowManager windowManager;
    private WindowManager.LayoutParams layout;
    private LinearLayout capsule;
    private TextView headline, detail;
    private boolean expanded=false;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private String lastInfo="";
    private int dp(float n){return GlassVisual.dp(this,n);}
    private static final class State {
        String title="",subtitle="";
        boolean active=false;
    }
    private final Runnable refresh=new Runnable(){
        @Override public void run(){
            if(capsule!=null)updateIsland();
            handler.postDelayed(this,2500);
        }
    };
    @Override public void onCreate(){
        super.onCreate();
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel("island","Ilha Dinâmica Glass26",NotificationManager.IMPORTANCE_LOW));
        Intent open=new Intent(this,HomeActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pending=PendingIntent.getActivity(this,261,open,
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification notice=new Notification.Builder(this,"island")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Glass26 — Ilha Dinâmica")
            .setContentText("Mostra atividade de mídia e notificações quando autorizadas")
            .setContentIntent(pending)
            .setOngoing(true).build();
        try{startForeground(26,notice);}catch(Exception e){stopSelf();return;}
        if(!Settings.canDrawOverlays(this)){stopSelf();return;}
        windowManager=(WindowManager)getSystemService(WINDOW_SERVICE);
        capsule=new LinearLayout(this);capsule.setGravity(Gravity.CENTER);
        capsule.setOrientation(LinearLayout.VERTICAL);
        capsule.setPadding(dp(12),dp(2),dp(12),dp(2));
        GradientDrawable background=new GradientDrawable();
        background.setColor(0xF9090B12);background.setCornerRadius(dp(35));
        background.setStroke(Math.max(1,dp(1)),0x607789A2);
        capsule.setBackground(background);
        capsule.setElevation(dp(9));
        headline=new TextView(this);headline.setTextColor(0xFFF6F9FF);
        headline.setTextSize(12);headline.setTypeface(null,1);
        headline.setGravity(Gravity.CENTER);headline.setSingleLine(true);
        headline.setEllipsize(TextUtils.TruncateAt.END);
        detail=new TextView(this);detail.setTextColor(0xFFC6D9E7);
        detail.setTextSize(11);detail.setGravity(Gravity.CENTER);
        detail.setMaxLines(2);detail.setEllipsize(TextUtils.TruncateAt.END);
        capsule.addView(headline,new LinearLayout.LayoutParams(-1,-2));
        capsule.addView(detail,new LinearLayout.LayoutParams(-1,-2));
        detail.setVisibility(View.GONE);
        layout=new WindowManager.LayoutParams(dp(72),dp(28),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);
        layout.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;
        layout.y=dp(5);
        try{windowManager.addView(capsule,layout);}
        catch(Exception e){stopSelf();return;}
        capsule.setOnClickListener(v->{
            if(!expanded){expanded=true;updateIsland();}
            else{
                State info=state();
                if(!info.active)openNotificationCenter();
                else{expanded=false;updateIsland();}
            }
        });
        capsule.setOnLongClickListener(v->{openNotificationCenter();return true;});
        handler.post(refresh);
    }
    private void openNotificationCenter(){
        try {
            Intent i=new Intent(this,NotificationCenterActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }catch(Exception ignored){}
    }
    private State state(){
        State out=new State();
        // Active Android media session: actual track names, not invented samples.
        try{
            MediaSessionManager sessions=(MediaSessionManager)getSystemService(MEDIA_SESSION_SERVICE);
            List<MediaController> controllers=sessions.getActiveSessions(
                new ComponentName(this,GlassNotificationListener.class));
            for(MediaController controller:controllers){
                PlaybackState play=controller.getPlaybackState();
                if(play==null||play.getState()!=PlaybackState.STATE_PLAYING)continue;
                MediaMetadata metadata=controller.getMetadata();
                CharSequence song=metadata==null?null:metadata.getText(MediaMetadata.METADATA_KEY_TITLE);
                CharSequence artist=metadata==null?null:metadata.getText(MediaMetadata.METADATA_KEY_ARTIST);
                out.title="♫ "+(song==null?"Música em reprodução":song.toString());
                out.subtitle=artist==null?"Toque e segure para ver notificações":artist.toString();
                out.active=true;return out;
            }
        }catch(Exception ignored){}
        // NotificationListener is optional and cannot read notifications without consent.
        long now=System.currentTimeMillis();
        for(GlassNotificationListener.Card card:GlassNotificationListener.snapshot()){
            if(card==null||"Glass26".equals(card.app))continue;
            if(now-card.postedAt>120000||card.postedAt>now+10000)continue;
            out.title=card.app;
            out.subtitle=card.title.isEmpty()?card.message:card.title;
            out.active=true;return out;
        }
        out.title="●";
        out.subtitle="Sem atividade no momento. Toque para abrir notificações.";
        return out;
    }
    private void updateIsland(){
        State info=state();
        if(capsule==null||windowManager==null)return;
        String title=info.title;
        String subtitle=info.subtitle;
        String key=title+"|"+subtitle+"|"+expanded;
        if(key.equals(lastInfo))return;
        lastInfo=key;
        headline.setText(title);
        detail.setText(subtitle);
        detail.setVisibility(expanded?View.VISIBLE:View.GONE);
        layout.width=dp(expanded?Math.min(310,Math.round(
            getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density-38)):info.active?232:70);
        layout.height=dp(expanded?72:info.active?40:29);
        try{windowManager.updateViewLayout(capsule,layout);}catch(Exception ignored){}
        capsule.setContentDescription(info.active?
            "Ilha Dinâmica: "+title+". Toque para expandir.": 
            "Ilha Dinâmica sem atividade. Toque para opções de notificações.");
    }
    @Override public int onStartCommand(Intent i,int f,int id){return START_STICKY;}
    @Override public void onDestroy(){
        handler.removeCallbacks(refresh);
        if(capsule!=null&&windowManager!=null){
            try{windowManager.removeView(capsule);}catch(Exception ignored){}
        }
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent i){return null;}
}
