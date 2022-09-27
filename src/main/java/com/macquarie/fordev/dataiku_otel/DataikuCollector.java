package com.macquarie.fordev.dataiku_otel;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataikuCollector {

    private static OkHttpClient httpClient = new OkHttpClient();

    public static void start(String baseUrl, String username, String password) {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            try {
                recordJobs(baseUrl, username, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private static void recordJobs(String baseUrl, String username, String password) throws Exception {
        List<String> projects = jsonRequest(baseUrl + "projects", username, password).read("$.*.projectKey");

        for (String projectKey: projects) {
            List<String> jobs = jsonRequest(baseUrl + "projects/" + projectKey + "/jobs", username, password).read("$.*.jobId");

            for (String jobId: jobs) {
                DocumentContext json = jsonRequest(baseUrl + "projects/" + projectKey + "/jobs/" + jobId, username, password);

                if (json.read("$.baseStatus.status").equals("DONE")) {
                    long duration = Long.parseLong(json.read("$.baseStatus.jobEndTime")) - Long.parseLong(json.read("$.baseStatus.jobStartTime"));

                    OpenTelemetry.meter().histogramBuilder("dataiku_" + projectKey.toLowerCase() + "_" + jobId)
                            .setUnit("ms")
                            .build()
                            .record(duration);
                }
            }
        }
    }

    private static OkHttpClient httpClient() {
        if (httpClient == null) {
            new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();
        }

        return httpClient;
    }

    private static DocumentContext jsonRequest(String url, String username, String password) throws Exception {
        Request request = new Request.Builder().url(url)
                .addHeader("Authorization", Credentials.basic(username, password))
                .addHeader("Content-Type", "application/json")
                .build();
        String response = httpClient().newCall(request).execute().body().string();
        return JsonPath.parse(response);
    }
}
