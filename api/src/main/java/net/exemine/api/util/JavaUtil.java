package net.exemine.api.util;

public class JavaUtil {

    public static boolean isAnsiSupported() {
        String os = System.getProperty("os.name").toLowerCase();
        String term = System.getenv("TERM");

        // Check for Windows OS
        if (os.contains("win")) {
            // Windows 10+ supports ANSI by default (build version 10.0.10586+)
            String version = System.getProperty("os.version");
            if (version != null) {
                String[] versionParts = version.split("\\.");
                if (versionParts.length >= 2) {
                    int majorVersion = Integer.parseInt(versionParts[0]);
                    int minorVersion = Integer.parseInt(versionParts[1]);
                    return majorVersion > 10 || (majorVersion == 10 && minorVersion >= 10586);
                }
            }
            // ANSI is not supported by default on older Windows versions
            return false;
        } else {
            // For Unix-based systems (Linux, macOS), check the TERM environment variable
            return term != null && !term.contains("dumb");
        }
    }
}
