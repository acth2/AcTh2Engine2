package fr.acth2.engine.utils.loader;

import java.io.InputStream;
import java.util.Scanner;

public class Loader {
    public static String loadResource(String fileName) throws Exception {
        String result;
        try (InputStream in = Class.forName(Loader.class.getName()).getResourceAsStream(fileName);
             Scanner scanner = new Scanner(in, java.nio.charset.StandardCharsets.UTF_8.name())) {
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }

}
