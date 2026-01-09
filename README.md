# GrieferUtils Installer

A specialized Java-based installer and discovery tool designed to find and interact with running Minecraft JVM instances.

## Project Structure

This is a multi-module Maven project with specific versioning requirements to ensure compatibility with various Minecraft environments:

*   **`agent` (Java 8):** A Java Instrumentation Agent designed to be dynamically injected into running JVMs. Java 8 is used to ensure compatibility with older Minecraft versions.
*   **`common` (Java 8):** Shared constants, data models (like `GameInstance`), and utilities used by both the agent and the installer.
*   **`installer` (Java 17):** A Swing-based GUI (using FlatLaf) that manages the discovery process and coordinates with the agent.

## How it Works

1.  **Discovery:** The **Installer** scans the system for running Java processes.
2.  **Injection:** It dynamically attaches the **Agent** to target JVMs.
3.  **Identification:** The Agent checks internal properties, resources (e.g., `version.json`, assets), and environment variables to determine if the process is a Minecraft instance (Vanilla or Forge).
4.  **Communication:** The Agent reports metadata (version, PID, game directory) back to the Installer via a local socket.
## 

## Prerequisites

- **Java 17 SDK** (for building and running the installer).
- **Maven 3.6+**.

## Building the Project

To build the entire project, including the agent and the installer, run the following command from the root directory:

```bash
mvn clean install
```

## Licensing
An exception to the NoDerivatives clause has been explicitly granted to GrieferUtils according to Section 2a § 2.
