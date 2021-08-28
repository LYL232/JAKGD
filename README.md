# jakgd

web

## 前端:

- vue
- eslint
- element-plus
- v-md-editor
- mathjax

## 后端:

- springboot security jwt restful
- nginx
- neo4j 3.5

## 部署:

- 第一次:

  - 修改后端jwt秘钥:

    ./backend/src/main/resources/application.yml:

    - jwt-secret: 'JAKGD' 修改成任意字符串

  - 初始化数据库: 执行脚本:

    ./neo4j/init_db.sh
    
  - 打包后端springboot:

    cd backend

    mvn -Dmaven.test.skip -U clean package
    
  - docker-compose up --build

- 启动之后停止：docker-compose stop

- 停止之后重新启动：docker-compose start

- https部署
  - 将证书文件分别重命名为'server.crt'和'server.key'放入./nginx/ssl文件夹下
  - 修改./nginx/default.conf中的server_name为ssl证书对应的域名

## neo4j

### 数据备份与恢复: 暴力复制覆盖

直接将项目目录下/neo4j/data的所有内容复制备份并在新环境中的相关目录删除干净后复制过去即可

### 数据备份：使用neo4j-admin

由于docker-compose 启动的容器以neo4j作为入口，所以不能停止neo4j，否则会导致容器停止，所以需要另起一个容器在停止neo4j的情况下进行数据备份

#### windows下用bash run，顺便把7474和7687端口开放

```bash
winpty docker run -it -v E:\\path\\to\\jakgd\\neo4j\\data:/data -v E:\\path\\to\\jakgd\\neo4j\\logs:/var/lib/neo4j/logs -p 7474:7474 -p 7687:7687 --name jakgd_neo4j_access <neo4j-images> bash
```

之后就可以在关闭docker-compose的情况下进行数据备份，只需要启动这个容器即可

```bash
docker start jakgd_neo4j_access
```

由于jakgd_neo4j_access这个容器的/data是与宿主机挂载的，所以将其备份到/data/下就可以在宿主机上访问

```bash
neo4j-admin dump --database=graph.db --to=/data/
```

### 数据恢复：使用neo4j-admin

#### windows下用bash run一个不挂载宿主机目录的，用于测试备份的数据

```bash
winpty docker run -it -v E:\\path\\to\\jakgd:/jakgd -p 7474:7474 -p 7687:7687 --name jakgd_neo4j_test <neo4j-images> bash
```

加载数据

```bash
neo4j-admin load --from=/jakgd/neo4j/data/graph.db.dump --database=graph.db
```

此时启动neo4j访问即可得到备份的数据

### 将数据转换为json

见项目目录下/neo4j/dump_json.py

