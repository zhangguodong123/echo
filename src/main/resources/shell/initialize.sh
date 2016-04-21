#!/bin/sh
##################################
#@author:zhangguodong
#@email:zhangguodong_mail@163.com
#@初始化并且部署投放服务器环境
##################################
  
   #set the dad file Variables 
   ftp="/ftp"                                      #拷贝复制文件路径
   data="/data"                                    #数据区
   root="/wwwroot"                                 #投放路径
   logs="/wwwlogs"                                 #投放服务器日志
   resources="/resources"                          #资源区
   dad="/echo.adsense.cig.com.cn"                  #投放路径
   usr_local="/usr/local"                          #liunx安装文件路径
   haproxy="/haproxy"                              #haproxy路径
   java="/java"                                    #jdk路径
   json="/json_data"                               #json_data临时灾备文件 1、惠买车 2、二手车 3、易鑫资本
   resource_tar="resources.tar.gz"                 #资源库
   models_tar="models.tar.gz"                      #竞品
   echo_tar="echo-1.3.8-jar-with-dependencies.jar" #DAD投放jar文件
   startup1="startup-1.sh"                         #DAD投放启动文件1
   startup2="startup-2.sh"                      
   jdk_tar="jdk-8u65-linux-x64.gz"                 #JDK_jar
   haproxy_tar="haproxy-1.5.3.tar.gz"              #haproxy安装文件
   haproxy_unzip="haproxy-1.5.3/"                  #haproxy解压出来的路径
   haproxy_cfg="haproxy.cfg"                       #haproxy配置文件
   haproxy_errorfile="errorfile.http"              #haproxy容灾接口文件
   huimaiche_data="json.txt"
   yixin_data="yx_json.txt"
   usedcar_data="used_car.txt"

   #set the Hosts and java profile
   in_url="a.b.c"                                                                              #@in_ip hosts ip
   in_ip="192.168.1.163"                                                                       #diamond 地址变更之后也需要修改
   jdk_home='export JAVA_HOME=/usr/local/java/jdk1.8.0_65'                                     #jdk升级之后必须修改该变量
   jdk_classpath='export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar'           #要保持原样输出必须加单引号
   jdk_path='export PATH=$JAVA_HOME/bin:$VERTX_HOME/bin:$PATH'
   
   #####@描述:添加diamond  >>>hosts################
   updateHost(){
       # read 
       inner_host=`cat /etc/hosts | grep ${in_url} | awk '{print $1}'` #查找hosts里的字符、如果找到 in_url 则打印其IP，赋值给inner_host
       if [ ${inner_host} = ${in_ip} ];then
           echo "${inner_host}  ${in_url} already ok!"
       else
           #替换
           sed -i "s#${inner_host}#${in_ip}#g" /etc/hosts
           if [ $? = 0 ];then
               echo "change ${inner_host} to ${in_ip} ok"
           else
               inner_ip_map="${in_ip} ${in_url}"
               echo ${inner_ip_map} >> /etc/hosts
               if [ $? = 0 ]; then
                   echo "${inner_ip_map} to hosts success host is `cat /etc/hosts`"
               fi
           fi
       fi
    }

    #######[配置jdk_classpath]##############
    updateProfile(){
        inner_profile_java=`cat /etc/profile | grep "${jdk_home}" | awk '{print $2}'`
        #echo "${jdk_home}"
        #echo $inner_profile_java
        if [ "x${inner_profile_java}" != "x" ];then
            echo "jdk environment already exist！"
        else
            echo ${jdk_home} >> /etc/profile
            echo ${jdk_classpath} >> /etc/profile
            echo ${jdk_path} >> /etc/profile
            source /etc/profile
        fi
    }
   
   #######################
   #@描述:初始化环境
   #######################
   createEv(){
       if [ -d $data ];then
           echo "Directory:$data already exists!"
           cd $data
           rm -rf *   #删除data下的所有文件
           echo "目录:$data 下文件已清理干净......"
       else
           mkdir $data
           echo "Directory:$data already created!"
       fi
   
       if [ -d $data$resources ];then
           echo "Directory:$data$resources already exists!"
       else
           mkdir $data$resources
           echo "Directory:$data$resources already created!"
       fi
 
       if [ -d $data$root ];then
           echo "Directory:$data$root already exists!"
       else
           mkdir $data$root
           echo "Directory:$data$root already created!"
       fi

       if [ -d $data$logs ];then
           echo "Directory:$data$logs already exists!"
       else
           mkdir $data$logs
           echo "Directory:$data$logs already created!"
       fi

       if [ -d $data$logs$dad ];then
           echo "Directory:$data$logs$dad already exists!"
       else
           mkdir $data$logs$dad
           echo "Directory:$data$logs$dad already created!"
       fi

       if [ -d $data$root$dad ];then
           echo "Directory:$data$root$dad already exists!"
       else
           mkdir $data$root$dad
           echo "Directory:$data$root$dad already created!"
       fi

       if [ -d $usr_local ];then
           echo "Directory:$usr_local already exists!"
           #1）删除haproxy目录
           if [ -d $usr_local$haproxy ];then
               cd $usr_local
               rm -rf $haproxy
               echo "目录:$usr_local$haproxy 已清理......"
           fi
           #2）删除jdk目录
           if [ -d $usr_local$java ];then
               cd $usr_local
               rm -rf $java
               echo "目录:$usr_local$haproxy 已清理......"
           fi
       else
           mkdir $usr_local
           echo "Directory:$usr_local already created!"
       fi

       if [ -d $usr_local$haproxy ];then
           echo "Directory:$usr_local$haproxy already exists!"
       else
           mkdir $usr_local$haproxy
           echo "Directory:$usr_local$haproxy already created!"
       fi

       if [ -d $usr_local$java ];then
           echo "Directory:$usr_local$java already exists!"
       else
           mkdir $usr_local$java
           echo "Directory:$usr_local$java already created!"
       fi
   
       if [ -d $data$root$dad$json ];then
           echo "Diectory:$data$root$dad$json already exists!"
       else
           mkdir $data$root$dad$json
           echo "Directory:$data$root$dad$json already created!"
       fi

       ####################
       #【copy拷贝部署文件】
       ###################
       cp -rf $ftp/$resource_tar $data/
       cp -rf $ftp/$models_tar $data$root$dad/
       cp -rf $ftp/$echo_tar  $data$root$dad/
       cp -rf $ftp/$startup1 $data$root$dad/
       cp -rf $ftp/$startup2 $data$root$dad/
       cp -rf $ftp/$jdk_tar $usr_local$java/
       cp -rf $ftp/$haproxy_tar $usr_local$haproxy/
       cp -rf $ftp/$haproxy_cfg $usr_local$haproxy/
       cp -rf $ftp/$haproxy_errorfile $usr_local$haproxy/
       cp -rf $ftp/$huimaiche_data $data$root$dad$json/
       cp -rf $ftp/$yixin_data $data$root$dad$json/
       cp -rf $ftp/$usedcar_data $data$root$dad$json/         

       cd $data/
       tar xvf $resource_tar
       rm -rf $resource_tar
       cd $data$root$dad/
       tar xvf $models_tar
       rm -rf $models_tar
       cd $usr_local$java/
       tar -zxvf $jdk_tar
       rm -rf $jdk_tar
       cd $usr_local$haproxy/
       tar -zxvf $haproxy_tar
       cd $usr_local$haproxy/$haproxy_unzip
       make TARGET=linux26 PREFIX=$usr_local$haproxy
       make install PREFIX=$usr_local$haproxy
       cd $usr_local$haproxy/
       rm -rf $haproxy_tar
       echo '>>>>>>>>>>>>>>>投放部署文件已初始化完毕>>>>>>>>>>>>>>>>>>>>'
     }
     main(){
         updateHost
         createEv
         updateProfile
     }
     main
