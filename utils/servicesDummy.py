from flask import Flask, request, jsonify, Response
import threading
import time

app = Flask(__name__)


visibility_model_state = {}
membership_state = {}


class AdapterEndpoints:
    MESSAGES = "messages"
    VISIBILITY_MODEL = "visibilityModel"
    UPDATE_MEMBERSHIP = "updateMembership"


def async_handler(endpoint, data, respond):
    print(f"Handling async request for {endpoint} with data: {data}")
    time.sleep(0.5)

    if endpoint == AdapterEndpoints.MESSAGES:
        msg = f"Processed {endpoint} with data: {data}"
        respond(Response(msg, status=200, mimetype="text/plain"))

    elif endpoint == AdapterEndpoints.VISIBILITY_MODEL:
        global visibility_model_state
        visibility_model_state = data
        msg = f"Updated visibility model with data: {data}"
        respond(Response(msg, status=200, mimetype="text/plain"))

    elif endpoint == AdapterEndpoints.UPDATE_MEMBERSHIP:
        global membership_state
        membership_state = data
        msg = f"Updated membership with data: {data}"
        respond(Response(msg, status=200, mimetype="text/plain"))

    else:
        respond(Response("Unknown endpoint", status=404, mimetype="text/plain"))


@app.route("/rest/adapter/messages", methods=["POST"])
def messages():
    data = request.get_json()
    thread = threading.Thread(
        target=async_handler, args=(AdapterEndpoints.MESSAGES, data, lambda r: None)
    )
    thread.start()
    return Response(
        "Request received and processing...", status=202, mimetype="text/plain"
    )


@app.route("/rest/adapter/visibilityModel", methods=["POST"])
def visibility_model_post():
    data = request.get_json()
    thread = threading.Thread(
        target=async_handler,
        args=(AdapterEndpoints.VISIBILITY_MODEL, data, lambda r: None),
    )
    thread.start()
    return Response(
        "Request received and processing...", status=202, mimetype="text/plain"
    )


@app.route("/rest/adapter/membership", methods=["POST"])
def membership_post():
    data = request.get_json()
    thread = threading.Thread(
        target=async_handler,
        args=(AdapterEndpoints.UPDATE_MEMBERSHIP, data, lambda r: None),
    )
    thread.start()
    return Response(
        "Request received and processing...", status=202, mimetype="text/plain"
    )


@app.route("/rest/adapter/visibilityModel", methods=["GET"])
def visibility_model_get():
    return jsonify(visibility_model_state), 200


@app.route("/rest/adapter/membership", methods=["GET"])
def membership_get():
    return jsonify(membership_state), 200


if __name__ == "__main__":
    app.run(debug=True, port=8080)
