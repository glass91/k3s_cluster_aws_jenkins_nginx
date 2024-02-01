pipeline {
    options {
        ansiColor('xterm')
    }
    agent any
    tools {
        terraform 'tf1.6'
    }

    stages {
        stage('Clone Git repo') {
            steps {
                git(
                    branch: 'main', 
                    url: 'https://github.com/glass91/k3s_cluster_aws_jenkins_nginx.git', 
                    credentialsId: 'acces_to_git'
                )
            }
        } 
        stage('Terraform Plan Destroy Worker Nodes') {
            steps {
                sh '''
                cd ./cluster_init/terraform/worker_node_config
                terraform init -input=false
                terraform plan -destroy -out=terraform_destroy.tfplan
                '''
            }
        }
        stage('Terraform Apply Destroy Worker Nodes') {
            steps {
                sh '''
                cd ./cluster_init/terraform/worker_node_config
                terraform apply -input=false terraform_destroy.tfplan
                '''
            }
        }
        stage('Terraform Plan Destroy Master Node(s)') {
            steps {
                sh '''
                cd ./cluster_init/terraform/master_node_config
                terraform init -input=false
                terraform plan -destroy -out=terraform_destroy.tfplan
                '''
            }
        }
        stage('Terraform Apply Destroy Master Node(s)') {
            steps {
                sh '''
                cd ./cluster_init/terraform/master_node_config
                terraform apply -input=false terraform_destroy.tfplan
                '''
            }
        }
        stage('Terraform Plan Destroy VPC') {
            steps {
                sh '''
                cd ./cluster_init/terraform/main_vpc_config
                terraform init -input=false
                terraform plan -destroy -out=terraform_destroy.tfplan
                '''
            }
        }
        stage('Terraform Apply Destroy VPC') {
            steps {
                sh '''
                cd ./cluster_init/terraform/main_vpc_config
                terraform apply -input=false terraform_destroy.tfplan
                '''
            }
        }
    }
}
