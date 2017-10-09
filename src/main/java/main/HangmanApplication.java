package main;

import main.health.TemplateHealthCheck;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import main.resource.HangmanResource;

import java.io.IOException;

/**
 * Created by x on 10/8/2017.
 */
public class HangmanApplication extends Application<HangmanConfiguration> {
    public static void main(String[] args) throws Exception {
        new HangmanApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<HangmanConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets", "/", "index1.html"));
    }

    @Override
    public void run(HangmanConfiguration configuration,
                    Environment environment) throws IOException {
        final HangmanResource resource = new HangmanResource(configuration.getMemcacheServerIp(), configuration.getMemcacheServerPort(), configuration.getWords());
        environment.jersey().register(resource);
        environment.jersey().setUrlPattern("/api/*");

        final TemplateHealthCheck healthCheck =
                new TemplateHealthCheck(configuration.getHeatlTest());
        environment.healthChecks().register("template", healthCheck);
    }
}
