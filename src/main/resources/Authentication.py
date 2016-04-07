from com.fererlab.service import Service


class Authentication(Service):
    def handle(self, event):
        print("Python object: {}".format(self))
        print("Python Event: {}".format(event))
        print("username2: {}".format(event["username"]))
        return {"logged": True, "groups": ["user"]}
