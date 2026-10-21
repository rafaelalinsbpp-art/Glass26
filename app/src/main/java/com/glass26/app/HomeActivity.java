package com.glass26.app;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public final class HomeActivity extends Activity {
    private FrameLayout root;
    private TextView timeValue,batteryValue,dateValue;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final ArrayList<ResolveInfo> apps=new ArrayList<>();
    private static final String[] PRIORITY={
        "com.sec.android.app.camera","com.google.android.GoogleCamera",
        "com.android.camera2","com.google.android.calendar","com.samsung.android.calendar",
        "com.sec.android.gallery3d","com.google.android.apps.photos",
        "com.google.android.apps.messaging","com.samsung.android.messaging",
        "com.google.android.apps.maps","com.google.android.deskclock",
        "com.samsung.android.app.clockpackage","com.whatsapp",
        "com.instagram.android","com.android.vending",
        "com.android.settings","com.google.android.gm"
    };
    private final Runnable tick=new Runnable(){
        @Override public void run(){
            if(timeValue!=null)timeValue.setText(now("HH:mm"));
            if(batteryValue!=null)batteryValue.setText("▰  "+battery()+"%");
            if(dateValue!=null)dateValue.setText(now("EEEE, d 'de' MMMM"));
            handler.postDelayed(this,20000);
        }
    };
    private int d(float n){return GlassVisual.dp(this,n);}
    private String now(String format){return new SimpleDateFormat(format,new Locale("pt","BR")).format(new Date());}
    private int battery(){
        Intent i=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if(i==null)return 0;
        int level=i.getIntExtra(BatteryManager.EXTRA_LEVEL,0),scale=i.getIntExtra(BatteryManager.EXTRA_SCALE,100);
        return scale>0?Math.round(level*100f/scale):0;
    }
    private TextView text(String value,float sp,boolean strong){
        TextView t=new TextView(this);t.setText(value);
        t.setTextSize(sp*AppearancePrefs.fontScale(this));
        t.setTextColor(0xFFF8FDFF);
        if(strong)t.setTypeface(android.graphics.Typeface.create("sans-serif-medium",1));
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setShadowLayer(d(2),0,d(1),0x57021D46);
        return t;
    }
    private LinearLayout vertical(){
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;
    }
    private LinearLayout horizontal(){
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;
    }
    private void spacer(LinearLayout l,int dp){
        l.addView(new View(this),new LinearLayout.LayoutParams(1,d(dp)));
    }
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setFlags(1024,1024);loadApps();render();}
    @Override public void onResume(){super.onResume();loadApps();render();handler.removeCallbacks(tick);handler.post(tick);}
    @Override public void onPause(){handler.removeCallbacks(tick);super.onPause();}
    @Override public void onDestroy(){handler.removeCallbacks(tick);super.onDestroy();}
    private void loadApps(){
        PackageManager pm=getPackageManager();
        Intent list=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        ArrayList<ResolveInfo> all=new ArrayList<>(pm.queryIntentActivities(list,0));
        all.sort((a,b)->String.CASE_INSENSITIVE_ORDER.compare(
            a.loadLabel(pm).toString(),b.loadLabel(pm).toString()));
        LinkedHashMap<String,ResolveInfo> ordered=new LinkedHashMap<>();
        for(String pkg:PRIORITY)for(ResolveInfo a:all)
            if(a.activityInfo!=null&&pkg.equals(a.activityInfo.packageName))
                ordered.putIfAbsent(pkg,a);
        for(ResolveInfo a:all)if(a.activityInfo!=null)
            ordered.putIfAbsent(a.activityInfo.packageName,a);
        apps.clear();apps.addAll(ordered.values());
    }
    private void render(){
        getWindow().setNavigationBarColor(0xFF082650);
        getWindow().getDecorView().setSystemUiVisibility(0);
        root=new FrameLayout(this);
        root.addView(new GlassVisual.Wallpaper(this),new FrameLayout.LayoutParams(-1,-1));
        String selected=getSharedPreferences(AppearancePrefs.PREFERENCES,0).getString("wall","");
        if(!selected.isEmpty())try{
            ImageView wall=new ImageView(this);wall.setScaleType(ImageView.ScaleType.CENTER_CROP);
            wall.setImageURI(Uri.parse(selected));
            if(wall.getDrawable()!=null)root.addView(wall,new FrameLayout.LayoutParams(-1,-1));
        }catch(Exception ignored){}
        ScrollView sc=new ScrollView(this);
        sc.setFillViewport(true);sc.setClipToPadding(false);sc.setVerticalScrollBarEnabled(false);
        FrameLayout.LayoutParams scp=new FrameLayout.LayoutParams(-1,-1);
        root.addView(sc,scp);
        LinearLayout body=vertical();body.setPadding(d(16),d(9),d(16),d(130));
        sc.addView(body);
        topBar(body);
        spacer(body,17);
        widgets(body);
        spacer(body,24);
        TextView heading=text("APLICATIVOS",11,true);heading.setLetterSpacing(.2f);
        body.addView(heading,new LinearLayout.LayoutParams(-1,d(29)));
        appGrid(body);
        spacer(body,13);
        LinearLayout actions=horizontal();
        TextView n=glassButton("◉  Notificações",()->startActivity(new Intent(this,NotificationCenterActivity.class)));
        TextView c=glassButton("▦  Central de controle",()->showControl());
        LinearLayout.LayoutParams a=new LinearLayout.LayoutParams(0,d(44),1);a.rightMargin=d(5);
        actions.addView(n,a);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,d(44),1);ap.leftMargin=d(5);
        actions.addView(c,ap);body.addView(actions);
        floatingDock();
        setContentView(root);
        handler.removeCallbacks(tick);handler.post(tick);
    }
    private void topBar(LinearLayout host){
        LinearLayout bar=horizontal();bar.setGravity(Gravity.CENTER_VERTICAL);
        timeValue=text(now("HH:mm"),14,true);
        timeValue.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        bar.addView(timeValue,new LinearLayout.LayoutParams(0,d(42),1));
        TextView island=text("●   Glass26",13,true);
        island.setGravity(Gravity.CENTER);
        island.setTextColor(0xFFFFFFFF);island.setShadowLayer(0,0,0,0);
        island.setBackground(GlassVisual.island(this,40));
        LinearLayout.LayoutParams center=new LinearLayout.LayoutParams(d(154),d(37));
        bar.addView(island,center);
        island.setOnClickListener(v->showIslandOptions());
        batteryValue=text("▰ "+battery()+"%",13,true);
        batteryValue.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        bar.addView(batteryValue,new LinearLayout.LayoutParams(0,d(42),1));
        batteryValue.setOnClickListener(v->showControl());
        host.addView(bar,new LinearLayout.LayoutParams(-1,d(48)));
        dateValue=text(now("EEEE, d 'de' MMMM"),12,false);
        dateValue.setGravity(Gravity.CENTER);dateValue.setAlpha(.86f);
        host.addView(dateValue,new LinearLayout.LayoutParams(-1,d(22)));
    }
    private void widgets(LinearLayout host){
        LinearLayout row=horizontal();
        LinearLayout weather=vertical();weather.setPadding(d(13),d(12),d(10),d(11));
        weather.setBackground(GlassVisual.panel(this,22,false));
        TextView small=text("CLIMA",11,true);small.setTextColor(0xFF32547B);
        weather.addView(small,new LinearLayout.LayoutParams(-1,d(23)));
        TextView sun=text("☀  --°",32,false);sun.setTextColor(0xFF125B94);
        sun.setShadowLayer(0,0,0,0);weather.addView(sun,new LinearLayout.LayoutParams(-1,d(56)));
        TextView message=text("Adicionar previsão",12,true);
        message.setTextColor(0xFF32547B);message.setShadowLayer(0,0,0,0);
        weather.addView(message,new LinearLayout.LayoutParams(-1,d(23)));
        TextView hint=text("Toque para abrir clima",10,false);
        hint.setTextColor(0xFF345679);hint.setShadowLayer(0,0,0,0);
        weather.addView(hint);
        weather.setOnClickListener(v->openWeather());
        LinearLayout.LayoutParams w=new LinearLayout.LayoutParams(0,d(158),1);
        w.rightMargin=d(7);row.addView(weather,w);
        LinearLayout media=vertical();media.setPadding(d(13),d(12),d(11),d(11));
        media.setBackground(GlassVisual.panel(this,22,false));
        LinearLayout title=horizontal();title.setGravity(Gravity.CENTER_VERTICAL);
        TextView album=text("♫",29,true);album.setGravity(Gravity.CENTER);
        album.setBackground(GlassVisual.accent(this,12,0xFFF8A2AA,0xFFAA6DB9));
        title.addView(album,new LinearLayout.LayoutParams(d(49),d(49)));
        LinearLayout info=vertical();info.setPadding(d(7),0,0,0);
        TextView now=text("Mídia",13,true),no=text("Sem reprodução",11,false);
        for(TextView t:new TextView[]{now,no}){t.setTextColor(0xFF23446C);t.setShadowLayer(0,0,0,0);}
        info.addView(now,new LinearLayout.LayoutParams(-1,d(25)));
        info.addView(no,new LinearLayout.LayoutParams(-1,d(25)));
        title.addView(info,new LinearLayout.LayoutParams(0,d(52),1));
        media.addView(title);
        spacer(media,10);
        View progress=new View(this);
        progress.setBackground(GlassVisual.accent(this,4,0xFF66B9DE,0xFF9EE9E3));
        media.addView(progress,new LinearLayout.LayoutParams(-1,d(4)));
        spacer(media,8);
        TextView playback=text("◀◀     ▶❚❚     ▶▶",18,true);
        playback.setTextColor(0xFF214F81);playback.setGravity(Gravity.CENTER);
        playback.setShadowLayer(0,0,0,0);
        media.addView(playback,new LinearLayout.LayoutParams(-1,d(35)));
        media.setOnClickListener(v->mediaDialog());
        LinearLayout.LayoutParams m=new LinearLayout.LayoutParams(0,d(158),1);
        m.leftMargin=d(7);row.addView(media,m);
        host.addView(row);
    }
    private void appGrid(LinearLayout host){
        final int count=Math.min(12,apps.size());
        for(int start=0;start<count;start+=4){
            LinearLayout row=horizontal();row.setGravity(Gravity.TOP);
            for(int i=0;i<4;i++){
                int index=start+i;
                if(index<count)row.addView(appCell(apps.get(index)),new LinearLayout.LayoutParams(0,d(109),1));
                else row.addView(new View(this),new LinearLayout.LayoutParams(0,d(109),1));
            }
            host.addView(row);
        }
        if(apps.size()==0){
            TextView empty=text("Nenhum aplicativo encontrado. Toque em Todos os apps.",14,false);
            host.addView(empty);
        }
    }
    private View appCell(ResolveInfo ri){
        LinearLayout cell=vertical();cell.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        cell.setPadding(d(2),d(4),d(2),0);
        FrameLayout iconTile=new FrameLayout(this);
        iconTile.setBackground(GlassVisual.panel(this,17,true));
        ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        try{image.setImageDrawable(ri.loadIcon(getPackageManager()));}catch(Exception ignored){}
        FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(-1,-1);
        ip.setMargins(d(5),d(5),d(5),d(5));
        iconTile.addView(image,ip);
        cell.addView(iconTile,new LinearLayout.LayoutParams(d(57),d(57)));
        TextView title=text(ri.loadLabel(getPackageManager()).toString(),11,false);
        title.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        title.setMaxLines(1);title.setEllipsize(TextUtils.TruncateAt.END);
        title.setPadding(d(1),d(7),d(1),0);
        cell.addView(title,new LinearLayout.LayoutParams(-1,d(30)));
        cell.setOnClickListener(v->launch(ri));
        return cell;
    }
    private void floatingDock(){
        LinearLayout dock=horizontal();dock.setGravity(Gravity.CENTER);
        dock.setPadding(d(10),d(10),d(10),d(10));
        dock.setBackground(GlassVisual.panel(this,25,false));
        int[] picked=new int[4];Arrays.fill(picked,-1);
        String[][] preferences={
            {"com.google.android.dialer","com.samsung.android.dialer"},
            {"com.google.android.apps.messaging","com.samsung.android.messaging"},
            {"com.whatsapp","com.google.android.gm"},
            {"com.sec.android.app.camera","com.android.camera2","com.android.settings"}
        };
        Set<Integer> used=new HashSet<>();
        for(int i=0;i<4;i++){
            outer:for(String pkg:preferences[i])for(int j=0;j<apps.size();j++)
                if(!used.contains(j)&&pkg.equals(apps.get(j).activityInfo.packageName)){picked[i]=j;used.add(j);break outer;}
            if(picked[i]<0)for(int j=0;j<apps.size();j++)if(!used.contains(j)){picked[i]=j;used.add(j);break;}
        }
        for(int i=0;i<4;i++){
            final int index=picked[i];FrameLayout tile=new FrameLayout(this);
            tile.setBackground(GlassVisual.panel(this,17,true));
            if(index>=0){
                ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                try{image.setImageDrawable(apps.get(index).loadIcon(getPackageManager()));}catch(Exception ignored){}
                FrameLayout.LayoutParams imageP=new FrameLayout.LayoutParams(-1,-1);imageP.setMargins(d(5),d(5),d(5),d(5));
                tile.addView(image,imageP);
                tile.setOnClickListener(v->launch(apps.get(index)));
            }else{
                TextView more=text("+",27,true);more.setGravity(Gravity.CENTER);tile.addView(more,new FrameLayout.LayoutParams(-1,-1));
                tile.setOnClickListener(v->allApps());
            }
            LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,d(55),1);tp.setMargins(d(6),0,d(6),0);
            dock.addView(tile,tp);
        }
        FrameLayout.LayoutParams pos=new FrameLayout.LayoutParams(-1,d(77),Gravity.BOTTOM);
        pos.setMargins(d(16),0,d(16),d(17));
        root.addView(dock,pos);
        // Open all installed apps without substituting Samsung's protected system navigation.
        dock.setOnLongClickListener(v->{allApps();return true;});
    }
    private TextView glassButton(String label,Runnable action){
        TextView t=text(label,12,true);t.setGravity(Gravity.CENTER);
        t.setBackground(GlassVisual.panel(this,18,true));
        t.setOnClickListener(v->action.run());return t;
    }
    private void launch(ResolveInfo ri){
        try{
            PackageManager pm=getPackageManager();
            Intent i=pm.getLaunchIntentForPackage(ri.activityInfo.packageName);
            if(i==null)i=new Intent(Intent.ACTION_MAIN).setClassName(
                ri.activityInfo.packageName,ri.activityInfo.name);
            startActivity(i);
        }catch(Exception e){Toast.makeText(this,"Não foi possível abrir",Toast.LENGTH_SHORT).show();}
    }
    private void allApps(){
        loadApps();String[] names=new String[apps.size()];
        for(int i=0;i<apps.size();i++)names[i]=apps.get(i).loadLabel(getPackageManager()).toString();
        new AlertDialog.Builder(this).setTitle("Todos os aplicativos")
            .setItems(names,(d,index)->launch(apps.get(index)))
            .setNegativeButton("Voltar",null).show();
    }
    private void mediaDialog(){
        new AlertDialog.Builder(this).setTitle("Mídia Glass26")
            .setMessage("Este widget tem a aparência inspirada no conceito. Ele ainda não lê o título da música reproduzida. Os controles de reprodução são enviados ao player Android ativo.")
            .setPositiveButton("Reproduzir / pausar",(d,w)->{
                android.media.AudioManager audio=(android.media.AudioManager)getSystemService(AUDIO_SERVICE);
                if(audio!=null){
                    audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
                    audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
                }
            }).setNegativeButton("Fechar",null).show();
    }
    private void openWeather(){
        Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q=previs%C3%A3o+do+tempo"));
        try{startActivity(i);}catch(Exception e){Toast.makeText(this,"Aplicativo de clima não disponível",Toast.LENGTH_SHORT).show();}
    }
    private void showControl(){startActivity(new Intent(this,ControlCenterActivity.class));}
    private void showIslandOptions(){
        new AlertDialog.Builder(this).setTitle("Ilha Dinâmica Glass26")
            .setMessage("A ilha pode aparecer sobre outros aplicativos, com relógio e bateria, se você autorizar a sobreposição.")
            .setPositiveButton("Ativar",(d,w)->{
                if(!Settings.canDrawOverlays(this)){
                    startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:"+getPackageName())));
                }else try{startForegroundService(new Intent(this,IslandService.class));}
                    catch(Exception e){Toast.makeText(this,"Não foi possível ativar",Toast.LENGTH_SHORT).show();}
            })
            .setNeutralButton("Personalizar", (d,w)->moreOptions())
            .setNegativeButton("Desativar",(d,w)->stopService(new Intent(this,IslandService.class))).show();
    }
    private void moreOptions(){
        String[] menu={"Todos os aplicativos","Tema, fontes e espaçamento","Notificações","Central de controle",
            "Alterar papel de parede","Configurar teclado Glass26","Tela de bloqueio (prévia)","Definir Glass26 como launcher"};
        new AlertDialog.Builder(this).setTitle("Personalizar Glass26").setItems(menu,(dialog,i)->{
            switch(i){
                case 0:allApps();break;
                case 1:startActivity(new Intent(this,AppearanceActivity.class));break;
                case 2:startActivity(new Intent(this,NotificationCenterActivity.class));break;
                case 3:showControl();break;
                case 4:chooseWallpaper();break;
                case 5:startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));break;
                case 6:lockPreview();break;
                case 7:startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));break;
            }
        }).show();
    }
    private void lockPreview(){
        new AlertDialog.Builder(this).setTitle("Tela de bloqueio — prévia")
            .setMessage(now("HH:mm")+"\n"+now("EEEE, dd 'de' MMMM")+"\n\nA proteção de tela e a autenticação originais da Samsung continuam ativas.")
            .setPositiveButton("Fechar",null).show();
    }
    private void chooseWallpaper(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i,26);
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==26&&result==RESULT_OK&&data!=null&&data.getData()!=null){
            Uri uri=data.getData();
            try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}
            catch(Exception ignored){}
            getSharedPreferences(AppearancePrefs.PREFERENCES,0).edit().putString("wall",uri.toString()).apply();
            render();
        }
    }
}
