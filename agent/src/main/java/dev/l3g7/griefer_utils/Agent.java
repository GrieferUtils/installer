package dev.l3g7.griefer_utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.l3g7.griefer_utils.data.CommonConstants;
import dev.l3g7.griefer_utils.data.GameInstance;

import java.io.File;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.net.Socket;

public class Agent {
    /**
     * EntryPoint of a dynamically injected agent.
     *
     * @param agentArgs command line arguments from injecting the agent.
     * @param inst      reference to the instrumentation API.
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("GrieferUtils agent attached to JVM.");
        try {
            if (agentArgs.startsWith(CommonConstants.TEST_MC_KEY)) {
                String rawArgs = agentArgs.substring(CommonConstants.TEST_MC_KEY.length() + CommonConstants.DELIMITER.length());
                testMC(rawArgs.split(CommonConstants.DELIMITER));
            }
            if (agentArgs.startsWith(CommonConstants.DELETE_FILES_KEY)) {
                String rawArgs = agentArgs.substring(CommonConstants.DELETE_FILES_KEY.length() + CommonConstants.DELIMITER.length());
                deletefiles(rawArgs.split(CommonConstants.DELIMITER));
            }
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
    }

    public static void deletefiles(String[] args) {
        System.out.println("Deleting on exit: " + String.join(", ", args));
        for (String path : args) new File(path).deleteOnExit();
    }

    public static void testMC(String[] args) {
        int pid = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);
        String log = "Hello from GrieferUtils agent!\n";
        if (isMinecraft()) {
            if (labyMcVersion() != null) {
                GameInstance instance = new GameInstance(pid, gameDir(), labyMcVersion(), 4);
                log += "This seems to be a labymod 4 instance. Details: " + instance;
                post(port, instance);
            } else {
                GameInstance instance = new GameInstance(pid, gameDir(), mcVersion(), isLaby3() ? 3 : -1);
                log += "This seems to be a minecraft instance. Details: " + instance;
                post(port, instance);
            }
        } else {
            log += "This doesnt seem to be a minecraft instance.";
        }
        System.out.println(log);
    }

    public static String mcVersion() {
        try (java.io.InputStream is = Agent.class.getResourceAsStream("/version.json")) {
            if (is != null) {
                com.fasterxml.jackson.databind.JsonNode node = new ObjectMapper().readTree(is);
                if (node.has("id")) {
                    return node.get("id").asText().replaceAll("-.*", "");
                }
            }
        } catch (Exception ignored) {
        }

        try {
            return (String) Class.forName("net.labymod.main.Source")
                    .getDeclaredField("ABOUT_MC_VERSION")
                    .get(null);
        } catch (Exception ignored) {
        }

        return "unknown";
    }

    public static String labyMcVersion() {
        if (System.getProperties().get("net.labymod.running-version") != null)
            return System.getProperties().get("net.labymod.running-version").toString();

        return null;
    }

    public static boolean isLaby3() {
        // Check direct LabyMod
        try {
            Class.forName("net.labymod.main.LabyMod", false,
                    ClassLoader.getSystemClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
        }

        // Check wrapped LabyMod
        try {
            Class<?> launch = Class.forName("net.minecraft.launchwrapper.Launch", false, ClassLoader.getSystemClassLoader());
            ClassLoader cl = (ClassLoader) launch.getDeclaredField("classLoader").get(null);
            Class.forName("net.labymod.main.LabyMod", false, cl);
            return true;
        } catch (Exception ignored) {
        }

        return false;
    }

    public static boolean isMinecraft() {
        if (System.getProperty("minecraft.launcher.brand") != null)
            return true;
        if (Agent.class.getResource("assets/minecraft/lang/en_us.lang") != null ||
                Agent.class.getResource("assets/minecraft/lang/en_us.json") != null)
            return true;
        if (gameDir() != null && new File(gameDir(), "texturepacks").exists())
            return true;
        return false;
    }

    public static String gameDir() {
        return System.getProperty("user.dir");
    }

    public static void post(int port, GameInstance gameInstance) {
        try {
            String json = new ObjectMapper().writeValueAsString(gameInstance);
            try (Socket socket = new Socket("localhost", port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.print(json);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}