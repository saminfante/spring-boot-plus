/*
 * Copyright 2019-2029 geekidea(https://github.com/geekidea)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.geekidea.springbootplus.core;

import com.alibaba.fastjson.JSON;
import io.geekidea.springbootplus.common.web.interceptor.PermissionInterceptor;
import io.geekidea.springbootplus.resource.web.interceptor.DownloadInterceptor;
import io.geekidea.springbootplus.resource.web.interceptor.ResourceInterceptor;
import io.geekidea.springbootplus.resource.web.interceptor.UploadInterceptor;
import io.geekidea.springbootplus.util.IniUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * WebMvc配置
 * @author geekidea
 * @date 2018-11-08
 */
@Slf4j
@Configuration
public class SpringBootPlusWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private SpringBootPlusProperties springBootPlusProperties;

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Autowired
    private ResourceInterceptor resourceInterceptor;

    @Autowired
    private UploadInterceptor uploadInterceptor;

    @Autowired
    private DownloadInterceptor downloadInterceptor;

    @PostConstruct
    public void init(){
        // 打印SpringBootPlusProperties配置信息
        log.debug("SpringBootPlusProperties：{}", JSON.toJSONString(springBootPlusProperties));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        SpringBootPlusInterceptorConfig interceptorConfig = springBootPlusProperties.getInterceptorConfig();

        // 上传拦截器
        if (interceptorConfig.getUploadConfig().isEnabled()){
            registry.addInterceptor(uploadInterceptor)
                    .addPathPatterns(interceptorConfig.getUploadConfig().getIncludePaths());
        }

        // 资源拦截器注册
        if (interceptorConfig.getResourceConfig().isEnabled()){
            registry.addInterceptor(resourceInterceptor)
                    .addPathPatterns(interceptorConfig.getResourceConfig().getIncludePaths());
        }

        // 下载拦截器注册
        if (interceptorConfig.getDownloadConfig().isEnabled()){
            registry.addInterceptor(downloadInterceptor)
                    .addPathPatterns(interceptorConfig.getDownloadConfig().getIncludePaths());
        }

        if (interceptorConfig.getPermissionConfig().isEnabled()){
            // 权限拦截器注册
            registry.addInterceptor(permissionInterceptor)
                    .addPathPatterns(interceptorConfig.getPermissionConfig().getIncludePaths())
                    .excludePathPatterns(interceptorConfig.getPermissionConfig().getExcludePaths());
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 设置项目静态资源访问
        String resourceHandlers = springBootPlusProperties.getResourceHandlers();
        if (StringUtils.isNotBlank(resourceHandlers)){
            Map<String,String> map = IniUtil.parseIni(resourceHandlers);
            for (Map.Entry<String,String> entry : map.entrySet()){
                String pathPatterns = entry.getKey();
                String resourceLocations = entry.getValue();
                registry.addResourceHandler(pathPatterns)
                        .addResourceLocations(resourceLocations);
            }
        }

        // 设置上传文件访问路径
        registry.addResourceHandler(springBootPlusProperties.getResourceAccessPatterns())
                .addResourceLocations("file:" + springBootPlusProperties.getUploadPath());
    }

}
