package com.glass26.app;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public final class NotificationCenterActivity extends Activity {
    private int d(float v){return GlassVisual.dp(this,v);}
    private TextView text(String s,int size,boolean strong){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size*AppearancePrefs.fontScale(this));
        t.setTextColor(0xFFFAFFFF);t.setGravity(Gravity.CENTER_VERTICAL);
        if(strong)t.setTypeface(null,1);
        return t;
    }
    private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setFlags(1024,1024);render();}
    @Override public void onResume(){super.onResume();render();}
    private void render(){
        getWindow().setNavigationBarColor(0xFF08294E);
        FrameLayout root=new FrameLayout(this);
        root.addView(new GlassVisual.Wallpaper(this),new FrameLayout.LayoutParams(-1,-1));
        ScrollView sc=new ScrollView(this);sc.setVerticalScrollBarEnabled(false);
        root.addView(sc,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout content=col();content.setPadding(d(18),d(17),d(18),d(36));sc.addView(content);
        LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=text("‹",33,false);back.setGravity(Gravity.CENTER);
        top.addView(back,new LinearLayout.LayoutParams(d(38),d(48)));
        back.setOnClickListener(v->finish());
        TextView island=text("●  Glass26",13,true);island.setGravity(Gravity.CENTER);
        island.setBackground(GlassVisual.island(this,35));
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(0,d(40),1);ip.setMargins(d(22),0,d(22),0);
        top.addView(island,ip);
        TextView menu=text("•••",21,true);menu.setGravity(Gravity.CENTER);
        top.addView(menu,new LinearLayout.LayoutParams(d(40),d(45)));
        menu.setOnClickListener(v->openAccess());
        content.addView(top);
        TextView heading=text("Notificações",33,true);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,d(88));
        content.addView(heading,hp);
        String date=new SimpleDateFormat("EEEE, d 'de' MMMM",new Locale("pt","BR")).format(new Date());
        TextView sub=text(date,15,false);content.addView(sub,new LinearLayout.LayoutParams(-1,d(39)));
        View gap=new View(this);content.addView(gap,new LinearLayout.LayoutParams(1,d(25)));
        String enabled=Settings.Secure.getString(getContentResolver(),"enabled_notification_listeners");
        boolean granted=enabled!=null&&enabled.contains(getPackageName());
        if(!granted){
            LinearLayout card=col();card.setPadding(d(18),d(18),d(18),d(18));
            card.setBackground(GlassVisual.panel(this,24,true));
            TextView title=text("◉  Ativar notificações",19,true);
            card.addView(title,new LinearLayout.LayoutParams(-1,d(47)));
            TextView msg=text("Autorize o acesso nas configurações para mostrar seus avisos reais em cartões Glass26. O conteúdo fica somente na memória do aplicativo.",14,false);
            msg.setMaxLines(5);card.addView(msg,new LinearLayout.LayoutParams(-1,d(102)));
            TextView act=text("Abrir configurações  ›",15,true);
            act.setGravity(Gravity.CENTER);act.setBackground(GlassVisual.panel(this,16,false));
            act.setTextColor(0xFF14375F);
            act.setOnClickListener(v->openAccess());
            card.addView(act,new LinearLayout.LayoutParams(-1,d(48)));
            content.addView(card);
        }else{
            List<GlassNotificationListener.Card> cards=GlassNotificationListener.snapshot();
            if(cards.isEmpty()){
                LinearLayout empty=col();empty.setGravity(Gravity.CENTER);
                empty.setPadding(d(16),d(34),d(16),d(34));
                empty.setBackground(GlassVisual.panel(this,25,true));
                TextView icon=text("✓",37,true);icon.setGravity(Gravity.CENTER);
                empty.addView(icon,new LinearLayout.LayoutParams(-1,d(65)));
                TextView title=text("Tudo em dia",21,true);title.setGravity(Gravity.CENTER);
                empty.addView(title,new LinearLayout.LayoutParams(-1,d(40)));
                TextView desc=text("Nenhuma notificação no momento",13,false);
                desc.setGravity(Gravity.CENTER);
                empty.addView(desc,new LinearLayout.LayoutParams(-1,d(37)));
                content.addView(empty);
            }
            for(GlassNotificationListener.Card item:cards){
                LinearLayout card=col();card.setPadding(d(17),d(13),d(17),d(14));
                card.setBackground(GlassVisual.panel(this,22,false));
                LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.bottomMargin=d(10);
                content.addView(card,cp);
                TextView app=text(item.app+"   ·   "+new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date(item.postedAt)),12,true);
                app.setTextColor(0xFF285377);card.addView(app);
                TextView title=text(item.title.isEmpty()?item.app:item.title,16,true);
                title.setTextColor(0xFF17385A);title.setMaxLines(2);
                title.setEllipsize(TextUtils.TruncateAt.END);card.addView(title);
                if(!item.message.isEmpty()){
                    TextView msg=text(item.message,14,false);msg.setTextColor(0xFF244663);
                    msg.setMaxLines(4);msg.setEllipsize(TextUtils.TruncateAt.END);
                    card.addView(msg);
                }
            }
        }
        TextView footer=text("As notificações originais do Android e da Samsung permanecem disponíveis.",12,false);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(d(6),d(24),d(6),d(12));
        content.addView(footer);
        setContentView(root);
    }
    private void openAccess(){
        new AlertDialog.Builder(this).setTitle("Privacidade das notificações")
            .setMessage("Esta permissão é opcional. O Android permitirá ao Glass26 ler o conteúdo de suas notificações para apresentá-las nesta central, inclusive mensagens potencialmente sensíveis. O app não as envia para a Internet e você pode revogar o acesso nas configurações.")
            .setPositiveButton("Abrir configurações",(dialog,which)->{
                try{startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));}
                catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}
            }).setNegativeButton("Cancelar",null).show();
    }
}
