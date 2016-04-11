class Authentication
  include com.fererlab.service.WSService

  def handle(event)
    puts "Ruby Object: #{self}"
    puts "Ruby Event: #{event}"
    puts "body: #{event.body}"
    puts "username: #{event.body["username"]}"
    # Hash[:logged => true, :groups => %w(admin user)]
  end
end
Authentication.new