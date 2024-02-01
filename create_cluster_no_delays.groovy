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


        stage('Install JQ, kubectl and Ansible') {
            steps {
                sh '''
                sudo apt-get update
                sudo apt-get install -y jq apt-transport-https ca-certificates curl
                sudo curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
                echo "deb https://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee -a /etc/apt/sources.list.d/kubernetes.list
                sudo apt-get update
                sudo apt-get install -y kubectl ansible
                '''
            }
        }
        stage('Terraform Plan - Main VPC') {
            steps {
                sh '''
                cd ./k3s_cluster_aws_jenkins_nginx/cluster_init/terraform/main_vpc_config
                terraform init -input=false
                terraform plan -out=terraform.tfplan
                '''
            }
        }
        stage('Terraform Apply - Main VPC') {
            steps {
                sh '''
                cd ./k3s_cluster_aws_jenkins_nginx/cluster_init/terraform/main_vpc_config
                terraform apply -input=false terraform.tfplan
                '''
            }
        }
        stage('Terraform Plan - Master Node') {
            steps {
                sh '''
                cd ./k3s_cluster_aws_jenkins_nginx/cluster_init/terraform/master_node_config
                terraform init -input=false
                terraform plan -out=terraform.tfplan
                '''
            }
        }
        stage('Terraform Apply - Master Node') {
            steps {
                sh '''
                cd ./k3s_cluster_aws_jenkins_nginx/cluster_init/terraform/master_node_config
                terraform apply -input=false terraform.tfplan
                terraform output -json k3s_master_instance_private_ip | jq -r 'if type == "array" then .[] else . end' > ../../ansible/master_ip.txt
                terraform output -json k3s_master_instance_public_ip | jq -r 'if type == "array" then .[] else . end' > ../../ansible/master_ip_public.txt
                '''
            }
        }
        stage('Terraform Plan - Worker Nodes') {
            steps {
                sh '''
                cd ./cluster_init/terraform/worker_node_config
                terraform init -input=false
                terraform plan -out=terraform.tfplan
                '''
            }
        }
        stage('Terraform Apply - Worker Nodes') {
            steps {
                sh '''
                cd ./cluster_init/terraform/worker_node_config
                terraform apply -input=false terraform.tfplan
                '''
            }
        }
        stage('Terraform Apply - Worker Nodes IP outputs') {
            steps {
                sh '''
                sleep 120
                cd ./cluster_init/terraform/worker_node_config
                terraform plan -out=terraform.tfplan
                terraform apply -input=false terraform.tfplan
                terraform output -json k3s_workers_instance_private_ip | jq -r '.[]' > ../../ansible/worker_ip.txt
                '''
            }
        }
        stage('Run Ansible Playbooks') {
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'jenkins-ansible', keyFileVariable: 'SSH_KEY')]) {
                sh '''
                cd ./cluster_init/ansible
                ansible-playbook -i master_ip.txt master_setup.yml -u ubuntu --private-key=$SSH_KEY -e 'ansible_ssh_common_args="-o StrictHostKeyChecking=no"'
                ansible-playbook -i worker_ip.txt worker_setup.yml -u ubuntu --private-key=$SSH_KEY -e 'ansible_ssh_common_args="-o StrictHostKeyChecking=no"'
                '''
                }
            }
        }
        stage('Install ingress and pacman in k3s') {
    steps {
        script {
            sh '''
            cd ./cluster_init/aws_ingress_setup
            kubectl apply -f 1.metallb.yaml
            sleep 60
            kubectl apply -f 2.nginx-ingress.yaml
            '''
        }
        script {
            sh '''
            cd ./cluster_entities/pacman
            kubectl apply -f mongo-deployment.yaml
            kubectl apply -f packman-deployment.yaml
            '''
            }
        }
    }

        stage('Create Route53 Record') {
            steps {
                sh '''
                cd ./cluster_init/terraform/route53_record
                terraform init -input=false
                terraform plan -out=terraform.tfplan
                terraform apply -input=false terraform.tfplan
                '''
            }
        }
    }
}
