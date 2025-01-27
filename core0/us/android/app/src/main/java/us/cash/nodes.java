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
import android.app.Activity;                                                                   // Activity
import android.widget.AdapterView;                                                             // AdapterView
import androidx.appcompat.app.AlertDialog;                                                     // AlertDialog
import android.widget.ArrayAdapter;                                                            // ArrayAdapter
import java.util.ArrayList;                                                                    // ArrayList
import android.graphics.BitmapFactory;                                                         // BitmapFactory
import android.graphics.Bitmap;                                                                // Bitmap
import us.wallet.trader.bookmarks_t;                                                           // bookmarks_t
import us.wallet.trader.bookmark_t;                                                            // bookmark_t
import android.os.Build;                                                                       // Build
import android.os.Bundle;                                                                      // Bundle
import java.util.Collections;                                                                  // Collections
import android.widget.CompoundButton;                                                          // CompoundButton
import android.content.Context;                                                                // Context
import androidx.appcompat.view.ContextThemeWrapper;                                            // ContextThemeWrapper
import us.gov.socket.datagram;                                                                 // datagram
import static android.graphics.BitmapFactory.decodeResource;                                   // decodeResource
import android.content.DialogInterface;                                                        // DialogInterface
import android.widget.EditText;                                                                // EditText
import us.wallet.trader.endpoint_t;                                                            // endpoint_t
import com.google.firebase.crashlytics.FirebaseCrashlytics;                                    // FirebaseCrashlytics
import java.util.HashMap;                                                                      // HashMap
import us.gov.crypto.ripemd160.hash_t;                                                         // hash_t
import android.widget.ImageView;                                                               // ImageView
import static us.ko.*;                                                                         // *
import static us.stdint.*;                                                                     // *
import android.view.inputmethod.InputMethodManager;                                            // InputMethodManager
import android.text.InputType;                                                                 // InputType
import android.content.Intent;                                                                 // Intent
import static us.ko.is_ko;                                                                     // is_ko
import java.util.Iterator;                                                                     // Iterator
import us.ko;                                                                                  // ko
import android.view.LayoutInflater;                                                            // LayoutInflater
import java.util.Map;                                                                          // Map
import android.view.Menu;                                                                      // Menu
import android.view.MenuItem;                                                                  // MenuItem
import android.view.View.OnFocusChangeListener;                                                // OnFocusChangeListener
import us.pair;                                                                                // pair
import us.wallet.protocol;                                                                     // protocol
import us.wallet.trader.qr_t;                                                                  // qr_t
import android.widget.RelativeLayout;                                                          // RelativeLayout
import androidx.annotation.RequiresApi;                                                        // RequiresApi
import us.string;                                                                              // string
import android.widget.Switch;                                                                  // Switch
import android.widget.TextView;                                                                // TextView
import android.widget.Toast;                                                                   // Toast
import androidx.appcompat.widget.Toolbar;                                                      // Toolbar
import android.view.ViewGroup;                                                                 // ViewGroup
import android.view.View;                                                                      // View

public final class nodes extends activity implements datagram_dispatcher_t.handler_t {

    static void log(final String line) {         //--strip
       CFG.log_android("nodes: " + line);        //--strip
    }                                            //--strip

    public static class adapter_t extends ArrayAdapter<pair<String, bookmark_t>> {
        private LayoutInflater inflater;

        public adapter_t(Activity ac, ArrayList<pair<String, bookmark_t>> data) {
            super(ac, R.layout.trade_list_item, data);
            log("adapter constructor"); //--strip
            inflater = (LayoutInflater)ac.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        private class view_holder {
            TextView subject;
            TextView tvitem;
            TextView tvitem2;
            ImageView image;
        }

        @Override
        public View getView(int position, View convert_view, ViewGroup parent) {
            view_holder holder = null;
            //View vi = convert_view;
            pair<String, bookmark_t> sbm = getItem(position);
            bookmark_t bm = sbm.second;
            if (convert_view == null) {
                convert_view = inflater.inflate(R.layout.node_list_item, null, true);
                holder = new view_holder();
                holder.subject = convert_view.findViewById(R.id.subject);
                holder.tvitem = convert_view.findViewById(R.id.the_item);
                holder.tvitem2 = convert_view.findViewById(R.id.the_item2);
                holder.image = convert_view.findViewById(R.id.img);
                convert_view.setTag(holder);
            }
            else {
                holder = (view_holder) convert_view.getTag();
            }
            log("bm.get_label() " + bm.get_label()); //--strip
            //log("bm.qr.to_string() " + bm.qr.to_string()); //--strip
            holder.subject.setText(sbm.first);
            holder.tvitem.setText(bm.get_label());
            holder.tvitem2.setText(bm.qr.to_string());
            if (bm.ico == null || bm.ico.length == 0) {
                holder.image.setImageResource(R.drawable.ic_node_busy);
            }
            else {
                try {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bm.ico, 0, bm.ico.length);
//                    holder.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, holder.image.getWidth(), holder.image.getHeight(), false));
                    holder.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, 64, 64, false));
                }
                catch(Exception e) {
                    //log("XXXXXXXXXXXXXXXXXXXXXXXXXXXXX" + e.getMessage());
                    holder.image.setImageResource(R.drawable.ic_node_busy);
                }
            }
            return convert_view;
        }
    }

/*
/TODO wipe off this harcoded deadbeef
            if (ep.is_valid()) {
                String x=ep.wloc.first.encode();
 //               log("A "+x); //--strip
                if (x.equals("2i35c5Urfa1PrUFtSzFa3eBgVasS")) {
                    tvitem.setText("Dr Megan Hall - GP");
                    image_view.setImageResource(R.drawable.ic_node_gp);
                }
                else if (x.equals("6vAWK14ssvmnDGYLxDC4sAyrR9t")) {
                    tvitem.setText("Impact Shopping");
                    image_view.setImageResource(R.drawable.ic_node_shop);
                }
                else if (x.equals("4Tr81agLbMR43goFwu4SetMqZwDa")) {
                    tvitem.setText("Sothecam NeOS - AI");
                    image_view.setImageResource(R.drawable.ic_node_ai);
                }
                else if (x.equals("2Mm1Pz3RuKQG4RSU6thuSX5fbdAg")) {
                    tvitem.setText("Dr Joshua Warner - Dermatologist");
                    image_view.setImageResource(R.drawable.ic_node_specialist);
                }
                else if (x.equals("2T1LngV2jcxVZguYY62Kf7tmUvQ3")) {
                    tvitem.setText("Riverside Pharmacy");
                    image_view.setImageResource(R.drawable.ic_node_pharmacy);
                }
                else {
                    tvitem.setText("Wallet");
                    image_view.setImageResource(R.drawable.ic_node_busy);
                }
            }
            else {
*/
//                log("B ");  //--strip
//                image_view.setImageResource(R.drawable.ic_node_busy);
//            }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //toolbar.setTitle(R.string.world);
    }

    ArrayList<pair<String, bookmark_t>> convert(bookmarks_t bm) {
        ArrayList<pair<String, bookmark_t>> o = new ArrayList<pair<String, bookmark_t>>();
        o.ensureCapacity(bm.size());
        for (Map.Entry<String, bookmark_t> entry : bm.entrySet()) {
            o.add(new pair<String, bookmark_t>(entry.getKey(), entry.getValue()));
        }
        return o;
    }

    ArrayList<pair<String, bookmark_t>> convert(us.gov.io.types.vector_hash bm) {
        ArrayList<pair<String, bookmark_t>> o = new ArrayList<pair<String, bookmark_t>>();
        o.ensureCapacity(bm.size());
        for (hash_t i: bm) {
            try {
                endpoint_t ep = new endpoint_t(a.hmi.p.channel, i);
                o.add(new pair<String, bookmark_t>("wallet", new bookmark_t(ep)));
            }
            catch (Exception e) {
                error_manager.manage(e, e.getMessage() + "    " + e.toString());
                log("KO 6950 - Invalid endpoint "); //--strip
            }
        }
        return o;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mode = (Switch) findViewById(R.id.mode);
        mode_cap = (TextView) findViewById(R.id.mode_cap);

//TODO review
//        drawerLayout = findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.bookmarks, R.string.bookmarks);
//        drawerLayout.addDrawerListener(toggle);
//        toggle.syncState();
//        navigationView = findViewById(R.id.navigation_view);
//        navigationView.setNavigationItemSelectedListener(this);
//        Menu nav_menu = navigationView.getMenu();
//        MenuItem menuItem = nav_menu.findItem(R.id.nav_world);
//        menuItem.setChecked(true);
//        world_view = !getIntent().hasExtra("bookmarksmode");

        bookmarks = new ArrayList<pair<String, bookmark_t>>();
        world = new ArrayList<pair<String, bookmark_t>>();
        //log("WORLD"); //--strip
        //toolbar.setTitle(R.string.world);
/*
            log("Filling bookmarks"); //--strip
//TODO wipe off this harcoded deadbeef
            bookmarks=new HashMap<String, String>();
            bookmarks.put("0 4Tr81agLbMR43goFwu4SetMqZwDa pat2ai pat", "Soathecam A.I.");
            bookmarks.put("0 2i35c5Urfa1PrUFtSzFa3eBgVasS pat2slt pat", "Dr. Megan Hall - GP");
            bookmarks.put("0 2Mm1Pz3RuKQG4RSU6thuSX5fbdAg pat2slt pat", "Dr. Joshua Warner - Dermatologist");
            bookmarks.put("0 2T1LngV2jcxVZguYY62Kf7tmUvQ3 pat2phy pat", "Riverside Pharmacy");
            bookmarks.put("0 6vAWK14ssvmnDGYLxDC4sAyrR9t bid2ask bid", "Impact Shopping");
*/
//TODO review
//            menuItem = nav_menu.findItem(R.id.nav_bookmarks);
//            menuItem.setChecked(true);
//        }
        //log("Bomkmarks size: " + bookmarks.size()); //--strip

        progressbarcontainer = findViewById(R.id.progressbarcontainer);
        progressbarcontainer.setVisibility(View.VISIBLE);
        lv = (no_scroll_list_view) findViewById(R.id.listviewX);
        assert lv != null;
        adapter = new adapter_t(this, bookmarks);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parentView, View childView, int position, long id) {
//                progressbarcontainer.setVisibility(View.VISIBLE);
                assert main != null;

                if (main._nodes_mode_custom != null) {
                    item_click2(bookmarks.get(position), position);
                    return;
                }
                if (main._nodes_mode_all) {
                    item_click(world.get(position));
                }
                else {
                    item_click1(bookmarks.get(position));
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView parentView, View childView, int position, long id) {
                if (main._nodes_mode_custom != null) {
                    return true;
                }
                if (!main._nodes_mode_all) return true;
                final EditText input = new EditText(parentView.getContext());
                input.setOnFocusChangeListener(new OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            input.post(new Runnable() {
                                @Override
                                public void run() {
                                    InputMethodManager inputMethodManager= (InputMethodManager) nodes.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                                }
                            });
                        }
                    });
                input.requestFocus();
                input.setSingleLine();
                AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(nodes.this, R.style.myDialog))
                    .setTitle("Bookmark")
                    .setMessage("Enter label for qr " + world.get(position).second.qr.to_string())
                    .setView(input)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pair<String, bookmark_t> bm = world.get(position);
                                //byte[] ico = new byte[0];
                                bm.second.label = String.valueOf(input.getText()).trim();
                                us.wallet.engine.rpc_peer_t.bookmark_add_in_t o = new us.wallet.engine.rpc_peer_t.bookmark_add_in_t(new string(bm.first), bm.second);
                                string s = new string();
                                ko r = a.hmi.rpc_peer.call_bookmark_add(o, s);
                                if (is_ko(r)) { //--strip
                                    log(r.msg);  //--strip
                                }//--strip
                                else {//--strip
                                    log("added bookmark"); //--strip
                                }//--strip
                             }
                        })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                    .create();
                dialog.show();
                return true;
            }
        });

        toolbar_button refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dorefresh();
            }
        });

        mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                log("TOUCHED ROLE VIEW"); //--strip
                if (isChecked) {
                    log("checked"); //--strip
                    main._nodes_mode_all = false;
               }
                else {
                    log("not checked"); //--strip
                    main._nodes_mode_all = true;
                }
                dorefresh();
            }
        });
        dispatchid = a.datagram_dispatcher.connect_sink(this);

        dorefresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("onDestroy"); //--strip
        a.datagram_dispatcher.disconnect_sink(dispatchid);
        main._nodes_mode_custom = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        log("onResume"); //--strip
    }

    @Override
    public void onPause() {
        super.onPause();
        log("onPause"); //--strip
    }

    void item_click(pair<String, bookmark_t> bm) {

        String[] options = {"Banking (move coins)", a.getResources().getString(R.string.start_new_trade), a.getResources().getString(R.string.cancel)};
        final nodes i = nodes.this;
        new AlertDialog.Builder(i).setTitle(bm.second.get_label())
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
                            case 0:
                                qr_t qr = new qr_t(bm.second.qr);
                                qr.protocol_selection.first = "w2w";
                                qr.protocol_selection.second = "w";
                                log("selected " + qr.to_string()); //--strip
                                main.new_trade(new hash_t(0), "", qr);
                                break;
                            case 1:
                                main.new_trade(new hash_t(0), "", bm.second.qr);
                                break;
                        }
                    }
                })
                .setIcon(R.drawable.ic_world).show();
    }

    void item_click1(pair<String, bookmark_t> bm) {

        String[] options = {a.getResources().getString(R.string.start_new_trade), "edit bookmark", "delete bookmark", a.getResources().getString(R.string.cancel)};
        final nodes i = nodes.this;
        new AlertDialog.Builder(i).setTitle(bm.second.get_label())
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
                            case 0:
                                main.new_trade(new hash_t(0), "", bm.second.qr);
                                break;
                            case 1:
                                break;
                            case 2:
                                string ans = new string();
                                ko r = a.hmi.rpc_peer.call_bookmark_delete(new string(bm.first), ans);
                                Toast.makeText(main, ans.value, Toast.LENGTH_LONG).show();
                                dorefresh();
                                break;
                        }
                    }
                })
                .setIcon(R.drawable.ic_world).show();
    }

    void item_click2(pair<String, bookmark_t> bm, final int pos) {

        String[] options = {a.getResources().getString(R.string.start_new_trade), "Copy to my bookmarks", a.getResources().getString(R.string.cancel)};
        final nodes i = nodes.this;
        new AlertDialog.Builder(i).setTitle(bm.second.get_label())
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
                            case 0:
                                main.new_trade(new hash_t(0), "", bm.second.qr);
                                break;
                            case 1:
                                log("Copy to my bookmarks"); //--strip
                                a.hmi.command_trade(main._nodes_mode_custom_tid, "copybm " + (pos + 1));

                                break;
                        }
                    }
                })
                .setIcon(R.drawable.ic_world).show();
    }

/*
    @Override    //entry point. Datagrams from the wire are processed here, called for every arriving datagram
    public boolean dispatch(datagram d) { //returns true if the datagram has been handled
        log("dispatch svc "+d.service); //--strip
        switch(d.service) {
            case protocol.wallet_daemon_list_wallets_response: {
                log("handling svc "+d.service); //--strip
                if (bookmarks!=null) {
                    log("KO 2201 - Unexpected list in bookmark mode"); //--strip
                    break;
                }
                final String b=d.parse_string();
                final String[] lines = b.split("\\r?\\n");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<trader_endpoint_t> al=new ArrayList<trader_endpoint_t>();
                        if (lines.length > 0) {
                            for (String line : lines) {
                                try {
                                    trader_endpoint_t t = new trader_endpoint_t(line);
                                    al.add(t);
                                }
                                catch (Exception e) {
                                    error_manager.manage(e, e.getMessage() + "    " + e.toString());
                                    log("KO 6950 - Invalid endpoint "+line); //--strip
                                }
                            }
                        }
                        Collections.sort(al);
                        adapter.addAll(al);
                        progressbarcontainer.setVisibility(View.GONE);
                    }
                });
                return true;
            }
        }
        log("unhandled svc "+d.service); //--strip
        return false;
    }
*/

    @Override
    public void on_push(hash_t target_tid, uint16_t code, byte[] payload) {
    }

    void redraw() {
        log("redraw"); //--strip
        adapter.clear();
        if (main._nodes_mode_custom != null) {
            adapter.addAll(bookmarks);
            return;
        }
        if (main._nodes_mode_all) {
            adapter.addAll(world);
        }
        else {
            adapter.addAll(bookmarks);
        }
    }

    void load_remote() {
        set_busy(true);
        log("load_remote " + main._nodes_mode_custom.size()); //--strip
        bookmarks = convert(main._nodes_mode_custom);
        set_busy(false);
    }

    void load_bookmarks() {
        set_busy(true);
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                log("load_bookmarks-run"); //--strip
                bookmarks_t bm = new bookmarks_t();
                ko r = a.hmi.rpc_peer.call_bookmark_list(bm);
                if (is_ko(r)) {
                    set_busy__worker(false);
                    log(r.msg); //--strip
                    report_ko__worker(r);
                    return;
                }
                bookmarks = convert(bm);
                log("OK " + bookmarks.size()); //--strip
                set_busy__worker(false);
            }
        });
        thread1.start();
    }

    void load_world() {
        set_busy(true);
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                log("load_world-run"); //--strip
                us.gov.io.types.vector_hash vh = new us.gov.io.types.vector_hash();
                ko r = a.hmi.rpc_peer.call_world(vh);
                if (is_ko(r)) {
                    set_busy__worker(false);
                    log(r.msg); //--strip
                    return;
                }
                world = convert(vh);
                set_busy__worker(false);
                return;
            }
        });
        thread1.start();
    }

    int _busy = 0;

    void set_busy__worker(boolean b) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                set_busy(b);
            }
        });
    }

    void set_busy(boolean b) {
        if (b) {
            ++_busy;
            if (_busy == 1) {
                log("busy=1"); //--strip
                progressbarcontainer.setVisibility(View.VISIBLE);
            }
            return;
        }
        --_busy;
        if (_busy == 0) {
            log("busy=0"); //--strip
            redraw();
            progressbarcontainer.setVisibility(View.GONE);
        }
    }

    void dorefresh() {
        log("dorefresh. main._nodes_mode_all=" + main._nodes_mode_all); //--strip
        if (main.main._nodes_mode_custom != null) {
            toolbar.setTitle("Listing remote bookmarks");
            mode.setVisibility(View.GONE);
            mode_cap.setVisibility(View.GONE);
            load_remote();
            return;
        }
        mode.setVisibility(View.VISIBLE);
        mode_cap.setVisibility(View.VISIBLE);
        if (main._nodes_mode_all) {
            toolbar.setTitle(R.string.world);
            mode.setChecked(false);
            mode_cap.setText("Listing all world. turn-right for listing my bookmarks.");
            load_world();
        }
        else {
            mode.setChecked(true);
            mode_cap.setText("Listing my bookmarks. turn-left for listing all world.");
            toolbar.setTitle(R.string.label_bookmarks);
            load_bookmarks();
        }
    }

    no_scroll_list_view lv;

    ArrayList<pair<String, bookmark_t>> world = null;
    ArrayList<pair<String, bookmark_t>> bookmarks = null;
    adapter_t adapter = null;
    RelativeLayout progressbarcontainer;
    Toolbar toolbar;
    int dispatchid;
    private Switch mode;
    private TextView mode_cap;
//    boolean mode_all = true;

}

