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
import com.sun.tools.attach.*;


import static de.byteandbit.Constants.AGENT;
import static de.byteandbit.Util.downloadUnzippedTempFile;
import static de.byteandbit.Util.extractResourceToTempFile;

/**
 * Api responsible for finding running minecraft instances and extracting their metadata.
 **/
public class AgentApi implements AutoCloseable {
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
        agent = unzip_agent();
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
        String agentArgs = pid + " " + port;
        String agentPath = agent.getAbsolutePath();
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(String.valueOf(pid));
            vm.loadAgent(agentPath, agentArgs);

            System.out.println("Agent loaded into JVM with PID " + pid);
        } catch (AttachNotSupportedException | AgentLoadException | AgentInitializationException e) {
            throw new IOException("Cannot attach to target JVM with PID " + pid, e);
        } finally {
            if (vm != null) {
                vm.detach();
            }
        }
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
        ArrayList<Integer> pids = new ArrayList<>();
        try {
            // List all JVMs visible to the Attach API
            List<VirtualMachineDescriptor> vms = VirtualMachine.list();
            for (VirtualMachineDescriptor vmd : vms) {
                try {
                    int pid = Integer.parseInt(vmd.id());
                    pids.add(pid);
                } catch (NumberFormatException ignored) {
                    // Skip any non-numeric IDs (very rare)
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pids;
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
