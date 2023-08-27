mvn clean compile install

local:
docker build . -t yerganat/broker

docker build . --platform linux/amd64 -t yerganat/broker

docker push yerganat/broker


additional:
docker ps
docker stop broker_container
docker rm broker_container


server:
docker login
docker pull yerganat/broker

docker run -p 80:8080 --name broker_container -d yerganat/broker


MYSQL
GRANT ALL PRIVILEGES ON broker.* TO 'admin'@'%';                             
https://zomro.com/blog/faq/291-kak-ustanovit-mysql-8-v-docker
docker-compose up -d


http://89.223.120.217:8091/index.php?route=/sql&pos=0&db=broker&table=excel
admin/broker2023


http://89.223.120.217
admin/Broker01012022
