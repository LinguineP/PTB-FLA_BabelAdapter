import json
import socket
import time
import sys
import requests
from flask import Flask, request
from multiprocessing import Process
from multiprocessing.connection import Listener, Client

# Constants
DEFAULT_MPAPI_LOCAL_MAILBOX_PORT = 6000  # Default port
HTTP_TIMEOUT = 50
MAX_RETRY_COUNT = 1

MSG_ITER_NO = "msgIterNo"
MSG_SEQ_NO = "msgSeqNo"
MSG_SRC_ADR = "msgSrcAdr"
MSG_DATA = "msgData"
MSG_DST = "dst"
MSG_SRC = "src"
MSG_PAYLOAD = "payload"
MSG_REMOTE_FLAG = "remote"

app = Flask(__name__)

# ---------- HTTP Server (Receiving Messages from Java) ----------


def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(
            ("10.255.255.255", 1)
        )  # the address 10.255.255.255 is from the Private IP Address Range
        local_ip = s.getsockname()[0]
        s.shutdown(socket.SHUT_RDWR)
        s.close()
        return local_ip
    except Exception as e:
        print("Error:", e)
        return None


@app.route("/messages", methods=["POST"])
def bringup():
    payload = request.json
    print(
        "HTTP POST /messages — Received payload from Java:", payload
    )  # TODO enhance parsing, align java and json, construct it firs on paper
    print(
        "sending message from java via mpapithere",
        payload[MSG_DST],
        payload[MSG_PAYLOAD],
    )
    sendMsg(
        ("localhost", payload[MSG_DST]), payload[MSG_PAYLOAD]
    )  # this is where it fails
    return "OK", 200


def start_http_server(local_port, interface_ip):
    app.run(host="localhost", port=local_port + 1000)


# ---------- Multiprocessing Server (Receiving Messages from PTBFLA) ----------


def server_fun(local_port, interface_ip):
    local_server_address = ("localhost", local_port)
    with Listener(local_server_address) as listener:
        print(
            f"[MPAPI] Listening for multiprocessing connections on {local_server_address}..."
        )
        while True:
            with listener.accept() as conn:
                print(
                    "Accepted multiprocessing connection from", listener.last_accepted
                )
                bmsg = conn.recv()
                msg = json.loads(bmsg)
                print("Received multiprocessing message:", msg)

                if msg == "exit":
                    break

                send_to_java(msg, local_port, interface_ip)


# ---------- Message Forwarding (Python to Java via HTTP) ----------


def send_to_java(payload, local_port, interface_ip):
    global MSG_DATA, MSG_DST, MSG_SRC, MSG_PAYLOAD, MSG_ITER_NO, MSG_SEQ_NO, MSG_SRC_ADR
    try:

        msg = {
            MSG_SRC_ADR: interface_ip,
            MSG_SRC: payload.get(MSG_SRC_ADR)[1],
            MSG_DST: local_port,
            MSG_PAYLOAD: payload,
        }

        url = f"http://{interface_ip}:8080/rest/adapter/messages"  # TODO parametraize this,

        print(msg)
        print(url)

        print("--------------------####################____________________")

        response = requests.post(url, json=msg, timeout=HTTP_TIMEOUT)
        print(f"[send_to_java] post to {url} succeeded")

    except Exception as e:
        print(f"[send_to_java] Fatal error: {e}")


# ---------- Forwarding Message to Python Processes ----------


def sendMsg(remoteServerAddress, msg):

    with Client(remoteServerAddress) as conn:
        bmsg = json.dumps(msg).encode("utf-8")
        if msg.get(MSG_SRC_ADR) != INTERFACE_IP:
            conn.send(bmsg)
        return


# ---------- Entrypoint ----------

if __name__ == "__main__":
    global MPAPI_LOCAL_MAILBOX_PORT
    print(sys.argv)

    if len(sys.argv) > 1:
        try:
            MPAPI_LOCAL_MAILBOX_PORT = int(sys.argv[1])
            INTERFACE_IP = sys.argv[2] if len(sys.argv) > 2 else "localhost"
        except ValueError:
            print("Invalid port number provided. Using the default value.")
            MPAPI_LOCAL_MAILBOX_PORT = DEFAULT_MPAPI_LOCAL_MAILBOX_PORT
    else:
        print("No port provided. Using the default value.")
        MPAPI_LOCAL_MAILBOX_PORT = DEFAULT_MPAPI_LOCAL_MAILBOX_PORT

    print(MPAPI_LOCAL_MAILBOX_PORT)
    # Start the server process to listen for multiprocessing connections
    mpapi_process = Process(
        target=server_fun,
        args=(
            MPAPI_LOCAL_MAILBOX_PORT,
            INTERFACE_IP,
        ),
    )
    flask_process = Process(
        target=start_http_server,
        args=(
            MPAPI_LOCAL_MAILBOX_PORT,
            INTERFACE_IP,
        ),
    )

    mpapi_process.start()
    flask_process.start()

    mpapi_process.join()
    flask_process.join()
