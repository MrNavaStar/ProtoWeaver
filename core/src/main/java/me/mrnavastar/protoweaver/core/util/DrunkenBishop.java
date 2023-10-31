package me.mrnavastar.protoweaver.core.util;

// No idea how this works, Robbed from https://codegolf.stackexchange.com/questions/59670/the-drunken-bishop
public class DrunkenBishop {

    // Returns a randomart image that represents the given fingerprint
    public static String parse(String fingerprint) {
        var m = new char[9][17];
        Integer x = 4, y = 8, i = 0, s;

        for (var p : fingerprint.split(":"))
            for (p = x.toString(x.parseInt(p, 16), 2), i = 4; i-- > 0; m[x -= s < 2 ? x > 0 ? 1 : 0 : x / 8 - 1][y -= s % 2 > 0 ? y / 16 - 1 : y > 0 ? 1 : 0]++)
                s = x.decode(("0".repeat(8 - p.length()) + p).split("(?<=\\G..)")[i]);

        String t = "+" + "-".repeat(m[x][y] = 16) + "-+\n";
        StringBuilder r = new StringBuilder(t + "|");
        for (m[4][8] = 15; ++i < 153; r.append(y > 15 ? "|\n" + (x < 8 ? "|" : "") : "")) r.append(" .o+=*BOX@%&#/^SE".charAt(m[x = i / 17][y = i % 17]));
        return r + t;
    }
}