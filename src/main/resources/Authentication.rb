class Authentication
  include com.fererlab.service.Service

  def handle(event)
    puts "Ruby Object: #{self}"
    puts "Ruby Event: #{event}"
    puts "Username: #{event['username']}"
    Hash[:logged => true, :groups => %w(admin user)]
  end
end
Authentication.new