package main;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by x on 10/8/2017.
 */
@Getter
public class HangmanConfiguration extends Configuration {
    private String heatlTest;

    private String memcacheServerIp;

    private int memcacheServerPort;

    private String dictonaryPath;
}
