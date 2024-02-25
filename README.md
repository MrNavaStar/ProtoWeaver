[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://GitHub.com/Naereen/StrapDown.js/graphs/commit-activity)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)

[![name](https://github.com/modrinth/art/blob/main/Branding/Badge/badge-dark__184x72.png?raw=true)](https://modrinth.com/mod/protoweaver)

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/G2G4DZF4D)

<img src="https://raw.githubusercontent.com/MrNavaStar/ProtoWeaver/master/loader-common/src/main/resources/assets/protoweaver/icon.png" width="300" height="300">


# ProtoWeaver
A minecraft networking library for creating custom protocols that run on the internal netty server. 

# Creating a Protocol
```java
Protocol protocol = Protocol.create("protoweaver", "proto-message")
    .enableCompression(CompressionType.GZIP)
    .setServerHandler(ProtoMessage.class)
    .setClientHandler(ProtoMessage.class)
    .addPacket(Message.class)
    .build();

ProtoWeaver.load(protocol);
```
