# JDial

[![CircleCI](https://circleci.com/gh/detached/jdial/tree/master.svg?style=svg)](https://circleci.com/gh/detached/jdial/tree/master)

JDial is an plain java implementation of the Discovery and Launch (DIAL) protocol version 2.1 defined by Netflix and YouTube.

DIAL allows second screen devices (smartphone, laptop, ...) to discover server instances in the local network and 
launch applications on a first screen device (smart TV).

For additional information about the protocol see [Wikipedia](https://en.wikipedia.org/wiki/Discovery_and_Launch) 
and [dial-multiscreen.org](http://www.dial-multiscreen.org).

A list of reserved application names can also be found on the [dial-multiscreen.org](http://www.dial-multiscreen.org/dial-registry/namespace-database) site.

JDial has no dependencies to any library and can therefore integrated in every program and app.

## Discover

```
List<DialServer> devices = new Discovery().discover();
```

## Creat a DielClientConnection

```
DialServer dialServer = devices.get(0);
DialClient dialClient = new DialClient();

DialClientConnection tv = dialClient.connectTo(dialServer);
```

## Discover applications

```
Optional<Application> app = tv.getApplication(Application.YOUTUBE);
```

## Start applications

```
tv.startApplication(app.get());
```

## Stop applications

```
tv.stopApplication(app.get());
```

## Implement application vendor protocol
```
myTv.startApplication(app.get(), "{ \"example\": \"foobaar\" }"::getBytes)
```

## Legacy support

Some server implementations are not compatible with current versions of the DIAL protocol.
For backward compatibility create a tweaked ProtocolFactory.

```
bool supportLegacyDevices = true;
ProtocolFactory factory = new ProtocolFactoryImpl(supportLegacyDevices);
DialClient dialClient = new DialClient(factory);
```

## Logging

Logging is done via java util logging.
