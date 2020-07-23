package cimc.raffles.mybatis.configurer;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import cimc.raffles.mybatis.interceptor.AuditingInterceptor;
import cimc.raffles.mybatis.interceptor.PageHandlerInterceptor;
import cimc.raffles.mybatis.interceptor.RequestParameterInterceptor;

@Configuration
public class MybatisPlusConfig 
{
	
	@Bean
	@Primary
	@ConditionalOnMissingBean
	public AuditingInterceptor auditingInterceptor()
	{
		return new AuditingInterceptor() ;
	}
	
//	@Bean
//	@Primary
//  @ConditionalOnMissingBean( value = { PaginationInterceptor.class })
//	public PaginationInterceptor paginationInterceptor() 
//	{
//		return new PaginationInterceptor();
//	}
	
	@Bean
    @ConditionalOnBean
	public WebMvcConfigurer paginationRequestDefault()
	{
		return new WebMvcConfigurer() {
			@Override
			public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers)
			{
				WebMvcConfigurer.super.addArgumentResolvers( resolvers) ;
				resolvers.add( new PageHandlerInterceptor<Object>()) ;
				resolvers.add( new RequestParameterInterceptor()) ;
			}
		};
	}
	
}
