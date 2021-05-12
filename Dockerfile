from sspreitzer/shellinabox:latest
RUN apt-get update
RUN apt-get install -y curl
RUN curl -O https://packages.chef.io/files/stable/chefdk/2.4.17/ubuntu/16.04/chefdk_2.4.17-1_amd64.deb
RUN dpkg -i chefdk_2.4.17-1_amd64.deb
RUN apt-get update
RUN mkdir /myvol
#WORKDIR /etc
#RUN mkdir chef
#RUN mkdir secrets
RUN mkdir /etc/secrets
RUN mkdir /etc/chef
#WORKDIR /etc/chef
#ADD ./Files/authe* /etc/chef/
#ADD ./Files/secrets/* /etc/secrets/
ADD ./Files/script/* /etc/script/
ADD ./Files/entrypoint/* /usr/local/sbin/
########################
#RUN apt-get install -y vim
#ENV VISUAL=vim
#ENV EDITOR="$VISUAL"

RUN apt-get update
RUN apt-get install -y openjdk-8-jre
EXPOSE 8080:8080
COPY ./build/libs/*.jar chef-databag-service.jar
#ENTRYPOINT /etc/script/startScript.sh