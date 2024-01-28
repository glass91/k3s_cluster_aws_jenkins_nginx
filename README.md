hi iy is aws cluster 
* This directory containes files to be applied for the proper ingress setup to be working with AWS k3s self spin up cluster *
* Files needs to be applied in the certain order to make it work *


                            # k3s_cluster_aws #

                             ### TO_DO LIST:###

1. Do this step just once!!! run command "sudo visudo" on Jenkins server and add string(running pipeline without passw request):
   jenkins ALL=(ALL) NOPASSWD: /usr/bin/apt-get, /usr/bin/curl, /usr/bin/tee, /usr/bin/apt-key, /usr/bin/sh

2. Add AWS instance access key to the Jenkins credentials for the ancible playbook running (kind: SSH Username with private key) Do it once!!!

3. Find at the Jenkins logs certificate-authority-data JASON and convert it into YAML(use goodle!!!)

4. On local PC or any instance with kubectl "cd ~/.kube" and run "sudo nano config". Insert YAML config into config, and insert  MASTER-NODE IP and save it(IPMASTER:6443)

6. kubectx (check namespace) We need default namespace(#kubectx namespace# use it, if you need to change namespace)

5. Check connection "kubectl get nodes" (If you can see pods, it means we are connected to our cluster. We can see information for each pod like name, status, roles, age, and version)

6. (open second terminal window for convenience) After we connected to the cluster, we need to go to my(your) repository locally: Lessons NEXT k3s_cluster_aws_jenkins_nginx NEXT cluster_init  NEXT  aws_ingress_setup and run there two commands:

   kubectl apply -f 1.metallb.yaml (run LoadBalancer)

   kubectl apply -f 2.nginx-ingress.yaml(nginx ingress controler)

After you ran two commands in the terminal, you can go and open your OpenLens and you can see there is everything were applied correctly and running. Go to DE(default), network(services). If we see IPaddress, it means everything is working.

8. Deploying PACMAN. Go to my repository locally: Lessons - k3s_cluster_aws_jenkins_nginx NEXT cluster_entities/pacman

     kubectl apply -f mongo-deployment.yaml (running DB first of all)

     kubectl apply -f packman-deployment.yaml

     kubens (run it to see namespaces)

     kubens pacman(choose namespace pacman)

     kubectl get pods (run to see what pods do you have)

     # kubectl get pods -A (OPTIONAL, to check how many pods total do we have)
     Go to the OpenLens, network-service(you can see that we got one more servece, pacman. Type of service is LB, because it's 
     serviced by metallb)


#  8.  Create Route 53 in AWS(only if yiu have your own IP)
#  Copy master node IP NEXT go to serch area and type Route 53 NEXT click on HoustedZone #  NEXT click on Craete Record NEXT insert master IP into value section.


9. run command: curl insert(public IPv4 DNS). If you see some information like "head", means it is working.


10. If you do not have Route 53 do next: in pacman-deployment.yaml in line 22 put Master node public IPv4 DNS (example how it's looks like:ec2-54-146-202-253.compute-1.amazonaws.com). Next, you need to apply this, so run this command at pacman namespace in the terminal: kubectl apply -f packman-deployment.yaml


11. run command: curl insert(public IPv4 DNS). If you see some information like "head", means it is working.

  


               CONGRATULATIONS!!! You can play PACMAN game!!! :))) 











