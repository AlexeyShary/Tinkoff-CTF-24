import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        generateAndCheckKeys();
    }

    public static void generateAndCheckKeys() {
        Random random = new Random();

        boolean find = false;

        while (!find) {
            StringBuilder seed = new StringBuilder();
            for (int s = 0; s < 8; s++) {
                int digit = random.nextInt(9);
                seed.append(Integer.toHexString(digit));
            }

            System.out.println(seed);
            String subkey = "";
            try {
                subkey = String.format("%2s", getSubkeyFromSeed(seed.toString(), 36, 1, 137)).replace(' ', '0');
            } catch (Exception e) {
                continue;
            }

            for (int i1 = 0; i1 <= 4095; i1++) {
                for (int i2 = 0; i2 <= 4095; i2++) {
                    StringBuilder key = new StringBuilder();

                    key.append(seed).append("-").append(subkey).append("-");
                    key.append(String.format("%03X", i1)).append("-");
                    key.append(String.format("%03X", i2)).append("-");

                    StringBuilder keyForSum = new StringBuilder();
                    keyForSum.append(seed).append(subkey).append(String.format("%03X", i1)).append(String.format("%02X", i2));

                    String sum = String.format("%4s", getChecksumForSerial(keyForSum.toString())).replace(' ', '0');
                    key.append(sum);

                    if (checkLicense(key.toString())) {
                        System.out.println("FOUND " + key);
                        return;
                    }

                    System.out.println(key);
                }
            }
        }
    }

    public static boolean checkLicense(String license) {
        if (isLicenseValid(license)) {
            System.out.println("Lic is ok");
            return true;
        } else {
            return false;
        }
    }

    public static boolean isLicenseValid(String e) {
        if (!isKeyFormatValid(e)) return false;
        System.out.println("Format ok");

        String[] parts = e.replaceAll("-", "").split("(?<=\\G.{16})");
        String t = parts[0];
        String n = parts[1];
        if (!isSerialChecksumValid(t, n)) return false;
        System.out.println("Checksum ok");
        String i = t.substring(0, 8);
        if (!isSeedFormatValid(i)) return false;
        System.out.println("Seed ok");

        System.out.println("Compare " + getSubkeyFromSeed(i, 36, 1, 137) + " and " + t.substring(8, 10));

        String r = getSubkeyFromSeed(i, 36, 1, 137);
        return t.substring(8, 10).equals(r);
    }

    public static boolean isKeyFormatValid(String e) {
        return e.length() == 24 && e.replaceAll("-", "").length() == 20;
    }

    public static boolean isSerialChecksumValid(String e, String t) {
        return getChecksumForSerial(e).equals(t);
    }

    public static boolean isSeedFormatValid(String e) {
        Pattern pattern = Pattern.compile("[A-F0-9]{8}");
        Matcher matcher = pattern.matcher(e);
        return matcher.matches();
    }

    public static String getChecksumForSerial(String e) {
        int t = 175;
        int n = 86;
        for (int i = 0; i < e.length(); i++) {
            t += e.charAt(i);
            if (t > 255) t -= 255;
            n += t;
            if (n > 255) n -= 255;
        }
        return toFixedHex((n << 8) + t, 4);
    }

    public static String toFixedHex(int e, int t) {
        return Integer.toHexString(e).toUpperCase().substring(0, Math.min(Integer.toHexString(e).length(), t));
    }

    public static String getSubkeyFromSeed(String e, int t, int n, int i) {
        int r;
        if (e.matches("[0-9A-Fa-f]+")) {
            r = Integer.parseInt(e, 16);
        } else {
            return "";
        }
        n %= 3;
        t %= 25;
        int subkey;
        if (t % 2 == 0) {
            subkey = r >> t & 255 ^ 255 & (r >> n | i);
        } else {
            subkey = r >> t & 255 ^ r >> n & i & 255;
        }
        return toFixedHex(subkey, 2);
    }
}