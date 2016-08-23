package io.remorekontrol

import helper.RemotingServletInitializer
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.ServletContextInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.servlet.ServletContext

@SpringBootApplication @Configuration
open class DemoAppApplication : RemotingServletInitializer() {

    @Bean open fun remotingBean() = RemotingInitializer()
}

open class RemotingInitializer : ServletContextInitializer {
    override fun onStartup(servletContext: ServletContext) {
        RemotingServletInitializer().onStartup(servletContext)
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(arrayOf(DemoAppApplication::class.java), args)
}


