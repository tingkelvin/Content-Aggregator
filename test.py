import threading
import os
import subprocess
import time

complied = False


try:
    os.system("javac utils/*.java ")
    os.system("javac xml/*.java ")
    os.system("javac *.java ")
except:
    print("Failed to compile")

os.system("kill -9 $(lsof -ti:8080)")
os.system("kill -9 $(ps -ef | pgrep -f \"java\")")

os.system("java AggregatorServer &")
time.sleep(3)

os.system("java GETClient \"127.0.0.1:8080\" \"Client-0\" &")
time.sleep(3)

os.system(
    "java ContentServer \"127.0.0.1:8080\" \"Content-Server-0\" content00.txt 0 &")
time.sleep(3)

os.system("java GETClient \"127.0.0.1:8080\" \"Client-1\" &")
time.sleep(3)

os.system(
    "java ContentServer \"127.0.0.1:8080\" \"Content-Server-1\" content01.txt 10 &")

time.sleep(3)
os.system("java GETClient \"127.0.0.1:8080\" \"Client-2\"")


os.system("rm utils/*.class ")
os.system("rm xml/*.class")
os.system("rm *.class")

os.system("kill -9 $(lsof -ti:8080)")
os.system("kill -9 $(ps -ef | pgrep -f \"java\")")

os._exit(0)
