* This directory containes files to be applied for the proper ingress setup to be working with AWS k3s self spin up cluster *
* Files needs to be applied in the certain order to make it work *

1. metal-lb LB
2. nginx-ingress-setup

# k3s_cluster_aws

TO_DO list:
1. run command "sudo visudo" on Jenkins server and add string(running pipeline without passw request):
   jenkins ALL=(ALL) NOPASSWD: /usr/bin/apt-get, /usr/bin/curl, /usr/bin/tee, /usr/bin/apt-key, /usr/bin/sh
2. Add AWS instance access key to the Jenkins credentials for the ancible playbook running (kind: SSH Username with private key) 
3. Find TASK [Print kubeconfig content], take a config text and convert it from JSON to YAML
4. On local PC or any instance with kubectl "cd ~/.kube" and run "sudo nano config". Paste yaml config, change master-node IP and save it.
5. Check connection "kubectl get nodes"

6. Go to my repository locally: Lessons - k3s_cluster_aws_jenkins_nginx - cluster - aws_ingress_setup and run:
   kubectl apply -f 1.metallb.yaml (run LB)
   kubectl apply -f 2.nginx-ingress.yaml
7. Go to my repository locally: Lessons - k3s_cluster_aws_jenkins_nginx - cluster_entities/pacman
     kubectl apply -f mongo-deployment.yaml (running DB first of all)
     kubectl apply -f packman-deployment.yaml
     kubens (run it to see namespaces)
     kubens pacman(choose namespace pacman)
     kubectl get pods (run to see what pods do you have)
     # kubectl get pods -A (OPTIONAL, to check how many pods total do we have)
8. * Create Route 53 in AWS
   Hosted zones - copy master node ip into value
   * If you do not have Route 53 do next: in pacman-deployment.yaml in line 22 put Master node public IPv4 DNS # ec2-54-146-202-253.compute-1.amazonaws.com # остановилась здесь !!!



7. Edit pacman-deployment.yaml: specifiy K3s_master url in line 22. Example:
   - host: ec2-34-230-71-4.compute-1.amazonaws.com
   
8. Go to cluster_entities/pacman and run:
   kubectl apply -f mongo-deployment.yaml
   kubectl apply -f packman-deployment.yaml    