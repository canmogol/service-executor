class Authentication
  include com.fererlab.service.Service

  def handle(event)
    puts "Ruby Object: #{self}"
    puts "Ruby Event: #{event}"
    puts "username: #{event.body["username"]}"
    Hash[:logged => true, :groups => %w(admin user)]
  end

  def sayHi(request)
    puts "Ruby Object: #{self}"
    puts "Ruby Request: #{request}"
    Hash[:say => "Hi #{request.params["name"]}"]
  end

end
AuthenticationG.new
