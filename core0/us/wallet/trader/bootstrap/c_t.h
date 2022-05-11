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
#include "handshake_t.h"
#include <string>
#include <us/gov/engine/signed_data.h>
#include <us/wallet/trader/params_t.h>
#include <iostream>
#include <string>
#include <vector>
#include "protocols_t.h"
#include "b_t.h"
#include <us/wallet/trader/personality/proof_t.h>
#include <us/wallet/trader/protocol_selection_t.h>
#include <us/wallet/trader/business.h>

namespace us::wallet::trader::bootstrap {

    using namespace std;

    struct c1_t: handshake_t {
        using endpoint_t = trader::endpoint_t;
        using challenge_t = trader::personality::proof_t::challenge_t;
        using params_t = trader::kv;

        c1_t();
        c1_t(const endpoint_t&, const string& wloc, protocol_selection_t&&, params_t&&, const challenge_t&);

    public: //serialization
        size_t blob_size() const override;
        void to_blob(blob_writer_t&) const override;
        ko from_blob(blob_reader_t&) override;

    public:
        endpoint_t endpoint;
        string wloc;
        protocol_selection_t protocol_selection;
        params_t params;
        challenge_t challenge;
    };

    struct c2_t: c1_t {
        using b = c1_t;
        using personality_proof_t = trader::personality::proof_t;

        c2_t();
        c2_t(const endpoint_t&, params_t&&, personality_proof_t&&, const challenge_t&);
        c2_t(endpoint_t&&, params_t&&, personality_proof_t&&, const challenge_t&);

    public: //serialization
        size_t blob_size() const override;
        void to_blob(blob_writer_t&) const override;
        ko from_blob(blob_reader_t&) override;

    public:
        personality_proof_t personality_proof;
    };

    using c3_t = b2_t; //params+personality
    using c4_t = b3_t; //params

}

