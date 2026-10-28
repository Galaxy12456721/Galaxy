package com.galaxy.checkin;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {
    private SharedPreferences sp;
    private Calendar selected = Calendar.getInstance();
    private CalendarView calendar;
    private TextView streak, cards, stats, month;
    private int cardCount;

    private String key(Calendar c) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }
    private String todayKey() { return key(Calendar.getInstance()); }

    private JSONObject load() {
        try { return new JSONObject(sp.getString("data", "{}")); }
        catch(Exception e) { return new JSONObject(); }
    }
    private void save(JSONObject o) {
        sp.edit().putString("data", o.toString()).apply();
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("galaxy", MODE_PRIVATE);
        calendar = findViewById(R.id.calendar);
        streak = findViewById(R.id.streak);
        cards = findViewById(R.id.cards);
        stats = findViewById(R.id.stats);
        month = findViewById(R.id.month);

        calendar.setOnDateChangeListener((v,y,m,d) -> {
            selected.set(y,m,d);
            update();
        });

        findViewById(R.id.checkin).setOnClickListener(v -> checkToday());
        findViewById(R.id.makeup).setOnClickListener(v -> useMakeup());

        selected.setTime(new Date());
        update();
    }

    private void checkToday() {
        Calendar now = Calendar.getInstance();
        JSONObject o = load();
        try {
            String k = key(now);
            if (!o.has(k)) {
                o.put(k, "actual");
                awardCards(o);
                save(o);
                Toast.makeText(this, "Galaxy：今天打卡成功 ✦", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "今天已经打卡啦", Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) {}
        update();
    }

    private void useMakeup() {
        String k = key(selected);
        if (k.equals(todayKey())) {
            Toast.makeText(this, "补签卡不能用于今天", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject o = load();
        try {
            if (o.has(k)) {
                Toast.makeText(this, "这个日期已经有记录", Toast.LENGTH_SHORT).show();
                return;
            }
            int count = o.optInt("_cards", 0);
            if (count <= 0) {
                Toast.makeText(this, "没有补签卡了", Toast.LENGTH_SHORT).show();
                return;
            }
            o.put(k, "makeup");
            o.put("_cards", count - 1);
            save(o);
            Toast.makeText(this, "补签成功，连续打卡已修复", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {}
        update();
    }

    private void awardCards(JSONObject o) throws Exception {
        int actual = 0;
        Iterator<String> it = o.keys();
        while(it.hasNext()) {
            String k = it.next();
            if (!k.startsWith("_") && "actual".equals(o.optString(k))) actual++;
        }
        int earned = actual / 30;
        int claimed = o.optInt("_claimed", 0);
        int count = o.optInt("_cards", 0);
        if (earned > claimed) {
            int add = earned - claimed;
            count = Math.min(3, count + add);
            o.put("_cards", count);
            o.put("_claimed", earned);
        }
    }

    private boolean done(JSONObject o, Calendar c) {
        return o.has(key(c));
    }

    private int currentStreak(JSONObject o) {
        Calendar c = Calendar.getInstance();
        if (!done(o,c)) c.add(Calendar.DATE, -1);
        int n = 0;
        while(done(o,c) && n < 10000) { n++; c.add(Calendar.DATE,-1); }
        return n;
    }

    private int longestStreak(JSONObject o) {
        ArrayList<String> dates = new ArrayList<>();
        Iterator<String> it = o.keys();
        while(it.hasNext()) {
            String k = it.next();
            if (!k.startsWith("_")) dates.add(k);
        }
        Collections.sort(dates);
        int best=0, run=0;
        Calendar prev=null;
        for(String s: dates) {
            try {
                Calendar cur=Calendar.getInstance();
                cur.setTime(new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(s));
                if(prev!=null) {
                    Calendar x=(Calendar)prev.clone(); x.add(Calendar.DATE,1);
                    if(key(x).equals(s)) run++; else run=1;
                } else run=1;
                best=Math.max(best,run); prev=cur;
            } catch(Exception ignored) {}
        }
        return best;
    }

    private int actualDays(JSONObject o) {
        int n=0; Iterator<String> it=o.keys();
        while(it.hasNext()) { String k=it.next(); if(!k.startsWith("_") && "actual".equals(o.optString(k))) n++; }
        return n;
    }

    private int makeupDays(JSONObject o) {
        int n=0; Iterator<String> it=o.keys();
        while(it.hasNext()) { String k=it.next(); if(!k.startsWith("_") && "makeup".equals(o.optString(k))) n++; }
        return n;
    }

    private void update() {
        JSONObject o=load();
        try { awardCards(o); save(o); } catch(Exception ignored) {}
        cardCount=o.optInt("_cards",0);
        streak.setText("🔥 连续\n"+currentStreak(o)+" 天");
        cards.setText("🎫 补签卡\n"+cardCount+" / 3");
        month.setText(new SimpleDateFormat("yyyy年 M月",Locale.CHINA).format(selected.getTime()));
        stats.setText("累计正常打卡 "+actualDays(o)+" 天  ·  补签 "+makeupDays(o)+" 天\n最长连续 "+longestStreak(o)+" 天");
    }
}
