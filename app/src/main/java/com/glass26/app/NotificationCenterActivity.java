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
    private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private TextView text(String s,int size,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size*AppearancePrefs.fontScale(this));
        t.setTextColor(AppearancePrefs.foreground(this));t.setGravity(Gravity.CENTER_VERTICAL);
        if(bold)t.setTypeface(null,1);return t;
    }
    private void openAccess(){
        new AlertDialog.Builder(this).setTitle("Acesso às notificações")
            .setMessage("O Glass26 poderá ler o conteúdo das notificações para mostrá-las nesta tela. Esse conteúdo fica somente na memória do aparelho, sem envio para servidores. A permissão é opcional e pode ser revogada a qualquer momento.")
            .setPositiveButton("Abrir configurações",(d,w)->startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)))
            .setNegativeButton("Cancelar",null).show();
    }
    @Override protected void onCreate(Bundle b){super.onCreate(b);render();}
    @Override protected void onResume(){super.onResume();render();}
    private void render(){
        boolean dark=AppearancePrefs.isDark(this);
        getWindow().setStatusBarColor(dark?0xFF111B30:0xFFE3ECF9);
        getWindow().setNavigationBarColor(dark?0xFF111B30:0xFFE3ECF9);
        getWindow().getDecorView().setSystemUiVisibility(dark?0:
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        ScrollView sc=new ScrollView(this);sc.setBackground(AppearancePrefs.gradient(this));
        LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(17),dp(35),dp(17),dp(20));sc.addView(content);
        TextView header=text("Notificações",33,true);
        content.addView(header,new LinearLayout.LayoutParams(-1,dp(65)));
        TextView sub=text(new SimpleDateFormat("EEEE, d 'de' MMMM",new Locale("pt","BR")).format(new Date()),14,false);
        content.addView(sub,new LinearLayout.LayoutParams(-1,dp(40)));
        String enabled=Settings.Secure.getString(getContentResolver(),"enabled_notification_listeners");
        boolean granted=enabled!=null&&enabled.contains(getPackageName());
        if(!granted){
            TextView warning=text("As notificações estão desativadas. Autorize o acesso opcional para exibi-las nesta central.",15,false);
            warning.setPadding(dp(16),dp(12),dp(16),dp(12));
            warning.setBackground(AppearancePrefs.glass(this,18));
            content.addView(warning,new LinearLayout.LayoutParams(-1,dp(115)));
            TextView activate=button("Autorizar notificações",()->openAccess());
            content.addView(activate,new LinearLayout.LayoutParams(-1,dp(55)));
        }else{
            List<GlassNotificationListener.Card> cards=GlassNotificationListener.snapshot();
            if(cards.isEmpty()){
                TextView empty=text("Sem notificações no momento. As notificações do sistema continuam funcionando normalmente.",16,false);
                empty.setPadding(dp(20),dp(20),dp(20),dp(20));
                empty.setBackground(AppearancePrefs.glass(this,22));
                content.addView(empty,new LinearLayout.LayoutParams(-1,dp(130)));
            }
            for(GlassNotificationListener.Card item:cards){
                LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(dp(18),dp(14),dp(17),dp(14));
                card.setBackground(AppearancePrefs.glass(this,24));
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
                p.bottomMargin=dp(10);content.addView(card,p);
                TextView app=text(item.app+"   ·   "+
                    new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date(item.postedAt)),12,true);
                card.addView(app);
                TextView title=text(item.title.isEmpty()?item.app:item.title,16,true);
                title.setMaxLines(2);title.setEllipsize(TextUtils.TruncateAt.END);card.addView(title);
                if(!item.message.isEmpty()){
                    TextView body=text(item.message,14,false);body.setMaxLines(3);
                    body.setEllipsize(TextUtils.TruncateAt.END);card.addView(body);
                }
            }
            TextView privacy=text("Os avisos aparecem somente nesta central do Glass26. A cortina de notificações original da Samsung permanece intacta.",12,false);
            privacy.setPadding(0,dp(10),0,dp(20));content.addView(privacy);
        }
        TextView back=button("‹  Voltar",()->finish());
        content.addView(back,new LinearLayout.LayoutParams(-1,dp(54)));
        setContentView(sc);
    }
    private TextView button(String s,Runnable action){
        TextView t=text(s,16,true);t.setGravity(Gravity.CENTER);
        t.setBackground(AppearancePrefs.glass(this,20));t.setOnClickListener(v->action.run());return t;
    }
}
