Overview
========
This plugin prevent impersonation in chat and running dangerous commands on bukkit/spigot servers connected to bungeecord.

It is designed for multiuser servers where different persons administrate the bukkit/spigot instances, and they can potentially ssh forward connections to bypass authentication to other instances.

How well does it secure your server
===================================
To ensure as low load on the server, it currently only guards against chat and commands. So an attacker can potentially harm the server by destroying blocks/or making suicide. But the attacker only have (by default) 1 second to do the damage before getting kicked.

If you see a lot of reconnects from an important account, you can seach for "SECURITY WARNING" in the bukkit/spigot log files which will tell you if it is a bad connection or something more serious (having a cron job or similar to watch for such lines is also a good plan ofc ;))
Using the timestamps from the log you should also be able to undo damage using some log plugin.

Usage
=====
1. Add BungeeBouncerServer.jar the in your bungeecord plugins folder
2. Startup/reload bungeecord
3. Make sure only the newly created "bungeebouncer.private.key" has secure file permissions
4. Copy bungeebouncer.public.key to the root folder of a bukkit/spigot connected to this bungeecord instance
5. Add BungeeBouncerClient.jar to the bukkit/spigot instance

How it works
============
It uses 1024but RSA keys to verify messages from the bungeecord plugin to the bukkit/spigot plugin.

When a player initially connects chat/commands are denied. The player name and a nounce it sent to the bungeecord (using the plugin messaging protocol)

If the bungeecord receives this a reply is made which is signed using the private key.

The bukkit/server gets this reply and verifies the signature, and kicks the player if either a timeout happens or that anything is wrong with the reply.
