//===-                           P L E B B L E
//===-                         https://plebble.us
//===-
//===-              Copyright (C) 2017-2022 root1m3@plebble.us
//===-
//===-                      GNU GENERAL PUBLIC LICENSE
//===-                       Version 3, 29 June 2007
//===-
//===-    This program is free software: you can redistribute it and/or modify
//===-    it under the terms of the GPLv3 License as published by the Free
//===-    Software Foundation.
//===-
//===-    This program is distributed in the hope that it will be useful,
//===-    but WITHOUT ANY WARRANTY; without even the implied warranty of
//===-    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//===-
//===-    You should have received a copy of the General Public License
//===-    along with this program, see LICENCE file.
//===-    see https://www.gnu.org/licenses
//===-
//===----------------------------------------------------------------------------
//===-
package us.cash;
import androidx.appcompat.app.ActionBarDrawerToggle;                                           // ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity;                                               // AppCompatActivity
import us.gov.crypto.base58;                                                                   // base58
import us.gov.io.blob_reader_t;                                                                // blob_reader_t
import us.wallet.trader.bookmark_t;                                                            // bookmark_t
import com.google.android.material.bottomsheet.BottomSheetBehavior;                            // BottomSheetBehavior
import android.os.Build;                                                                       // Build
import android.os.Bundle;                                                                      // Bundle
import static us.wallet.trader.chat.chat_t;                                                    // chat_t
import android.graphics.Color;                                                                 // Color
import android.widget.CompoundButton;                                                          // CompoundButton
import android.content.Context;                                                                // Context
import us.gov.socket.datagram;                                                                 // datagram
import us.wallet.trader.data_t;                                                                // data_t
import us.wallet.trader.endpoint_t;                                                            // endpoint_t
import com.google.firebase.crashlytics.FirebaseCrashlytics;                                    // FirebaseCrashlytics
import androidx.fragment.app.Fragment;                                                         // Fragment
import androidx.fragment.app.FragmentTransaction;                                              // FragmentTransaction
import android.widget.FrameLayout;                                                             // FrameLayout
import static us.gov.crypto.ripemd160.hash_t;                                                  // hash_t
import android.widget.ImageView;                                                               // ImageView
import static us.gov.io.types.*;                                                               // *
import static us.stdint.*;                                                                     // *
import static us.tuple.*;                                                                      // *
import android.content.Intent;                                                                 // Intent
import static us.ko.is_ko;                                                                     // is_ko
import us.ko;                                                                                  // ko
import android.widget.LinearLayout;                                                            // LinearLayout
import com.google.android.material.button.MaterialButton;                                      // MaterialButton
import android.view.Menu;                                                                      // Menu
import android.view.MenuItem;                                                                  // MenuItem
import androidx.annotation.NonNull;                                                            // NonNull
import static us.ko.ok;                                                                        // ok
import us.pair;                                                                                // pair
import us.wallet.protocol;                                                                     // protocol
import us.wallet.trader.qr_t;                                                                  // qr_t
import android.widget.RelativeLayout;                                                          // RelativeLayout
import androidx.annotation.RequiresApi;                                                        // RequiresApi
import us.string;                                                                              // string
import android.widget.Switch;                                                                  // Switch
import android.widget.TextView;                                                                // TextView
import java.util.Timer;                                                                        // Timer
import java.util.TimerTask;                                                                    // TimerTask
import android.widget.Toast;                                                                   // Toast
import android.media.ToneGenerator;                                                            // ToneGenerator
import androidx.appcompat.widget.Toolbar;                                                      // Toolbar
import android.view.View;                                                                      // View

public class trader extends activity implements datagram_dispatcher_t.handler_t  {

    static final int PROTOCOLROLE_RESULT = 902;
//    static final int TX_RESULT = 914;

    static void log(final String line) {         //--strip
       CFG.log_android("trader: " + line);   //--strip
    }                                            //--strip

    public synchronized data_t get_data() {
        return data;
    }

    public synchronized String get_src_data() {
        return src_data;
    }

    private synchronized void set_data(String src, data_t x) {
        src_data = src;
        data = x;
    }

/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                // go to previous screen when app icon in action bar is clicked
                Intent intent = new Intent(this, trades.class);
                startActivityForResult(intent,TRADES_RESULT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
*/

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        log("WORKAROUND_FOR_BUG_19917"); //--strip
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate"); //--strip
        setContentView(R.layout.activity_trader);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(findViewById(R.id.toolbar));
//TODO review
//        drawerLayout = findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.bookmarks, R.string.bookmarks);
//        drawerLayout.addDrawerListener(toggle);
//        toggle.syncState();
//        navigationView = findViewById(R.id.navigation_view);
//        navigationView.setNavigationItemSelectedListener(this);
        //Menu nav_menu = navigationView.getMenu();
        //MenuItem menuItem = nav_menu.findItem(R.id.nav_balance);
        //menuItem.setChecked(true);


        //getSupportActionBar().hide();
        tradeid = findViewById(R.id.tradeid);
        //tradeid.setBackgroundColor(Color.parseColor("#32bac2"));

        progressbarcontainer = findViewById(R.id.progressbarcontainer);

        if (getIntent().hasExtra("tid")) {
            Bundle bundle = getIntent().getExtras();
            tid = new hash_t(bundle.getByteArray("tid"));
            log("tid=" + tid.encode()); //--strip
        }
        else {
            log("KO 40310 Missing tid."); //--strip
            finish();
        }
        toolbar.setTitle(getResources().getString(R.string.trader) + " #" + tid.encode());

/*
        if (getIntent().hasExtra("referrer_tid")) {
            Bundle bundle = getIntent().getExtras();
            referrer_tid = new hash_t(bundle.getByteArray("referrer_tid"));
            log("referrer_tid = "+referrer_tid.encode()); //--strip
        }
*/

        tradeid.setText("id " + tid.encode());
        procol = findViewById(R.id.procol);
        procol_cap = findViewById(R.id.procol_cap);
        toolbar_button refresh = findViewById(R.id.refresh);

        bottom_sheet = findViewById(R.id.bottom_sheet);
        button_chat = findViewById(R.id.button_chat);
        button_chat.setVisibility(View.VISIBLE);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        toolbar_button action = findViewById(R.id.action);
        action.setVisibility(View.GONE);

        chat_handlers();

        vf = 0;
        ft = new fragment_trader();
        ft.setArguments(getIntent().getExtras());
        log("Set Fragment Trader"); //--strip
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, ft).commitAllowingStateLoss();

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invalidate_data_fetch0();
            }
        });

        procol.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                log("TOUCHED ROLE VIEW"); //--strip
                if (cur_protocol == null) {
                    log("cur_protocol is null"); //--strip
                }
                else {
                    if (procol.isChecked()) {
                        log("checked vf = " + vf); //--strip
                        if (vf != 1) {
                            log("vf must change to 1"); //--strip
                            vf = 1;
                        }
                    }
                    else {
                        log("not checked vf = " + vf); //--strip
                        if (vf != 0) {
                            log("vf must change to 0"); //--strip
                            vf = 0;
                        }
                    }
                }
                refresh();
            }
        });

        log("connect 'dispatch' to network-datagram hose"); //--strip
        dispatchid = a.datagram_dispatcher.connect_sink(this);
        invalidate_data_fetch0();
    }

    @Override
    public void onDestroy() {
        log("onDestroy " + a.datagram_dispatcher); //--strip

        super.onDestroy();

        a.datagram_dispatcher.disconnect_sink(dispatchid);
    }

    @Override public void on_push(final hash_t target_tid, final uint16_t code, final byte[] payload) {
        log("on_push"); //--strip
        if (!tid.equals(target_tid)) {
            return;
        }
        switch(code.value) {
            case us.wallet.trader.trader_t.push_data: {
                log("a data for me");  //--strip
                string s = new string();
                ko r = us.gov.io.blob_reader_t.parse(new blob_t(payload), s);
                if (is_ko(r)) {
                    return;
                }
                setdata__worker(s.value);
                log("OK - Imported data"); //--strip
            }
            break;
            case us.wallet.trader.trader_t.push_roles: {
                us.wallet.trader.roles_t roles = new us.wallet.trader.roles_t();
                ko r = us.gov.io.blob_reader_t.parse(new blob_t(payload), roles);
                if (is_ko(r)) {
                    return;
                }
                log("roles for me");  //--strip
                setroles(roles);
                log("OK - Imported roles"); //--strip
            }
            break;
            case us.wallet.trader.trader_t.push_chat: {
                log("a chat for me "); //--strip
                openchat_worker(payload);
            }
            break;
            case us.wallet.trader.trader_t.push_killed: {
                log("killed "); //--strip
                archive();
            }
            break;
        }
    }

    void chat_handlers() {
        button_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    /*
                    log("set timer refresh fragment"); //--strip
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chat_fragment.smessage.requestFocus();
                                }
                            });
                        }
                    }, 100);
                    */
                }
                else if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                else {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });

        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        //bottom_sheet.getLayoutParams().height = 400; //ViewGroup.LayoutParams.MATCH_PARENT;
                        //sheetBehavior.setPeekHeight(400, true);
                        openchat(null);
                        //MaterialButton button_chat = view.findViewById(R.id.button_chat);
                        //button_chat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_chat, 0, R.drawable.ic_arrow_drop_down, 0);
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        //sheetBehavior.setPeekHeight(0, true);
                        //MaterialButton button_chat = view.findViewById(R.id.button_chat);
                        //button_chat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_chat, 0, R.drawable.ic_arrow_drop_up, 0);
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
    }

    void openchat_worker(final byte[] chatpayload) {
        app.assert_worker_thread(); //--strip
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                openchat(chatpayload);
            }
        });
    }

    public String getendpoint() {
        //app.assert_ui_thread(); //--strip
        String endpoint = data.find("remote_endpoint");
        if (endpoint == null) {
            endpoint = "";
        }
        return endpoint;
    }

    public String peer_moniker() {
        //app.assert_ui_thread(); //--strip
        String s = data.find("peer_moniker");
        if (s == null) {
            s = "Anonymous";
        }
        return s;
    }

    public hash_t gettid() {
        return tid;
    }

/*
    public void openchat(final String label) { //TODO label must come from data
        openchat(label, null, false, null);
    }
*/

    boolean setroles(final us.wallet.trader.roles_t roles) {
        this.roles = roles;
/*
        aroles = s.split("\\r?\\n");
        log("sdata = " + s); //--strip

        //    2 prot role
        String[] a = s.split("\\s",2);
        int n = Integer.parseInt(a[0]);
        aroles = new String[n];
        for (int i = 0; i < n; ++i) {
            String[] b = a[1].split("\\s", 3);
            aroles[i] = b[0] + " " + b[1];
            a[1] = b[2];
        }
*/
        if (forward_roles != null) {
            final fragment_trader fr = forward_roles;
            final us.wallet.trader.roles_t r = this.roles;
            forward_roles = null;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fr.select_roles(r);
                }
            });
        }
        return true;
    }

    boolean setdata0(final String sdata) {
        log("sdata:"); //--strip
        log("-------------------------");//--strip
        log(sdata);//--strip
        log("-------------------------");//--strip
        data_t dat = new data_t();
        ko r = dat.from(sdata);
        if (is_ko(r)) {
            log(r.msg); //--strip
        }
        set_data(src_data, dat);
        return true;
    }

    boolean setdata1(final String sdata) {
        log("new data object "); //--strip
        if (!setdata0(sdata)) {
            log("KO 30306 failed setdata0"); //--strip
            return false;
        }
        if (get_data() == null) {
            log("data is null"); //--strip
            cur_protocol = null;
            vf = 0;
        }
        else {
            log("data is not null"); //--strip
            String r = data.find("protocol");
            if (r == null) {
                log("KO 40322 'protocol' entry cannot be found in data"); //--strip
                cur_protocol = null;
                vf = 0;
            }
            else {
                log("'protocol' entry found in data: " + r); //--strip
                if (r.equals("not set")) {
                    log("'protocol' is not set:"); //--strip
                    cur_protocol = null;
                    vf = 0;
                }
                else {
                    log("'protocol' set to " + r); //--strip
                    if (!r.equals(cur_protocol)) {
                        cur_protocol = r;
                        vf = 1;
                    }
                }
            }
            String pt = data.find("parent_trade");
            if (pt == null) {
                parent_trade = new hash_t(0);
            }
            else {
                parent_trade = new hash_t(base58.decode(pt));
            }

        }
        log("cur_protocol=" + cur_protocol); //--strip
        return true;
    }

    boolean setdata__worker(final String sdata) {
        app.assert_worker_thread(); //--strip
        setdata1(sdata);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                log("data has been renewed -> refresh"); //--strip
                refresh();
            }
        });

        return true;
    }

    void get_sourceshit_qrs(String key, Context ctx) {
        app.assert_ui_thread(); //--strip

        if (command_show_param_qrs(key, ctx)) {
            return;
        }
        log("requesting " + key); //--strip
        request_item_qrs(key, ctx); //ask other node
    }

    void get_sourceshit(String key, Context ctx) {
        app.assert_ui_thread(); //--strip

        if (command_show_param(key, ctx)) {
            return;
        }
        log("requesting " + key); //--strip
        request_item(key, ctx); //ask other node
    }


    void on_redirect(final bookmark_t redirect) {
        log("redirection to " + redirect.to_string()); //--strip
        //String[] rd=redirect.split("-", 2);
        //if (rd.length!=2) {
        //    log("KO 6859 - Unrecognized endpoint in pattern: '"+redirect+"'"); //--strip
        //    return;
        //}
        log("referrertid " + tid.encode()); //--strip
        //log("go_endpoint " + rd[0].trim()); //--strip
        main.new_trade(tid, "", redirect.qr);
        Toast.makeText(trader.this, R.string.newtradeadded, 6000).show();  //--strip
        /*
        Intent data = new Intent();
        //data.setData(Uri.parse(redirect));
        data.putExtra("referrer_tid",tid);
        data.putExtra("go_endpoint",rd[0].trim());
        data.putExtra("go_endpoint_text",rd[1].trim());
        setResult(RESULT_OK, data);
        finish();
        */
    }

    public void go_parent_trade() {

        if (parent_trade == null) {
            log("KO 01281 Nowhere to go"); //--strip
            return;
        }
        if (parent_trade.is_zero()) {
            log("KO 01282 Nowhere to go"); //--strip
            return;
        }

        Intent data = new Intent();
        main.go_trade(parent_trade);
        /*
        Intent data = new Intent();
        //data.putExtra("referrer_tid",tid);
        data.putExtra("gotid",referrer_tid);
        setResult(RESULT_OK, data);
        finish();
        */
    }

    public void setmode_loading() {
        app.assert_ui_thread(); //--strip
        progressbarcontainer.setVisibility(View.VISIBLE);
    }

    public void setmode_ready() {
        app.assert_ui_thread(); //--strip
        progressbarcontainer.setVisibility(View.GONE);
    }

    public void setmode_wrong(final String reason) {
        log("setmode_wrong " + reason); //--strip
        app.assert_ui_thread(); //--strip
        progressbarcontainer.setVisibility(View.GONE);
        //progressbarcontainer.setVisibility(View.VISIBLE);
    }

    public void invalidate_data_fetch0() {
        app.assert_ui_thread(); //--strip
        if (a.hmi == null) {
            log("HMI is not ready"); //--strip
            Toast.makeText(getApplicationContext(), "HMI is not ready", 6000).show();
            return;
        }
        setmode_loading();
        log("invalidate_data_fetch"); //--strip
        set_data(null, null);
        //get_sourceshit("data");
        //String key="data";
        log("show data"); //--strip
        a.hmi.command_trade(tid, "show data"); //backend will push data
    }

    void refresh() {
        log("refresh"); //--strip
        app.assert_ui_thread(); //--strip
        log("cur_protocol " + cur_protocol); //--strip
        log("procol.isChecked " + procol.isChecked()); //--strip

        if (get_data() == null) {
            setmode_wrong("KO 8675");
            return;
        }

        if (cur_protocol != null) {
            procol.setEnabled(true);
            if (vf == 1) {
                procol.setChecked(true);
                procol_cap.setText(" Role View - " + cur_protocol);
            }
            else {
                procol_cap.setText(" Trader View");
                procol.setChecked(false);
            }
        }
        else {
            procol.setChecked(false);
            procol.setEnabled(false);
            procol_cap.setText(" Trader View");
        }

        if (vf == 1) {
            log("role view"); //--strip
            log("create fragment for cur_protocol=" + cur_protocol); //--strip
            if (!create_ui(cur_protocol)) {
                log("KO 5032 - "); //--strip
                Toast.makeText(getApplicationContext(), "Cannot create UI " + cur_protocol, 6000).show();
            }
            else {
                log("replacing fragment ->1"); //--strip
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, specialized_fragment).commitAllowingStateLoss();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, specialized_fragment).commitNowAllowingStateLoss(); //AllowingStateLoss();
            }
        }
        else {
            log("trader view"); //--strip
            log("replacing fragment ->0"); //--strip
            try {
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, ft).commitAllowingStateLoss();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, ft).commitNowAllowingStateLoss(); //AllowingStateLoss();
            }
            catch(Exception e) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.cannotcreatetraderUI), 6000).show();
                error_manager.manage(e, e.getMessage() + "    " + e.toString());
            }
        }

        if (vf == 0) {
            log("call refreshfragment 0");  //--strip
            //ft.refresh();
            if (ft != null) {
                if (ft.getContext() != null) {
                    ft.refresh();
                }
            }
        }
        else {
            log("call refreshfragment 1");  //--strip
            if (specialized_fragment != null) {
                if (specialized_fragment.getContext() != null) {
                    specialized_fragment.refresh();
                }
            }
        }

/*
        log("set timer refresh fragment"); //--strip
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (vf == 0) {
                            log("call refreshfragment 0");  //--strip
                            ft.refresh();
                        }
                        else {
                            log("call refreshfragment 1");  //--strip
                            if (specialized_fragment != null) {
                                if (specialized_fragment.getContext() != null) {
                                    specialized_fragment.refresh();
                                }
                            }
                        }
                    }
                });
            }
        }, 10);
  */
    }

/*
    int busycount=0;

    public synchronized void clearbusy() {
        busycount--;
        if (busycount==0) {
            if (Looper.getMainLooper().isCurrentThread()) {
                netactivity.setVisibility(View.INVISIBLE);
            }
            else {
                runOnUiThread(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        netactivity.setVisibility(View.INVISIBLE);
                    }
                }));
            }
        }
    }

    public synchronized void setbusy() {
        busycount++;
        if (busycount==1) {
            if (Looper.getMainLooper().isCurrentThread()) {
                netactivity.setVisibility(View.VISIBLE);
            }
            else {
                runOnUiThread(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        netactivity.setVisibility(View.VISIBLE);
                    }
                }));
            }
        }
    }
*/

    boolean create_ui(String protocol) {
        if (protocol == null) return false;
        log("create_ui for " + protocol); // --strip
        if (protocol.equals("bid2ask bid")) {
            if (specialized_fragment != null) {
                if (specialized_fragment instanceof fragment_bid2ask_bid) {
                    log("right fragment in place"); // --strip
                    return true;
                }
            }
            log("new instance fragment_bid2ask_bid"); // --strip
            specialized_fragment = new fragment_bid2ask_bid();
        }
        else if (protocol.equals("pat2slt pat")) {
            if (specialized_fragment != null) {
                if (specialized_fragment instanceof fragment_pat2slt_pat) {
                    log("right fragment in place"); // --strip
                    return true;
                }
            }
            log("new instance"); // --strip
            specialized_fragment = new fragment_pat2slt_pat();
        }
        else if (protocol.equals("pat2phy pat")) {
            if (specialized_fragment != null) {
                if (specialized_fragment instanceof fragment_pat2phy_pat) {
                    log("right fragment in place"); // --strip
                    return true;
                }
            }
            log("new instance"); // --strip
            specialized_fragment = new fragment_pat2phy_pat();
        }
        else if (protocol.equals("pat2ai pat")) {
            if (specialized_fragment != null) {
                if (specialized_fragment instanceof fragment_pat2ai_pat) {
                    log("right fragment in place"); // --strip
                    return true;
                }
            }
            log("new instance"); // --strip
            specialized_fragment = new fragment_pat2ai_pat();
        }
        else if (protocol.equals("w2w w")) {
            if (specialized_fragment != null) {
                if (specialized_fragment instanceof fragment_w2w) {
                    log("right fragment in place"); // --strip
                    return true;
                }
            }
            log("new instance"); // --strip
            specialized_fragment = new fragment_w2w();
        }
        else {
            return false;
        }
        log("Setting args in specialized fragment"); // --strip
        specialized_fragment.setArguments(getIntent().getExtras());
        return true;
    }


    public void start_protocol(String selected_protocol) {
        log("start_protocol " + selected_protocol);  //--strip
        a.start_protocol(tid, selected_protocol);
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        log("onActivityResult " + requestCode + " " + resultCode); //--strip
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            //Toast.makeText(this, "Cancelled", 1000).show();
            return;
        }
        if (requestCode == PROTOCOLROLE_RESULT) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                String prtocol = data.getDataString();
                log("prtocol=" + prtocol); //--strip
                start_protocol(prtocol);
            }
            else if (resultCode == 111) {
                String KOreason = data.getDataString();
                Toast.makeText(this, KOreason, 6000).show();
            }
            else if (resultCode == 112) {
                String KOreason = data.getDataString();
                Toast.makeText(this, getResources().getString(R.string.noptrotocolsavailable) + KOreason, 6000).show();
            }
            else {
                Toast.makeText(this, getResources().getString(R.string.sorryselectionunsuccesful), 1000).show();
            }
            return;
        }
/*
        if (requestCode == TX_RESULT) {
            log("TX_RESULT"); //--strip
            app.assert_ui_thread(); //--strip
            // Check which request we're responding to
            final String actioncommand = data.getExtras().getString("actioncommand");
            log("deferred actioncommand=" + actioncommand); //--strip
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            a.hmi.command_trade(tid, actioncommand); //backend will push     ~
                        }
                    });
                }
            }, 300);
            return;
        }
*/
        /*
        else if (requestCode==CHAT_RESULT) {
            log("CHAT_RESULT"); //--strip
            chat_opened=false;

            final String dta = data.getExtras().getString("data");
            if (dta!=null) {
                log("chat brought an update on data"); //--strip
                setdata_ui(dta);
            }
        }
        */
        specialized_fragment.onActivityResult(requestCode, resultCode, data);
    }

/*
    public void send_command(final String cmd) {
        app.assert_ui_thread(); //--strip
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        a.hmi.command_trade(tid, cmd); //backend will push     ~
                    }
                });
            }
        }, 300);
    }
*/

/*
    public void send_doc(workflow_item i) {
        log("send_doc "+i.cmd_action); //--strip
        a.hmi.command_trade(tid, i.cmd_action);
    }


    public void doc_viewer(workflow_item i, boolean action) {
        Intent intent = new Intent(getApplicationContext(), tx.class);
        intent.putExtra("tid", tid.value);
        intent.putExtra("title", i.title);
        intent.putExtra("contentcommand", i.cmd_content);
        intent.putExtra("actioncommand", action ? i.cmd_action : null);
        intent.putExtra("doccode", i.code.value);
        intent.putExtra("icon", i.icon);
        startActivityForResult(intent,TX_RESULT);
    }

*/

/*
    public void doc_viewer(final String title, final short doccode, final String contentcommand, final String actioncommand, int icon) {
        Intent intent = new Intent(getApplicationContext(), tx.class);
        intent.putExtra("tid",tid);
        intent.putExtra("title",title);
        intent.putExtra("contentcommand",contentcommand);
        intent.putExtra("actioncommand",actioncommand);
        intent.putExtra("doccode",doccode);
        intent.putExtra("icon",icon);
        startActivityForResult(intent,TX_RESULT);
    }
*/

    boolean require_request(String key) {
        boolean rr = true;
        if (key.equals("log")) {
            rr = false;
        }
        return rr;
    }

    boolean command_show_param_qrs(final String key, Context ctx) {
        app.assert_ui_thread(); //--strip

        String val = data.find(key);
        if (val != null) {
            log("val not null");  //--strip
            if (val.equals("Y")) {
                log("key '" + key + "' found Y");  //--strip
                a.hmi.command_trade(tid, "show " + key + " peer");
                return true;
            }
            log("val is " + val);  //--strip
        }
        else {
            log("val is null");  //--strip
        }
        return false;
    }

    boolean command_show_param(final String key, Context ctx) {
        app.assert_ui_thread(); //--strip

        boolean rr = require_request(key);
        if (rr) {
            String val = data.find(key);
            if (val != null) {
                log("val not null");  //--strip
                if (val.equals("Y")) {
                    log("key '" + key + "' found Y");  //--strip
                    a.hmi.command_trade(tid, "show " + key);
                    return true;
                }
                log("val is " + val);  //--strip
            }
            else {
                log("val is null");  //--strip
            }
        }
        else {
            a.hmi.command_trade(tid, "show " + key);
            return true;
        }
        return false;
    }

    void request_item_qrs(final String key, Context ctx) {
        app.assert_ui_thread(); //--strip
        a.hmi.command_trade(tid, "request qrs");
    }

    void request_item(final String key, Context ctx) {
        app.assert_ui_thread(); //--strip
        //String cmdpfx=get_cmdpfx(key);

        String request_verb = "request";
        if (key.equals("invoice")) { //chapu until backend verb is consistent across keys
            request_verb = "query";
        }
        a.hmi.command_trade(tid, key + " " + request_verb);
    }

    public void openchat(byte[] chatpayload) {
        app.assert_ui_thread(); //--strip

//        if (frompush) {
//            MaterialButton button_chat = findViewById(R.id.button_chat);
//            button_chat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_chat, 0, R.drawable.ic_arrow_drop_down, 0);
//        }


        if (chat_fragment == null) {
            String lbl = peer_moniker();
            log("Starting fragment chat_fragment"); //--strip
            //chat_opened = true;
            chat_fragment = new chat();
            Bundle fbundle = new Bundle();
            fbundle.putByteArray("tid", tid.value);
            fbundle.putString("lbl", lbl);
            //fbundle.putString("follower", label);
            fbundle.putByteArray("raw", chatpayload);
            chat_fragment.setArguments(fbundle);
            final androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.bottom_sheet, chat_fragment);
            transaction.addToBackStack(null);
            try {
                log("Commit fragment tx"); //--strip
                transaction.commitAllowingStateLoss();
            }
            catch (Exception e) {
                log("Ignoring exception " + e.getMessage()); //--strip
            }
        }

        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            a.tone.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_NORMAL, 150);
        }

    }

    void archive() {
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(trader.main, "This trade has been archived.", 6000).show();
                finish();
            }
        }));
    }


    void close_trade() { //means archive
        app.assert_ui_thread(); //--strip
        final pair<ko, String> b = a.hmi.kill_trade(tid);
        if (is_ko(b.first)) {
            Toast.makeText(trader.this, b.first.msg, 6000).show();
            return;
        }
        finish();
/*
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final pair<ko, String> b = a.hmi.kill_trade(tid);
                runOnUiThread(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (b.first != ok) {
                            Toast.makeText(trader.this, b.first.msg, 6000).show();
                        }
                        else {
                            finish();
                        }
                    }
                }));
            }
        });
        thread.start();
*/
    }

    private Switch procol;
    private TextView procol_cap;
    private TextView tradeid;
    private fragment_trader ft = null;
    private role_fragment specialized_fragment = null;
    private RelativeLayout progressbarcontainer;
    private int vf; //displayed fragment
    public hash_t tid;
    public hash_t parent_trade = null;
    public String cur_protocol = null;
//    public hash_t referrer_tid = null;
    int dispatchid;
    private data_t data = null;
    private String src_data = null;
//    public String[] aroles = null;
    public us.wallet.trader.roles_t roles = null;
    public fragment_trader forward_roles = null;
    chat chat_fragment = null;

//    boolean chat_opened = false;
    private BottomSheetBehavior sheetBehavior;
    private FrameLayout bottom_sheet;
    private toolbar_button button_chat;

}

