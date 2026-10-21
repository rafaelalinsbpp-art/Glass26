package com.glass26.app;

import android.app.Activity;
import android.content.*;
import android.graphics.drawable.*;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public final class ControlCenterActivity extends Activity {
    private LinearLayout body;
    private AudioManager audio;
    private int d(float n){return GlassVisual.dp(this,n);}
    private TextView label(String s,float sp,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(sp*AppearancePrefs.fontScale(this));
        t.setTextColor(0xFFF8FDFF);t.setGravity(Gravity.CENTER_VERTICAL);
        if(bold)t.setTypeface(null,1);return t;
    }
    private LinearLayout row(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);return r;}
    private LinearLayout col(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);return c;}
    private void gap(LinearLayout l,int n){l.addView(new View(this),new LinearLayout.LayoutParams(1,d(n)));}
    @Override public void onCreate(Bundle b){super.onCreate(b);audio=(AudioManager)getSystemService(AUDIO_SERVICE);
        getWindow().setFlags(1024,1024);render();}
    private void render(){
        getWindow().setNavigationBarColor(0xFF09264D);
        FrameLayout root=new FrameLayout(this);
        root.addView(new GlassVisual.Wallpaper(this),new FrameLayout.LayoutParams(-1,-1));
        View tint=new View(this);tint.setBackgroundColor(0x5202193D);
        root.addView(tint,new FrameLayout.LayoutParams(-1,-1));
        ScrollView sc=new ScrollView(this);sc.setVerticalScrollBarEnabled(false);
        root.addView(sc,new FrameLayout.LayoutParams(-1,-1));
        body=col();body.setPadding(d(17),d(30),d(17),d(32));sc.addView(body);
        LinearLayout header=row();header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=label("Central de Controle",26,true);
        header.addView(title,new LinearLayout.LayoutParams(0,d(74),1));
        TextView close=label("×",31,false);close.setGravity(Gravity.CENTER);
        close.setBackground(GlassVisual.panel(this,22,true));
        header.addView(close,new LinearLayout.LayoutParams(d(44),d(44)));
        close.setOnClickListener(v->finish());body.addView(header);
        gap(body,10);
        LinearLayout first=row();first.setGravity(Gravity.TOP);
        LinearLayout connections=col();
        connections.setPadding(d(10),d(13),d(10),d(8));
        connections.setBackground(GlassVisual.panel(this,24,true));
        LinearLayout buttons1=row(),buttons2=row();
        round(buttons1,"✈", "Modo avião",Settings.ACTION_WIRELESS_SETTINGS,true);
        round(buttons1,"◉", "Wi-Fi",Settings.ACTION_WIFI_SETTINGS,true);
        round(buttons2,"ᛒ", "Bluetooth",Settings.ACTION_BLUETOOTH_SETTINGS,true);
        round(buttons2,"⌁", "Conexões",Settings.ACTION_WIRELESS_SETTINGS,false);
        connections.addView(buttons1,new LinearLayout.LayoutParams(-1,d(53)));
        connections.addView(buttons2,new LinearLayout.LayoutParams(-1,d(53)));
        TextView connTitle=label("Conectividade",12,true);connTitle.setGravity(Gravity.CENTER);
        connections.addView(connTitle,new LinearLayout.LayoutParams(-1,d(33)));
        LinearLayout.LayoutParams cn=new LinearLayout.LayoutParams(0,d(162),1);cn.rightMargin=d(6);
        first.addView(connections,cn);
        LinearLayout focus=col();
        focus.setPadding(d(17),d(17),d(13),d(12));
        focus.setBackground(GlassVisual.panel(this,24,true));
        TextView moon=label("☾",28,false);
        focus.addView(moon,new LinearLayout.LayoutParams(-1,d(45)));
        TextView foc=label("Foco",18,true);focus.addView(foc,new LinearLayout.LayoutParams(-1,d(46)));
        TextView explain=label("Não perturbe",12,false);
        focus.addView(explain,new LinearLayout.LayoutParams(-1,d(34)));
        focus.setOnClickListener(v->settings(Settings.ACTION_SOUND_SETTINGS));
        LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(0,d(162),1);fp.leftMargin=d(6);
        first.addView(focus,fp);body.addView(first);
        gap(body,13);
        LinearLayout levels=row();
        LinearLayout.LayoutParams lp1=new LinearLayout.LayoutParams(0,d(185),1);lp1.rightMargin=d(6);
        levels.addView(levelTile("☼","Brilho",true),lp1);
        LinearLayout.LayoutParams lp2=new LinearLayout.LayoutParams(0,d(185),1);lp2.leftMargin=d(6);
        levels.addView(levelTile("◖))","Volume",false),lp2);
        body.addView(levels);
        gap(body,13);
        mediaWidget();
        gap(body,12);
        LinearLayout bottom=row();
        quick(bottom,"⌨",()->settings(Settings.ACTION_INPUT_METHOD_SETTINGS));
        quick(bottom,"◉",()->startActivity(new Intent(this,NotificationCenterActivity.class)));
        quick(bottom,"Aa",()->startActivity(new Intent(this,AppearanceActivity.class)));
        quick(bottom,"⚙",()->settings(Settings.ACTION_SETTINGS));
        body.addView(bottom,new LinearLayout.LayoutParams(-1,d(58)));
        gap(body,14);
        TextView privacy=label("Atalhos de sistema: o Android mantém o controle das permissões.",11,false);
        privacy.setGravity(Gravity.CENTER);
        privacy.setAlpha(.86f);
        body.addView(privacy,new LinearLayout.LayoutParams(-1,d(45)));
        setContentView(root);
    }
    private void round(LinearLayout parent,String symbol,String purpose,String setting,boolean accent){
        TextView circle=label(symbol,20,true);circle.setGravity(Gravity.CENTER);
        circle.setBackground(GlassVisual.accent(this,22,
            accent?0xFF1B9DFA:0xFF8094AD,accent?0xFF2B78D8:0xFF7B92AA));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,d(45),1);
        p.setMargins(d(5),d(2),d(5),d(4));
        parent.addView(circle,p);circle.setContentDescription(purpose);
        circle.setOnClickListener(v->settings(setting));
    }
    private View levelTile(String icon,String title,boolean brightness){
        FrameLayout frame=new FrameLayout(this);
        frame.setBackground(GlassVisual.panel(this,26,true));
        final int[] level={brightness?62:(audio==null?50:(int)(audio.getStreamVolume(AudioManager.STREAM_MUSIC)*100f/
            Math.max(1,audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC))))};
        View fill=new View(this);
        fill.setBackground(GlassVisual.accent(this,24,0xDEF9FFFF,0xCCE8F8FF));
        FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,d(185)*level[0]/100,Gravity.BOTTOM);
        p.setMargins(d(1),0,d(1),d(1));frame.addView(fill,p);
        TextView symbol=label(icon,30,true);symbol.setTextColor(0xFF113D67);
        symbol.setGravity(Gravity.CENTER);
        frame.addView(symbol,new FrameLayout.LayoutParams(-1,-1));
        TextView header=label(title,11,true);header.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        FrameLayout.LayoutParams h=new FrameLayout.LayoutParams(-1,d(36),Gravity.TOP);
        h.topMargin=d(10);frame.addView(header,h);
        frame.setContentDescription(title+" — deslize para ajustar");
        frame.setOnTouchListener((view,event)->{
            if(event.getAction()==MotionEvent.ACTION_DOWN||event.getAction()==MotionEvent.ACTION_MOVE){
                int value=Math.max(4,Math.min(100,100-Math.round(100*event.getY()/Math.max(1,view.getHeight()))));
                level[0]=value;
                FrameLayout.LayoutParams next=(FrameLayout.LayoutParams)fill.getLayoutParams();
                next.height=Math.max(1,view.getHeight()*value/100);
                fill.setLayoutParams(next);
                if(brightness){
                    WindowManager.LayoutParams attributes=getWindow().getAttributes();
                    attributes.screenBrightness=Math.max(.04f,value/100f);
                    getWindow().setAttributes(attributes);
                }else if(audio!=null){
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC,
                        Math.round(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*value/100f),0);
                }
                return true;
            }
            return event.getAction()==MotionEvent.ACTION_UP;
        });
        return frame;
    }
    private void mediaWidget(){
        LinearLayout media=col();media.setPadding(d(16),d(13),d(16),d(13));
        media.setBackground(GlassVisual.panel(this,25,true));
        LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);
        TextView album=label("♫",31,true);album.setGravity(Gravity.CENTER);
        album.setBackground(GlassVisual.accent(this,14,0xFFF4A18E,0xFFD847A3));
        top.addView(album,new LinearLayout.LayoutParams(d(65),d(65)));
        LinearLayout info=col();info.setPadding(d(12),0,0,0);
        TextView music=label("Reprodutor",17,true);
        TextView artist=label("Sem música em reprodução",12,false);
        info.addView(music,new LinearLayout.LayoutParams(-1,d(32)));
        info.addView(artist,new LinearLayout.LayoutParams(-1,d(30)));
        top.addView(info,new LinearLayout.LayoutParams(0,d(65),1));media.addView(top);
        gap(media,9);
        View progress=new View(this);progress.setBackground(GlassVisual.accent(this,6,0xFFC3E4ED,0xFFE7FFFF));
        media.addView(progress,new LinearLayout.LayoutParams(-1,d(4)));
        gap(media,8);
        TextView controls=label("|◀             ▶❚❚             ▶|",23,true);
        controls.setGravity(Gravity.CENTER);controls.setContentDescription("Reproduzir ou pausar música");
        controls.setOnClickListener(v->{
            if(audio!=null){
                audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
                audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
            }
        });
        media.addView(controls,new LinearLayout.LayoutParams(-1,d(37)));
        body.addView(media,new LinearLayout.LayoutParams(-1,d(159)));
    }
    private void quick(LinearLayout row,String value,Runnable action){
        TextView t=label(value,22,true);t.setGravity(Gravity.CENTER);
        t.setBackground(GlassVisual.panel(this,16,true));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);p.setMargins(d(4),0,d(4),0);
        row.addView(t,p);t.setOnClickListener(v->action.run());
    }
    private void settings(String action){
        try{startActivity(new Intent(action));}
        catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}
    }
}
