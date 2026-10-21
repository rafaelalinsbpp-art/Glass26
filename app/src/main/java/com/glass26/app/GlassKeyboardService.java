package com.glass26.app;

import android.graphics.*;
import android.graphics.drawable.*;
import android.inputmethodservice.InputMethodService;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.util.*;

public final class GlassKeyboardService extends InputMethodService {
    private LinearLayout board;
    private boolean shift=false, symbols=false,emoji=false;
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    @Override public View onCreateInputView(){
        board=new LinearLayout(this);board.setOrientation(LinearLayout.VERTICAL);
        board.setPadding(dp(3),dp(8),dp(3),dp(13));
        GradientDrawable backdrop=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            AppearancePrefs.isDark(this)?new int[]{0xFF25334D,0xFF17283C}
                :new int[]{0xFFDEE9F5,0xFFBCCEDB});
        board.setBackground(backdrop);
        render();return board;
    }
    @Override public void onStartInput(EditorInfo info,boolean restart){
        super.onStartInput(info,restart);
        int type=info==null?0:info.inputType & InputType.TYPE_MASK_CLASS;
        symbols=type==InputType.TYPE_CLASS_NUMBER||type==InputType.TYPE_CLASS_PHONE;
        shift=false;emoji=false;if(board!=null)render();
    }
    private void render(){
        if(board==null)return;board.removeAllViews();
        LinearLayout header=new LinearLayout(this);header.setOrientation(LinearLayout.HORIZONTAL);
        TextView brand=key("Português (Brasil)  ·  Glass26",true);brand.setTextSize(12);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        brand.setPadding(dp(16),0,0,0);
        header.addView(brand,new LinearLayout.LayoutParams(0,dp(34),1));
        TextView switcher=key("🌐",true);switcher.setTextSize(17);
        header.addView(switcher,new LinearLayout.LayoutParams(dp(55),dp(34)));
        switcher.setOnClickListener(v->{
            android.view.inputmethod.InputMethodManager im=
                (android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            if(im!=null)im.showInputMethodPicker();
        });
        LinearLayout.LayoutParams top=new LinearLayout.LayoutParams(-1,dp(34));
        top.bottomMargin=dp(6);board.addView(header,top);
        if(emoji){
            row("😀","😁","😂","😊","😍","🥰","😎","😭");
            row("👍","❤️","🙏","🔥","🎉","✨","💙","👋");
            row("😉","😅","🤔","🙌","😘","👏","💕","🌟");
        }else if(symbols){
            row("1","2","3","4","5","6","7","8","9","0");
            row("@","#","$","%","&","-","+","(",")","/");
            row("ABC","*","'","!",":",";","?","⌫");
        }else{
            row("q","w","e","r","t","y","u","i","o","p");
            row("a","s","d","f","g","h","j","k","l");
            row("⇧","z","x","c","v","b","n","m","⌫");
        }
        row(emoji?"ABC":symbols?"ABC":"?123","☺",",","espaço",".","↵");
    }
    private TextView key(String s,boolean special){
        TextView v=new TextView(this);v.setText(s);v.setGravity(Gravity.CENTER);
        v.setTextSize((s.length()>5?13:18)*AppearancePrefs.fontScale(this));
        v.setLetterSpacing(.01f);
        v.setTextColor(AppearancePrefs.isDark(this)?0xFFF7F9FF:0xFF172239);
        boolean dark=AppearancePrefs.isDark(this);
        GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            dark?(special?new int[]{0xFF627A9C,0xFF455573}:new int[]{0xFF5D718D,0xFF3F526B}):
                 (special?new int[]{0xFFD2E0F0,0xFFBACBD9}:new int[]{0xFFFFFFFF,0xFFEAF2FB}));
        g.setCornerRadius(dp(8));
        g.setStroke(Math.max(1,dp(1)),dark?0x5DBACDE4:0xE0FFFFFF);
        v.setBackground(g);v.setElevation(dp(1));return v;
    }
    private void row(String... keys){
        LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(49));
        rp.bottomMargin=dp(4);board.addView(r,rp);
        for(String s:keys){
            boolean special=s.equals("ABC")||s.equals("?123")||s.equals("☺")||s.equals("🌐")||s.equals("⇧")||s.equals("⌫")||s.equals("↵");
            TextView v=key(s,special);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(45),s.equals("espaço")?4.5f:1f);
            p.setMargins(dp(2),0,dp(2),0);r.addView(v,p);
            v.setOnClickListener(w->type(s));
            if(s.length()==1&&"aeiou".contains(s))
                v.setOnLongClickListener(w->{
                    String accent=s.equals("a")?"á":s.equals("e")?"é":s.equals("i")?"í":s.equals("o")?"ó":"ú";
                    commit(shift?accent.toUpperCase(new Locale("pt","BR")):accent);return true;
                });
        }
    }
    private void commit(String value){
        InputConnection c=getCurrentInputConnection();if(c!=null)c.commitText(value,1);
    }
    private void type(String s){
        InputConnection c=getCurrentInputConnection();if(c==null)return;
        switch(s){
            case "⌫":c.deleteSurroundingTextInCodePoints(1,0);return;
            case "⇧":shift=!shift;render();return;
            case "ABC":symbols=false;emoji=false;render();return;
            case "?123":symbols=true;emoji=false;render();return;
            case "☺":emoji=!emoji;symbols=false;render();return;
            case "espaço":commit(" ");return;
            case "🌐":{
                android.view.inputmethod.InputMethodManager im=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                if(im!=null)im.showInputMethodPicker();return;
            }
            case "↵":
                EditorInfo info=getCurrentInputEditorInfo();
                int action=info==null?EditorInfo.IME_ACTION_NONE:info.imeOptions & EditorInfo.IME_MASK_ACTION;
                if(action!=EditorInfo.IME_ACTION_NONE && action!=EditorInfo.IME_ACTION_UNSPECIFIED)
                    c.performEditorAction(action);
                else commit("\n");return;
            default:commit(shift?s.toUpperCase(new Locale("pt","BR")):s);
                if(shift){shift=false;render();}
        }
    }
}
