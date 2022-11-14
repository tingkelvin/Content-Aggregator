import filecmp
from genericpath import isfile
import os
import signal
from subprocess import PIPE, Popen
import random

bcolors = {
    "HEADER": '\033[95m',
    "OKBLUE": '\033[94m',
    "OKCYAN": '\033[96m',
    "OKGREEN": '\033[92m',
    "WARNING": '\033[4m\033[1m\u001b[31m',
    "FAIL": '\033[91m',
    "ENDC": '\033[0m',
    "BOLD": '\033[1m',
    "UNDERLINE": '\033[4m',
}


def killport(port):
    # printStatus("HEADER}")
    printStatus(
        "HEADER", f"Killilng process at PORT {port} and java process...")
    try:
        process = Popen(
            ["lsof", "-i", ":{0}".format(port)], stdout=PIPE, stderr=PIPE)
        stdout, stderr = process.communicate()
        for process in str(stdout.decode("utf-8")).split("\n")[1:]:
            data = [x for x in process.split(" ") if x != '']
            if (len(data) <= 1):
                continue
            os.kill(int(data[1]), signal.SIGKILL)
    except:
        # printStatus("WARNING}Failed to kill.")
        printStatus("WARNING", "Failed to kill.")


def compile():
    # printStatus("HEADER}Removing .class and .txt files...")
    printStatus("HEADER", "Removing .class and .txt files...")
    try:
        os.system("rm utils/*.class ")
        os.system("rm xml/*.class")
        os.system("rm *.class")
        os.system("rm *.txt")
        os.system("rm aggregatedContent")
    except:
        printStatus("WARNING", "Failed to remove.")
    printStatus("HEADER", "Removed sucessfully.")

    printStatus("HEADER", "Compiling java files... Then run.")
    try:
        os.system("javac utils/*.java")
        os.system("javac xml/*.java")
        os.system("javac *.java")
    except:
        printStatus("WARNING", "Failed to compile.")


def command(request, port, serverID, delay, heartbeat, badReq, lastCommand, verboseAS, verboseCS, verboseClient):
    if serverID < 10:
        serverID = "0" + str(serverID)
    if request == "PUT":
        os.system(
            f"java ContentServer \"127.0.0.1:{port}\" \"Content-Server-{serverID}\" content{serverID}.txt {delay} {heartbeat} {badReq} {verboseCS} {('' if lastCommand else '&')}")
    elif request == "GET":
        os.system(
            f"java GETClient \"127.0.0.1:{port}\" \"Client-{serverID}\" {verboseClient} {('' if lastCommand else '&')}")
    elif request == "Invalid-PUT":
        os.system(
            f"java ContentServer \"127.0.0.1:{port}\" \"Content-Server-{serverID}\" invalid01.txt {delay} {heartbeat} {badReq} {verboseCS} {('' if lastCommand else '&')}")
    elif request == "Null-PUT":
        os.system(
            f"java ContentServer \"127.0.0.1:{port}\" \"Content-Server-{serverID}\" null01.txt {delay} {heartbeat} {badReq} {verboseCS} {('' if lastCommand else '&')}")


def printStatus(type, str):
    type = bcolors[type]
    end = bcolors["ENDC"]
    print(f"{type}{str}{end}")


def compareTest(test, numsGet):
    passed = True

    for c in range(numsGet):
        if c < 10:
            c = "0" + str(c)
        grandTruth = f"Grandtruth-Client-{c}-Content.txt"

        output = f"Client-{c}-Content.txt"
        while not os.path.isfile(output):
            pass
        if not filecmp.cmp(output, grandTruth, shallow=False):
            printStatus(
                "WARNING", f"Test {test} did not passed, failed at Client-{c}.")
            passed = False
        else:
            printStatus(
                "OKCYAN", f"Comparing {grandTruth} and {output} \033[92m\u2713")
    if os.path.exists("Client-99-Content.txt"):
        if not filecmp.cmp("Client-99-Content.txt", "Client-999-Content.txt", shallow=False) or os.stat("Grandtruth-Client-999-Content.txt").st_size != 0:
            printStatus(
                "WARNING", f"Test {test} did not passed. The replicate does not match.")
            passed = False
        else:
            printStatus(
                "OKCYAN", f"Comparing Client-99-Content.txt, Client-999-Content.txt and Grandtruth-Client-999-Content.txt \033[92m\u2713")
    if os.path.exists("Client-9999-Content.txt"):
        if not filecmp.cmp("Client-9999-Content.txt", "Grandtruth-Client-9999-Content.txt"):
            printStatus(
                "WARNING", f"Test {test} did not passed. The replicate does not match.")
            passed = False
        else:
            printStatus(
                "OKCYAN", f"Comparing Client-9999-Content.txt, Grandtruth-Client-9999-Content.txt \033[92m\u2713")
    if passed:

        printStatus("OKGREEN", f"Test {test} passed.")


def generateCommands(delay, verboseAS, verboseCS, verboseClient):
    putOrders = random.sample(range(12), 12)
    currentPut = 0
    currentGet = 0
    commands = []
    while currentPut != len(putOrders):
        if random.choice([0, 1]) == 1:
            commands.append({"type": "PUT", "serverID": putOrders[currentPut], "delay": random.randint(0, delay),
                             "last": False, "heartbeat": "false", "badReq": False, "verboseAS": verboseAS, "verboseCS": verboseCS, "verboseClient": verboseClient})
            currentPut += 1
        else:
            commands.append({"type": "GET", "serverID": currentGet, "delay": 0,
                             "last": False, "heartbeat": "false", "badReq": False, "verboseAS": verboseAS, "verboseCS": verboseCS, "verboseClient": verboseClient})
            currentGet += 1

    commands.append({"type": "GET", "serverID": currentGet, "delay": 0,
                     "last": True, "heartbeat": "false", "badReq": False, "verboseAS": verboseAS, "verboseCS": verboseCS, "verboseClient": verboseClient})
    currentGet += 1
    return currentGet, commands


def generateFinalCommands(delay, verboseAS, verboseCS, verboseClient):
    putOrders = random.sample(range(21, 26), 5)
    currentPut = 0
    currentGet = 0
    commands = []
    while currentPut != len(putOrders):
        if random.choice([0, 1]) == 1:
            if random.choice([0, 1]) == 1:
                commands.append({"type": "PUT", "serverID": putOrders[currentPut], "delay": random.randint(0, delay),
                                "last": False, "heartbeat": "true", "badReq": False, "verboseAS": verboseAS, "verboseCS": verboseCS, "verboseClient": verboseClient})
                currentPut += 1
        else:
            commands.append({"type": "GET", "serverID": currentGet, "delay": 0,
                             "last": False, "heartbeat": "false", "badReq": False, "verboseAS": verboseAS, "verboseCS": verboseCS, "verboseClient": verboseClient})
            currentGet += 1

    commands.append({"type": "GET", "serverID": currentGet, "delay": 0,
                     "last": True, "heartbeat": "false", "badReq": False, "verboseAS": verboseAS, "verboseCS": verboseCS, "verboseClient": verboseClient})
    currentGet += 1
    return currentGet, commands
