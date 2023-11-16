# JenkinsPluginAppToServer
   一款针对Jenkins打包Android/iOS安装并上传到服务器的插件。<br>

[![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-brightgreen.svg)](https://github.com/peihua8858)
[![Star](https://img.shields.io/github/stars/peihua8858/JenkinsPluginAppToServer.svg)](https://github.com/peihua8858/JenkinsPluginAppToServer)


## 目录
-[最新版本](https://github.com/peihua8858/JenkinsPluginAppToServer/releases/)<br>
-[如何使用](#进阶使用)<br>
-[如何提Issues](https://github.com/peihua8858/JenkinsPluginAppToServer/wiki/%E5%A6%82%E4%BD%95%E6%8F%90Issues%3F)<br>
-[License](#License)<br>

## 进阶使用

简单用例如下所示:

### 1、打包编译

直接编译：mvn package -DskipTests
清除历史编译并打包：mvn clean package -DskipTests
### 2、Jenkins插件安装
安装界面路径： 系统管理(Manage Jenkins) >插件管理(Manage Plugins) >高级(Advanced) >上传插件(Upload Plugin),之后选中下载好的插件，点击上传(upload),重启Jenkins即可。
        安装插件如图：
|   插件安装示例      | 
|:----------------------:|
| ![](images/image1.jpg) |

### 3、插件系统级配置
   系统级配置位于Jenkins下 系统管理(Manage Jenkins)>系统设置(Configure System)>App上传服务设置:

  如下图所示：
|   Android配置示例      | 
|:----------------------:|
| ![](images/image2.jpg) |

|   iOS配置示例          | 
|:----------------------:|
| ![](images/image3.jpg) |
     ①上传服务器地址：配置日志系统IP地址加端口即可，此项位防止日志系统IP地址变动而设
     ②下载服务器地址：配置当前Jenkins打包系统IP地址加端口即可，如果不配置，则默认取日志服务器IP地址，IOS因打包服务器不同，此项必须配置
     ③Jenkins 归档成品job目录：输入Jenkins归档job目录。默认Android为/jenkins/job/,iOS 为/job/。
          如何取得job目录？
          进入项目点击左侧构成成功的编译号（如：#618）点击#618进入，可以看到“构建产生文件(Build Artifacts)”下面的安装包，
          右键点击安装包，在弹窗中选中“复制链接地址”，之后截取路径项目名之后，端口号之后的字符串即可。
          如地址为http://ip:port/jenkins/job/Zaful-Test/618/**/*.apk，则取“/jenkins/job/”。
          如地址为http://ip:port/job/Zaful/283/**/*.ipa，则取“/job/”。
### 4、插件项目级配置
插件项目级配置位于Jenkins下 项目左侧配置(Configure) ,在增加构建后操作步骤选项中选择插件APP-Upload
如下图所示：
|   Android配置示例      | 
|:----------------------:|
| ![](images/image4.jpg) |

|   iOS配置示例      | 
|:----------------------:|
| ![](images/image5.jpg) |
     ①编译类型：优先取设置内容，对于IOS 可以不填，插件将默认读取plist文件中的ProvisionedDevices（Adhoc）和ProvisionsAllDevices（Inhouse），对于Android，如果不设置，将取
           包路径下的父一级目录作为编译类型（即如果包路径为app/build/outputs/apk/google/develop/Zaful.apk，则编译类型为develop）。
     ②IPA/APK文件(可选)：要上传到服务器的文件的路径。 可以使用“module/dist/**/*.ipa”之类的通配符。 如果未指定，则默认模式为“**/*.ipa,**/*.apk”，即将递归搜索其找到的任何IPA和APK。 有关确切格式，请参见Ant文件集的@includes。 基本目录是工作空间。
     ③排除APK/IPA文件名：文件的路径不会上传到服务器.例如：**/*unaligned*.apk,**/*unsigned*.apk。 如果你没有设置它，插件将上传你项目中找到的所有apk。
     ④App安装包文件上传：是否将安装包文件上传到服务器。如果勾选，则上传安装包到服务器。否则，如果本插件在Archive the artifacts插件之后，且能获取到归档成品，则不做上传，如果未获取到归档成品，则上传安装包到服务器。
     ⑤多语言文案上传：此功能目前只支持Android多语言文案上传，iOS暂不支持，如果勾选，则扫描 “多语言文案目录筛选” 框所示目录下的多语言文案，并上传到服务器，否则不做上传操作。
     ⑥多语言文案目录筛选：如果勾选 “多语言文案上传” ,则插件将扫描该目录下的多语言文案。
     ⑦Plist远程地址：仅iOS需要，为iOS安装包plist存放地址，因插件通过git操作，故仅支持提交到Git相关的服务器，如果GitHub 、gitlab等。
     ⑧Plist远程地址凭证：仅iOS需要，填写plist远程地址用户名和密码即可。
     ⑨Jenkins凭证添加入口按钮

## License
```sh
Copyright 2023 peihua

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
