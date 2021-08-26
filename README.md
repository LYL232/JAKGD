# jakgd

web

前端:

- vue
- eslint
- element-ui
- neo4jd3
- mavon-editor
- mathjax

后端:

- springboot security jwt restful
- nginx
- neo4j

部署:

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