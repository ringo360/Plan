package main.java.com.djrapitops.plan.utilities.file.dump;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Fuzzlemann
 * @since 3.7.0
 */
public class DumpUtils {

    /**
     * Dumps the following things to Hastebin
     * <ul>
     * <li>The current time with the time zone</li>
     * <li>The system details</li>
     * <li>The server details</li>
     * <li>The Plan details</li>
     * <li>Some important Configuration details</li>
     * <li>The Plan timings</li>
     * <li>The error log (if present)</li>
     * <li>The debug log (if present)</li>
     * </ul>
     *
     * @param plugin The Plan instance
     * @return The link to the Dump Log
     */
    public static String dump(Plan plugin) {
        DumpLog log = new DumpLog();

        addTime(log);
        addSystemDetails(log);
        addServerDetails(log, plugin);
        addPlanDetails(log, plugin);
        addConfigurationDetails(log, plugin);
        addTimings(log, plugin);
        try {
            addErrorLog(log, plugin);
            addDebugLog(log, plugin);
        } catch (IOException e) {
            Log.toLog("DumpUtils.dump", e);
            return "Error";
        }

        return log.upload();
    }

    /**
     * Adds the current time to the Dump log
     * <p>
     * The format of the time is "dd.MM.yyy HH:mm:ss z"
     *
     * @param log The log
     */
    private static void addTime(DumpLog log) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss z");
        String time = simpleDateFormat.format(new Date());

        log.add("Time", time);
    }

    /**
     * Adds the following system details to the Dump log
     * <ul>
     * <li>The Operating System Name</li>
     * <li>The Operating System Version</li>
     * <li>The Operating System Architecture</li>
     * <li>The Java Vendor</li>
     * <li>The Java Version</li>
     * <li>The JVM Vendor</li>
     * <li>The JVM Version</li>
     * <li>The JVM Name</li>
     * <li>The JVM Flags</li>
     * </ul>
     *
     * @param log The log
     */
    private static void addSystemDetails(DumpLog log) {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        Properties properties = System.getProperties();

        String osName = properties.getProperty("os.name");
        String osVersion = properties.getProperty("os.version");
        String osArch = properties.getProperty("os.arch");

        String javaVendor = properties.getProperty("java.vendor");
        String javaVersion = properties.getProperty("java.version");

        String javaVMVendor = properties.getProperty("java.vm.vendor");
        String javaVMName = properties.getProperty("java.vm.name");
        String javaVMVersion = properties.getProperty("java.vm.version");
        List<String> javaVMFlags = runtimeMxBean.getInputArguments();

        log.addHeader("System Details");

        log.add("Operating System ", osName + " (" + osArch + ") version " + osVersion);

        log.add("Java Version", javaVersion + ", " + javaVendor);
        log.add("Java VM Version", javaVMName + " version " + javaVMVersion + ", " + javaVMVendor);
        log.add("Java VM Flags", javaVMFlags);
    }

    /**
     * Adds the following server details to the Dump log
     * <ul>
     * <li>The Minecraft Version</li>
     * <li>The Server Type</li>
     * <li>The installed plugins with the version</li>
     * </ul>
     *
     * @param log  The log
     * @param plan The Plan instance
     */
    private static void addServerDetails(DumpLog log, Plan plan) {
        Server server = plan.getServer();

        String minecraftVersion = server.getVersion();
        String serverType = server.getName();

        List<String> plugins = Arrays.stream(server.getPluginManager().getPlugins())
                .map(Plugin::getDescription)
                .map(description -> description.getName() + " " + description.getVersion())
                .sorted()
                .collect(Collectors.toList());

        log.addHeader("Server Details");

        log.add("Minecraft Version", minecraftVersion);
        log.add("Server Type", serverType);

        log.addHeader("Plugins");
        log.addLines(plugins);
    }

    /**
     * Adds the following Plan details to the Dump log
     * <ul>
     * <li>The Plan Version</li>
     * <li>The Abstract Plugin Framework Version</li>
     * </ul>
     *
     * @param log  The log
     * @param plan The Plan instance
     */
    private static void addPlanDetails(DumpLog log, Plan plan) {
        String planVersion = plan.getVersion();
        String apfVersion = plan.getAPFVersion();

        log.addHeader("Plan Details");

        log.add("Plan Version", planVersion);
        log.add("Abstract Plugin Framework Version", apfVersion);
    }

    /**
     * Adds the following Configuration Details to the Dump Log
     * <ul>
     * <li>WebServer enabled</li>
     * <li>HTTPS used</li>
     * <li>Analysis on enable refresh</li>
     * <li>Analysis Export</li>
     * <li>Alternative Server IP usage</li>
     * <li>Chat Gathering</li>
     * <li>Kill Gathering</li>
     * <li>Command Gathering</li>
     * <li>Alias Combining</li>
     * <li>Unknown Command Logging</li>
     * <li>The locale</li>
     * <li>The DB Type</li>
     * </ul>
     *
     * @param log  The log
     * @param plan The Plan instance
     */
    private static void addConfigurationDetails(DumpLog log, Plan plan) {
        boolean webServerEnabled = Settings.WEBSERVER_ENABLED.isTrue();
        boolean usingHTTPS = plan.getUiServer().usingHttps();
        boolean refreshAnalysisOnEnable = Settings.ANALYSIS_REFRESH_ON_ENABLE.isTrue();
        boolean analysisExport = Settings.ANALYSIS_EXPORT.isTrue();
        boolean usingAlternativeServerIP = Settings.USE_ALTERNATIVE_UI.isTrue();

        boolean chatGathering = Settings.GATHERCHAT.isTrue();
        boolean killGathering = Settings.GATHERKILLS.isTrue();
        boolean commandGathering = Settings.GATHERCOMMANDS.isTrue();

        boolean combineAliases = Settings.COMBINE_COMMAND_ALIASES_TO_MAIN_COMMAND.isTrue();
        boolean unknownCommandLogging = Settings.DO_NOT_LOG_UNKNOWN_COMMANDS.isTrue();

        String locale = Settings.LOCALE.toString();
        String dbType = Settings.DB_TYPE.toString();

        log.addHeader("Plan Configuration");

        log.add("Webserver Enabled", webServerEnabled);
        log.add("Webserver HTTPS", usingHTTPS);
        log.add("Refresh Analysis on Enable", refreshAnalysisOnEnable);
        log.add("Analysis Export", analysisExport);
        log.add("Alternative Server IP", usingAlternativeServerIP);

        log.add("Chat Gathering", chatGathering);
        log.add("Kill Gathering", killGathering);
        log.add("Command Gathering", commandGathering);

        log.add("Combine Aliases", combineAliases);
        log.add("Unknown Command Logging", unknownCommandLogging);
        log.add("Locale", locale);

        log.add("Database Type", dbType);
    }

    /**
     * Adds the timings to the Dump log
     *
     * @param log  The log
     * @param plan The Plan instance
     */
    private static void addTimings(DumpLog log, Plan plan) {
        String[] timings = plan.benchmark().getTimings().getTimings();

        log.addHeader("Timings");
        log.addLines(timings);
    }

    /**
     * Adds the error log to the Dump Log if present
     *
     * @param log  The log
     * @param plan The Plan instance
     * @throws IOException when an error while reading occurred
     */
    private static void addErrorLog(DumpLog log, Plan plan) throws IOException {
        Path errorFile = FileSystems.getDefault().getPath(plan.getDataFolder().getAbsolutePath(), Log.getErrorsFilename());

        if (Files.notExists(errorFile)) {
            return;
        }

        List<String> lines = readLines(errorFile);

        log.addHeader("Error Log");
        log.addLines(lines);
    }

    /**
     * Adds the debug log to the Dump Log if present
     *
     * @param log  The log
     * @param plan The Plan instance
     * @throws IOException when an error while reading occurred
     */
    private static void addDebugLog(DumpLog log, Plan plan) throws IOException {
        Path debugFile = FileSystems.getDefault().getPath(plan.getDataFolder().getAbsolutePath(), "DebugLog.txt");

        if (Files.notExists(debugFile)) {
            return;
        }

        List<String> lines = readLines(debugFile);

        log.addHeader("Debug Log");
        log.addLines(lines);
    }

    /**
     * Reads the lines of a file
     *
     * @param file The file
     * @return The lines
     * @throws IOException when an error while reading occurred
     */
    private static List<String> readLines(Path file) throws IOException {
        for (Charset charset : Charset.availableCharsets().values()) {
            try {
                return readLines(file, charset);
            } catch (MalformedInputException ignored) {
                /* Ignores MalformedInputException, just trying the next charset */
            }
        }

        return null;
    }

    /**
     * Reads the lines of a file with that specific charset
     *
     * @param file    The file
     * @param charset The CharSet
     * @return The lines
     * @throws IOException when an error while reading occurred
     */
    private static List<String> readLines(Path file, Charset charset) throws IOException {
        return Files.lines(file, charset).collect(Collectors.toList());
    }
}
