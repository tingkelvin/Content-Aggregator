# Content Aggregator in Java
## Objectives

The main Objectives of this project are:

An ATOM server (or aggregation server) that responds to requests for feeds and also accepts feed updates from clients. The aggregation server will store feed information persistently, only removing it when the content server who provided it is no longer in contact, or when the feed item is not one of the most recent 20.

A client that makes an HTTP GET request to the server and then displays the feed data, stripped of its XML information.

A CONTENT SERVER that makes an HTTP PUT request to the server and then uploads a new version of the feed to the server, replacing the old one. This feed information is assembled into ATOM XML after being read from a file on the content server's local filesystem.

## Requirement

Multiple clients may attempt to GET simultaneously and are required to GET the aggregated feed that is correct for the Lamport clock adjusted time if interleaved with any PUTs. Hence, if A PUT, a GET, and another PUT arrive in that sequence then the first PUT must be applied and the content server advised, then the GET returns the updated feed to the client then the next PUT is applied. In each case, the participants will be guaranteed that this order is maintained if they are using Lamport clocks.

Multiple content servers may attempt to simultaneously PUT. This must be serialised and the order maintained by Lamport clock timestamp.

Aggregation server will expire and remove any content from a content server that it has not communicated within the last 12 seconds. You may choose the mechanism for this but you must consider efficiency and scale.

All elements must be capable of implementing Lamport clocks, for synchronization and coordination purposes.

## Aggregation Server
To keep things simple, we will assume that there is one file in your filesystem which contains a list of entries and where are they come from. It does not need to be an ATOM format, but it must be able to convert to a standard ATOM file when the client sends a GET request. However, this file must survive the server crashing and re-starting, including recovering if the file was being updated when the server crashed! Your server should restore it as was before re-starting or a crash. You should, therefore, be thinking about the PUT as a request to handle the information passed in, possibly to an intermediate storage format, rather than just as overwriting a file. This reflects the subtle nature of PUT - it is not just a file write request! You should check the feed file provided from a PUT request to ensure that it is valid. The file details that you can expect are detailed in the Content Server specification.

All the entities in your system must be capable of maintaining a Lamport clock.

The first time your ATOM feed is created, you should return status 201 - HTTP_CREATED. If later uploads are ok, you should return status 200. (This means, if a Content Server first connects to the Aggregation Server, then return 201 as succeed code, then before the content server lost connection, all other succeed response should use 200). Any request other than GET or PUT should return status 400 (note: this is not standard but to simplify your task). Sending no content to the server should cause a 204 status code to be returned. Finally, if the ATOM XML does not make sense you may return status code 500 - Internal server error.

Your server is designed to stay current and will remove any items in the feed that have come from content servers which it has not communicated with for 12 seconds. How you do this is up to you but please be efficient!

## GET client

Your Content Server
Your content server will start up, reading two parameters from the command line, where the first is the server name and port number (as for GET) and the second is the location of a file in the file system local to the Content Server (It is expected that this file located in your project folder). The file will contain a number of fields from the ATOM format that are to be assembled into an ATOM XML feed and then uploaded to the server. You may assume that all fields are text and that there will be no embedded HTML or XHMTL. The list of ATOM elements that you need to support are:

title
subtitle
link
updated
author
name
id
entry
summary
 

Input file format
To make parsing easier, you may assume that input files will follow this format:

title:My example feed
subtitle:for demonstration purposes
link:www.cs.adelaide.edu.au
updated:2015-08-07T18:30:02Z
author:Santa Claus
id:urn::uuid:60a76c80-d399-11d9-b93C-0003939e0af6
entry
title:Nick sets assignment
link:www.cs.adelaide.edu.au/users/third/ds/
id:urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a
updated:2015-08-07T18:30:02Z
summary:here is some plain text. Because I'm not completely evil, you can assume that this will always be less than 1000 characters. And, as I've said before, it will always be plain text.
entry
title:second feed entry
link:www.cs.adelaide.edu.au/users/third/ds/14ds2s1
id:urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6b
updated:2015-08-07T18:29:02Z
summary:here's another summary entry which a reader would normally use to work out if they wanted to read some more. It's quite handy.

Your GET client will start up, read the command line to find the server name and port number (in URL format) and will send a GET request for the ATOM feed. This feed will then be stripped of XML and displayed, one line at a time, with the attribute and its value. Your GET client's main method will reside in a file called GETClient.java. Possible formats for the server name and port number include "http://servername.domain.domain:portnumber", "http://servername:portnumber" (with implicit domain information) and "servername:portnumber" (with implicit domain and protocol information).

You should display the output so that it is easy to read but you do not need to provide active hyperlinks. You should also make this client failure-tolerant and, obviously, you will have to make your client capable of maintaining a Lamport clock.

## PUT message format
Your PUT message should take the format:

PUT /atom.xml HTTP/1.1
User-Agent: ATOMClient/1/0
Content-Type: (You should work this one out)
Content-Length: (And this one too)

<?xml version='1.0' encoding='iso-8859-1' ?>
<feed xml:lang="en-US" xmlns="http://www.w3.org/2005/Atom">
(And then your file of data)
...
</feed>
Your content server will need to confirm that it has received the correct acknowledgment from the server and then check to make sure that the information is in the feed as it was expecting. It must also support Lamport clocks.


## Test

There are 8 tests written in python. The test script, will kill the process at the port, remove .class and test results, compile the java files and run them. The txt files generated by the GETClient will compare to the grandtruth result generated from aggregator server.
You can run each test one by one using this command:
```sh
python3 test1.py
```
or run all the tests:
```sh
python3 run.py
```

Script agruments:
These arguements work on both individual test and run.py:
```sh
--port PORT (default is 4567)
--verboseAS true/false (default: true) set true to show aggregation server logs
--verboseCS true/false (default: false) set true to show content server logs
--verboseClient true/false (default: false) set true to show client server logs
```
For example, this will run all tests at port 8080 without showing any logs:
```sh
python3 run.py --port 8080 --verboseAS false
```

### Test 1 - Normal Circumstance
This will test the server under normal circumstance.
After aggregator server is initiated, client-00 will send a GET request, follow by a PUT request from content-server-00, finally a GET request from client-01.

### Test 2 - Delay Upload
This will test the server when content server uploading content with some delay.
After aggregator server is initiziated, client-00 will send a GET request, follow by a PUT request from content-server-00 but it will take 10 seconds to upload, then another PUT request from content-server-01 to show that the delay casused by content-server-00 does not block incoming request, then finally a PUT request from client-01.
The PUT request from client-01 will be hold until content-server-00 finish the uploading to retrieve content from content-server-00 and content-server-01.

### Test 3 - Invalid Conent, Null Content, Invalid Request and Retry Mechanic.
The aggregator will recieved 3 PUT request: invalid content where missing id entry from content-sever-00, null content from content-server-01 and a POST request from content-server-02.
"500 Internal Server Error", "204 No Content" and "400 Bad Request" are the response of these invalid request respectively. A GET request from client-00 to show that there are no content from the aggregator server.
The content servers will retry again with valid content.
Finally, a GET request from client-01 to show that valid content is retrieved from aggregator server.

### Test 4 - Most Recent 20 Content
The aggregator will recieved 21 PUT request follow by a GET request. The GET request will retrieve the most recent 20 content.

### Test 5 - Heartbeat
The aggregator will recieved 2 PUT request from 2 different content servers. The second content server will keep sending heartbeat message to aggregator server while the first content server will not.
A GET request is sent after 12 seconds to test if the only content sent by second content server will be retrivied while the content at the first server is expired.

### Test 6 - Replicate Content
The aggregator will recieved 2 PUT request and 1 GET request. Then the aggragtor server will reboot and a GET request is sent. The content retrieved from 2 GET request should be same.

### Test 7 - Lamport Clock, Race Condition and Order of Content
Around 30 commands mix of PUT and GET request will be randomly generated and fired at once to see if the Lamport clock is working as expected. This is a more robust automactic testing. The txt files generated by the GETClient will compare to the grandtruth result generated from aggregator server.

### Test 8 - Final Test
Similar to previous test, around 30 commands mix of PUT and GET request will be randomly generated and fired at once. Some of the PUT commands will be delay in uploading. All the content servers will send heartbeat messages. After these 30 commands, client-99 will send a GET request then all java process are killed and the aggregator server will reboot, client-999 will send a GET request immediately to see if the aggregator content is preserved. After 12 seconds, another PUT request is sent to see if all the content are expired.

### Extra Features
- XML Parser, from content server to aggregator and from aggregator to client server.
- Robust actomatically testing.
- Mutiple entries support.
- Colorful terminal text
- Delay content uploading
