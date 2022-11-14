import os
import time
from utils import killport, bcolors, compile, command, printStatus, compareTest, generateCommands
import argparse

parser = argparse.ArgumentParser()
# Add an argument
parser.add_argument('--port', type=int, default=4567)
parser.add_argument('--verboseAS', type=str, default="true")
parser.add_argument('--verboseCS', type=str, default="false")
parser.add_argument('--verboseClient', type=str, default="false")
# Parse the argument
args = parser.parse_args()
port = args.port


def init():
    killport(port)
    compile()


def runTest():
    commands = [
        {"type": "Invalid-PUT", "serverID": 0,
            "delay": 0, "last": False, "heartbeat": "false", "badReq": "false", "verboseAS": args.verboseAS, "verboseCS": args.verboseCS, "verboseClient": args.verboseClient},
        {"type": "Null-PUT", "serverID": 1, "delay": 0,
                 "last": False, "heartbeat": "false", "badReq": "false", "verboseAS": args.verboseAS, "verboseCS": args.verboseCS, "verboseClient": args.verboseClient},
        {"type": "PUT", "serverID": 2, "delay": 0,
         "last": False, "heartbeat": "false", "badReq": True, "verboseAS": args.verboseAS, "verboseCS": args.verboseCS, "verboseClient": args.verboseClient},
        {"type": "GET", "serverID": 0, "delay": 0, "last": False,
         "heartbeat": "false", "badReq": "false", "verboseAS": args.verboseAS, "verboseCS": args.verboseCS, "verboseClient": args.verboseClient},
        {"type": "GET", "serverID": 1, "delay": 0, "last": True, "heartbeat": "false", "badReq": "false", "verboseAS": args.verboseAS, "verboseCS": args.verboseCS, "verboseClient": args.verboseClient}]

    printStatus("BOLD", "--------Running Test--------")
    os.system(f"java AggregatorServer {port} \"true\" {args.verboseAS} &")
    time.sleep(3)

    for c in commands:
        if c["last"]:
            # each command ends with & if not last command
            # make sure the last command run at last otherwise the print in terminal will be hold.
            time.sleep(6)
            command(c["type"], port, c["serverID"],
                    c["delay"], c["heartbeat"], c["badReq"], c["last"], c["verboseAS"], c["verboseCS"], c["verboseClient"])
        else:
            command(c["type"], port, c["serverID"],
                    c["delay"], c["heartbeat"], c["badReq"],  c["last"], c["verboseAS"], c["verboseCS"], c["verboseClient"])
            time.sleep(1)

    printStatus("BOLD", "--------Finished Test--------")

    time.sleep(5)
    printStatus("OKCYAN", "Comparing Test Results")
    compareTest(3, 2)


if __name__ == "__main__":
    init()
    runTest()
    killport(port)
