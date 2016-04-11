from com.fererlab.service import WSService


class Authentication(WSService):
    def handle(self, event):
        print("Python object: {}".format(self))
        print("Python Event: {}".format(event))
        print("body: {}".format(event.body))
        print("username: {}".format(event.body["username"]))
        # return {"logged": True, "groups": ["user"]}
