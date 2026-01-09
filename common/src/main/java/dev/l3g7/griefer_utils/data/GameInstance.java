package dev.l3g7.griefer_utils.data;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Data describing a running minecraft instance.
 */
@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
public class GameInstance {
    int pid;
    String gameDir;
    String mcVersion;
    int labyVersion;
}
