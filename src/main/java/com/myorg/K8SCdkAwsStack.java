package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;

import java.util.List;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.sqs.Queue;

public class K8SCdkAwsStack extends Stack {
	public K8SCdkAwsStack(final Construct scope, final String id) {
		this(scope, id, null);
	}

	public K8SCdkAwsStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);

		// The code that defines your stack goes here

		// example resource
		final Queue queue = Queue.Builder.create(this, "K8SCdkAwsQueue").visibilityTimeout(Duration.seconds(300))
				.build();
		
        final Cluster cluster = Cluster.Builder.create(this, "ecs-cluster")
                .clusterName("my-test-cluster")
                .vpc(getVPCStack(this, "app-name", "my-test-eks-vpc"))
            .build();
	}
	
    public Vpc getVPCStack(final Construct scope, final String appName, String vpcName) {
        Vpc vpc = Vpc.Builder.create(this, "vpc")
                .cidr("10.0.0.0/16")
                .enableDnsHostnames(true)
                .enableDnsSupport(true)
                .maxAzs(2)
                .subnetConfiguration(
                        List.of(
                            SubnetConfiguration.builder()
                        .name("public")
                        .subnetType(SubnetType.PUBLIC)
                                .build()))
                .build();
        Tags.of(vpc).add("Name", vpcName);
        Tags.of(vpc).add("Purpose", "training");
        
        List<ISubnet> subnets = vpc.getPublicSubnets();
        int counter =0;
        for (ISubnet subnet : subnets) {
            counter++;
            CfnOutput.Builder.create(this,"output-subnet-id-"+counter).value(subnet.getSubnetId()).build();
        }
        CfnOutput.Builder.create(this, "output-vpc-id").value(vpc.getVpcId()).build();
        
        return vpc;
    }
}
