# Chatty
### Chatty application based on Socket programming in Java and its GUIs designed with javaFX. Chat main features:
 The server and clients may run in the same network on different computers, e.g. Local
Area Networks (LAN).
 The user is informed when a new user arrives and when a user has gone by showing the
information.
 Each message is prefixed with server or client to keep track of who sent the message.
 The application consists of two parts (projects): server and client. Each of them can run
on separate computers independently

### The main two parts:
1. Server: a single server responsible for listening to and accepting request for a connection
at a given port (number)
-A thread for the successful connection to get messages from users and send them back
2. Client: one or more clients that request connection by providing host address (IP
address) and port number
-A thread to accept the incoming message from the server continuously

## Note:
To execute the application run Server class at first then run Client class
without closing the server.
