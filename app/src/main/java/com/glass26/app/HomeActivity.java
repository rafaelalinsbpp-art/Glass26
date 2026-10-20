package com.glass26.app;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public final class HomeActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView clock, battery;
    private FrameLayout root;
    private List<ResolveInfo> apps = new ArrayList<>();
    private final Runnable updateClock = new Runnable() {
        @Override public void run() {
            if (clock != null) clock.setText(now("HH:mm"));
            if (battery != null) battery.setText("●  "+batteryLevel()+"%");
            handler.postDelayed(this, 20000);
        }
    };
    private int d(float x){return Math.round(x*getResources().getDisplayMetrics().density);}
    private String now(String pattern){return new SimpleDateFormat(pattern,new Locale("pt","BR")).format(new Date());}
    private int batteryLevel(){
        Intent i=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if(i==null)return 0;
        int level=i.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
        int scale=i.getIntExtra(BatteryManager.EXTRA_SCALE,100);
        return scale<=0?0:Math.round(100f*level/scale);
    }
    private GradientDrawable glass(int color,int radius){
        GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(d(radius));
        g.setStroke(d(1),0x70FFFFFF);return g;
    }
    private TextView text(String s,int size,boolean bold) {
        TextView v=new TextView(this);v.setText(s);v.setTextSize(size*AppearancePrefs.fontScale(this));
        v.setTextColor(AppearancePrefs.foreground(this));v.setGravity(Gravity.CENTER);
        if(bold)v.setTypeface(android.graphics.Typeface.DEFAULT,1);
        return v;
    }
    @Override public void onCreate(Bundle state){super.onCreate(state);loadApps();render();handler.post(updateClock);}
    @Override public void onResume(){super.onResume();render();handler.removeCallbacks(updateClock);handler.post(updateClock);}
    @Override public void onDestroy(){handler.removeCallbacks(updateClock);super.onDestroy();}
    private void loadApps() {
        PackageManager pm=getPackageManager();
        Intent query=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        apps=new ArrayList<>(pm.queryIntentActivities(query,0));
        Collections.sort(apps,(a,b)->String.CASE_INSENSITIVE_ORDER.compare(
            a.loadLabel(pm).toString(),b.loadLabel(pm).toString()));
    }
    private LinearLayout row(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.CENTER);return r;}
    private LinearLayout col(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);return c;}
    private void addGap(LinearLayout c,int height){View v=new View(this);c.addView(v,new LinearLayout.LayoutParams(1,d(height)));}
    private void render(){
        boolean dark=AppearancePrefs.isDark(this);
        getWindow().setStatusBarColor(dark?0xFF11182E:0xFFE3ECF8);
        getWindow().setNavigationBarColor(dark?0xFF11182E:0xFFE3ECF8);
        getWindow().getDecorView().setSystemUiVisibility(dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        root=new FrameLayout(this);
        root.setBackground(AppearancePrefs.gradient(this));
        String selected=getSharedPreferences(AppearancePrefs.PREFERENCES,0).getString("wall","");
        if(!selected.isEmpty()) {
            try { ImageView wall=new ImageView(this);wall.setScaleType(ImageView.ScaleType.CENTER_CROP);
                wall.setImageURI(Uri.parse(selected));
                if(wall.getDrawable()!=null)root.addView(wall,new FrameLayout.LayoutParams(-1,-1));
            }catch(Exception ignored){}
        }
        View shade=new View(this);shade.setBackgroundColor(0x38071020);
        root.addView(shade,new FrameLayout.LayoutParams(-1,-1));
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setVerticalScrollBarEnabled(false);
        root.addView(sc);
        LinearLayout content=col();content.setPadding(d(14),d(22),d(14),d(18));
        sc.addView(content);
        LinearLayout stat=row();
        TextView brand=text("Glass26",15,true), power=text("",13,true);
        battery=power;
        stat.addView(brand,new LinearLayout.LayoutParams(0,d(35),1));
        stat.addView(power,new LinearLayout.LayoutParams(0,d(35),1));
        content.addView(stat);
        TextView notices=action("◉  Notificações",()->startActivity(new Intent(this,NotificationCenterActivity.class)));
        content.addView(notices,new LinearLayout.LayoutParams(-1,d(42)));
        addGap(content,17);
        TextView island=text("●   Glass26    •    "+now("HH:mm")+"    "+batteryLevel()+"%",14,true);
        island.setBackground(glass(0xEE090D19,30));
        island.setTextColor(0xFFFFFFFF);island.setPadding(d(20),0,d(20),0);
        content.addView(island,new LinearLayout.LayoutParams(d(255),d(46)));
        island.setOnClickListener(v->showTools());
        addGap(content,25);
        clock=text("",64,false);
        content.addView(clock,new LinearLayout.LayoutParams(-1,d(78)));
        TextView date=text(now("EEEE, dd 'de' MMMM"),16,false);
        content.addView(date);
        addGap(content,30);
        TextView search=action("⌕    Buscar aplicativos",()->allApps());
        content.addView(search,new LinearLayout.LayoutParams(-1,d(47)));
        addGap(content,15);
        TextView title=text("SEUS APLICATIVOS",12,true);title.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        content.addView(title);
        addGap(content,7);
        int limit=Math.min(12,apps.size());
        for(int start=0;start<limit;start+=4){
            LinearLayout icons=row();
            for(int j=0;j<4;j++){
                int ix=start+j;
                if(ix<limit){ResolveInfo ri=apps.get(ix);icons.addView(appIcon(ri),new LinearLayout.LayoutParams(0,d(AppearancePrefs.gridHeight(this)),1));}
                else icons.addView(new View(this),new LinearLayout.LayoutParams(0,d(AppearancePrefs.gridHeight(this)),1));
            }
            content.addView(icons);
        }
        addGap(content,15);
        LinearLayout quick=row();
        quick.addView(button("◫ Central",()->showControl()),new LinearLayout.LayoutParams(0,d(55),1));
        quick.addView(button("⌨ Teclado",()->keyboardSettings()),new LinearLayout.LayoutParams(0,d(55),1));
        content.addView(quick);
        addGap(content,8);
        LinearLayout lower=row();
        lower.addView(button("◉ Ilha",()->showIslandOptions()),new LinearLayout.LayoutParams(0,d(55),1));
        lower.addView(button("▣ Visual",()->startActivity(new Intent(this,AppearanceActivity.class))),new LinearLayout.LayoutParams(0,d(55),1));
        content.addView(lower);
        addGap(content,15);
        TextView dock=action("☰  Todos os aplicativos",()->allApps());
        content.addView(dock,new LinearLayout.LayoutParams(-1,d(58)));
        addGap(content,15);
        TextView nav=text("━━━━━",21,true);
        nav.setContentDescription("Indicador visual. A navegação real permanece controlada pelo Android.");
        content.addView(nav,new LinearLayout.LayoutParams(-1,d(35)));
        setContentView(root);
    }
    private View appIcon(ResolveInfo ri){
        LinearLayout icon=col();icon.setPadding(d(3),d(5),d(3),d(5));
        ImageView image=new ImageView(this);
        try{image.setImageDrawable(ri.loadIcon(getPackageManager()));}catch(Exception ignored){}
        icon.addView(image,new LinearLayout.LayoutParams(d(49),d(49)));
        TextView label=text(ri.loadLabel(getPackageManager()).toString(),11,true);
        label.setMaxLines(2);label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        icon.addView(label,new LinearLayout.LayoutParams(-1,d(37)));
        icon.setOnClickListener(v->launch(ri));return icon;
    }
    private void launch(ResolveInfo ri){
        try{Intent i=getPackageManager().getLaunchIntentForPackage(ri.activityInfo.packageName);
            if(i==null)i=new Intent(Intent.ACTION_MAIN).setClassName(ri.activityInfo.packageName,ri.activityInfo.name);
            startActivity(i);
        }catch(Exception e){Toast.makeText(this,"Não foi possível abrir",Toast.LENGTH_SHORT).show();}
    }
    private TextView action(String name,Runnable run){
        TextView t=text(name,15,true);
        t.setBackground(AppearancePrefs.glass(this,18));t.setOnClickListener(v->run.run());return t;
    }
    private TextView button(String name,Runnable run){
        TextView t=action(name,run);LinearLayout.LayoutParams p=(LinearLayout.LayoutParams)t.getLayoutParams();
        t.setPadding(d(5),0,d(5),0);return t;
    }
    private void allApps() {
        loadApps();
        String[] names=new String[apps.size()];
        for(int i=0;i<apps.size();i++)names[i]=apps.get(i).loadLabel(getPackageManager()).toString();
        new AlertDialog.Builder(this).setTitle("Aplicativos instalados").setItems(names,(dialog,which)->launch(apps.get(which)))
            .setNegativeButton("Voltar",null).show();
    }
    private void showTools(){
        String[] names={"Central de controle","Papel de parede","Prévia da tela de bloqueio","Ativar/desativar Ilha Dinâmica","Configurar teclado","Definir como tela inicial","Tema, fontes e espaçamento","Notificações Glass26"};
        new AlertDialog.Builder(this).setTitle("Personalizar Glass26").setItems(names,(dialog,index)->{
            switch(index){
                case 0:showControl();break;
                case 1:chooseWallpaper();break;
                case 2:lockPreview();break;
                case 3:showIslandOptions();break;
                case 4:keyboardSettings();break;
                case 5:openSetting(Settings.ACTION_HOME_SETTINGS);break;
                case 6:startActivity(new Intent(this,AppearanceActivity.class));break;
                case 7:startActivity(new Intent(this,NotificationCenterActivity.class));break;
            }
        }).show();
    }
    private void showControl(){
        startActivity(new Intent(this,ControlCenterActivity.class));
    }
    private void showControlLegacy(){
        String[] names={"Wi-Fi (configuração oficial)","Bluetooth (configuração oficial)","Brilho e tela","Teclados","Configurações Android"};
        String[] intents={Settings.ACTION_WIFI_SETTINGS,Settings.ACTION_BLUETOOTH_SETTINGS,Settings.ACTION_DISPLAY_SETTINGS,Settings.ACTION_INPUT_METHOD_SETTINGS,Settings.ACTION_SETTINGS};
        new AlertDialog.Builder(this).setTitle("◫  Central Glass26").setItems(names,(d,i)->openSetting(intents[i]))
            .setNegativeButton("Fechar",null).show();
    }
    private void openSetting(String action){
        try{startActivity(new Intent(action));}catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}
    }
    private void keyboardSettings(){
        new AlertDialog.Builder(this).setTitle("Teclado Glass26")
            .setMessage("Ative o teclado nas configurações do Android e selecione-o no campo de texto do WhatsApp. O Glass26 não solicita acesso à Internet.")
            .setPositiveButton("Configurar",(d,w)->openSetting(Settings.ACTION_INPUT_METHOD_SETTINGS))
            .setNegativeButton("Depois",null).show();
    }
    private void lockPreview(){
        String[] styles={"Azul translúcido","Rosa translúcido"};
        new AlertDialog.Builder(this).setTitle("Tela de bloqueio — prévia visual")
            .setSingleChoiceItems(styles,getSharedPreferences(AppearancePrefs.PREFERENCES,0).getInt("lock",0),(dialog,choice)->{
                getSharedPreferences(AppearancePrefs.PREFERENCES,0).edit().putInt("lock",choice).apply();dialog.dismiss();showLockScreen();
            }).setNeutralButton("Ver prévia",(dialog,which)->showLockScreen()).show();
    }
    private void showLockScreen(){
        LinearLayout view=col();view.setPadding(d(20),d(80),d(20),d(80));
        int st=getSharedPreferences(AppearancePrefs.PREFERENCES,0).getInt("lock",0);
        view.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            st==0?new int[]{0xFF273675,0xFF809FE4}:new int[]{0xFF672A71,0xFFEA9DB3}));
        view.addView(text(now("HH:mm"),64,true));view.addView(text(now("EEEE, dd MMMM"),19,false));
        new AlertDialog.Builder(this).setView(view).setMessage("Esta é uma prévia. A tela de bloqueio Samsung não será substituída.")
            .setPositiveButton("Fechar",null).show();
    }
    private void chooseWallpaper(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i,26);
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==26&&result==RESULT_OK&&data!=null&&data.getData()!=null){
            Uri uri=data.getData();
            try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
            getSharedPreferences(AppearancePrefs.PREFERENCES,0).edit().putString("wall",uri.toString()).apply();render();
            new AlertDialog.Builder(this).setTitle("Papel de parede")
                .setMessage("A imagem já foi aplicada à tela inicial Glass26. Deseja aplicar também ao papel de parede do Android?")
                .setPositiveButton("Aplicar",(di,which)->new Thread(()->{
                    try(java.io.InputStream stream=getContentResolver().openInputStream(uri)){
                        android.app.WallpaperManager.getInstance(this).setStream(stream);
                        runOnUiThread(()->Toast.makeText(this,"Aplicado",Toast.LENGTH_SHORT).show());
                    }catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Não foi possível aplicar",Toast.LENGTH_SHORT).show());}
                }).start())
                .setNegativeButton("Não",null).show();
        }
    }
    private void showIslandOptions(){
        new AlertDialog.Builder(this).setTitle("Ilha Dinâmica Glass26")
            .setMessage("Sobreposição opcional com hora e bateria. Não lê mensagens nem notificações.")
            .setPositiveButton("Ativar",(d,w)->startIsland())
            .setNegativeButton("Desativar",(d,w)->stopService(new Intent(this,IslandService.class))).show();
    }
    private void startIsland(){
        if(!Settings.canDrawOverlays(this)){
            Toast.makeText(this,"Autorize a sobreposição e volte ao Glass26",Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));
            return;
        }
        try{startForegroundService(new Intent(this,IslandService.class));}
        catch(Exception e){Toast.makeText(this,"Não foi possível ativar a ilha",Toast.LENGTH_LONG).show();}
    }
}
