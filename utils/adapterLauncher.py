"""
launcher.py - launcher for the Adapter app and doppelgangers.

This script automates:
  1. (Optionally) building a Java project using Maven in a specified directory.
  2. Launching the built Java JAR with specific arguments.
  3. Launching multiple instances of the same Python script, each with different node IDs,
     each running in a separate terminal window.

Configuration:
--------------
Edit the `config` dictionary in the script:

- config["address"]: Shared IP address passed to both Java and Python processes.
- config["build"]:
    - enabled: Whether the Maven build system is used.
    - dir: Path to the Maven project directory (must contain pom.xml).
    - jar_relative_path: Path to the JAR file relative to the build directory.
- config["java"]:
    - interface: Network interface to pass to the Java app.
- config["python"]:
    - script: Python script to launch.
    - node_id_ranges: List of inclusive ranges, e.g., ["6000-6002", "6004-6006"].

Command-line Options:
---------------------
  -b,  --build         Run `mvn clean package -U` before launching.
  -bo, --build_only    Only run Maven build, do not launch Java or Python.

Examples:
---------
  python3 adapterLauncher.py
    Launch Java and Python processes (no Maven build).

  python3 adapterLauncher.py
    Run Maven build first, then launch Java and Python.

  python3 adapterLauncher.py -bo
    Only run Maven build, then exit.

Requirements:
-------------
- Java must be available on the system path.
- Python script must be executable with the system's Python interpreter.
- On Linux, one of the following terminal emulators must be available:
    - gnome-terminal
    - xterm
    - konsole

Platform Support:
-----------------
- Windows (opens each Python instance in a new cmd window)
- Linux (opens each Python instance in a new terminal window)
- Should also work on macos but i haven't tried it

PS. check the paths
"""

import subprocess
import sys
import platform
import shutil
import psutil
import socket
import re
import argparse
import os
from pathlib import Path


config = {
    "address": "10.1.212.188",
    "build": {
        "enabled": True,
        "dir": "../adapter2.0",
        "jar_relative_path": "target/gmv-uc-adapter.jar",
    },
    "python": {
        "script": "../doppelgangers/doppelganger2.0.py",
        "node_id_ranges": ["6002-6003", "6100-6112","6030"],
    },
}


def _friendly_by_ip(host_ip: str):
    for if_name, addrs in psutil.net_if_addrs().items():
        for a in addrs:
            if a.family in (socket.AF_INET, socket.AF_INET6) and a.address == host_ip:
                return if_name
    return None


def get_java_iface_name_by_local_ip(host_ip: str) -> str:
    """
    Return the interface name that Java's NetworkInterface.getName() exposes.
    On Windows, this yields strings like 'wireless_32768' / 'ethernet_32769'.
    """
    friendly = _friendly_by_ip(host_ip)
    if not friendly:
        raise RuntimeError(f"No local interface owns IP {host_ip}")

    if sys.platform != "win32":
        return friendly  # non-Windows: Java uses the OS name directly

    # --- Windows path: Alias ('Wi-Fi') -> LUID -> Java name ---
    import ctypes
    from ctypes import wintypes

    iphlpapi = ctypes.WinDLL("iphlpapi.dll")

    class NET_LUID(ctypes.Structure):
        _fields_ = [("Value", ctypes.c_ulonglong)]

    # Convert alias (e.g., "Wi-Fi") to LUID
    ConvertInterfaceAliasToLuid = iphlpapi.ConvertInterfaceAliasToLuid
    ConvertInterfaceAliasToLuid.argtypes = [wintypes.LPCWSTR, ctypes.POINTER(NET_LUID)]
    ConvertInterfaceAliasToLuid.restype = wintypes.DWORD

    # Convert LUID to the canonical "wireless_32768" / "ethernet_XXXXX" name
    ConvertInterfaceLuidToNameW = iphlpapi.ConvertInterfaceLuidToNameW
    ConvertInterfaceLuidToNameW.argtypes = [
        ctypes.POINTER(NET_LUID),
        wintypes.LPWSTR,
        ctypes.c_size_t,
    ]
    ConvertInterfaceLuidToNameW.restype = wintypes.DWORD

    luid = NET_LUID()
    err = ConvertInterfaceAliasToLuid(friendly, ctypes.byref(luid))
    if err != 0:
        # Fallback: try via if_nametoindex -> LUID if available on your system
        # (Some environments don’t map alias via socket; alias path above is the robust one.)
        try:
            if_index = socket.if_nametoindex(friendly)
        except OSError:
            raise RuntimeError(
                f"Could not resolve interface alias '{friendly}' to LUID (error {err}) "
                "and if_nametoindex also failed."
            )
        ConvertInterfaceIndexToLuid = iphlpapi.ConvertInterfaceIndexToLuid
        ConvertInterfaceIndexToLuid.argtypes = [
            wintypes.ULONG,
            ctypes.POINTER(NET_LUID),
        ]
        ConvertInterfaceIndexToLuid.restype = wintypes.DWORD
        err2 = ConvertInterfaceIndexToLuid(wintypes.ULONG(if_index), ctypes.byref(luid))
        if err2 != 0:
            raise RuntimeError(f"ConvertInterfaceIndexToLuid failed with error {err2}")

    # LUID -> Java-visible name
    buf_len = 512
    buf = ctypes.create_unicode_buffer(buf_len)
    err = ConvertInterfaceLuidToNameW(ctypes.byref(luid), buf, buf_len)
    if err != 0:
        raise RuntimeError(f"ConvertInterfaceLuidToNameW failed with error {err}")

    return buf.value


def parse_ranges(ranges):
    ids = []
    for r in ranges:
        if isinstance(r, int):
            ids.append(r)
            continue

        if isinstance(r, str):

            parts = r.split(",")
            for part in parts:
                part = part.strip()
                if part.isdigit():
                    ids.append(int(part))
                else:
                    match = re.match(r"^(\d+)-(\d+)$", part)
                    if match:
                        start, end = map(int, match.groups())
                        ids.extend(range(start, end + 1))
                    else:
                        raise ValueError(f"Invalid range format: {part}")
            continue

        raise TypeError(f"Unsupported type: {type(r)}")
    return ids


def run_maven_build(build_dir):
    if not os.path.isdir(build_dir):
        print(f"Build directory does not exist: {build_dir}")
        sys.exit(1)

    print(f"Running Maven build in {build_dir}")

    # Check if Maven wrapper exists (preferred for projects)
    mvnw = Path(build_dir) / ("mvnw.cmd" if os.name == "nt" else "mvnw")
    if mvnw.exists():
        mvn_cmd = [str(mvnw)]
    else:
        # Try to find Maven on PATH
        if os.name == "nt":
            mvn_path = (
                shutil.which("mvn.cmd")
                or shutil.which("mvn.bat")
                or shutil.which("mvn")
            )
        else:
            mvn_path = shutil.which("mvn")

        if not mvn_path:
            print("Maven not found! Install Maven or use mvnw in the project.")
            sys.exit(1)

        mvn_cmd = [mvn_path]

    cmd = [*mvn_cmd, "clean", "package", "-U"]

    try:
        result = subprocess.run(cmd, cwd=build_dir)
    except FileNotFoundError:
        print("Failed to run Maven. Make sure Maven is installed and on your PATH.")
        sys.exit(1)

    if result.returncode != 0:
        print("Maven build failed.")
        sys.exit(1)

    print("Maven build successful.")


import os, sys, platform, subprocess, shutil
from pathlib import Path
from typing import List, Optional


def _run_in_new_terminal(
    cmd: List[str], *, cwd: Optional[str] = None, keep_open: bool = False
) -> None:
    system = platform.system()
    cwd = cwd or os.getcwd()

    if system == "Windows":
        flags = subprocess.CREATE_NEW_CONSOLE      
        if keep_open:
            from tempfile import NamedTemporaryFile

            bat = NamedTemporaryFile(
                delete=False, suffix=".bat", mode="w", encoding="utf-8"
            )
            try:
                bat.write(f'@echo off\r\ncd /d "{cwd}"\r\n')
                bat.write(subprocess.list2cmdline(cmd) + "\r\n")
                bat.write("echo.\r\npause\r\n")
                bat.close()
                subprocess.Popen(
                    ["cmd", "/c", "start", "", bat.name],
                    creationflags=flags,
                    stdin=subprocess.DEVNULL,
                    stdout=subprocess.DEVNULL,
                    stderr=subprocess.STDOUT,
                )
            finally:
                pass
        else:
            subprocess.Popen(
                cmd,
                creationflags=flags,
                cwd=cwd,
                stdin=subprocess.DEVNULL,
                stdout=subprocess.DEVNULL,
                stderr=subprocess.STDOUT,
            )
        return

    if system == "Darwin":
        def q(s: str) -> str:
            return '"' + s.replace("\\", "\\\\").replace('"', '\\"') + '"'

        cmd_joined = " ".join(q(x) for x in cmd)
        cd_quoted = q(cwd)

        pause = 'printf "\\nPress Enter to close..."; read -r _'
        trailer = f"; {pause}" if keep_open else ""

        
        shell_line = f"cd {cd_quoted} && {cmd_joined}{trailer}"
        osa = (
            'tell application "Terminal"\n'
            "  activate\n"
            f'  do script {q(shell_line)}\n'
            "end tell"
        )
        subprocess.Popen(["osascript", "-e", osa], shell=False)
        return


    if system == "Linux":
        # Try common terminals with argv (no shell)
        for prog, argv in [
            ("gnome-terminal", ["gnome-terminal", "--", *cmd]),
            ("konsole", ["konsole", "-e", *cmd]),
            ("kitty", ["kitty", *cmd]),
            ("alacritty", ["alacritty", "-e", *cmd]),
            ("xterm", ["xterm", "-e", *cmd]),
            ("urxvt", ["urxvt", "-e", *cmd]),
        ]:
            if shutil.which(prog):
                subprocess.Popen(argv, cwd=cwd)
                return
        if shutil.which("x-terminal-emulator"):
            subprocess.Popen(["x-terminal-emulator", "-e", *cmd], cwd=cwd)
            return
        # Fallback: run detached in the current session
        subprocess.Popen(cmd, cwd=cwd)
        return

    # Unknown OS fallback
    subprocess.Popen(cmd, cwd=cwd)


def launch_java(
    build_dir: str,
    jar_relative_path: str,
    interface: str,
    address: str,
    *,
    keep_open: bool = True,
) -> None:
    jar_path = Path(build_dir, jar_relative_path)
    if not jar_path.is_file():
        raise FileNotFoundError(f"JAR file not found: {jar_path}")
    java_cmd = [
        "java",
        "-jar",
        str(jar_path),
        f"babel.interface={interface}",
        f"babel.address={address}",
    ]
    print("Launching Java:", " ".join(java_cmd))
    _run_in_new_terminal(
        java_cmd, cwd=str(Path(build_dir).resolve()), keep_open=keep_open
    )


def launch_python(
    script: str, node_id, address: str, *, keep_open: bool = True
) -> None:
    script_abs = str(Path(script).resolve())
    if not Path(script_abs).is_file():
        raise FileNotFoundError(f"Script not found: {script_abs}")
    cmd = [sys.executable, script_abs, str(node_id), str(address)]
    print("Launching Python:", " ".join(cmd))
    _run_in_new_terminal(
        cmd, cwd=str(Path(script).parent.resolve()), keep_open=keep_open
    )


def main(build=False, build_only=False):
    build_dir = config["build"]["dir"]
    jar_relative_path = config["build"]["jar_relative_path"]
    address = config["address"]
    interface = get_java_iface_name_by_local_ip(address)
    print("network interface is:",interface)

    if build or build_only:
        run_maven_build(build_dir)
        if build_only:
            return

    # Launch Adapter App (after Maven if requested)
    launch_java(build_dir, jar_relative_path, interface, address)

    # Launch Python doppelgangers
    node_ids = parse_ranges(config["python"]["node_id_ranges"])
    for node_id in node_ids:
        launch_python(config["python"]["script"], node_id, address)


if __name__ == "__main__":

    # run with python3 adapterLauncher.py
    parser = argparse.ArgumentParser(
        description="Launcher for Java and Python scripts."
    )
    parser.add_argument(
        "-b", "--build", action="store_true", help="Build using Maven before launching"
    )
    parser.add_argument(
        "-bo",
        "--build_only",
        action="store_true",
        help="Only build, do not launch anything",
    )
    args = parser.parse_args()

    main(build=args.build, build_only=args.build_only)
