[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://GitHub.com/Naereen/StrapDown.js/graphs/commit-activity)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)

[<img alt="modrinth" height="40" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/modrinth_vector.svg">](https://modrinth.com/plugin/protoweaver)

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/G2G4DZF4D)

<img src="https://raw.githubusercontent.com/MrNavaStar/ProtoWeaver/master/loader-common/src/main/resources/assets/protoweaver/icon.png" width="300" height="300">


# ProtoWeaver
A minecraft networking library for creating custom protocols that run on the internal netty server. 

#### What dis mean?
Sick and tired of hyjacking client connections to send data to and from a proxy? Well I sure was. ProtoWeaver allows you to create a custom protocol that runs on the same port that the minecraft server uses.
This means a proxy such as velocity is able to communicate with the server regardless if players are on online or not, without running an extra socket server on a different port. Protoweaver is also not just
limited to proxies. Using the `client` module, any 3rd party java application can talk directly with ProtoWeaver!

Protoweaver is fast, secure, and easy to use. All protocols run under the same netty instance used by minecraft, are encypted with ssl, and are wrapped in a very straight forward api. Lets take a look:

### Features
- [x] Zero user config required
- [x] SSL encrytion
- [x] Protocol authentication
- [x] Packets as POJO's
- [x] Compression (Gzip, Snappy, LZ)
- [x] All major mod loaders (neoforge soon tm)
- [ ] API for registering raw protocols (http, ssh, etc)
- [x] Custom serialization
- [ ] Custom SSL providers/certs
- [ ] Muti-protocol connections
- [ ] Tell me what you want to see!

### Project Setup
In your build.gradle include

*** Note: **Do not shade protoweaver! It will not work properly. ProtoWeaver is a mod/plugin that is installed along side yours!**
``` gradle
repositories {
    maven { url "https://maven.mrnavastar.me/releases" }
}

dependencies {
    // 'common' can be replaced with any of: `client`, `fabric`, `forge`, `paper` or `proxy`.
    implementation "me.mrnavastar.protoweaver:common:1.3.11"
}
```

### Creating a Protocol
Protocols are very customizable with a lot of built in functionality. Here is an example protocol:
```java
Protocol protocol = Protocol.create("my_mod_id", "cool_protocol")
    .setCompression(CompressionType.GZIP)
    .setServerHandler(MyCustomServerHandler.class)
    .setClientHandler(MyCustomClientHandler.class)
    .setMaxConnections(15)
    .addPacket(String.class)
    .build();

// Load the protocol before it can be used
ProtoWeaver.load(protocol);
```

### Sending packets
Sending packets is extremely simple. Any POJO that has been added to the protocol (See [Creating a Protcol](#creating-a-protocol)) is a valid packet that can be sent. Note that all POJO serialization is handled through [kyro](https://github.com/EsotericSoftware/kryo), a fast and fast serialization library.
```java
// If you have a reference to a 'ProtoConnection', you can simply:
connection.send(myObject);
// If you have a reference to a 'ProtoClient', you can also:
client.send(myObject);
```

### Handling packets
All protocols need handlers in order to implement functionality. The client and server can either use the same handler, or both use different ones.
```java
public class MyCustomServerHandler implements ProtoConnectionHandler {
    // Note that all functions are optional to implement

    @Override
    public void onReady(ProtoConnection connection) {
        System.out.println("awesome! looks like a new client has connected from: " + connection.getRemoteAddress());
    }

    @Override
    public void onDisconnect(ProtoConnection connection) {
        System.out.println("goodbye: " + connection.getRemoteAddress());
    }

    @Override
    public void handlePacket(ProtoConnection connection, Object packet) {
        System.out.println("wow! got: " + packet.toString() + " from: " + connection.getRemoteAddress());
    }
}
```

```java
public class MyCustomClientHandler implements ProtoConnectionHandler {
    // Note that all functions are optional to implement

    @Override
    public void onReady(ProtoConnection connection) {
        System.out.println("awesome! connected to: " + connection.getRemoteAddress());
    }

    @Override
    public void onDisconnect(ProtoConnection connection) {
        System.out.println("goodbye: " + connection.getRemoteAddress());
    }

    @Override
    public void handlePacket(ProtoConnection connection, Object packet) {
        System.out.println("saying hi!");
        connection.send("Hey server!");
    }
}
```

### Custom Serialization
Sometimes it can be useful to send minecraft objects or other premade POJO's in your protocol. To allow this, you can register a custom serializer.

For example, here is a serializer for NBT tags:
```java
public class NbtSerializer extends ProtoSerializer<CompoundTag> {

    @Override
    public void write(ByteArrayOutputStream buffer, CompoundTag value) {
        try {
            NbtIo.writeCompressed(value, buffer);
        } catch (IOException e) {
            Platform.throwException(e);
        }
    }

    @Override
    public CompoundTag read(ByteArrayInputStream buffer) {
        try {
            return NbtIo.readCompressed(buffer, NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            Platform.throwException(e);
            throw new RuntimeException(e);
        }
    }
}
```
Then to register the serializer, simply do:
```java
Protocol protocol = Protocol.create("my_mod_id", "cool_protocol")
    .addPacket(CompoundTag.class, NbtSerializer.class)
    .build();
```

### More Docs Coming soon! Maybe a real website??
