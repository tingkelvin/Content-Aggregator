import filecmp
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
    printStatus("BOLD", "--------Running Test--------")
    os.system(f"java AggregatorServer {port} \"false\" {args.verboseAS} &")
    time.sleep(3)
    commands = [{"type": "PUT", "serverID": 0, "delay": 0,
                 "last": False, "heartbeat": "false", "badReq": False, "verboseAS": args.verboseAS, "verboseCS": args.verboseCS, "verboseClient": args.verboseClient},
                {"type": "PUT", "serverID": 1, "delay": 0,
                 "last": False, "heartbeat": "false", "badReq": False, "verboseAS": args.verboseAS, "verboseCS": args.verboseCS, "verboseClient": args.verboseClient},
                {"type": "GET", "serverID": 0, "delay": 0,
                 "last": False, "heartbeat": "false", "badReq": False, "verboseAS": args.verboseAS, "verboseCS": args.verboseCS, "verboseClient": args.verboseClient},
                {"type": "GET", "serverID": 99, "delay": 0,
                 "last": True, "heartbeat": "false", "badReq": False, "verboseAS": args.verboseAS, "verboseCS": args.verboseCS, "verboseClient": args.verboseClient}

                ]

    for c in commands:
        if c["last"]:
            time.sleep(1)
            killport(port)
            time.sleep(1)
            os.system(
                f"java AggregatorServer {port} \"false\" {args.verboseAS} &")
            time.sleep(1)
            # each command ends with & if not last command
            # make sure the last command run at last otherwise the print in terminal will be hold.
            command(c["type"], port, c["serverID"],
                    c["delay"], c["heartbeat"], c["badReq"], c["last"], c["verboseAS"], c["verboseCS"], c["verboseClient"])
        else:
            time.sleep(1)
            command(c["type"], port, c["serverID"],
                    c["delay"], c["heartbeat"], c["badReq"],  c["last"], c["verboseAS"], c["verboseCS"], c["verboseClient"])

    printStatus("BOLD", "--------Finished Test--------")
    printStatus("OKCYAN", "Comparing Test Results")

    if not filecmp.cmp("Client-00-Content.txt", "Client-99-Content.txt", shallow=False):
        printStatus(
            "WARNING", f"Test 6 did not passed.")
    else:
        printStatus(
            "OKCYAN", f"Comparing Client-00-Content.txt and Client-99-Content.txt \033[92m\u2713")
        printStatus("OKGREEN", f"Test 6 passed.")
    # Wait for files to write
    time.sleep(1)
    # compareTest(5, numsGet)


if __name__ == "__main__":
    init()
    runTest()
    killport(port)
