sudo add-apt-repository -y ppa:openjdk-r/ppa
sudo apt-get update
sudo apt-get install -y openjdk-8-jdk
sudo apt-get install -y rabbitmq-server

sudo service rabbitmq-server stop
sudo bash -c 'echo -n 0123456789 > /var/lib/rabbitmq/.erlang.cookie'
sudo service rabbitmq-server start
sudo rabbitmqctl status
