package com.myorg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.eks.CapacityType;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.EndpointAccess;
import software.amazon.awscdk.services.eks.KubernetesVersion;
import software.amazon.awscdk.services.eks.Nodegroup;
import software.amazon.awscdk.services.eks.NodegroupAmiType;
import software.amazon.awscdk.services.eks.NodegroupRemoteAccess;
import software.amazon.awscdk.services.iam.IManagedPolicy;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.constructs.Construct;

/**
 * CDK EKS Stack
 */
public class EksStack extends Stack {

    private Cluster cluster;
    public EksStack(final Construct scope, final String id) {
        this(scope, id, null, null);
    }

    /**
     * Entrypoint
     *
     * @param scope
     * @param id
     * @param props
     */
    public EksStack(final Construct scope, final String id, final StackProps props, IVpc vpc) {
        super(scope, id, props);

        // Get properties object
        final Properties properties = Config.properties;

        Tags.of(scope).add("owner", properties.getProperty("labels.owner", "vlado"));
        Tags.of(scope).add("env", properties.getProperty("labels.env", "dev"));
        Tags.of(scope).add("app", properties.getProperty("labels.app", "testApp"));


        String eksId = Strings.getPropertyString("eks.id",
                properties,
                Constants.EKS_ID.getValue());

        this.cluster = Cluster.Builder.create(this, eksId)
                .clusterName(eksId)
                .defaultCapacity(Strings.getPropertyInt("eks.default.capacity", properties, Constants.EKS_DEFAULT_CAPACITY.getIntValue()))
                .endpointAccess(EndpointAccess.PRIVATE)
                .version(KubernetesVersion.V1_21)
                .vpc(vpc)
                .build();

        Tags.of(cluster).add("owner", properties.getProperty("labels.owner"));
        Tags.of(cluster).add("env", properties.getProperty("labels.env"));
        Tags.of(cluster).add("app", properties.getProperty("labels.app"));

        // Gather policies for node role
        List<IManagedPolicy> policies = new ArrayList<>();
        policies.add(ManagedPolicy.fromManagedPolicyArn(this, "node-policy",
                Strings.getPropertyString("iam.policy.arn.eks.node", properties, Constants.NOT_FOUND.getValue())));
        policies.add(ManagedPolicy.fromManagedPolicyArn(this, "cni-policy",
                Strings.getPropertyString("iam.policy.arn.eks.cni", properties, Constants.NOT_FOUND.getValue())));
        policies.add(ManagedPolicy.fromManagedPolicyArn(this, "registry-policy",
                Strings.getPropertyString("iam.policy.arn.ecr.read", properties, Constants.NOT_FOUND.getValue())));
        policies.add(ManagedPolicy.fromManagedPolicyArn(this, "autoscaler-policy",
                Strings.getPropertyString("iam.policy.arn.eks.node.autoscaler", properties, Constants.NOT_FOUND.getValue())));
        policies.add(ManagedPolicy.fromManagedPolicyArn(this, "ssm-policy",
                Strings.getPropertyString("iam.policy.arn.ssm.core", properties, Constants.NOT_FOUND.getValue())));
        policies.add(ManagedPolicy.fromManagedPolicyArn(this, "kms-policy",
                Strings.getPropertyString("iam.policy.arn.kms.ssm.use", properties, Constants.NOT_FOUND.getValue())));

        Role nodeRole = Role.Builder.create(this, "eks-nodes-role")
                .roleName("EksNodes")
                .managedPolicies(policies)
                .assumedBy(new ServicePrincipal(Strings.getPropertyString("ec2.service.name", properties, "")))
                .build();

        /*
         * Build Nodegroup
         */
        Nodegroup nodegroup = Nodegroup.Builder.create(this, "ng1")
                .cluster(cluster)
                //.launchTemplateSpec(LaunchTemplateSpec.builder().id("cdk-eks-launch-template").build())
                .tags(Map.of("owner","jimmy","env","dev","app","read-only"))
                .amiType(NodegroupAmiType.AL2_X86_64)
                .capacityType(CapacityType.ON_DEMAND)
                .desiredSize(3)
                .maxSize(5)
                .minSize(3)
                .diskSize(100)
                .labels(Map.of("node-group", "ng1", "instance-type", Strings.getPropertyString("eks.instance.type",
                        properties,
                        Constants.EKS_INSTANCE_TYPE.getValue())))
                .remoteAccess(NodegroupRemoteAccess.builder()
                        .sshKeyName(Strings.getPropertyString("ssh.key.name",
                                properties, "")).build())
                .nodegroupName("ng1")
                .instanceTypes(List.of(new InstanceType(Strings.getPropertyString("eks.instance.type",
                        properties,
                        Constants.EKS_INSTANCE_TYPE.getValue()))))
                .subnets(SubnetSelection.builder().subnets(cluster.getVpc().getPrivateSubnets()).build())
                .nodeRole(nodeRole)
                .build();

        Tags.of(nodegroup).add("name", "ng1-node");
        Tags.of(nodegroup).add("owner", properties.getProperty("labels.owner"));
        Tags.of(nodegroup).add("env", properties.getProperty("labels.env"));
        Tags.of(nodegroup).add("app", properties.getProperty("labels.app"));
    }

    public Cluster getCluster() {
        return this.cluster;
    }
}