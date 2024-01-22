package me.mrnavastar.protoweaver.core.util;

// Robbed & modified from https://codegolf.stackexchange.com/questions/59670/the-drunken-bishop
public class DrunkenBishop {

    // Returns a randomart image that represents the given fingerprint
    public static String parse(String fingerprint) {
        var m = new char[9][17];
        Integer x = 4, y = 8, i = 0, s;

        for (int j = 0; j < fingerprint.length(); j+=2) {
            String bit = String.valueOf(fingerprint.charAt(j)) + fingerprint.charAt(j+1);

            for (bit = x.toString(x.parseInt(bit, 16), 2), i = 4; i-- > 0; m[x -= s < 2 ? x > 0 ? 1 : 0 : x / 8 - 1][y -= s % 2 > 0 ? y / 16 - 1 : y > 0 ? 1 : 0]++)
                s = x.decode(("0".repeat(8 - bit.length()) + bit).split("(?<=\\G..)")[i]);
        }

        StringBuilder r = new StringBuilder("|");
        for (m[4][8] = 15; ++i < 153; r.append(y > 15 ? " |\n" + (x < 8 ? "|" : "") : "")) r.append(" .o+=*BOX@%&#/^SE".charAt(m[x = i / 17][y = i % 17]));
        // Hard coded because they will always be these values on the server
        return "+----[RSA 2048]----+\n" + r + "+-----[SHA256]-----+";
    }

    public static String inlineImages(String image1, String image2) {
        String[] image1Lines = image1.split("\n");
        String[] image2Lines = image2.split("\n");
        StringBuilder inlined = new StringBuilder();

        for (int i = 0; i < 11; i++) {
            inlined.append(image1Lines[i]).append("   ").append(image2Lines[i]).append("\n");
        }
        return inlined.toString();
    }
}