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
package us;
import java.io.PrintStream;                                                                    // PrintStream

public class vcs {

//#include <us/vcs_git_java>
//------------------------------------------------------------__begin__------generated by configure, do not edit.
//content of file: <us/vcs_git_java>
public static final String devjob = "";
public static final String devjobtag = "";
public static final String brand = "us";
public static final String branch = "alpha-32";
public static final String codehash = "f79c5c75f9be3844ece7a197ab669dc104b2f4e4+";
public static final String cfghash = "5c7f9c530b248e6d5cdd54963d060e58f131f5869eee96bf40da3555b1b29607";
public static final String hashname = "f79c5c+.5c7f9c";
public static final String version_name = "us-alpha-32_f79c5c+.5c7f9c";
public static final String build_date = "2022-05-28_18-19-12";
//-/----------------------------------------------------------___end___------generated by configure, do not edit.

    public static void version(PrintStream os) {
        os.print(version_name + ' ' + codehash + ' ' + build_date);
    }

    public static String version() {
        return version_name + ' ' + codehash + ' ' + build_date;
    }

    public static String name_date() {
        return version_name + ' ' + build_date;
    }
}

