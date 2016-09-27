# WSService-executor
Micro Service Executor


ReloadFilter is available for every request

## Sample Requests
#### POST

Blocking, Open/Request/Response/Close restful web service call

URL:
<http://localhost:9876/api/handle>

Header:
`"Content-Type":"application/json"`

Body:

```
{
    "head":{
        "id":"d95749da-dd6c-45dd-965c-9aa07ca6cf76",
        "type":"Authenticate",
        "createdAt":1460127590058,
        "origin":{
            "name":"Web",
            "domain":"fererlab.com",
            "ip":"10.0.1.4",
            "port":9877,
            "memoryMappedFile":"/tmp/mmFiles/fererlab.com/Web",
            "startedAt":1460127494386
        }
    },
    "body":{
        "username":"john",
        "password":"wick",
        "source":"browser"
    }
}
```

Expected Result:
```
{
  "status": "OK"
}
```
----
#### GET

Blocking, Open/Request/Response/Close restful web service call

URL:
<http://localhost:9876/api/sayHi?name=john&surname=wick>

Header:
`"Content-Type":"application/json"`

No - Body:

Expected Result:
```
{
  "say": "Hi! john wick"
}
```
----
#### WEB SOCKET

Non-Blocking, multi request response restful web service call, check "index.html" content.

Sample Site:
<http://localhost:9876/index/>

WS: <ws://localhost:9876/event>

No - Header

Body:

```
{
    "head":{
        "id":"d95749da-dd6c-45dd-965c-9aa07ca6cf76",
        "type":"Authenticate",
        "createdAt":1460127590058,
        "origin":{
            "name":"Web",
            "domain":"fererlab.com",
            "ip":"10.0.1.4",
            "port":9877,
            "memoryMappedFile":"/tmp/mmFiles/fererlab.com/Web",
            "startedAt":1460127494386
        }
    },
    "body":{
        "username":"john",
        "password":"wick",
        "source":"browser"
    }
}
```

Expected Result:
```
{
  "status": "OK"
}
```
