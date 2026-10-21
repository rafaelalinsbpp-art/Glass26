package com.glass26.app;

import android.app.*;
import android.app.role.RoleManager;
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
    private PagedScroller workspace;
    private TextView pageIndicator;
    private int currentPage=0;
    private int totalPages=2;
    private static final int ROLE_REQUEST=27;
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
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        loadApps();render();
    }
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
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(0xFF0C2C54);
        getWindow().setNavigationBarColor(0xFF0A2A4D);
        getWindow().getDecorView().setSystemUiVisibility(0);
        root=new FrameLayout(this);
        root.addView(new GlassVisual.Wallpaper(this),new FrameLayout.LayoutParams(-1,-1));
        String selected=getSharedPreferences(AppearancePrefs.PREFERENCES,0).getString("wall","");
        if(!selected.isEmpty())try{
            ImageView wall=new ImageView(this);
            wall.setScaleType(ImageView.ScaleType.CENTER_CROP);
            wall.setImageURI(Uri.parse(selected));
            if(wall.getDrawable()!=null)
                root.addView(wall,new FrameLayout.LayoutParams(-1,-1));
        }catch(Exception ignored){}

        totalPages=Math.max(2,1+(int)Math.ceil(Math.max(0,apps.size()-12)/20.0));
        int pageWidth=getResources().getDisplayMetrics().widthPixels;
        workspace=new PagedScroller(this);
        workspace.setHorizontalScrollBarEnabled(false);
        workspace.setFillViewport(true);
        workspace.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout pages=horizontal();
        workspace.addView(pages,new HorizontalScrollView.LayoutParams(-2,-1));
        for(int page=0;page<totalPages;page++){
            ScrollView verticalScroll=new ScrollView(this);
            verticalScroll.setFillViewport(true);
            verticalScroll.setVerticalScrollBarEnabled(false);
            LinearLayout pageBody=vertical();
            pageBody.setPadding(d(16),d(10),d(16),d(155));
            verticalScroll.addView(pageBody);
            if(page==0)firstPage(pageBody);
            else additionalPage(pageBody,page);
            pages.addView(verticalScroll,new LinearLayout.LayoutParams(pageWidth,-1));
        }
        root.addView(workspace,new FrameLayout.LayoutParams(-1,-1));
        pageIndicator=text("",13,true);
        pageIndicator.setGravity(Gravity.CENTER);
        pageIndicator.setContentDescription("Indicador de páginas iniciais");
        FrameLayout.LayoutParams indicatorPos=new FrameLayout.LayoutParams(-1,d(29),Gravity.BOTTOM);
        indicatorPos.setMargins(0,0,0,d(110));
        root.addView(pageIndicator,indicatorPos);
        pageIndicator.setOnClickListener(v->goPage((currentPage+1)%totalPages));
        updatePageIndicator();
        floatingDock();
        setContentView(root);
        int restore=Math.min(Math.max(currentPage,0),totalPages-1);
        workspace.post(()->workspace.scrollTo(restore*workspace.getWidth(),0));
        handler.removeCallbacks(tick);handler.post(tick);
    }
    private void firstPage(LinearLayout body){
        // Use real Android/Samsung status bar indicators, not fake Wi-Fi or battery.
        LinearLayout header=horizontal();header.setGravity(Gravity.CENTER_VERTICAL);
        TextView date=text(now("EEEE, d 'de' MMMM"),14,true);
        header.addView(date,new LinearLayout.LayoutParams(0,d(48),1));
        TextView menu=glassButton("⚙  Personalizar",()->moreOptions());
        header.addView(menu,new LinearLayout.LayoutParams(d(128),d(40)));
        body.addView(header);
        spacer(body,23);
        widgets(body);
        spacer(body,26);
        TextView heading=text("APLICATIVOS",11,true);
        heading.setLetterSpacing(.2f);
        body.addView(heading,new LinearLayout.LayoutParams(-1,d(28)));
        addAppRange(body,0,12);
        spacer(body,12);
        LinearLayout actions=horizontal();
        TextView notifications=glassButton("◉  Notificações",
            ()->startActivity(new Intent(this,NotificationCenterActivity.class)));
        TextView center=glassButton("▦  Central de controle",()->showControl());
        LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,d(46),1);
        left.rightMargin=d(5);actions.addView(notifications,left);
        LinearLayout.LayoutParams right=new LinearLayout.LayoutParams(0,d(46),1);
        right.leftMargin=d(5);actions.addView(center,right);
        body.addView(actions);
        if(!isHomeLauncher()){
            spacer(body,14);
            TextView choose=glassButton("⌂  Definir Glass26 como tela inicial",()->requestHomeRole());
            body.addView(choose,new LinearLayout.LayoutParams(-1,d(49)));
        }
        spacer(body,10);
        TextView hint=text("Deslize para a esquerda para ver mais aplicativos  ›",12,false);
        hint.setGravity(Gravity.CENTER);
        body.addView(hint,new LinearLayout.LayoutParams(-1,d(38)));
    }
    private void additionalPage(LinearLayout body,int page){
        LinearLayout header=horizontal();header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=text("Biblioteca de aplicativos",23,true);
        header.addView(title,new LinearLayout.LayoutParams(0,d(61),1));
        TextView all=glassButton("Todos",()->allApps());
        header.addView(all,new LinearLayout.LayoutParams(d(76),d(45)));
        body.addView(header);
        TextView hint=text("Página "+(page+1)+" de "+totalPages+"  ·  Arraste para navegar",12,false);
        body.addView(hint,new LinearLayout.LayoutParams(-1,d(33)));
        spacer(body,17);
        addAppRange(body,12+(page-1)*20,20);
        spacer(body,18);
        body.addView(glassButton("☰  Todos os aplicativos",()->allApps()),
            new LinearLayout.LayoutParams(-1,d(55)));
        spacer(body,15);
        body.addView(glassButton("⚙  Configurações do Glass26",()->moreOptions()),
            new LinearLayout.LayoutParams(-1,d(55)));
        if(!isHomeLauncher()){
            spacer(body,15);
            body.addView(glassButton("⌂  Tornar launcher padrão",()->requestHomeRole()),
                new LinearLayout.LayoutParams(-1,d(55)));
        }
    }
    private void updatePageIndicator(){
        if(pageIndicator==null)return;
        StringBuilder dots=new StringBuilder();
        for(int i=0;i<totalPages;i++){
            if(i>0)dots.append("   ");
            dots.append(i==currentPage?"●":"○");
        }
        pageIndicator.setText(dots.toString());
        pageIndicator.setContentDescription("Página "+(currentPage+1)+" de "+totalPages+". Toque para mudar.");
    }
    private void goPage(int index){
        if(workspace==null)return;
        currentPage=Math.max(0,Math.min(index,totalPages-1));
        workspace.smoothScrollTo(workspace.getWidth()*currentPage,0);
        updatePageIndicator();
    }
    private final class PagedScroller extends HorizontalScrollView {
        private float downX;
        PagedScroller(Context context){super(context);}
        @Override public boolean onInterceptTouchEvent(android.view.MotionEvent event){
            if(event.getActionMasked()==MotionEvent.ACTION_DOWN)downX=event.getX();
            return super.onInterceptTouchEvent(event);
        }
        @Override public boolean onTouchEvent(MotionEvent event){
            if(event.getActionMasked()==MotionEvent.ACTION_DOWN)downX=event.getX();
            if(event.getActionMasked()==MotionEvent.ACTION_UP){
                float delta=event.getX()-downX;
                final int current=currentPage;
                final int selected=Math.abs(delta)>d(45)
                    ?(delta<0?Math.min(totalPages-1,current+1):Math.max(0,current-1))
                    :Math.round(getScrollX()/(float)Math.max(1,getWidth()));
                boolean handled=super.onTouchEvent(event);
                postDelayed(()->goPage(selected),25);
                return handled;
            }
            return super.onTouchEvent(event);
        }
        @Override protected void onScrollChanged(int x,int y,int oldx,int oldy){
            super.onScrollChanged(x,y,oldx,oldy);
            if(getWidth()>0&&pageIndicator!=null){
                int active=Math.max(0,Math.min(totalPages-1,Math.round(x/(float)getWidth())));
                if(currentPage!=active){currentPage=active;updatePageIndicator();}
            }
        }
    }
    private boolean isHomeLauncher(){
        if(android.os.Build.VERSION.SDK_INT>=29){
            RoleManager rm=getSystemService(RoleManager.class);
            return rm!=null&&rm.isRoleHeld(RoleManager.ROLE_HOME);
        }
        Intent home=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo current=getPackageManager().resolveActivity(home,PackageManager.MATCH_DEFAULT_ONLY);
        return current!=null&&current.activityInfo!=null&&getPackageName().equals(current.activityInfo.packageName);
    }
    private void requestHomeRole(){
        if(isHomeLauncher()){
            Toast.makeText(this,"Glass26 já é a tela inicial padrão",Toast.LENGTH_SHORT).show();
            return;
        }
        if(Build.VERSION.SDK_INT>=29){
            RoleManager roles=getSystemService(RoleManager.class);
            if(roles!=null&&roles.isRoleAvailable(RoleManager.ROLE_HOME)){
                startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_HOME),ROLE_REQUEST);
                return;
            }
        }
        try{startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));}
        catch(Exception e){Toast.makeText(this,"Escolha Glass26 em Apps padrão > App da tela inicial",Toast.LENGTH_LONG).show();}
    }
    private void widgets(LinearLayout host){
        LinearLayout row=horizontal();
        LinearLayout weather=vertical();weather.setPadding(d(13),d(12),d(10),d(11));
        weather.setBackground(GlassVisual.panel(this,22,AppearancePrefs.isDark(this)));
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
        media.setBackground(GlassVisual.panel(this,22,AppearancePrefs.isDark(this)));
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
        LinearLayout playback=horizontal();playback.setGravity(Gravity.CENTER);
        String[] symbols={"◀◀","▶❚❚","▶▶"};
        int[] mediaKeys={KeyEvent.KEYCODE_MEDIA_PREVIOUS,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,KeyEvent.KEYCODE_MEDIA_NEXT};
        for(int i=0;i<3;i++){
            final int key=mediaKeys[i];
            TextView control=text(symbols[i],17,true);
            control.setTextColor(0xFF214F81);
            control.setGravity(Gravity.CENTER);
            control.setSingleLine(true);
            control.setShadowLayer(0,0,0,0);
            control.setOnClickListener(v->sendMediaKey(key));
            playback.addView(control,new LinearLayout.LayoutParams(0,d(35),1));
        }
        media.addView(playback,new LinearLayout.LayoutParams(-1,d(35)));
        media.setOnClickListener(v->mediaDialog());
        LinearLayout.LayoutParams m=new LinearLayout.LayoutParams(0,d(158),1);
        m.leftMargin=d(7);row.addView(media,m);
        host.addView(row);
    }
    private void addAppRange(LinearLayout host,int begin,int max){
        final int count=Math.min(apps.size(),begin+max);
        for(int start=begin;start<count;start+=4){
            LinearLayout row=horizontal();row.setGravity(Gravity.TOP);
            for(int i=0;i<4;i++){
                int index=start+i;
                if(index<count)row.addView(appCell(apps.get(index)),new LinearLayout.LayoutParams(0,d(AppearancePrefs.gridHeight(this)+11),1));
                else row.addView(new View(this),new LinearLayout.LayoutParams(0,d(AppearancePrefs.gridHeight(this)+11),1));
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
        dock.setBackground(GlassVisual.panel(this,25,AppearancePrefs.isDark(this)));
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
            android.content.ComponentName target=new android.content.ComponentName(
                ri.activityInfo.packageName,ri.activityInfo.name);
            Intent i=Intent.makeMainActivity(target);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(i);
        }catch(Exception e){
            android.util.Log.e("Glass26Launcher","Falha ao iniciar "+ri.activityInfo.packageName,e);
            Toast.makeText(this,"Não foi possível abrir "+ri.loadLabel(getPackageManager()),Toast.LENGTH_LONG).show();
        }
    }
    private void allApps(){
        loadApps();String[] names=new String[apps.size()];
        for(int i=0;i<apps.size();i++)names[i]=apps.get(i).loadLabel(getPackageManager()).toString();
        new AlertDialog.Builder(this).setTitle("Todos os aplicativos")
            .setItems(names,(d,index)->launch(apps.get(index)))
            .setNegativeButton("Voltar",null).show();
    }
    private void sendMediaKey(int code){
        android.media.AudioManager audio=(android.media.AudioManager)getSystemService(AUDIO_SERVICE);
        if(audio!=null){
            audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,code));
            audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,code));
        }
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
                case 7:requestHomeRole();break;
            }
        }).show();
    }
    private void lockPreview(){startActivity(new Intent(this,LockPreviewActivity.class));}
    private void chooseWallpaper(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i,26);
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==ROLE_REQUEST){
            Toast.makeText(this,isHomeLauncher()?"Glass26 selecionado como tela inicial":"Você pode escolher o launcher nas configurações",Toast.LENGTH_SHORT).show();
            render();return;
        }
        if(request==26&&result==RESULT_OK&&data!=null&&data.getData()!=null){
            Uri uri=data.getData();
            try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}
            catch(Exception ignored){}
            getSharedPreferences(AppearancePrefs.PREFERENCES,0).edit().putString("wall",uri.toString()).apply();
            render();
        }
    }
}
