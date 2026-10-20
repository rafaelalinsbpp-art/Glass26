package com.glass26.app;

import android.app.Notification;
import android.content.pm.PackageManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.*;

public final class GlassNotificationListener extends NotificationListenerService {
    public static final class Card {
        public final String id, app, title, message;
        public final long postedAt;
        Card(String id,String app,String title,String message,long postedAt) {
            this.id=id;this.app=app;this.title=title;this.message=message;this.postedAt=postedAt;
        }
    }
    private static final LinkedHashMap<String,Card> active=new LinkedHashMap<>();
    public static synchronized List<Card> snapshot(){
        List<Card> list=new ArrayList<>(active.values());
        list.sort((a,b)->Long.compare(b.postedAt,a.postedAt));
        return list.size()>30?new ArrayList<>(list.subList(0,30)):list;
    }
    private static String shortText(CharSequence c){
        String s=c==null?"":c.toString().replace('\\n',' ');
        return s.length()>200?s.substring(0,200)+"…":s;
    }
    private synchronized void refresh(){
        try {
            StatusBarNotification[] notifications=getActiveNotifications();
            if(notifications==null)return;
            synchronized(GlassNotificationListener.class){active.clear();}
            for(StatusBarNotification n:notifications)onNotificationPosted(n);
        }catch(Exception ignored){}
    }
    @Override public void onListenerConnected(){super.onListenerConnected();refresh();}
    @Override public void onNotificationPosted(StatusBarNotification sbn){
        if(sbn==null || sbn.getNotification()==null)return;
        Notification n=sbn.getNotification();
        if((n.flags & Notification.FLAG_GROUP_SUMMARY)!=0)return;
        String title=shortText(n.extras.getCharSequence(Notification.EXTRA_TITLE));
        String message=shortText(n.extras.getCharSequence(Notification.EXTRA_TEXT));
        String app=sbn.getPackageName();
        try{
            PackageManager pm=getPackageManager();
            app=String.valueOf(pm.getApplicationLabel(pm.getApplicationInfo(sbn.getPackageName(),0)));
        }catch(Exception ignored){}
        synchronized(GlassNotificationListener.class){
            active.put(sbn.getKey(),new Card(sbn.getKey(),app,title,message,sbn.getPostTime()));
            if(active.size()>80){
                String first=active.keySet().iterator().next();
                active.remove(first);
            }
        }
    }
    @Override public void onNotificationRemoved(StatusBarNotification sbn){
        if(sbn!=null)synchronized(GlassNotificationListener.class){active.remove(sbn.getKey());}
    }
    @Override public void onListenerDisconnected(){
        synchronized(GlassNotificationListener.class){active.clear();}
        super.onListenerDisconnected();
    }
}
