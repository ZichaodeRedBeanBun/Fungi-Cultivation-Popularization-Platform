# 🍄 蘑菇科普智能服务平台 

> 基于 Spring Boot 3 的食用菌主题 Web 应用后端，集成蘑菇图像识别、AI 智能问答、科普内容管理与种植计划管理等功能。

本项目是蘑菇科普智能服务平台的后端服务，内置自研食用菌分类识别 AI 模型，提供从 **图像识别** → **毒性判断** → **AI 科普问答** → **种植管理** 的完整知识服务闭环。系统分为用户端和管理端两大模块，支持多角色权限控制。

---
## 核心功能

### 用户端

| 功能模块 | 说明 |
|---------|------|
| 🔬 AI 蘑菇识别 | 上传蘑菇图片，通过 ONNX 模型推理识别种类与毒性 |
| 💬 AI 智能问答 | 基于火山引擎大模型，支持文字 + 图片多模态对话 |
| 📖 科普内容浏览 | 科普文章阅读、轮播图展示、内容搜索 |
| ❤️ 收藏与点赞 | 科普内容收藏、评论互动 |
| 🌱 种植计划管理 | 创建种植计划、记录种植节点、查看种植数据与结果 |
| 👤 用户中心 | 注册登录（JWT 认证）、邮箱验证、个人信息管理 |

### 管理端

| 功能模块 | 说明 |
|---------|------|
| 🍄 蘑菇数据管理 | 蘑菇品种信息的增删改查 |
| 🐛 病虫害管理 | 病虫害信息维护与关联管理 |
| 📝 科普内容管理 | 科普文章发布、编辑、审核、轮播图管理 |
| ✍️ 科普作者管理 | 认证作者审核与管理 |
| 📊 数据统计 | 产量数据（全国/省级）统计展示 |
| 📨 反馈管理 | 用户反馈查看与处理 |
| 👥 用户管理 | 用户账号状态管理 |

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 开发语言 | Java 17 |
| 核心框架 | Spring Boot 3.3.7 |
| Web | Spring Web + Spring WebFlux |
| ORM 框架 | MyBatis-Plus 3.5.9 |
| 数据库 | MySQL + Druid 连接池 |
| 缓存 | Redis（Spring Data Redis） |
| 文件存储 | MinIO 对象存储 |
| AI 推理 | ONNX Runtime 1.17.1（本地模型推理） |
| 大模型对话 | 火山引擎 Ark API |
| 认证鉴权 | JWT（auth0 java-jwt 4.4.0）+ 拦截器 |
| 权限控制 | 基于角色的路径权限管理（RBAC） |
| 接口文档 | Knife4j 4.4.0 + SpringDoc OpenAPI |
| 邮箱服务 | Jakarta Mail（SMTP） |
| 响应式编程 | Reactor + RxJava |
| 构建工具 | Maven |

---

## 项目结构

```
mh-web-server/
├── src/main/java/cn/edu/seig/MhWeb/
│   ├── config/              # 配置类（跨域、Redis、MinIO、权限等）
│   ├── constant/            # 常量定义（JWT Claims、消息、路径）
│   ├── context/             # 上下文工具（BaseContext）
│   ├── controller/
│   │   ├── admin/           # 管理端接口
│   │   └── user/            # 用户端接口
│   ├── enumeration/         # 枚举类（角色、状态、类型等）
│   ├── handler/             # 全局异常处理 & MyBatis 枚举处理器
│   ├── interceptor/         # 登录拦截器（JWT 验证 + 角色鉴权）
│   ├── mapper/              # MyBatis Mapper 接口
│   ├── model/
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # 数据库实体类
│   │   └── vo/              # 视图返回对象
│   ├── result/              # 统一响应封装（Result、PageResult）
│   ├── service/             # 业务逻辑层
│   │   └── impl/            # Service 实现类
│   └── util/                # 工具类（JWT、随机码、类型转换等）
├── src/main/resources/
│   ├── mapper/              # MyBatis XML 映射文件
│   ├── application-example.yml  # 配置参考文件（需复制为 application.yml）
│   ├── mushroom_predict.py      # 蘑菇识别 Python 推理脚本
│   └── class_names.json         # 模型类别映射文件
└── pom.xml                  # Maven 依赖配置
```

---


默认启动端口为 **8081**。
部分页面截图

<img width="750" height="380" alt="image" src="https://github.com/user-attachments/assets/5b4f1461-c010-4447-9e3b-89f9469fd5b0" />
<img width="750" height="380" alt="image" src="https://github.com/user-attachments/assets/454c2a93-447b-459e-bc9b-508f61e093a7" />
<img width="750" height="380" alt="image" src="https://github.com/user-attachments/assets/1263905c-f5be-4cf8-9c58-5993833af120" />


