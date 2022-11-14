import argparse
import os

parser = argparse.ArgumentParser()
# Add an argument
parser.add_argument('--port', type=int, default=4567)
parser.add_argument('--verboseAS', type=str, default="true")
parser.add_argument('--verboseCS', type=str, default="false")
parser.add_argument('--verboseClient', type=str, default="false")
# Parse the argument
args = parser.parse_args()
port = args.port

print("\033[96mRunning Tests...")

for i in range(8):

    print(f"\033[96mRunning Test {i+1}...")
    os.system(
        f"python3 test{i+1}.py --port {port} --verboseAS {args.verboseAS} --verboseCS  {args.verboseCS} --verboseClient {args.verboseClient}")
