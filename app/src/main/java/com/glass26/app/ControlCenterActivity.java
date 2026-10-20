package com.glass26.app;

import android.app.Activity;
import android.content.*;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public final class ControlCenterActivity extends Activity {
    private LinearLayout pane;
    private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private TextView text(String s,int size,boolean bold){
        TextView t=new TextView(this);t.setText(s);
        t.setTextColor(AppearancePrefs.foreground(this));
        t.setTextSize(size*AppearancePrefs.fontScale(this));
        t.setGravity(Gravity.CENTER_VERTICAL);if(bold)t.setTypeface(null,1);return t;
    }
    private void row(String label1,String action1,String label2,String action2){
        LinearLayout line=new LinearLayout(this);line.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(98));p.bottomMargin=dp(12);
        pane.addView(line,p);
        tile(line,label1,action1);
        tile(line,label2,action2);
    }
    private void tile(LinearLayout row,String label,String action){
        TextView t=text(label,17,true);t.setGravity(Gravity.CENTER);
        t.setBackground(AppearancePrefs.glass(this,23));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);
        p.setMargins(dp(5),0,dp(5),0);row.addView(t,p);
        t.setOnClickListener(v->{
            try{startActivity(new Intent(action));}
            catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}
        });
    }
    @Override public void onCreate(Bundle b){super.onCreate(b);render();}
    @Override public void onResume(){super.onResume();render();}
    private void render(){
        boolean dark=AppearancePrefs.isDark(this);
        getWindow().setStatusBarColor(dark?0xFF111A32:0xFFE3ECF9);
        getWindow().setNavigationBarColor(dark?0xFF111A32:0xFFE3ECF9);
        getWindow().getDecorView().setSystemUiVisibility(dark?0:
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        ScrollView scroll=new ScrollView(this);
        scroll.setBackground(AppearancePrefs.gradient(this));
        pane=new LinearLayout(this);pane.setOrientation(LinearLayout.VERTICAL);
        pane.setPadding(dp(17),dp(26),dp(17),dp(20));
        scroll.addView(pane);
        TextView title=text("Central de controle",28,true);
        pane.addView(title,new LinearLayout.LayoutParams(-1,dp(65)));
        TextView time=text(new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date())+
            "  •  Glass26",17,false);
        pane.addView(time,new LinearLayout.LayoutParams(-1,dp(38)));
        row("◉\nWi-Fi",Settings.ACTION_WIFI_SETTINGS,"ᛒ\nBluetooth",Settings.ACTION_BLUETOOTH_SETTINGS);
        row("✈\nRede",Settings.ACTION_WIRELESS_SETTINGS,"☀\nTela",Settings.ACTION_DISPLAY_SETTINGS);
        TextView label=text("BRILHO DESTE APLICATIVO",12,true);
        pane.addView(label,new LinearLayout.LayoutParams(-1,dp(44)));
        SeekBar brightness=new SeekBar(this);
        brightness.setMax(100);brightness.setProgress(55);
        pane.addView(brightness,new LinearLayout.LayoutParams(-1,dp(54)));
        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onStartTrackingTouch(SeekBar s){}
            public void onStopTrackingTouch(SeekBar s){}
            public void onProgressChanged(SeekBar s,int progress,boolean fromUser){
                if(!fromUser)return;
                WindowManager.LayoutParams window=getWindow().getAttributes();
                window.screenBrightness=Math.max(.05f,progress/100f);
                getWindow().setAttributes(window);
            }
        });
        TextView note=text("O controle acima altera apenas o brilho enquanto a central está aberta. Para o brilho geral, use a configuração oficial da Samsung.",12,false);
        pane.addView(note,new LinearLayout.LayoutParams(-1,dp(84)));
        TextView volume=text("VOLUME DE MÍDIA",12,true);pane.addView(volume,new LinearLayout.LayoutParams(-1,dp(34)));
        AudioManager audio=(AudioManager)getSystemService(AUDIO_SERVICE);
        SeekBar level=new SeekBar(this);
        int max=audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        level.setMax(Math.max(1,max));
        level.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
        pane.addView(level,new LinearLayout.LayoutParams(-1,dp(55)));
        level.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onStartTrackingTouch(SeekBar s){}
            public void onStopTrackingTouch(SeekBar s){}
            public void onProgressChanged(SeekBar s,int progress,boolean fromUser){
                if(fromUser)audio.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
            }
        });
        row("⚙\nAjustes",Settings.ACTION_SETTINGS,"⌨\nTeclados",Settings.ACTION_INPUT_METHOD_SETTINGS);
        TextView back=text("‹  Voltar ao Glass26",16,true);
        back.setGravity(Gravity.CENTER);
        back.setBackground(AppearancePrefs.glass(this,22));
        back.setOnClickListener(v->finish());
        pane.addView(back,new LinearLayout.LayoutParams(-1,dp(60)));
        setContentView(scroll);
    }
}
