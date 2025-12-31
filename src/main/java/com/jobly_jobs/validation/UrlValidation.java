package com.jobly_jobs.validation;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@Log4j2
public class UrlValidation {

    public boolean isValid(String url) {
        url = formatUrl(url);
        return urlSyntaxValid(url) && hostExists(url) && websiteIsReachable(url);

    }

    private String formatUrl(String url) {
        if(doesNotStartWithHttp(url)){
            url = "https://" + url;
        }
        url = url.trim().toLowerCase();
        return url;
    }

    private boolean urlSyntaxValid(String url) {
        try{
            URI uri = new URI(url);
            return (!ObjectUtils.isEmpty(uri.getHost()) || !ObjectUtils.isEmpty(uri.getScheme()));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private boolean hostExists(String url){
        try {
            URL parsedUrl= new URL(url);
            InetAddress.getByName(parsedUrl.getHost());
            return true;
        } catch (MalformedURLException | UnknownHostException e) {
            return false;
        }

    }

    private boolean websiteIsReachable(String url){
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest head = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(2))
                    .build();

            HttpResponse<Void> headResponse =
                    client.send(head, HttpResponse.BodyHandlers.discarding());

            if (headResponse.statusCode() < 400) return true;


            HttpRequest get = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(2))
                    .build();
            HttpResponse<Void> getResponse =
                    client.send(get, HttpResponse.BodyHandlers.discarding());

            return getResponse.statusCode() < 400;

        } catch (Exception e) {
            return false;
        }
    }

    private boolean doesNotStartWithHttp (String url) {
        return !ObjectUtils.isEmpty(url) && !(url.startsWith("http://")  || url.startsWith("https://"));
    }





}
