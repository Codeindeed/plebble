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
#pragma once

#include <us/gov/config.h>
#include <us/gov/bgtask.h>
#include <us/gov/crypto/types.h>
#include <us/gov/engine/net.h>

#include <us/test/test_platform.h>

#include "evc_t.h"
#include "okev.h"

namespace us { namespace test {

    struct networking;

    struct mezzanine: us::gov::bgtask {
        using b = us::gov::bgtask;
        mezzanine(networking* d0) { d = d0; }
        networking* d;
    };

    struct networking: us::gov::engine::net::daemon_t, us::test::test_platform, us::test::mezzanine {
        using b = us::gov::engine::net::daemon_t;
        using t = test_platform;
        using datagram = us::gov::socket::datagram;
        using load = us::test::mezzanine;

        struct peer_t: us::gov::engine::net::peer_t {
            using b = us::gov::engine::net::peer_t;
            using b::peer_t;

            bool process_work(datagram* d) override;
            void verification_completed(pport_t rpport, pin_t pin) override;
            bool authorize(const pub_t& p, pport_t pport) override;

        };

        static constexpr uint8_t edges{5};
        static constexpr int govport{22222};
        static constexpr const char* govaddr{"127.0.0.1"};

        networking(us::gov::crypto::ec::keys& k, uint16_t port, ostream& os);
        networking(us::gov::crypto::ec::keys& k, uint16_t port, const vector<pair<uint32_t, uint16_t>>& sn, int workers, ostream&);
        void constructor();

        //us::gov::engine::evidence* parse_custom_evidence(const string& payload) const;
        //bool process_evidence(us::gov::engine::evidence* ev);
        bool wait_rnd_before_start() const override;
        us::ko start();
        void start_load(const string& logdir);
        void stop();
        void join();
        void join_load();
        bool isup() const { return b::isup(); }
        bool wait_load_ready() const { return load::wait_ready(3); }

//        void wakeup() override;
        void set_load_function(function<void ()> r);
        static string addr_from_ndx(int ndx);
        //string homedir() const override;
        us::gov::socket::peer_t* create_client(int sock) override;
//        bool process_async_api__gov_engine_custom_ev(us::gov::socket::peer_t *c, const us::gov::socket::datagram& d);
        void relay_evidence(const datagram &d, peer_t* exclude);
        const keys& get_keys() const override;
        size_t clients_size();
        void check_clients_size(size_t expected);
        us::gov::socket::rpc_daemon_t* connect2backend();
        int ban_counter() const;
        void reset_ban();
        void reset_clients();
        void reset_dgram_counters();
        void check_ban_count(int expected);
        void check_initial_state();
        void connect_disconnect();
        void connect_disconnect2();
        void connect_banned();
        void bore_server();
        //thread* recv_datagram(const int& sock, const int& timeout_seconds, datagram*& dest);
        void cause_stall();
        void connect_quiet();
        void connect_from_invalid_net_address();
        //void check_disconnect_report(us::gov::socket::client* p, int secs, const char* expected_report);
        void send_bytes(size_t howmany, uint32_t decl_sz, uint16_t channel, uint16_t svc, bool slowly = false);
        void test_immediate_disconnect();
        void fuzzy_datagram();
        void fuzzy_datagram_fast();
        void fuzzy_datagram_slow();
        void self_test();
        bool is_loading() const;
        void test_load(int num_dgrams, chrono::milliseconds& timebetweendgrams);
        bool recv_ev(const okev&);

    public:
        atomic<short> resume{0};
        us::gov::crypto::ec::keys id;
        //us::wallet::engine::daemon_t* damn_api{nullptr};
        string test_load_logdir;
        mutable int dupevs{0};
        mutable int uniqevs{0};
        mutable evsseen_t evsseen;
        #if CFG_LOGS == 1
        string logdir;
        #endif
        static constexpr chrono::milliseconds latency{100}; //valgrind requires at least 0.14
        string myaddress;
        uint16_t myport;
        int evsent_succ{0};
        int evsent_fail{0};
        //mutable int recv_evs{0};
        bool ingrid{false};
        evc_t* evc{0};
        //keys k;
        int nodendx{0};
    };

}}
