package tactosy;

/**
 * Created by westside on 2016-04-28.
 */
public class CommonUtils {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }

        char[] hexChars = new char[bytes.length * 3];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ',';
        }
        return new String(hexChars);
    }

    final protected static char[] lowecaseHexArray = "0123456789abcdef".toCharArray();

    public static String bytesToLowercaseHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }

        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = lowecaseHexArray[v >>> 4];
            hexChars[j * 2 + 1] = lowecaseHexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String byteToHex(byte b) {
        char[] hexChars = new char[2];
        int v = b & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];
        return new String(hexChars);
    }

    public static float rangeFloatFromTo(float val, float from, float to) {
        if (val < from) {
            return from;
        } else if (val > to) {
            return to;
        }

        return val;
    }

    public static int rangeIntFromTo(int val, int from, int to) {
        if (val < from) {
            return from;
        } else if (val > to) {
            return to;
        }

        return val;
    }

    public static float rangeFloat(float val) {
        return rangeFloatFromTo(val, 0f, 1f);
    }

}
