通用统计分析库集成注意事项
---

#### 共同的注意事项
1. AndroidManifest.xml中权限的声明、application标记下组件声明都要复制到主项目的AndroidManifest.xml中。
1. proguard-project.txt中的混淆配置要复制到主项目的proguard-project.txt
1. res/values/analytics_config.xml直接复制到主项目的res/values目录下，再重写里面的参数配置。主项目的values会覆盖引用项目的values中的同名值。
1. res/xml/ga_app_tracker.xml直接复制到主项目的res/values目录下，再重写里面的参数配置。
