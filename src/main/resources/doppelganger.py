import json
import time
import sys
import socket
from multiprocessing import Process, Queue
from multiprocessing.connection import Listener, Client


ADAPTER_BABEL_ADDR = (
    "localhost",
    55432,
)  # Address of the BabelAdapterApp
ADAPTER_OFFSET = 1000
MPAPI_LOCAL_MAILBOX_PORT = 6000  # default for listening to incoming mpapi connections

JAVA_BUFFER_SIZE = 1024

localIP=""


MSG_ITER_NO = "msgIterNo"
MSG_SEQ_NO = "msgSeqNo"
MSG_SRC_ADR = "msgSrcAdr"
MSG_DATA = "msgData"
MSG_REMOTE_FLAG="remote"

MSG_DST = "dst"
MSG_SRC = "src"
MSG_PAYLOAD = "payload"
ORIGIN_HOST_IP="ip"

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


# Function to handle incoming connections from Python processes
def server_fun(local_port):
    local_server_address = ("localhost", local_port)

    with Listener(local_server_address) as listener:
        print(f"Listening for connections on {local_server_address}...")

        while True:
            with listener.accept() as conn:
                #print("Connection accepted from", listener.last_accepted)
                bmsg = conn.recv()

                msg = json.loads(bmsg)
                #Sprint("Received message from multiprocessing:", msg)
                if msg=='exit':
                    continue
                # Forward msg to the Java application
                send_to_java(msg,local_port)


def sendMsg(remoteServerAddress, msg):
    MAX_RETRY_COUNT = 100
    counter = 0
    while counter < MAX_RETRY_COUNT:
        try:
            with Client(remoteServerAddress) as conn:
                bmsg = json.dumps(msg).encode("utf-8")
                conn.send(bmsg)
                break
        except OSError as e:  # former socket.error exception
            print(
                f"Modlule msg_passing_api, Function sendMsg: An exectption {e} occured, the operation will be retried..."
            )
            time.sleep(0.2)  # wait for 200 ms
            counter += 1
    if counter >= MAX_RETRY_COUNT:
        sys.exit()


# Function to send messages to the Java application
def send_to_java(payload,dst):
    if payload[MSG_REMOTE_FLAG]==1:
        return
    payload[MSG_REMOTE_FLAG]=1
    msg = {
        MSG_DST: dst,
        MSG_SRC: payload[MSG_SRC_ADR][1],
        MSG_PAYLOAD: payload,
        ORIGIN_HOST_IP:get_local_ip(),
    }
    try:
        # Set up a socket to send messages to the Java application
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.connect(ADAPTER_BABEL_ADDR)
            bmsg = json.dumps(msg).encode("utf-8")
            s.sendall(bmsg)
            #print("Message sent to Java application:", msg)
    except Exception as e:
        print(f"Failed to send message to Java application: {e}")


# Function to listen for incoming messages from the babelAdapter app
def listen_for_java_messages(java_port):
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.bind(("localhost", java_port))
            s.listen()
            #print(f"Listening for messages on port {java_port}...")
            while True:
                conn, addr = s.accept()
                with conn:
                    #print("Connected by", addr)
                    while True:
                        data = conn.recv(JAVA_BUFFER_SIZE)
                        if not data:
                            break
                        msg = json.loads(data.decode())
                        #print("Received message from Java application:", msg)
                        # Forward the message if needed
                        if msg.get(ORIGIN_HOST_IP) != get_local_ip():
                            sendMsg(("localhost", msg[MSG_DST]), msg[MSG_PAYLOAD])
    except Exception as e:
        print(f"Error: {e}")
    finally:
        print("Closing socket...")


if __name__ == "__main__":

    mpapi_mailbox = int(sys.argv[1])

    localIP=get_local_ip()

    # Start the server process to listen for multiprocessing connections
    process = Process(target=server_fun, args=(mpapi_mailbox,))
    process.start()

    adapter_mailbox = mpapi_mailbox + ADAPTER_OFFSET
    MPAPI_LOCAL_MAILBOX_PORT=mpapi_mailbox

    # Start the process to listen for messages from the Java application
    java_listener_process = Process(
        target=listen_for_java_messages, args=(adapter_mailbox,)
    )
    java_listener_process.start()
    java_listener_process.join()

    process.join()
