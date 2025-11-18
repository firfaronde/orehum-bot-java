package firfaronde;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class Bundle {
    private static final Map<String, String> jobs = new HashMap<>();
    private static final Map<String, String> species = new HashMap<>();
    private static final Map<String, String> sexes = new HashMap<>();
    private static final Map<String, String> lifepaths = new HashMap<>();

    private static final HttpClient client = HttpClient.newHttpClient();

    public static void load() throws IOException, InterruptedException {
        System.out.println("Loading bundle");

        // Jobs
        jobs.putAll(loadFtl("https://raw.githubusercontent.com/BohdanNovikov0207/Orehum-Project/refs/heads/master/Resources/Locale/ru-RU/job/job-names.ftl"));
        jobs.put("Overall", "Общее");
        jobs.put("Admin", "Админ");
        System.out.println("Jobs localization loaded");

        // Species
        species.putAll(loadFtl("https://raw.githubusercontent.com/BohdanNovikov0207/Orehum-Project/refs/heads/master/Resources/Locale/ru-RU/species/species.ftl"));
        species.put("species-name-ipc", "КПБ");
        species.put("species-name-thaven", "Тавен");
        species.put("species-name-tajaran", "Таяр");
        species.put("species-name-felinid", "Фелинид");
        species.put("species-name-feroxi", "Ферокси");
        System.out.println("Species localization loaded");

        // Sexes
        sexes.put("male", "Мужской");
        sexes.put("female", "Женский");
        sexes.put("unsexed", "Бесполый");
        System.out.println("Sexes localization loaded");

        // Lifepaths
        lifepaths.putAll(loadFtl("https://raw.githubusercontent.com/BohdanNovikov0207/Orehum-Project/refs/heads/master/Resources/Locale/ru-RU/_Orehum/contractors/lifepath.ftl"));
        System.out.println("Lifepaths localization loaded");
    }

    private static Map<String, String> loadFtl(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to load FTL: " + response.statusCode());
        }

        return parseFtl(response.body());
    }

    private static Map<String, String> parseFtl(String text) {
        Map<String, String> map = new HashMap<>();
        for (String line : text.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                String value = parts[1].trim().replaceAll("^\"|\"$", "");
                map.put(key, value);
            }
        }
        return map;
    }

    public static String getJobName(String jobId) {
        return jobs.getOrDefault(jobId, jobId);
    }

    public static String getSpeciesName(String specId) {
        return species.getOrDefault("species-name-" + specId.toLowerCase(), specId);
    }

    public static String getSexName(String sex) {
        return sexes.getOrDefault(sex.toLowerCase(), sex);
    }

    public static String getLifepathName(String id) {
        return lifepaths.getOrDefault("lifepath_name_" + id.toLowerCase(), id);
    }
}
