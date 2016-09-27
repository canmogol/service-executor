from com.fererlab.service import Service


class Authentication(Service):
    def handle(self, event):
        print("Python object: {}".format(self))
        print("Python Event: {}".format(event))
        print("username: {}".format(event.body["username"]))
        return {"logged": True, "groups": ["admin", "user"]}

    def sayHi(self, request):
        print("Python object: {}".format(self))
        print("Python Request: {}".format(request))
        return {"say": "Hi {}".format(request.params["name"])}
