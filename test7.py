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
    os.system(f"java AggregatorServer {port} \"true\" {args.verboseAS} &")
    time.sleep(3)
    # commands = [
    #     {"type": "PUT", "serverID": 1, "delay": 0, "last": False},
    #     {"type": "PUT", "serverID": 2, "delay": 0, "last": False},
    #     {"type": "GET", "serverID": 1, "delay": 0, "last": True}]
    commands = [{"type": "PUT", "serverID": i, "delay": 0,
                 "last": False, "heartbeat": "false", "badReq": False, "verboseAS": args.verboseAS, "verboseCS": args.verboseCS, "verboseClient": args.verboseClient}
                for i in range(20)]
    numsGet, randomCommands = generateCommands(
        0, args.verboseAS, args.verboseCS, args.verboseClient)
    commands = commands + randomCommands
    for c in commands:
        if c["last"]:
            # each command ends with & if not last command
            # make sure the last command run at last otherwise the print in terminal will be hold.
            time.sleep(1)
            command(c["type"], port, c["serverID"],
                    c["delay"], c["heartbeat"], c["badReq"], False, c["verboseAS"], c["verboseCS"], c["verboseClient"])

            time.sleep(1)
            c = {"type": "GET", "serverID": 99, "delay": 0, "last": False,
                 "heartbeat": "false", "badReq": "false", "verboseAS": args.verboseAS, "verboseCS": args.verboseCS, "verboseClient": args.verboseClient}
            command(c["type"], port, c["serverID"],
                    c["delay"], c["heartbeat"], c["badReq"], c["last"], c["verboseAS"], c["verboseCS"], c["verboseClient"])
            time.sleep(1)
            killport(port)

            os.system(
                f"java AggregatorServer {port} \"true\" {args.verboseAS} &")

            time.sleep(1)
            c = {"type": "GET", "serverID": 999, "delay": 0, "last": True,
                 "heartbeat": "false", "badReq": "false", "verboseAS": args.verboseAS, "verboseCS": args.verboseCS, "verboseClient": args.verboseClient}
            command(c["type"], port, c["serverID"],
                    c["delay"], c["heartbeat"], c["badReq"], c["last"], c["verboseAS"], c["verboseCS"], c["verboseClient"])

        else:
            command(c["type"], port, c["serverID"],
                    c["delay"], c["heartbeat"], c["badReq"],  c["last"], c["verboseAS"], c["verboseCS"], c["verboseClient"])

    printStatus("BOLD", "--------Finished Test--------")
    printStatus("OKCYAN", "Comparing Test Results")

    # Wait for files to write
    time.sleep(1)
    compareTest(7, numsGet)


if __name__ == "__main__":
    init()
    runTest()
    killport(port)
