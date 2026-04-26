# trafficSim — README 🚗🚙🚕🚛

> Road traffic simulation application for comparing vehicle movement models. App implements a variety of forward 
> movement and lane-changing models, with a focus on flexibility and extensibility.
> The application supports both a graphical user interface (GUI) for interactive use and a
> console mode for batch processing. Users can create and edit custom map files, 
> configure simulation parameters, and export results in various formats.
> Developed as part of a bachelor's thesis at the University of West Bohemia in Pilsen.
> Author: Michael Hladký (2025/2026)

---

## Table of Contents

- [Requirements](#requirements)
- [Building the Application](#building-the-application)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Using the Application](#using-the-application)
    - [Creating and Saving Map Files](#creating-and-saving-map-files)
    - [Controlling the Simulation](#controlling-the-simulation)
    - [Output and Logging](#output-and-logging)
- [Console Mode](#console-mode)
- [Models](#models)
    - [Adding a Custom Model](#adding-a-custom-model)

---

## Requirements

The application is written in **Java 17**. The GUI uses the **JavaFX 23.0.1** library. To run the application, you need either:

- **JDK** (Java Development Kit), or
- **JRE** (Java Runtime Environment)

---

## Building the Application

A pre-built **JAR** file is available in the electronic attachment. If you need to build from source, use **Maven**.

Source code is available in the electronic attachment and on GitHub:

```bash
git clone https://github.com/Odmocnina/trafficSim.git
```

The build requires the `pom.xml` configuration file alongside the `src` directory. Copy both into your local working directory before building.

**Build with tests:**

```bash
mvn clean package
```

**Build without tests:**

```bash
mvn clean package -DskipTests
```

After a successful build, the resulting JAR will be located in the newly created `target/` directory.

---

## Configuration

Application behavior can be customized via an **XML configuration file** before launch. The file is divided into three main sections:

| Section | Description |
|---|---|
| `roadFile` | Path to the map file |
| `models` | Car-following and lane-changing models to use |
| `runDetails` | Runtime parameters (debug, collision prevention, step delay, logging, output) |

### `runDetails` parameters

- `laneChange` — enable/disable lane changes
- `preventCollision` — enable/disable collision prevention
- `debug` — toggle debug mode
- `timeBetweenSteps` — delay between simulation steps in GUI mode (milliseconds)
- `logging` — configure log levels: `info`, `warn`, `debug`, `error`, `fatal`

### `output` subsection

| Parameter | Description |
|---|---|
| `writeOutput` | Whether to write output to file |
| `file` | Output file path |
| `type` | File format: `txt` or `csv` |
| `csvSeparator` | Delimiter character for CSV format |
| `whatToWrite` | Which simulation data to include in output |

A full example configuration file is provided in [Appendix C](#) of the thesis.

---

## Running the Application

The application can be launched by **double-clicking the JAR file** or via the **command line** with optional parameters. Command-line parameters take precedence over values in the configuration file.

```bash
java -jar trafficSim-1.0.0.jar [options]
```

### Available parameters

| Parameter | Description                                                                                             |
|---|---------------------------------------------------------------------------------------------------------|
| `--help` | Print help and list of available models, then exit                                                      |
| `--dur=<seconds>` | Set simulation duration (in steps). Requires a map to be defined. **Launches in console mode.**         |
| `--config=<file>` | Path to XML configuration file. Defaults to `config/config.xml` (created if missing)                    |
| `--output=<file>` | Output file path (TXT or CSV). Defaults to `output.txt` / `output.csv`                                  |
| `--log=<true/false>` | Directly enable or disable logging (excluding data-loading logs)                                        |
| `--cfm=<model_id>` | Car-following model ID (e.g. `idm`)                                                                     |
| `--lcm=<model_id>` | Lane-changing model ID (e.g. `mobil`)                                                                   |
| `--map=<file>` | Path to map file (XML). Map is required to be sepecifed in parameters or in config when `--dur` is used |

### Example

```bash
java -jar trafficSim-1.0.0.jar --cfg=config/cfg.xml --out=test2.csv --cfm=juts --lcm=rickert --map=maps/map.xml
```

---

## Using the Application

After launch (without `--dur`), the main window opens. If a map file is defined via config or parameter, it is loaded automatically. Without a loaded map, simulation controls and map editing are disabled.

The main window consists of:
- **Menu bar** — all application controls
- **Toolbar** — quick-access buttons
- **Status bar** (bottom) — shows the currently opened map file, active models, and number of simulation steps completed

### Creating and Saving Map Files

**To create a new map:**
1. Click the first toolbar icon, or use the **Map file** menu.
2. Enter the map file name and the number of roads.
3. Edit roads individually (**Change one road**) or all at once (**Change all roads**).

> ⚠️ **Warning:** Editing all roads at once overwrites any individual road settings already made.

After creation, the application asks whether to open the new map. Confirming loads it immediately; declining saves it to disk without opening.

**To edit an open map:**
- Use the **Map file** menu, the second toolbar icon, or click a road directly in the main view.
- Clicking a road directly takes you straight to that road's edit mode.

**Road parameters:**
- Length (meters)
- Number of lanes
- Maximum speed (m/s)
- Traffic lights and vehicle generators per lane

**Traffic light settings:**
- Total cycle length
- Color change time within the cycle
- Initial state (green or red)

  *Example: green for 30 s, red for 90 s → cycle length 120, change time 30, start green. For a permanently green light, set change time equal to cycle length and start on green.*

**Generator settings:**
- Generation mode: exponential distribution during simulation, or pre-queued before simulation start
- Queue size (min/max — if different, size is randomized in that range)
- `flowRate` — corresponds to λ of the exponential distribution
- Per-vehicle parameters: name, key (must be unique), min/max values

  > ⚠️ **Warning:** The generator must include all parameters required by the active models, otherwise the map cannot be saved or edited.

**Saving:**
- **Save As** — save to a new file
- **Save** — overwrite the currently open file

Map files are XML and can also be edited externally with any text or XML editor (be careful to maintain data validity, e.g. min ≤ max for generator parameters).

---

### Controlling the Simulation

From the GUI you can:

- **Start / Pause** the simulation
- **Reset** the simulation
- **Step** manually (only when simulation is not running)
- Toggle **collision detection** on/off
- Toggle **lane changes** on/off
- Set **delay between steps** (milliseconds)

---

### Output and Logging

**Export formats:**
- `txt` — human-readable
- `csv` — suitable for further analysis in Excel, LibreOffice, Google Sheets, etc.

Export is available only when the simulation is **not running**.

**Export options:**
- Output file name
- CSV delimiter character
- Which data to include
- Optional detailed lane queue output — saved to a separate file named `<base_filename>DetailedLaneQueue`

**Logging levels** (individually configurable):
- `info`, `warning`, `error`, `fatal`, `debug`
- Logging can be fully disabled, except for data-loading logs (always active)

---

## Console Mode

Console mode skips GUI initialization and runs the simulation headlessly — useful for faster batch processing.

**Activate with:**

```bash
java -jar trafficSim-1.0.0.jar --dur=<steps> --map=<map_file>
```

The simulation runs for exactly the number of steps specified by `--dur`. A map file must be defined either via `--map` or in the configuration file.

> ⚠️ **Warning:** When using console mode, map file must be specified in parameters or config. If missing, the application will not start.

---

## Models

When starting the application, models must be specified either in the configuration file or via command-line parameters. The **types** of the chosen car-following and lane-changing models must match — either both continuous or both cellular automaton based.

**Built-in models:**
- 10 car-following models (5 continuous, 5 cellular automaton)
- 6 lane-changing models (2 continuous, 4 cellular automaton)
- 
> ⚠️ **Warning:** When starting app, types of forward and lane-changing models must match (both continuous or both 
> cellular automaton). If not, the application will not start.

To list all currently available models use parameter help:

```bash
java -jar trafficSim-1.0.0.jar --help
```

List of implemented models:

Forward models
- continuous
  - `idm` - taken from: TREIBER, Martin; HENNECKE, Ansgar; HELBING, Dirk. Congested traffic
                states in empirical observations and microscopic simulations.
  - `gipps` - taken from: GIPPS, P.G. A behavioural car-following model for computer simulation. Trans-
    portation Research Part B: Methodological.
    90037-0.
  - `helly` - taken from: AMBROSIO, ROBERTO; QUEZADA-TÉLLEZ, Luis Alberto; ROSAS-JAIMES,
    Oscar. Parameter Identification on Helly’s Car-Following Model.
  - `ovm-original` - taken from: BANDO, M.; HASEBE, K.; NAKAYAMA, A.; SHIBATA, A.; SUGIYAMA, Y. Dy-
    namical model of traffic congestion and numerical simulation.
  - `ovm-different` - taken from: WRIGHT, Craig S. Mathematical Modelling of Oscillatory Dynamics in Circular
    Traffic Systems.
  - `fvdm` - taken from: CHEN, Can; CHENG, Rongjun; GE, Hongxia. An extended car-following mo-
    del considering driver’s sensory memory and the backward looking effect.
    Physica A: Statistical Mechanics and its Applications.
- cellular automaton
  - `nagel-schreckenberg` - taken from: NAGEL, Kai; SCHRECKENBERG, Michael. A cellular automaton model for
    freeway traffic.
  - `juts` - taken from: HARTMAN, David. Head Leading Algorithm for Urban Traffic Modeling. In:
    Proceedings of the 16th European Simulation Symposium.
  - `kkw-linear` - taken from: KERNER, Boris S; KLENOV, Sergey L; WOLF, Dietrich E. Cellular automata
    approach to three-phase traffic theory.
  - `kkw-quadratic` - taken from: KERNER, Boris S; KLENOV, Sergey L; WOLF, Dietrich E. Cellular automata
    approach to three-phase traffic theory.
  - `rule-184` - taken from: ZHANG, Tianya Terry; JIN, Peter J.; MCQUADE, Sean T.; BAYEN, Alexan-
    dre; PICCOLI, Benedetto. Car-Following Models: A Multidisciplinary Review.

Lane-changing models
- continuous
  - `mobil` - taken from: KESTING, Arne; TREIBER, Martin; HELBING, Dirk. General Lane-Changing
    Model MOBIL for Car-Following Models.
  - `mobil-simple` - taken from: TREIBER, Martin. MOBIL: A Lane-Changing Model for Car-Following Simu-
    lations [https://traffic-simulation.de/info/info_MOBIL.html]
- cellular automaton
  - `rickert` - taken from: RICKERT, M.; NAGEL, K.; SCHRECKENBERG, M.; LATOUR, A. Two lane
    traffic simulations using cellular automata.
  - `rickert-transsims` - taken from: NAGEL, Kai; STRETZ, Paula; PIECK, Martin; DONNELLY, Rick; BARRETT,
    Christopher L. TRANSIMS traffic flow characteristics.
  - `stca` - taken from: CHOWDHURY, Debashish; WOLF, Dietrich E.; SCHRECKENBERG, Michael.
    Particle hopping models for two-lane traffic with two kinds of vehicles: Effects
    of lane-changing rules.
  - `f-stca` - taken from: XU, Hongxue; XU, Mingtong. A cellular automata traffic flow model based
    on safe lane-changing distance constraint rule.

---

### Adding a Custom Model

You can add your own car-following or lane-changing models. Models must implement the appropriate interface. All models
have @ModelId annotation with a unique string identifier, used for referencing the model in configuration and parameters
(also used in reflexion).

#### `ICarFollowingModel` interface (car-following)

Supports both continuous and cellular automaton models. Required methods:

| Method | Description                                                                  |
|---|------------------------------------------------------------------------------|
| `getNewSpeed(HashMap<String, Double> parameters)` | Calculate new vehicle speed                                                  |
| `getID()` | Return model's string identifier                                             |
| `getType()` | Return model type (`cellular` or `continuous`)                               |
| `getCellSize()` | Return cell size in meters (cellular models only; return `-1` for continuous) |
| `requestParameters()` | List of parameters that the calculation of new speed needs                   |
| `getParametersForGeneration()` | Parameters required for vehicle generation                                   |
| `getName()` | Full model name                                                              |

#### `ILaneChangingModel` interface (lane-changing)

Similar to `ICarFollowingModel`, but replaces `getNewSpeed` with lane-change logic:

| Method | Description |
|---|---|
| `requestParameters()` | Parameters for deciding lane changes to both neighbors |
| `requestParameters(Direction direction)` | Parameters for deciding lane change to one specific neighbor |
| `changeLaneIfDesired(HashMap<String, Double> parameters)` | Decide whether to change lane; returns a `Direction` |
| `changeLaneIfDesired(HashMap<String, Double> parameters, Direction direction)` | Decide lane change in a specific direction; returns the input direction (change) or `Direction.STRAIGHT` (stay) |

#### Accessing neighboring vehicle parameters in calculations of models

To access parameters of the **current vehicle**, use the parameter name as the key (e.g. `"move"`).

To access parameters of **surrounding vehicles**, build a compound key using `RequestConstants`:

```
<parameterName> + SUBREQUEST_SEPARATOR + <lane> + SUBREQUEST_SEPARATOR + <direction>
```

- **Lane:** `STRAIGHT` (current), `LEFT`, `RIGHT`
- **Direction:** `FORWARD` (ahead), `BACKWARD` (behind)

#### Example model

```java
@ModelId("test")
public class Test implements ICarFollowingModel {

    private final String type;

    public Test() {
        this.type = Constants.CONTINUOUS;
    }

    @Override public String getType() { return this.type; }
    @Override public String getID() { return "test"; }
    @Override public double getCellSize() { return -1; }

    @Override
    public String requestParameters() {
        String moveForward = "move" + RequestConstants.SUBREQUEST_SEPARATOR
            + RequestConstants.STRAIGHT + RequestConstants.SUBREQUEST_SEPARATOR
            + RequestConstants.FORWARD;
        return "move" + RequestConstants.REQUEST_SEPARATOR + moveForward;
    }

    @Override
    public String getParametersForGeneration() {
        return "move" + RequestConstants.REQUEST_SEPARATOR + RequestConstants.LENGTH_REQUEST;
    }

    @Override public String getName() { return "Test Model"; }

    @Override
    public double getNewSpeed(HashMap<String, Double> parameters) {
        double moveCurrent = parameters.get("move");
        String forwardKey = "move" + RequestConstants.SUBREQUEST_SEPARATOR
            + RequestConstants.STRAIGHT + RequestConstants.SUBREQUEST_SEPARATOR
            + RequestConstants.FORWARD;
        double moveForward = parameters.get(forwardKey);
        if (moveForward == Constants.NO_CAR_THERE) return moveCurrent;
        return moveCurrent - moveForward / 2;
    }
}
```

#### Deploying a custom model

**Option 1 — Recompile from source:**

Place the `.java` file into `models/carFollowingModels` (or `models/laneChangingModels`) and rebuild the project as described in [Building the Application](#building-the-application).

**Option 2 — Inject into existing JAR (no full recompile):**

1. Compile the model against the existing JAR:

```bash
javac -cp <path_to_jar>/trafficSim-1.0.0.jar <path_to_model>/<ModelName>.java
```

2. Create the required directory structure next to the JAR:

```bash
# For car-following models:
mkdir models/carFollowingModels

# For lane-changing models:
mkdir models/laneChangingModels
```

3. Place the compiled `.class` file into the appropriate directory.

4. Update the JAR:

```bash
jar uf trafficSim-1.0.0.jar -C classes models
```

---

*trafficSim — Bachelor's Thesis, Michael Hladký, University of West Bohemia, Faculty of Applied Sciences, 2025*
