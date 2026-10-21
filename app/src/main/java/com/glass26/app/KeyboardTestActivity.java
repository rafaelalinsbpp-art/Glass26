package com.glass26.app;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

/** A real editable field for verifying the Glass26 input method on Android. */
public final class KeyboardTestActivity extends Activity {
    private EditText editor;
    private int d(float value){return GlassVisual.dp(this,value);}
    private TextView text(String title,int size){
        TextView t=new TextView(this);t.setText(title);
        t.setTextColor(Color.WHITE);t.setTextSize(size);t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFF0A2543);
        getWindow().setNavigationBarColor(0xFF102D51);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        FrameLayout root=new FrameLayout(this);
        root.addView(new GlassVisual.Wallpaper(this),new FrameLayout.LayoutParams(-1,-1));
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(d(24),d(35),d(24),d(25));scroll.addView(page);
        TextView heading=text("Teclado Glass26",27);
        heading.setTypeface(null,1);page.addView(heading,new LinearLayout.LayoutParams(-1,d(58)));
        TextView subtitle=text("Teste de digitação real, inclusive acentos, espaço, apagar e Enter.",14);
        subtitle.setMaxLines(3);page.addView(subtitle,new LinearLayout.LayoutParams(-1,d(75)));
        editor=new EditText(this);editor.setId(View.generateViewId());
        editor.setTag("glass26-test-field");
        editor.setTextColor(0xFF153D65);
        editor.setTextSize(21);editor.setHint("Toque aqui e digite com Glass26");
        editor.setHintTextColor(0xFF6D869F);
        editor.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editor.setMinLines(4);editor.setGravity(Gravity.TOP|Gravity.START);
        editor.setPadding(d(17),d(19),d(17),d(19));
        editor.setBackground(GlassVisual.panel(this,24,false));
        page.addView(editor,new LinearLayout.LayoutParams(-1,d(190)));
        addButton(page,"1. Ativar teclado nas configurações",()->startActivity(
            new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        addButton(page,"2. Selecionar teclado para digitar",()->{
            InputMethodManager imm=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            if(imm!=null)imm.showInputMethodPicker();
        });
        addButton(page,"3. Escrever no campo de teste",()->{
            editor.requestFocus();
            InputMethodManager imm=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            if(imm!=null)imm.showSoftInput(editor,InputMethodManager.SHOW_IMPLICIT);
        });
        TextView privacy=text("O teclado Glass26 não precisa de conexão à Internet. A digitação fica no aplicativo em que você escreve, como ocorre com outros teclados Android. O Glass26 não salva o texto deste teste.",13);
        privacy.setMaxLines(6);
        page.addView(privacy,new LinearLayout.LayoutParams(-1,d(115)));
        addButton(page,"‹  Voltar",()->finish());
        root.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        setContentView(root);
    }
    private void addButton(LinearLayout host,String name,Runnable action){
        TextView view=text(name,15);view.setGravity(Gravity.CENTER);
        view.setBackground(GlassVisual.panel(this,18,true));
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,d(49));
        params.topMargin=d(10);
        host.addView(view,params);view.setOnClickListener(v->action.run());
    }
}
