package com.glass26.app;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;

public final class AppearancePrefs {
    private AppearancePrefs() {}
    public static final String PREFERENCES="glass26";
    public static int mode(Context c){return c.getSharedPreferences(PREFERENCES,0).getInt("appearance",0);}
    public static boolean isDark(Context c){
        int setting=mode(c);
        if(setting==1)return false;
        if(setting==2)return true;
        int ui=c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return ui==Configuration.UI_MODE_NIGHT_YES;
    }
    public static float fontScale(Context c){
        int v=c.getSharedPreferences(PREFERENCES,0).getInt("font",1);
        return v==0?.88f:v==2?1.12f:v==3?1.26f:1f;
    }
    public static int gridHeight(Context c){
        int v=c.getSharedPreferences(PREFERENCES,0).getInt("spacing",1);
        return v==0?88:v==2?112:98;
    }
    public static int foreground(Context c){return isDark(c)?0xFFF8F8FF:0xFF1A2844;}
    public static int secondary(Context c){return isDark(c)?0xFFC4CBEA:0xFF3C4A69;}
    public static int surface(Context c){return isDark(c)?0xB5313B67:0xD7F6F8FF;}
    public static GradientDrawable gradient(Context c){
        return new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            isDark(c)?new int[]{0xFF101931,0xFF33315F,0xFF382C55}:
                      new int[]{0xFFD5E7FC,0xFFF3E5F7,0xFFFFDDE5});
    }
    public static GradientDrawable glass(Context c,int radiusDp){
        GradientDrawable d=new GradientDrawable();
        d.setColor(surface(c));
        float px=radiusDp*c.getResources().getDisplayMetrics().density;
        d.setCornerRadius(px);d.setStroke(Math.max(1,(int)(c.getResources().getDisplayMetrics().density)),isDark(c)?0x668FA5D2:0xBFFFFFFF);
        return d;
    }
}
