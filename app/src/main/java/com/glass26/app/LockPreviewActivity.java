package com.glass26.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public final class LockPreviewActivity extends Activity {
    private int d(float px){return GlassVisual.dp(this,px);}
    private boolean dark(){return AppearancePrefs.isDark(this);}
    private TextView label(String txt,float sp,boolean bold){
        TextView v=new TextView(this);v.setText(txt);
        v.setTextSize(sp*AppearancePrefs.fontScale(this));v.setTextColor(0xFFF9FCFF);
        if(bold)v.setTypeface(android.graphics.Typeface.create("sans-serif-medium",1));
        v.setGravity(Gravity.CENTER);
        v.setShadowLayer(d(3),0,d(1),0x72000826);
        return v;
    }
    private LinearLayout vertical(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
    private LinearLayout horizontal(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.HORIZONTAL);return v;}
    private void space(LinearLayout v,float px){v.addView(new View(this),new LinearLayout.LayoutParams(1,d(px)));}
    private String now(String f){return new SimpleDateFormat(f,new Locale("pt","BR")).format(new Date());}
    @Override public void onCreate(Bundle s){super.onCreate(s);render();}
    @Override public void onResume(){super.onResume();render();}
    private void render(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setNavigationBarColor(0xFF092749);
        FrameLayout root=new FrameLayout(this);
        root.addView(new GlassVisual.Wallpaper(this),new FrameLayout.LayoutParams(-1,-1));
        if(dark()){View overlay=new View(this);overlay.setBackgroundColor(0x3E071431);
            root.addView(overlay,new FrameLayout.LayoutParams(-1,-1));}
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        root.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout page=vertical();page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(d(22),d(22),d(22),d(100));scroll.addView(page);
        LinearLayout status=horizontal();status.setGravity(Gravity.CENTER_VERTICAL);
        TextView left=label(now("HH:mm"),14,true);left.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        status.addView(left,new LinearLayout.LayoutParams(0,d(34),1));
        TextView island=label("●",16,true);
        island.setBackground(GlassVisual.island(this,28));
        status.addView(island,new LinearLayout.LayoutParams(d(125),d(33)));
        TextView right=label("▰  ◉",15,true);right.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        status.addView(right,new LinearLayout.LayoutParams(0,d(34),1));
        page.addView(status,new LinearLayout.LayoutParams(-1,d(41)));
        space(page,54);
        TextView date=label(now("EEEE, d 'de' MMMM"),17,true);
        page.addView(date,new LinearLayout.LayoutParams(-1,d(44)));
        TextView time=label(now("HH:mm"),82,true);
        time.setLetterSpacing(-.065f);
        page.addView(time,new LinearLayout.LayoutParams(-1,d(113)));
        TextView lock=label("⌑",18,true);
        lock.setAlpha(.88f);page.addView(lock,new LinearLayout.LayoutParams(-1,d(32)));
        space(page,54);
        TextView heading=label("NOTIFICAÇÕES",10,true);heading.setLetterSpacing(.16f);
        heading.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        page.addView(heading,new LinearLayout.LayoutParams(-1,d(32)));
        addNotification(page);
        space(page,18);
        TextView hint=label("Prévia visual da tela de bloqueio Glass26",12,false);
        page.addView(hint,new LinearLayout.LayoutParams(-1,d(36)));
        TextView privacy=label("Sua tela de bloqueio real, PIN e biometria permanecem protegidos pela Samsung.",11,false);
        privacy.setMaxLines(2);page.addView(privacy,new LinearLayout.LayoutParams(-1,d(54)));
        space(page,20);
        LinearLayout actions=horizontal();actions.setGravity(Gravity.CENTER_VERTICAL);
        TextView prefs=label("◐   Personalizar",15,true);
        prefs.setBackground(GlassVisual.panel(this,24,dark()));
        actions.addView(prefs,new LinearLayout.LayoutParams(0,d(53),1));
        prefs.setOnClickListener(v->startActivity(new Intent(this,AppearanceActivity.class)));
        TextView back=label("‹   Voltar",15,true);
        back.setBackground(GlassVisual.panel(this,24,dark()));
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,d(53),1);
        bp.leftMargin=d(12);actions.addView(back,bp);back.setOnClickListener(v->finish());
        page.addView(actions);
        setContentView(root);
    }
    private void addNotification(LinearLayout page){
        List<GlassNotificationListener.Card> cards=GlassNotificationListener.snapshot();
        boolean sample=cards.isEmpty();
        String app=sample?"Exemplo de notificação":cards.get(0).app;
        String title=sample?"Glass26":cards.get(0).title;
        String message=sample?"Este cartão demonstra o design da tela de bloqueio.":cards.get(0).message;
        LinearLayout c=vertical();c.setPadding(d(18),d(15),d(18),d(16));
        c.setBackground(GlassVisual.panel(this,23,dark()));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        page.addView(c,p);
        TextView from=label("◉   "+app,12,true);from.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        c.addView(from,new LinearLayout.LayoutParams(-1,d(24)));
        TextView t=label(title,15,true);t.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        c.addView(t,new LinearLayout.LayoutParams(-1,d(27)));
        TextView body=label(message,13,false);body.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        body.setMaxLines(3);c.addView(body,new LinearLayout.LayoutParams(-1,-2));
        c.setOnClickListener(v->startActivity(new Intent(this,NotificationCenterActivity.class)));
    }
}
