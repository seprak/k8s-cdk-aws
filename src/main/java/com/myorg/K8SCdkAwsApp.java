package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Aws;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.eks.Cluster;

import java.util.Arrays;
import java.util.Properties;

public class K8SCdkAwsApp {
    public static void main(final String[] args) {
        App app = new App();

        new K8SCdkAwsStack(app, "K8SCdkAwsStack", StackProps.builder()
                // If you don't specify 'env', this stack will be environment-agnostic.
                // Account/Region-dependent features and context lookups will not work,
                // but a single synthesized template can be deployed anywhere.

                // Uncomment the next block to specialize this stack for the AWS Account
                // and Region that are implied by the current CLI configuration.
                /*
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                */

                // Uncomment the next block if you know exactly what Account and Region you
                // want to deploy the stack to.
                /*
                .env(Environment.builder()
                        .account("123456789012")
                        .region("us-east-1")
                        .build())
                */

                // For more information, see https://docs.aws.amazon.com/cdk/latest/guide/environments.html
                .build());

        Properties properties = Config.properties;

        StackProps props = StackProps.builder().env(Environment.builder()
                .account(Aws.ACCOUNT_ID)
                .region(Aws.REGION).build())
                .build();

//        VpcStack vpcStack = new VpcStack(app, Strings.getPropertyString("vpc.stack",
//                properties,
//                Constants.VPC_STACK.getValue()), props);

        IVpc vpc = Vpc.fromLookup(app, "vpc-0d45e0b565cbf8994", VpcLookupOptions.builder().vpcName("eks-private-VPC").vpcId("vpc-0d45e0b565cbf8994").build()); // vpcStack.getVpc();

        EksStack eksStack = new EksStack(app, Strings.getPropertyString("eks.stack",
                properties,
                Constants.EKS_STACK.getValue()), props, vpc);

        Cluster cluster = eksStack.getCluster();
        
        // TODO add application
        app.synth();
    }
}

