var System = Java.type('java.lang.System');

function Authentication() {
    this.handle = function (event) {
        System.out.println('Javascript Object ' + this);
        System.out.println('Javascript Event ' + event);
        System.out.println('Javascript o>>> ' + event.head.origin.name);
        System.out.println('Javascript o>>> ' + event.head.origin.domain);
        System.out.println('Javascript o>>> ' + event.head.origin.ip);
        System.out.println('Javascript o>>> ' + event.head.origin.port);
        System.out.println('Javascript o>>> ' + event.head.origin.memoryMappedFile);
        System.out.println('Javascript o>>> ' + event.head.origin.startedAt);
        System.out.println('Javascript o>>> ' + event.head.origin.name);
        System.out.println('Javascript hi>>> ' + event.head.id);
        System.out.println('Javascript ht>>> ' + event.head.type);
        System.out.println('Javascript hc>>> ' + event.head.createdAt);
        System.out.println('Javascript b>>> ' + event.body);
        var response = {logged: true, 'groups': ['admin', 'user']};
        return response;
    };
}

function instance() {
    return new Authentication();
}
