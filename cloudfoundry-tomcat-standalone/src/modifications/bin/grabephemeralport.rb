#!ruby

# https://github.com/cloudfoundry/vcap-common/blob/master/lib/vcap/common.rb

require 'socket'

socket = TCPServer.new('0.0.0.0', 0)
socket.setsockopt(Socket::SOL_SOCKET, Socket::SO_REUSEADDR, true)
Socket.do_not_reverse_lookup = true
port = socket.addr[1]
socket.close
puts port