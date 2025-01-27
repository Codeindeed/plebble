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
#include "api_t.h"
#include <unordered_set>
#include <fstream>
#include <cassert>
#include "config.h"

using c = us::apitool::api_t;
using namespace us::apitool;
using namespace std;

void c::collect_in_specs(map<string, vector<pair<string, string>>>& s) const {
    for(auto& i: *this) {
        i.collect_in_specs(s);
    }
}

void c::collect_out_specs(map<string, vector<pair<string, string>>>& s) const {
    for(auto& i: *this) {
        i.collect_out_specs(s);
    }
}

void c::warn_h(ostream& os) {
    os << "//------------------generated by apitool- do not edit\n\n";
}

void c::info(ostream&os) const {
   os << "// " << name << " - master file: us/api/" << src << "\n\n";
}

void c::warn_f(ostream& os) {
   os << "//-/----------------generated by apitool- do not edit\n\n";
}

void c::templ_h(const string& reffile, ostream& os) {
    os << "//------------------apitool - API Spec defined @ us/api/" << reffile << "\n\n";
}

void c::templ_f(ostream& os) {
    os << "//-/----------------apitool - End of API implementation.\n\n";
}

#ifndef HAVE_CFG
    abort
#endif

#ifdef CFG_PERMISSIONED_NETWORK
    unordered_set<string> ignored_functions;
#else
    unordered_set<string> ignored_functions{"nodes_allow", "nodes_deny"};
#endif

c* c::from_stream(istream&is) {
    c* api = new c();
    while (is.good()) {
        apifun i = apifun::from_stream(is);
        if (!is.good()) {
            break;
        }
        if (!i.name.empty()) {
            if (ignored_functions.find(i.name) == ignored_functions.end()) {
                i.api = api;
                api->push_back(i);
            }
        }
    }
    if (api->empty()) {
        auto i = apifun::example1();
        i.api = api;
        api->push_back(i);
    }
    return api;
}

void c::feedback_load(const string&) {
}

c* c::load(const string& process, string file) {
    string nm = file;
    file = string("data/") + process + '/' + file;
    feedback_load(file);
    ifstream is(file);
    auto a = from_stream(is);
    a->name = nm;
    a->src = file;
    a->v = a->compute_get_protocol_vector();
    return a;
}

vector<pair<string, bool>> c::compute_get_protocol_vector() {
    vector<pair<string, bool>> r;
    r.push_back(make_pair(name + "_begin", false));
    for (auto& i: *this) {
         i.compute_get_protocol_vector(name, r);
    }
    r.push_back(make_pair(name + "_end", false));
    return r;
}

int c::svc_end(int svc_begin) const {
    assert(!v.empty());
    int n = 1;
    auto f = begin();
    int s = 0;
    for (; f != end(); ++n, ++f) {
        ++s;
        if (v[n].second) {
            ++s;
        }
    }
    return svc_begin+s;
}

