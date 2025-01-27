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
#include "traders_t.h"
#include <dlfcn.h>
#include <cstdio>
#include <iostream>
#include <memory>
#include <stdexcept>
#include <sstream>
#include <string>
#include <array>
#include <filesystem>

#include <us/gov/config.h>
#include <us/gov/socket/datagram.h>
#include <us/gov/cash/tx_t.h>
#include <us/gov/cash/ttx.h>
#include <us/gov/cash/map_tx.h>
#include <us/gov/cash/file_tx.h>
#include <us/gov/io/cfg0.h>
#include <us/wallet/protocol.h>
#include <us/wallet/engine/daemon_t.h>
#include <us/wallet/engine/peer_t.h>
#include <us/wallet/trader/r2r/w2w/business.h>
#include <us/wallet/wallet/local_api.h>

#include "trader_protocol.h"
#include "trader_t.h"
#include "business.h"
#include "bootstrap/initiator_t.h"
#include "bootstrap/follower_t.h"
#include "bootstrap/a_t.h"
#include "bootstrap/b_t.h"
#include "types.h"

#define loglevel "wallet"
#define logclass "traders"
#include <us/gov/logs.inc>

using namespace us::wallet::trader;
using c = us::wallet::trader::traders_t;

c::traders_t(engine::daemon_t& daemon, const string& home):
        daemon(daemon),
        home(home) {

    log("traders_t constructor", home);
    us::gov::io::cfg0::ensure_dir(home);
    {
        log("load_personality", home + "/k");
        load_set_personality(home + "/k");
    }
    log("loading bank pluging");
    load_bank();
    log("loading plugins");
    load_plugins();
    log("loading lf");
    load_lf();
    bookmarks.init(home);
    bookmarks.load();
}

c::~traders_t() {
    join();
    log("destroying traders");
    for (auto& i: *this) delete i.second;
    clear();
}

c::libs_t::libs_t() {
    log("creating libs");
}

c::libs_t::~libs_t() {
    log("destroying libs");
    for (auto& i: *this) delete i.second;
}

std::pair<ko, us::wallet::trader::trader_protocol*> c::create_protocol(protocol_selection_t&& protocol_selection) {
    log("create_protocol A", protocol_selection.to_string());
    if (protocol_selection.first.empty()) {
        return make_pair(ok, nullptr);
    }
    return libs.create_protocol(move(protocol_selection));
}

std::pair<ko, us::wallet::trader::trader_protocol*> c::create_opposite_protocol(protocol_selection_t&& protocol_selection) {
    log("create_opposite_protocol A", protocol_selection.to_string());
    if (protocol_selection.first.empty()) {
        return make_pair(ok, nullptr);
    }
    return libs.create_opposite_protocol(move(protocol_selection));
}

std::pair<ko, us::wallet::trader::trader_protocol*> c::create_protocol(protocol_selection_t&& protocol_selection, params_t&& remote_params) {
    log("create_protocol B", protocol_selection.to_string());
    if (protocol_selection.first.empty()) {
        return make_pair(ok, nullptr);
    }
    auto r = libs.create_protocol(move(protocol_selection));
    if (is_ko(r.first)) {
        assert(r.second == nullptr);
        return r;
    }
    *r.second->remote_params = move(remote_params);
    return r;
}

std::pair<ko, us::wallet::trader::trader_protocol*> c::libs_t::create_protocol(protocol_selection_t&& protocol_selection) {
    log("libs_t::create_protocol", protocol_selection.to_string());
    for (auto& i: *this) {
        log("testing a business");
        auto r = i.second->business->create_protocol(move(protocol_selection));
        if (r.first == ok) {
            log("found");
            return r;
        }
    }
    auto r = "KO 7895 Protocol not available";
    log(r);
    return make_pair(r, nullptr);
}

std::pair<ko, us::wallet::trader::trader_protocol*> c::libs_t::create_opposite_protocol(protocol_selection_t&& protocol_selection) {
    log("libs_t::create_opposite_protocol", protocol_selection.to_string());
    for (auto& i: *this) {
        log("testing a business");
        auto r = i.second->business->create_opposite_protocol(move(protocol_selection));
        if (r.first == ok) {
            log("found");
            return r;
        }
    }
    auto r = "KO 7895 Protocol not available";
    log(r);
    return make_pair(r, nullptr);
}

c::lib_t::lib_t(business_t* bz): plugin(nullptr), business(bz) { //constructor 1
}

c::lib_t::lib_t(const string& name, const string& filename, const string& home) { //constructor 2
    log("loading lib", name, filename, home);
    plugin = ::dlopen(filename.c_str(), RTLD_LAZY);
    if (!plugin) {
        log("Cannot open library", ::dlerror());
        return;
    }
    ::dlerror();
    business_create_t* create_bz = (business_create_t*) dlsym(plugin, "uswallet_business_create");
    const char *dlsym_error = ::dlerror();
    if (dlsym_error) {
        log("Cannot load symbol 'uswallet_business_create': ", dlsym_error);
        ::dlclose(plugin);
        plugin = nullptr;
        return;
    }
    business = create_bz(home.c_str(), business_t::interface_version);
    if (business == nullptr) {
        log("Cannot create bz");
        ::dlclose(plugin);
        plugin = nullptr;
        return;
    }
}

c::lib_t::~lib_t() {
    if (plugin == nullptr) { //constructor 1
        delete business;
        return;
    }
    assert(business != nullptr);
    business_destroy_t* destroy_bz = (business_destroy_t*) ::dlsym(plugin, "uswallet_business_destroy");
    const char* dlsym_error = ::dlerror();
    if (dlsym_error) {
        log("Cannot load symbol destroy", dlsym_error);
        return;
    }
    log("calling lib destroy", business);
    destroy_bz(business); ////constructor 2
    ::dlclose(plugin);
}

void c::load_bank() {
    log("creating w2w");
    business_t* bz = new us::wallet::trader::r2r::w2w::business_t();
    auto r = bz->init(home);
    if (is_ko(r)) {
        delete bz;
        return;
    }
    lib_t* l = new lib_t(bz);
    libs.insert(make_pair(bz->name, l));
}

void c::load_plugins() {
    namespace fs = std::filesystem;
    string plugindir = home + "/lib";
    us::gov::io::cfg0::ensure_dir(plugindir);
    vector<pair<string, string>> libfiles;
    string uswallet = string(PLATFORM) + "trader-";
    string libuswallet = string("lib") + uswallet;
    string ext = ".so";
    for (auto& p: fs::directory_iterator(plugindir)) {
        if (!is_regular_file(p.path())) continue;
        ostringstream os;
        string fn = p.path().filename().string();
        if (fn.size() < libuswallet.size() + ext.size()) continue;
        if (fn.substr(0, libuswallet.size()) != libuswallet ) continue;
        if (fn.substr(fn.size() - ext.size(), ext.size()) != ext) continue;
        string libname = fn.substr(libuswallet.size(), fn.size() - libuswallet.size() - ext.size());  //libuswallet-pat2ai-ai.so
        os << plugindir << '/' << fn;
        libfiles.push_back(make_pair(libname, os.str()));
    }
    if (libfiles.empty()) {
        log("no pugins found in", plugindir);
        return;
    }
    for (auto& i: libfiles) {
        log("loading lib", i.first, i.second);
        lib_t* l = new lib_t(i.first, i.second, home);
        if (l->plugin == nullptr) {
            log("error loading lib", i.second);
            delete l;
            continue;
        }
        log("added lib", i.first);
        libs.emplace(make_pair(i.first, l));
    }
}

void c::load_lf() {
    namespace fs = std::filesystem;
    string cgidir = home + "/bin";
    us::gov::io::cfg0::ensure_dir(cgidir);
    for (auto& p: fs::directory_iterator(cgidir)) {
        if (!is_regular_file(p.path())) continue;
        lf.emplace(p.path().filename().string());
    }
}

ko c::start() {
    #if CFG_LOGS == 1
        trades_logdir = logdir + "/trades";
    #endif
    lock_guard<mutex> lock(mx);
    for (auto& i: *this) {
        auto r = i.second->start();
        if (is_ko(r)) {
            stop();
            return r;
        }
    }
    return ok;
}

ko c::wait_ready(const time_point& deadline) const {
    lock_guard<mutex> lock(mx);
    for (auto& i: *this) {
        auto r = i.second->wait_ready(deadline);
        if (is_ko(r)) {
            return r;
        }
    }
    return ok;
}

void c::stop() {
    lock_guard<mutex> lock(mx);
    for (auto& i: *this) i.second->stop();
}

void c::join() {
    lock_guard<mutex> lock(mx);
    for (auto& i: *this) i.second->join();
}

pair<string, string> c::load_personality(const string& file) const {
    log("loading personality from", file);
    ifstream is(file);
    string sk;
    is >> sk;
    string moniker;
    getline(is, moniker);
    if (is.fail()) {
        auto r = "KO 55000 Nothing loaded from file";
        log(r, file);
        return make_pair("11111111111111111111", "anonymous");
    }
    return make_pair(sk, moniker);
}

ko c::load_set_personality(const string& file) {
    auto x = load_personality(file);
    set_personality(x.first, x.second);
    return ok;
}

int c::set_personality(const string& sk, const string& moniker) {
    log("set_personality", moniker);
    return personality.reset_if_distinct(sk, moniker);
}

pair<ko, hash_t> c::initiate(const hash_t parent_tid, const string& datadir, qr_t&& qr, wallet::local_api& w) {
    log("initiate trader", qr.to_string());
    if (unlikely(qr.endpoint.chan != daemon.channel)) {
        auto r = "KO 20100 Invalid channel";
        log(r);
        return make_pair(r, hash_t(0));
    }
    if (unlikely(qr.endpoint.pkh.is_zero())) {
        auto r = "KO 83376 Invalid endpoint address";
        log(r);
        return make_pair(r, hash_t(0));
    }
    if (unlikely(qr.endpoint == w.local_endpoint)) {
        auto r = "KO 83316 Trading with myself.";
        log(r);
        return make_pair(r, hash_t(0));
    }
    auto tder = new trader_t(*this, daemon, parent_tid, datadir);
    log("boot with initiator bootstrapper", tder);
    lock_guard<mutex> lock(mx);
    auto tid = tder->boot(daemon.id.pub.hash(), new bootstrap::initiator_t(move(qr), w));
    if (is_ko(tid.first)) {
        return make_pair(tid.first, hash_t(0));
    }
    log("new trade id", tid.second);
    emplace(tid.second, tder);
    return make_pair(ok, tid.second);
}

void c::erase_trader_(const hash_t& trade_id) {
    log("Erase trade.");
    trader_t* tder;
    auto i = find(trade_id);
    if (i == end()) return;
    delete i->second;
    erase(i);
}

trader_t* c::lock_trader_(const hash_t& trade_id) {
    log("lock_trader_");
    auto tder = unlocked_trader_(trade_id);
    log("++busyref", trade_id);
    ++tder->busyref;
    return tder;
}

trader_t* c::unlocked_trader_(const hash_t& trade_id) {
    log("unlocked_trader_");
    trader_t* tder;
    auto i = find(trade_id);
    if (i == end()) {
        log("new trader", trade_id);
        auto t = new trader_t(*this, daemon, hash_t(0), "");
        i = emplace(trade_id, t).first;
    }
    return i->second;
}

ko c::trading_msg(peer_t& peer, svc_t svc, const hash_t& trade_id, blob_t&& blob) {
    log("trading_msg [bin] svc", svc, blob.size(), " bytes");
    if (unlikely(svc < svc_end)) {
        switch(svc) {
            case svc_kill_trade: {
                log("svc_kill_trade");
                kill(trade_id, "peer sent message svc_kill_trade");
                return ok;
            }
            break;
            case svc_ko: { // KO Code  TODO: is this dead code?
                log("Received remote KO code:", "tid", trade_id);
                io::blob_reader_t reader(blob);
                string msg;
                auto r = reader.read(msg);
                if (is_ko(r)) {
                    return r;
                }
                log("Received remote KO code msg:", msg, "tid", trade_id);
                //process_svc_ko(trade_id, msg);
                return ok;
            }
            break;
        }
        return trader_protocol::KO_29100;
    }
    {
        switch(svc) {
            case trader_t::svc_handshake_a1:
            case trader_t::svc_handshake_b1:
            case trader_t::svc_handshake_c1: {
                log("resume trade as follower");
                if (trade_id.is_zero()) {
                    ko r = "KO 32010 Invalid trade id.";
                    peer.disconnect(0, r);
                    return r;
                }
                lock_guard<mutex> lock(mx);
                auto tder = lock_trader_(trade_id);
                assert(tder != nullptr);
                {
                    log("boot with follower bootstrapper", trade_id, tder);
                    auto r = tder->boot(peer.pubkey.hash(), new bootstrap::follower_t(trade_id, peer));
                    if (is_ko(r.first)) {
                        log("Oo");
                        erase_trader_(trade_id);
                        log("--busyref", trade_id);
                        --tder->busyref;
                        return r.first;
                    }
                }
                auto r = tder->trading_msg(peer, svc, move(blob));
                if (is_ko(r)) {
                    log("Oo2");
                    erase_trader_(trade_id);
                }
                log("--busyref", trade_id);
                --tder->busyref;
                return r;
            }
            break;
        }
    }
    log("looking up trader");
    auto* tr = lock_trader_(trade_id);
    if (unlikely(tr == nullptr)) {
        ko r = "KO 40391 Trader not connected.";
        log(r);
        return r;
    }
    auto r = tr->trading_msg(peer, svc, move(blob));
    --tr->busyref;
    return r;
}

void c::dump(ostream& os) const {
    lock_guard<mutex> lock(mx);
    for (auto& i: *this) {
        os << "trade # " << i.second->id << '\n';
        i.second->dump("  ", os);
    }
}

void c::dump(const string& prefix, ostream& os) const {
    lock_guard<mutex> lock(mx);
    string pfx = prefix + "  ";
    for (auto& i: *this) {
        os << prefix << "trade # " << i.second->id << '\n';
        i.second->dump(pfx, os);
    }
    os << prefix << size() << " active trades\n";
}

void c::list_trades(const hash_t& subhomeh, ostream& os) const {
    lock_guard<mutex> lock(mx);
    for (auto& i: *this) {
        if (!subhomeh.is_zero()) {
            assert(i.second->w != nullptr);
            if (subhomeh != i.second->w->subhomeh) continue;
        }
        os << i.second->id << ' ';
        i.second->list_trades(os);
        os << '\n';
    }
}

void c::lib_t::dump(ostream& os) const {
    log("lib list_prot bz=", business->name);
    assert(business != nullptr);
    business->list_protocols(os);
}

void c::lib_t::invert(protocols_t& protocols) const {
    business->invert(protocols);
}

void c::lib_t::published_protocols(protocols_t& protocols, bool inverse) const {
    assert(business != nullptr);
    business->published_protocols(protocols, inverse);
}

void c::lib_t::exec_help(const string& prefix, ostream& os) const {
    assert(business != nullptr);
    business->exec_help(prefix, os);
}

ko c::lib_t::exec(istream& is, traders_t& traders, wallet::local_api& w) {
    assert(business != nullptr);
    return business->exec(is, traders, w);
}

pair<protocol_selection_t, bookmark_info_t> c::lib_t::bookmark_info() const {
    return move(business->bookmark_info());
}

void c::libs_t::published_protocols(protocols_t& protocols, bool inverse) const {
    for (auto& i: *this) {
        i.second->published_protocols(protocols, inverse);
    }
}

void c::libs_t::invert(protocols_t& protocols) const {
    for (auto& i: *this) {
        i.second->invert(protocols);
    }
}

void c::libs_t::dump(ostream& os) const {
    for (auto& i: *this) {
        i.second->dump(os);
    }
}

void c::libs_t::exec_help(const string& prefix, ostream& os) const {
    for (auto& i: *this) {
        ostringstream p;
        p << prefix << i.first << ' ';
        i.second->exec_help(p.str(), os);
    }
}

ko c::libs_t::exec(istream& is, traders_t& traders, wallet::local_api& w) {
    log("libs_t::exec");
    string cmd;
    is >> cmd;
    auto i = find(cmd);
    if (i == end()) {
        auto r = "KO 85990 Library not found.";
        log(r);
        return r;
    }
    return i->second->exec(is, traders, w);
}

void c::libs_t::bookmark_info(vector<pair<protocol_selection_t, bookmark_info_t>>& o) const {
    for (auto& i: *this) {
        o.emplace_back(move(i.second->bookmark_info()));
    }
}

void c::api_list_protocols(ostream& os) const {
    log("api_list_protocols");
    libs.dump(os);
}

void c::published_protocols(protocols_t& protocols, bool inverse) const {
    libs.published_protocols(protocols, inverse);
}

c::protocols_t c::published_protocols(bool inverse) const {
    protocols_t protocols;
    libs.published_protocols(protocols, inverse);
    return move(protocols);
}

void c::invert(protocols_t& p) const {
    libs.invert(p);
}

void c::published_bookmarks(wallet::local_api& w, bookmarks_t& b) const {
    log("published_bookmarks", b.size());
    int n = b.size();
    vector<pair<protocol_selection_t, bookmark_info_t>> v;
    libs.bookmark_info(v);
    auto ep = w.local_endpoint;
    ep.wloc = w.subhome;
    for (auto& i: v) {
        ostringstream name;
        name << "bm_" << ++n;
        b.add(name.str(), bookmark_t(qr_t(ep, move(i.first)), move(i.second)));
        log("added", name.str());
    }
}

void c::local_functions(ostream& os) const {
}

void c::kill(const hash_t& id, const string& source) {

    log("spawned assassin thread");
    thread killwork([&, id, source]() {
        #if CFG_LOGS == 1
        {
            ostringstream os;
            os << "assassin_trade_" << id;
            log_start("assassin", os.str());
        }
        #endif
        log("source", source);
        trader_t* o;
        {
            lock_guard<mutex> lock(mx);
            auto i = find(id);
            if (i == end()) {
                auto r = "KO 40398 Trade not found";
                log(r);
                return;
            }
            o = i->second;
            erase(i); //take out so no new jobs are executed
        }
        o->die(string("killed by: ") + source);
    });
    killwork.detach();
}

void c::dispose(trader_t* tder) {
    log("dispose", tder->id);
    thread dispose_task([&, tder]() {
        auto i = tder->id;
        tder->join();
        delete tder;
        });
    dispose_task.detach();
}

void c::on_file_updated(const string& path, const string& name, const trader_t* source_trader) { //shall be locked on caller
    log("on_file_updated");
    for (auto i = begin(); i != end(); ++i) {
        if (i->second == source_trader) continue;
        i->second->on_file_updated(path, name);
    }
    log("end of on_file_updated");
}

void c::on_destroy(peer_t& cli) {
}

ko c::exec_shell(const string& cmd, ostream& os) const {
    std::array<char, 128> buffer;
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(cmd.c_str(), "r"), pclose);
    if (!pipe) {
        os << "KO 9476 - popen() failed!";
        return "KO 94760 Invalid command.";
    }
    while (fgets(buffer.data(), buffer.size(), pipe.get()) != nullptr) {
        os << buffer.data();
    }
    return ok;
}

void c::help(const string& ind, ostream& os) {
    using fmt = us::wallet::trader::trader_protocol;
    auto ind____=ind+"    ";
    os << ind << "Trading help.\n";
    os << ind << "Commands:\n";
    fmt::twocol(ind____, "[R2R Trades]", "----------", os);
    fmt::twocol(ind____, "qr", "Display my QRs", os);
    fmt::twocol(ind____, "save_qr_bookmark <name> <file>", "Save to file (or stdout if no file) a bookmark of me by name", os);
    fmt::twocol(ind____, "list_protocols", "Lists available trading protocols", os);
    fmt::twocol(ind____, "start <node_address> <protocol> <role>", "Initiates a new P2P private trade using endpoint", os);
    fmt::twocol(ind____, "list", "Lists all active trades", os);
    fmt::twocol(ind____, "kill <trade#>", "Terminates the specified trade", os);
    fmt::twocol(ind____, "<trade#> [<subcommand>]", "Enter a shell for the specified trade", os);
    fmt::twocol(ind____, "reload <full-path-doc>", "Tell all active trades the specified file changed on disk", os);
    fmt::twocol(ind____, "wallet", "Exit trading shell going back to wallet shell", os);
    os << '\n';
    fmt::twocol(ind____, "[Service API]", "----------", os);
    fmt::twocol(ind____, "exec [<cmd>]", "Exec command. Empty cmd for help", os);
    os << '\n';
    fmt::twocol(ind____, "[World]", "----------", os);
    fmt::twocol(ind____, "world", "Lists remote wallets", os);
    fmt::twocol(ind____, "bookmarks [file]", "Print my bookmarks (or those in input file).", os);
    fmt::twocol(ind____, "bookmarks_append <dstfile> <srcfile>", "Append bookmarks in srcfile into bookmarks file dstfile.", os);
    fmt::twocol(ind____, "bookmarks_rename <file> <old-name> <new-name>", "Rename entry in bookmarks file.", os);


    fmt::twocol(ind____, "save_bookmark <name> <file>", "Append to file (or stdout) a bookmark by name ", os);
    fmt::twocol(ind____, "load_bookmark <file> ", "Load ", os);
    os << '\n';
}

void c::exec_help(const string& prefix, ostream& os) const {
    log("exec_help");
    {
        os << prefix << " personality set <secret_key> <moniker>     Set initial personality for starting new trades.";
        os << "  * Current personality used: ";
        personality.one_liner(os);
        os << '\n';
    }
    lf.exec_help(prefix + "loc ", os); //--add_help_items
    libs.exec_help(prefix, os); //--api_list
}

void c::list_files_dir(const string& prefix, const string& dir, ostream& os) {
    namespace fs = std::filesystem;
    for (auto& p: fs::directory_iterator(dir)) {
        if (!is_regular_file(p.path())) continue;
        os << prefix << p.path().filename().string() << '\n';
    }
}

ko c::exec(const hash_t& tid, const string& cmd, wallet::local_api& w) {
    log("exec", tid, cmd);
    lock_guard<mutex> lock(mx);
    auto i = find(tid);
    if (i == end()) {
        auto r = "KO 15322 Trade not found.";
        push_KO(r, w);
        log(r, tid);
        return r;
    }
    i->second->schedule_exec(cmd);
    return ok;
}

ko c::exec(const string& cmd, wallet::local_api& w) {
    istringstream is(cmd);
    return exec(is, w);
}

ko c::exec(istream& is, wallet::local_api& w) {
    log("exec is");
    auto g = is.tellg();
    string cmd;
    is >> cmd;
    if (cmd.empty()) {
        ostringstream os;
        exec_help("", os);
        push_OK(hash_t(0), os.str(), w);
        return ok;
    }
    ostringstream os;
    if (cmd == "personality") {
        string cmd;
        is >> cmd;
        if (cmd == "set") {
            string sk;
            string moniker;
            is >> sk;
            getline(is, moniker);
            us::gov::io::cfg0::trim(moniker);
            if (is.fail()) {
                auto r = "KO 89979";
                log(r);
                return r;
            }
            auto r = personality.reset_if_distinct(sk, moniker);
            if (r == 0) {
                return push_OK(hash_t(0), "Personality didn't change.", w);
            }
            ostringstream os;
            os << "New trades will be initiated using personality ";
            personality.one_liner(os);
            os << '.';
            return push_OK(hash_t(0), os.str(), w);
        }
        auto r = "KO 50399 Invalid personality command.";
        log(r);
        return r;
    }
    if (cmd == "loc") { //handled by system program
        log("local_functions_exec");
        string cgidir = home + "/bin";
        string cmd;
        is >> cmd;
        if (cmd.empty()) {
            ostringstream os;
            os << "Available commands: \n";
            list_files_dir( "  ", cgidir, os );
            push_OK(hash_t(0), os.str(), w);
            return ok;
        }
        namespace fs = std::filesystem;
        for (auto& p: fs::directory_iterator(cgidir)) {
            if (!is_regular_file( p.path())) continue;
            if (p.path().filename().string() == cmd) {
                log("found file matching function name", cmd);
                string args;
                getline(is, args);
                ostringstream fcmd;
                fcmd << cgidir << '/' << cmd << ' ' << args;
                log("executing command", fcmd.str());
                ostringstream os;
                ko r = exec_shell(fcmd.str(), os);
                log("returned", (is_ko(r) ? r : "ok"));
                if (is_ok(r)) {
                    push_OK(hash_t(0), os.str(), w);
                }
                return r;
            }
        }
        log("KO 10478 Function ", cmd, "not found.");
        return "KO 10478 Invalid function.";
    }
    is.seekg(g);
    { //libs
        return libs.exec(is, *this, w);
    }
    return "KO 10479 Invalid function.";
}

c::bookmarksman_t::bookmarksman_t() {
}

ko c::bookmarksman_t::init(const string& h) {
    home = h;
    if (!us::gov::io::cfg0::ensure_dir(home)) {
        auto r = "KO 10882";
        log(r);
        return r;
    }
    return ok;
}

ko c::bookmarksman_t::load() {
    lock_guard<mutex> lock(mx);
    return load_();
}

ko c::bookmarksman_t::save() {
    lock_guard<mutex> lock(mx);
    return save_();
}

ko c::bookmarksman_t::load_() {
    log("loading bookmarks from", home + "/bookmarks");
    vector<uint8_t> v;
    auto r = us::gov::io::read_file_(home + "/bookmarks", v);
    if (is_ko(r)) {
        return r;
    }
    clear();
    return read(v);
}

ko c::bookmarksman_t::save_() {
    log("saving bookmarks to", home + "/bookmarks");
    vector<uint8_t> v;
    write(v);
    ofstream os(home + "/bookmarks");
    os.write((const char*)v.data(), v.size());
    if (os.fail()) {
        auto r = "KO 60127";
        log(r);
        return r;
    }
    return ok;
}

blob_t c::bookmarksman_t::blob() const {
    log("bookmarksman_t::blob");
    lock_guard<mutex> lock(mx);
    vector<uint8_t> v;
    write(v);
    log("bookmarksman_t::blob returned", v.size(), "bytes");
    return move(v);
}

ko c::bookmarksman_t::add(const string& name, bookmark_t&& bm) {
    lock_guard<mutex> lock(mx);
    auto r = b::add(name, move(bm));
    if (is_ko(r)) {
        return r;
    }
    return save_();
}

ko c::bookmarksman_t::remove(const string& key) {
    lock_guard<mutex> lock(mx);
    auto r = b::remove(key);
    if (is_ko(r)) {
        return r;
    }
    return save_();
}

void c::reload_file(const string& fqn) {
    log("reload_file", fqn);
    auto r = us::gov::io::cfg0::split_fqn(fqn);
    if (is_ko(r.first)) {
        return;
    }
    if (r.second.second.empty()) {
        auto r = "KO 74477 Path to a directory given.";
        log(r);
        return;
    }
    lock_guard<mutex> lock(mx);
    on_file_updated(r.second.first, r.second.second, nullptr);
}


void c::schedule_push(socket::datagram* d, wallet::local_api& w) {
    return daemon.pm.schedule_push(d, w.device_filter);
}

ko c::push_KO(ko msg, wallet::local_api& w) {
    return daemon.pm.push_KO(msg, w.device_filter);
}

ko c::push_KO(const hash_t& tid, ko msg, wallet::local_api& w) {
    return daemon.pm.push_KO(tid, msg, w.device_filter);
}

ko c::push_OK(const string& msg, wallet::local_api& w) {
    return daemon.pm.push_OK(msg, w.device_filter);
}

ko c::push_OK(const hash_t& tid, const string& msg, wallet::local_api& w) {
    return daemon.pm.push_OK(tid, msg, w.device_filter);
}

