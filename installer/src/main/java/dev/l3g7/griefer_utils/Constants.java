package dev.l3g7.griefer_utils;

import java.awt.*;
import java.net.URL;
import java.util.Objects;

/**
 * description missing.
 */
public class Constants {

    public static final String PROGRAM_TITLE = "GrieferUtils-Installer";

    public static final int COMMUNICATION_PORT = 61320;
    public static final int JVM_SEARCH_INTERVAL_MS = 1000;
    public static final Rectangle PROGRAM_GEOMETRY = new Rectangle(500, 300);
    public static final URL GU_LOGO = Objects.requireNonNull(Constants.class.getClassLoader().getResource("gu_logo.png"));
    public static final URL GU_ICON = Objects.requireNonNull(Constants.class.getClassLoader().getResource("gu_logo.png"));
    public static final URL AGENT = Objects.requireNonNull(Constants.class.getClassLoader().getResource("agent.jar"));
    public static final String I18N_PATH = "/i18n/de.json";

    public static final String LATEST_VERSION_URL = "https://api.grieferutils.l3g7.dev/v6/latest_release/installer/";
    public static final String STABLE_DOWNLOAD_URL = "https://github.com/L3g7/GrieferUtils/releases/download/v%s/griefer-utils-v%s.jar"; // version + version
    public static final String BETA_DOWNLOAD_URL = "://api.grieferutils.l3g7.dev/v6/builds/beta/griefer-utils-v%s.jar"; // version

    public static final int UI_ARTIFICIAL_DELAY_MS = 500;

    public static final String CLOCK_NTP_SERVER = "pool.ntp.org";
    public static final long CLOCK_ACCEPTABLE_DEVIATION_MS = 5000;

}
