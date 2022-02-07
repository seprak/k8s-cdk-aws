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
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.KubernetesVersion;
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
	}
	

}
