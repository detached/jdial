# JDial

JDial is an plain java implementation of the Discovery and Launch (DIAL) protocol version 2.1 defined by Netflix and YouTube.

DIAL allows second screen devices (smartphone, laptop, ...) to discover server instances in the local network and 
launch applications on a first screen device (smart TV).

For additional information about the protocol see [Wikipedia](https://en.wikipedia.org/wiki/Discovery_and_Launch) 
and [dial-multiscreen.org](http://www.dial-multiscreen.org).

A list of reserved application names can also be found on the [dial-multiscreen.org](http://www.dial-multiscreen.org/dial-registry/namespace-database) site.

JDial has no dependencies to any library and can therefore integrated in every program and app.

# Dependency declaration

You can find the latest releaese in the [central repository](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.w3is%22%20a%3A%22jdial%22).

## Maven

```
<dependency>
    <groupId>de.w3is</groupId>
    <artifactId>jdial</artifactId>
    <version>1.3</version>
</dependency>
```
## Gradle

```
compile 'de.w3is:jdial:1.3'
```

# Usage

## Discover

```
List<DialServer> devices = new Discovery().discover();
```

## Creat a DialClientConnection

```
DialServer dialServer = devices.get(0);
DialClient dialClient = new DialClient();

DialClientConnection tv = dialClient.connectTo(dialServer);
```

## Discover applications

```
Application youtube = tv.getApplication(Application.YOUTUBE);
```

## Start applications

```
tv.startApplication(youtube);
```

## Stop applications

```
tv.stopApplication(youtube);
```

## Implement application vendor protocol
```

 DialContent content = new DialContent() {
 
     @Override
     public String getContentType() {
         return "application/json; encoding=UTF-8";
     }

     @Override
     public byte[] getData() {
          return "{}".getBytes(Charset.forName("UTF-8"));
     }
 };


myTv.startApplication(youtube, content)
```

## Legacy support

Some server implementations are not compatible with current versions of the DIAL protocol.
For example some LG TVs support DIAL, but the server implementation can't handle query parameters.
By creating a ProtocolFactoryImpl and setting the `legacyCompatibility` flag the client doesn't set any query parameter.

```
bool supportLegacyDevices = true;
ProtocolFactory factory = new ProtocolFactoryImpl(supportLegacyDevices);
DialClient dialClient = new DialClient(factory);
```

## Logging

Logging is done via java util logging.
