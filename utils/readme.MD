

# **Adapter & Doppelganger Launcher**

This script automates launching the **Java Adapter** and multiple **Python Doppelganger nodes**.
It can also **optionally** build the Java project using Maven.

---

## **1. Prerequisites**

* **Python 3.8+**
* **Java 8+**
* **Maven** *(if using `-b` or `-bo`)*
* Python package:

  ```bash
  pip install psutil
  ```
* On **Linux**: at least one terminal emulator (`gnome-terminal`, `xterm`, `konsole`, etc.).

---

## **2. Configuration**

Edit the `config` dictionary in `adapterLauncher.py`:

```python
config = {
    "address": "10.1.212.188",
    "build": {
        "enabled": True,
        "dir": "../adapter2.0",
        "jar_relative_path": "target/gmv-uc-adapter.jar",
    },
    "python": {
        "script": "../doppelgangers/doppelganger2.0.py",
        "node_id_ranges": ["6002-6003", "6100-6112", "6030"],
    },
}
```

| Key                       | Description                   | Example                 |
| ------------------------- | ----------------------------- | ----------------------- |
| `address`                 | Shared IP address             | `"192.168.1.100"`       |
| `build.dir`               | Maven project path            | `"../adapter2.0"`       |
| `build.jar_relative_path` | Path to `.jar` inside project | `"target/app.jar"`      |
| `python.script`           | Path to Doppelganger script   | `"../doppelganger.py"`  |
| `python.node_id_ranges`   | Node IDs/ranges               | `["6000-6003", "6010"]` |

---

## **3. Usage**

| Command                          | Description                      |
| -------------------------------- | -------------------------------- |
| `python3 adapterLauncher.py`     | Launch Adapter + Python nodes    |
| `python3 adapterLauncher.py -b`  | Build project first, then launch |
| `python3 adapterLauncher.py -bo` | **Only** build, don’t launch     |

---

## **4. Typical Workflow**

```bash
# First build only
python3 adapterLauncher.py -bo

# Launch adapter & nodes
python3 adapterLauncher.py

# Rebuild and launch
python3 adapterLauncher.py -b
```

---

