package ru.search.github.githubsearch.Classes;

import java.util.Date;
import java.util.Map;

import ru.search.github.githubsearch.Classes.FullUserInfo;

/**
 * Created by Илья on 07.05.2017.
 */

public class Repository {
    public String name;
    public String description;
    public String language;
    public int stars;
    public int forks;
    public Date updated;
    public String repoUrl;

    public Repository(Map<String, String> reposMap) { //Извлекаем данные из Map
        name = reposMap.get("name");
        description = reposMap.get("description");
        language = reposMap.get("language");
        stars = (int)Double.parseDouble(String.valueOf(reposMap.get("stargazers_count")));
        forks = (int)Double.parseDouble(String.valueOf(reposMap.get("forks_count")));
        updated = FullUserInfo.makeDateFromString(reposMap.get("pushed_at"));
        repoUrl = reposMap.get("html_url");
    }
}
