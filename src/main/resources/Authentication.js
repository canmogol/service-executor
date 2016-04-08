var System = Java.type('java.lang.System');

function Authentication() {
    this.handle = function (event) {
        System.out.println('Javascript Object ' + this);
        System.out.println('Javascript Event ' + event);
        var response = {logged: true, 'groups': ['admin', 'user']};
        return response;
    };
}

function instance() {
    return new Authentication();
}
