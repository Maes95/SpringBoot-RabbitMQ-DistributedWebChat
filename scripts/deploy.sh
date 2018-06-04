if [ "$#" -lt  "3" ]
 then
   # Use: ./deploy MyPem.pem ec2-XXX-XXX-XXX-XXX.eu-west-1.compute.amazonaws.com myJar.jar XXX.XXX.XXX.XXX XXX.XXX.XXX.XXX
   echo "Use: ./deploy PEM DNS JAR IP SEED"
fi

PEM=$1
DNS=$2
FILE=$3
IS_SLAVE=$4
MASTER=$5

TARGET="ubuntu@${DNS}:/home/ubuntu/"

# INSTALL DEPENDENCIES

scp -i $PEM -o "StrictHostKeyChecking no" scripts/installDependencies.sh "${TARGET}installDependencies.sh"
ssh -i $PEM -o "StrictHostKeyChecking no" "ubuntu@${DNS}" chmod +x "installDependencies.sh"
ssh -i $PEM "ubuntu@${DNS}" ./installDependencies.sh




ssh -i $PEM "ubuntu@${DNS}" "sudo rabbitmqctl stop_app"
ssh -i $PEM "ubuntu@${DNS}" "sudo rabbitmqctl reset"
if [ "$IS_SLAVE" = "True" ]
then
	ssh -i $PEM "ubuntu@${DNS}" "sudo rabbitmqctl join_cluster rabbit@ip-${MASTER}"
fi
ssh -i $PEM "ubuntu@${DNS}" "sudo rabbitmqctl start_app"

ssh -i $PEM "ubuntu@${DNS}" "pkill -f java"
ssh -i $PEM "ubuntu@${DNS}" "rm WebChat.jar"
scp -i $PEM $FILE "${TARGET}WebChat.jar"
ssh -i $PEM "ubuntu@${DNS}" "java -jar WebChat.jar"



# FILE=Vertx-DistributedWebChat/target/WebChatVertxMaven-0.1.0-fat.jar
#
# # NODE 1
# scp -i $PEM $FILE ubuntu@ec2-54-154-158-98.eu-west-1.compute.amazonaws.com:/home/ubuntu/WebChatVertxMaven-0.1.0-fat.jar
# # NODE 2
# scp -i $PEM $FILE ubuntu@ec2-54-154-158-98.eu-west-1.compute.amazonaws.com:/home/ubuntu/WebChatVertxMaven-0.1.0-fat.jar
# target/WebChatVertxMaven-0.1.0-fat.jar
