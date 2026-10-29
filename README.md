# CyberGrid

A 2D cyberpunk-themed game built on a custom abstract game engine, using Java and LibGDX. It puts you in the shoes of a cyber agent navigating a hostile network. Inspired by real cybersecurity concepts like network scanning, intrusion detection, and system exploitation, each scene reflects a stage of a real-world cyber operation, in a fast-paced, arcade-style experience.

Built for an OOP module at the Singapore Institute of Technology.

## Screenshots

| Main menu | Scanning |
|---|---|
| ![Main menu](Project%20Screenshots/menu.png) | ![Scanning for servers](Project%20Screenshots/scanning.png) |

| Escape | Exploiting a vulnerable server |
|---|---|
| ![Escaping the network](Project%20Screenshots/escape.png) | ![Vulnerable server exploited](Project%20Screenshots/exploit.png) |

## Prerequisites
* **Java Development Kit (JDK):** JDK 11 or higher (JDK 17+ is highly recommended).
* **IDE:** [Visual Studio Code](https://code.visualstudio.com/) or [Eclipse IDE](https://www.eclipse.org/downloads/).

---

## How to Run the Project

This project uses Gradle. You do not need to install Gradle manually; the included `gradlew` wrapper will download everything automatically.

### Option 1: Using the Terminal / Command Prompt (Easiest)
1. Open your terminal or command prompt.
2. Navigate to the root folder of the project (where the `gradlew` file is located).
3. Execute the desktop run command:
   * **Windows:**
     ```cmd
     gradlew lwjgl3:run
     ```
   * **macOS / Linux:**
     ```bash
     ./gradlew lwjgl3:run
     ```

### Option 2: Running in Visual Studio Code
1. Open the project folder in VS Code.
2. Ensure you have the **Extension Pack for Java** installed.
3. Wait for the Java Language Server to recognize the project.
4. In the file explorer, navigate to the desktop launcher:
   `lwjgl3 / src / main / java / io / github / cybergrid / simulation / lwjgl3 / Lwjgl3Launcher.java`
5. Open the file and click the small **Run** button that appears hovering directly above the `public static void main(String[] args)` method.

### Option 3: Running in Eclipse
1. Open Eclipse and go to **File > Import**.
2. Expand the **Gradle** folder, select **Existing Gradle Project**, and click **Next**.
3. Browse to the root directory of this project and click **Finish**. 
4. Wait for Eclipse to synchronize the Gradle dependencies (you can track progress in the bottom right corner).
5. Once synced, navigate through the Package Explorer to:
   `lwjgl3 / src/main/java / io.github.cybergrid.simulation.lwjgl3 / Lwjgl3Launcher.java`
6. Right-click `Lwjgl3Launcher.java` and select **Run As > Java Application**.

---

## Project Structure

* **`core/`**: The heart of the project. Contains all the Abstract Engine logic (SceneManager, EntityManager, CollisionManager, etc.) and the core demo simulation files.
* **`lwjgl3/`**: The desktop-specific launcher module. This is the entry point used to start the application on Windows, Mac, or Linux.
* **`assets/`**: Contains all media files (audio, textures, fonts) loaded by the simulation.
