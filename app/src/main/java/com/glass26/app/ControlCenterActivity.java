package com.glass26.app;

import android.app.*;
import android.content.*;
import android.graphics.drawable.*;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class ControlCenterActivity extends Activity {
    private int d(float n){return GlassVisual.dp(this,n);}
    private boolean dark(){return AppearancePrefs.isDark(this);}
    private TextView text(String label,float size,boolean strong,int color){
        TextView t=new TextView(this);
        t.setText(label);
        t.setTextSize(size*AppearancePrefs.fontScale(this));
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if(strong)t.setTypeface(android.graphics.Typeface.create("sans-serif-medium",1));
        return t;
    }
    private LinearLayout column(){
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);return c;
    }
    private LinearLayout row(){
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.HORIZONTAL);return c;
    }
    private void gap(LinearLayout c,float amount){
        c.addView(new View(this),new LinearLayout.LayoutParams(d(1),d(amount)));
    }
    private void settings(String action){
        try{startActivity(new Intent(action));}
        catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}
    }
    private int white(){return 0xFFF8FCFF;}
    private TextView circle(String label,int color){
        TextView v=text(label,20,true,white());
        v.setGravity(Gravity.CENTER);
        v.setBackground(GlassVisual.accent(this,35,color,color));
        return v;
    }
    private View connectivity(){
        LinearLayout group=column();
        group.setPadding(d(13),d(13),d(13),d(10));
        group.setBackground(GlassVisual.panel(this,25,dark()));
        LinearLayout first=row(),second=row();
        addConnectivityButton(first,"✈",0xFF208EFF,Settings.ACTION_WIRELESS_SETTINGS);
        addConnectivityButton(first,"◉",0xFF198DFA,Settings.ACTION_WIFI_SETTINGS);
        addConnectivityButton(second,"ᛒ",0xFF198DFA,Settings.ACTION_BLUETOOTH_SETTINGS);
        addConnectivityButton(second,"◴",0xFF7186A0,Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
        group.addView(first,new LinearLayout.LayoutParams(-1,0,1));
        group.addView(second,new LinearLayout.LayoutParams(-1,0,1));
        TextView label=text("Conectividade",12,true,white());label.setGravity(Gravity.CENTER);
        group.addView(label,new LinearLayout.LayoutParams(-1,d(24)));
        return group;
    }
    private void addConnectivityButton(LinearLayout row,String symbol,int color,String action){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);
        p.setMargins(d(2),d(2),d(2),d(2));
        FrameLayout cell=new FrameLayout(this);row.addView(cell,p);
        TextView circle=circle(symbol,color);
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(d(47),d(47),Gravity.CENTER);
        cell.addView(circle,cp);
        cell.setOnClickListener(v->settings(action));
        cell.setContentDescription("Abrir configurações: "+action);
    }
    private View focus(){
        LinearLayout tile=column();tile.setPadding(d(16),d(15),d(10),d(12));
        tile.setBackground(GlassVisual.panel(this,25,dark()));
        TextView moon=circle("☾",0xFF7F9DB9);
        tile.addView(moon,new LinearLayout.LayoutParams(d(52),d(52)));
        TextView title=text("Foco",19,true,white());
        LinearLayout.LayoutParams titlep=new LinearLayout.LayoutParams(-1,0,1);
        tile.addView(title,titlep);
        TextView note=text("Não Perturbe",13,true,white());
        tile.addView(note,new LinearLayout.LayoutParams(-1,d(24)));
        tile.setOnClickListener(v->settings(Settings.ACTION_SOUND_SETTINGS));
        return tile;
    }
    private View levelCard(boolean brightness){
        LinearLayout card=column();
        card.setBackground(GlassVisual.panel(this,26,dark()));
        card.setPadding(d(12),d(14),d(12),d(10));
        TextView caption=text(brightness?"Brilho":"Volume",14,true,white());
        caption.setGravity(Gravity.CENTER);
        card.addView(caption,new LinearLayout.LayoutParams(-1,d(27)));
        FrameLayout base=new FrameLayout(this);
        LinearLayout.LayoutParams inner=new LinearLayout.LayoutParams(-1,0,1);
        inner.setMargins(d(10),d(1),d(10),d(6));card.addView(base,inner);
        base.setBackground(GlassVisual.accent(this,18,0x627187A3,0x868B9CAD));
        View fill=new View(this);fill.setBackground(GlassVisual.accent(this,18,0xFFF1F6FC,0xFFCCD9E7));
        FrameLayout.LayoutParams filled=new FrameLayout.LayoutParams(-1,-1,Gravity.BOTTOM);
        filled.height=d(brightness?64:43);
        base.addView(fill,filled);
        TextView pict=text(brightness?"☼":"◖))",29,false,0xFF244467);
        pict.setGravity(Gravity.CENTER);
        base.addView(pict,new FrameLayout.LayoutParams(-1,-1));
        int max=brightness?100:((AudioManager)getSystemService(AUDIO_SERVICE))
            .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int initial=brightness?55:((AudioManager)getSystemService(AUDIO_SERVICE))
            .getStreamVolume(AudioManager.STREAM_MUSIC);
        SeekBar seek=new SeekBar(this);seek.setMax(Math.max(1,max));seek.setProgress(initial);
        card.addView(seek,new LinearLayout.LayoutParams(-1,d(42)));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onStartTrackingTouch(SeekBar s){}
            @Override public void onStopTrackingTouch(SeekBar s){}
            @Override public void onProgressChanged(SeekBar s,int value,boolean fromUser){
                if(!fromUser)return;
                FrameLayout.LayoutParams p=(FrameLayout.LayoutParams)fill.getLayoutParams();
                float fraction=(float)value/Math.max(1,max);
                p.height=Math.max(d(8),Math.round(base.getHeight()*fraction));
                base.updateViewLayout(fill,p);
                if(brightness){
                    WindowManager.LayoutParams a=getWindow().getAttributes();
                    a.screenBrightness=Math.max(.03f,fraction);getWindow().setAttributes(a);
                }else{
                    AudioManager audio=(AudioManager)getSystemService(AUDIO_SERVICE);
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC,value,0);
                }
            }
        });
        return card;
    }
    private View musicCard(){
        LinearLayout card=column();card.setPadding(d(16),d(13),d(16),d(11));
        card.setBackground(GlassVisual.panel(this,27,dark()));
        LinearLayout info=row();
        TextView artwork=text("♫",28,true,white());artwork.setGravity(Gravity.CENTER);
        artwork.setBackground(GlassVisual.accent(this,16,0xFFFFAD9F,0xFFB266BB));
        info.addView(artwork,new LinearLayout.LayoutParams(d(64),d(64)));
        LinearLayout labels=column();labels.setPadding(d(11),0,0,0);
        TextView name=text("Reprodutor de mídia",17,true,white());
        TextView sub=text("Abra seu aplicativo de música",12,false,0xFFE2EBFF);
        labels.addView(name,new LinearLayout.LayoutParams(-1,d(34)));
        labels.addView(sub,new LinearLayout.LayoutParams(-1,d(27)));
        info.addView(labels,new LinearLayout.LayoutParams(0,d(64),1));
        card.addView(info);
        gap(card,8);
        SeekBar slider=new SeekBar(this);slider.setEnabled(false);slider.setProgress(0);
        card.addView(slider,new LinearLayout.LayoutParams(-1,d(23)));
        LinearLayout controls=row();controls.setGravity(Gravity.CENTER);
        addMedia(controls,"❮❮",KeyEvent.KEYCODE_MEDIA_PREVIOUS);
        addMedia(controls,"▶  ❚❚",KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        addMedia(controls,"❯❯",KeyEvent.KEYCODE_MEDIA_NEXT);
        card.addView(controls,new LinearLayout.LayoutParams(-1,d(39)));
        return card;
    }
    private void addMedia(LinearLayout host,String label,int keycode){
        TextView t=text(label,20,true,white());t.setGravity(Gravity.CENTER);
        host.addView(t,new LinearLayout.LayoutParams(0,-1,1));
        t.setOnClickListener(v->{
            AudioManager a=(AudioManager)getSystemService(AUDIO_SERVICE);
            if(a!=null){
                a.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,keycode));
                a.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,keycode));
            }
        });
    }
    private View mini(String glyph,String label,Runnable action){
        LinearLayout panel=column();panel.setGravity(Gravity.CENTER);
        panel.setBackground(GlassVisual.panel(this,20,dark()));
        TextView icon=text(glyph,25,true,white());icon.setGravity(Gravity.CENTER);
        panel.addView(icon,new LinearLayout.LayoutParams(-1,d(40)));
        TextView name=text(label,10,true,white());name.setGravity(Gravity.CENTER);
        panel.addView(name,new LinearLayout.LayoutParams(-1,d(20)));
        panel.setOnClickListener(v->action.run());return panel;
    }
    @Override public void onCreate(Bundle state){super.onCreate(state);render();}
    @Override public void onResume(){super.onResume();render();}
    private void render(){
        getWindow().setStatusBarColor(0xFF10365D);
        getWindow().setNavigationBarColor(0xFF061D3F);
        FrameLayout root=new FrameLayout(this);
        root.addView(new GlassVisual.Wallpaper(this),new FrameLayout.LayoutParams(-1,-1));
        if(dark()){
            View veil=new View(this);veil.setBackgroundColor(0x62010A25);
            root.addView(veil,new FrameLayout.LayoutParams(-1,-1));
        }
        ScrollView sc=new ScrollView(this);
        sc.setFillViewport(true);sc.setVerticalScrollBarEnabled(false);
        root.addView(sc,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout page=column();page.setPadding(d(17),d(14),d(17),d(28));
        sc.addView(page);
        LinearLayout status=row();status.setGravity(Gravity.CENTER_VERTICAL);
        String current=new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date());
        TextView now=text(current,13,true,white());status.addView(now,new LinearLayout.LayoutParams(0,d(30),1));
        TextView island=text("●",19,true,white());island.setGravity(Gravity.CENTER);
        island.setBackground(GlassVisual.island(this,26));
        status.addView(island,new LinearLayout.LayoutParams(d(102),d(29)));
        TextView signals=text("▰  ◉",16,true,white());signals.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        status.addView(signals,new LinearLayout.LayoutParams(0,d(30),1));
        page.addView(status);
        gap(page,15);
        TextView heading=text("Central de Controle",27,true,white());
        page.addView(heading,new LinearLayout.LayoutParams(-1,d(58)));
        gap(page,9);
        LinearLayout top=row();
        LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,d(170),1);left.rightMargin=d(7);
        top.addView(connectivity(),left);
        LinearLayout.LayoutParams right=new LinearLayout.LayoutParams(0,d(170),1);right.leftMargin=d(7);
        top.addView(focus(),right);
        page.addView(top);
        gap(page,14);
        LinearLayout sliders=row();
        LinearLayout.LayoutParams one=new LinearLayout.LayoutParams(0,d(185),1);one.rightMargin=d(7);
        sliders.addView(levelCard(true),one);
        LinearLayout.LayoutParams two=new LinearLayout.LayoutParams(0,d(185),1);two.leftMargin=d(7);
        sliders.addView(levelCard(false),two);
        page.addView(sliders);
        gap(page,14);
        page.addView(musicCard(),new LinearLayout.LayoutParams(-1,d(168)));
        gap(page,14);
        LinearLayout tools=row();
        addMini(tools,"☼","Lanterna",()->settings(Settings.ACTION_SETTINGS));
        addMini(tools,"◉","Notificações",()->startActivity(new Intent(this,NotificationCenterActivity.class)));
        addMini(tools,"◴","Relógio",()->settings(Settings.ACTION_DATE_SETTINGS));
        addMini(tools,"Aa","Aparência",()->startActivity(new Intent(this,AppearanceActivity.class)));
        page.addView(tools,new LinearLayout.LayoutParams(-1,d(86)));
        gap(page,10);
        TextView back=text("‹  Voltar",13,true,white());back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v->finish());
        page.addView(back,new LinearLayout.LayoutParams(-1,d(42)));
        setContentView(root);
    }
    private void addMini(LinearLayout row,String icon,String title,Runnable action){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);
        p.setMargins(d(4),0,d(4),0);
        row.addView(mini(icon,title,action),p);
    }
}
