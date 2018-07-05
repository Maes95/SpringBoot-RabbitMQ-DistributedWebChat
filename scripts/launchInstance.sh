aws ec2 run-instances \
	--image-id ami-58d7e821 \
	--count $1 \
	--instance-type t2.micro \
	--key-name TFG \
	--security-group-ids sg-ab59c2d6 \
	--subnet-id subnet-dd426694
