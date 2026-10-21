package com.glass26.app;

import android.app.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public final class AppearanceActivity extends Activity {
    private LinearLayout content;
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView item(String label,Runnable action){
        TextView v=new TextView(this);v.setText(label);v.setTextColor(AppearancePrefs.foreground(this));
        v.setTextSize(16*AppearancePrefs.fontScale(this));v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(18),dp(4),dp(14),dp(4));
        v.setBackground(AppearancePrefs.glass(this,18));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(58));p.bottomMargin=dp(10);
        content.addView(v,p);v.setOnClickListener(w->action.run());return v;
    }
    private void choose(String title,String key,String[] values){
        int current=getSharedPreferences(AppearancePrefs.PREFERENCES,0).getInt(key,1);
        if(key.equals("appearance"))current=AppearancePrefs.mode(this);
        final int selected=current;
        new AlertDialog.Builder(this).setTitle(title).setSingleChoiceItems(values,selected,(dialog,index)->{
            getSharedPreferences(AppearancePrefs.PREFERENCES,0).edit().putInt(key,index).apply();
            dialog.dismiss();render();
        }).setNegativeButton("Cancelar",null).show();
    }
    @Override public void onCreate(Bundle b){super.onCreate(b);render();}
    private void render(){
        boolean dark=AppearancePrefs.isDark(this);
        getWindow().setStatusBarColor(dark?0xFF14172A:0xFFE5EDFA);
        getWindow().setNavigationBarColor(dark?0xFF14172A:0xFFE5EDFA);
        getWindow().getDecorView().setSystemUiVisibility(dark?0:
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        ScrollView scroll=new ScrollView(this);
        scroll.setBackground(AppearancePrefs.gradient(this));
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20),dp(35),dp(20),dp(22));scroll.addView(content);
        TextView heading=new TextView(this);heading.setText("Glass26\nAparência");
        heading.setTextColor(AppearancePrefs.foreground(this));heading.setTextSize(30);
        heading.setTypeface(null,1);
        content.addView(heading,new LinearLayout.LayoutParams(-1,dp(105)));
        item("◐   Tema: automático / claro / escuro",()->
            choose("Tema de todo o Glass26","appearance",new String[]{"Automático (Android)","Claro","Escuro"}));
        item("Aa   Tamanho das letras",()->
            choose("Tamanho da fonte","font",new String[]{"Compacta","Normal","Grande","Muito grande"}));
        item("▦   Espaçamento entre aplicativos",()->
            choose("Espaçamento dos ícones","spacing",new String[]{"Compacto","Padrão","Amplo"}));
        item("◉   Relógio da tela de bloqueio",()->
            choose("Estilo do relógio na prévia","lockfont",
                new String[]{"Compacto","Padrão Liquid Glass","Relógio grande"}));
        item("⌨   Testar teclado Glass26",()->
            startActivity(new android.content.Intent(this,KeyboardTestActivity.class)));
        item("▣   Prévia da tela de bloqueio",()->
            startActivity(new android.content.Intent(this,LockPreviewActivity.class)));
        TextView preview=new TextView(this);
        preview.setGravity(Gravity.CENTER_VERTICAL);
        preview.setTextColor(AppearancePrefs.foreground(this));
        preview.setTextSize(16*AppearancePrefs.fontScale(this));
        preview.setPadding(dp(22),dp(14),dp(22),dp(14));
        preview.setText("Prévia Glass26\n\n" + (dark?"◉  Modo escuro":"◉  Modo claro")
            +"\n\nExemplo de notificação\nMensagem recebida · agora");
        preview.setBackground(AppearancePrefs.glass(this,26));
        content.addView(preview,new LinearLayout.LayoutParams(-1,dp(170)));
        TextView help=new TextView(this);help.setText("Os ajustes valem para as telas do Glass26 e o teclado. Outros aplicativos continuam com o tema e as fontes definidos por eles ou pelo Android.");
        help.setTextSize(13);help.setTextColor(AppearancePrefs.secondary(this));
        help.setPadding(0,dp(18),0,dp(22));content.addView(help);
        item("✓   Voltar para a tela inicial",()->{
            startActivity(new android.content.Intent(this,HomeActivity.class)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP|
                    android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        });
        setContentView(scroll);
    }
}
