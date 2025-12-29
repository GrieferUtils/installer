package de.byteandbit.api;

import de.byteandbit.Constants;
import de.byteandbit.Util;
import de.byteandbit.data.GameInstance;
import io.javalin.Javalin;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static de.byteandbit.Constants.AGENT;
import static de.byteandbit.Util.downloadUnzippedTempFile;
import static de.byteandbit.Util.extractResourceToTempFile;

/**
 * Api responsible for finding running minecraft instances and extracting their metadata.
 **/
public class AgentApi implements AutoCloseable {
    private static File jattach;
    private static File agent;
    private final HashSet<Integer> attachedPids = new HashSet<>();
    @Getter
    private final List<GameInstance> gameInstances = new ArrayList<>();
    private Javalin agentCommunicationServer;
    private int port;
    public AgentApi(int port) throws IOException {
        this.port = port;
        start_agent_communication();
    }

    public static void init() throws IOException {
        jattach = download_jattach();
        jattach.setExecutable(true);
        agent = unzip_agent();
    }

    private static File download_jattach() throws IOException {
        switch (Util.getOS()) {
            case WINDOWS:
                return downloadUnzippedTempFile(Constants.JATTACH_WINDOWS);
            case LINUX:
                return downloadUnzippedTempFile(Constants.JATTACH_LINUX);
            case MACOS:
                return downloadUnzippedTempFile(Constants.JATTACH_MACOS);
            default:
                return null;
        }
    }

    private static File unzip_agent() throws IOException {
        return extractResourceToTempFile(AGENT);
    }

    public void scan_for_new_game_instances() {
        for (int pid : list_jvm_pids()) {
            if (!attachedPids.contains(pid)) {
                try {
                    attach(pid);
                } catch (IOException | InterruptedException ignored) {
                } finally {
                    attachedPids.add(pid);
                }
            }
        }
    }


    private void attach(int pid) throws IOException, InterruptedException {
        String args = pid + " " + port;
        ProcessBuilder pb = new ProcessBuilder(
                jattach.getAbsolutePath(),
                String.valueOf(pid),
                "load",
                "instrument",
                "true",
                agent.getAbsolutePath() + "=" + args
        );
        Process p = pb.start();
        p.waitFor();

    }

    private void start_agent_communication() {
        agentCommunicationServer = Javalin.create().start(port);
        agentCommunicationServer.post("/", ctx -> {
            try {
                GameInstance obj = ctx.bodyAsClass(GameInstance.class);
                if (gameInstances.stream().noneMatch(gi -> gi.getGameDir().equals(obj.getGameDir()))) {
                    gameInstances.add(obj);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse game instance: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }


    private ArrayList<Integer> list_jvm_pids() {
        try {
            ProcessBuilder pb;
            switch (Util.getOS()) {
                case WINDOWS:
                    pb = new ProcessBuilder("tasklist", "/FO", "CSV", "/NH");
                    break;
                case LINUX:
                case MACOS:
                    pb = new ProcessBuilder("ps", "aux");
                    break;
                default:
                    return new ArrayList<>();
            }

            pb.redirectErrorStream(true);
            Process process = pb.start();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));

            ArrayList<Integer> pids = new java.util.ArrayList<>();
            String line;

            if (Util.getOS() == Util.OS.WINDOWS) {
                // Windows tasklist CSV format: "java.exe","1234","Console","1","12,345 K"
                while ((line = reader.readLine()) != null) {
                    if (line.toLowerCase().contains("java.exe") || line.toLowerCase().contains("javaw.exe")) {
                        String[] parts = line.split("\",\"");
                        if (parts.length >= 2) {
                            try {
                                String pidStr = parts[1].replace("\"", "").trim();
                                pids.add(Integer.parseInt(pidStr));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
            } else {
                // Unix ps aux format: columns are space-separated, PID is column 2
                while ((line = reader.readLine()) != null) {
                    if (line.contains("java") && !line.contains("ps aux")) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length >= 2) {
                            try {
                                pids.add(Integer.parseInt(parts[1]));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
            }
            process.waitFor();
            return pids;
        } catch (IOException | InterruptedException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Closes the agent communication server.
     */
    @Override
    public void close() {
        if (agentCommunicationServer != null) {
            agentCommunicationServer.close();
        }
    }
}
