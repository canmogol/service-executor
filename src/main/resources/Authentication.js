function println(log) {
    var System = Java.type('java.lang.System');
    System.out.println(log);
}

function Authentication() {

    this.handle = function (event) {
        println('Javascript Object: ' + this);
        println('Javascript Event: ' + event);
        println('Javascript username: ' + event.body.username);
        var response = {logged: true, 'groups': ['admin', 'user']};
        return response;
    };

    // http://localhost:9876/api/sayHi/?name=john&surname=wick
    this.sayHi = function (request) {
        var Objects = Java.type('java.util.Objects');
        println('Javascript Object: ' + Objects.hashCode(this));
        println('Javascript request: ' + request);
        return {"say": "Hi! " + request["params"]["name"] + " " + request["params"]["surname"]};
    }

}

function instance() {
    return new Authentication();
}
