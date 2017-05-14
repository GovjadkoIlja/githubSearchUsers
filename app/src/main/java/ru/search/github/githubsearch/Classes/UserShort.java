package ru.search.github.githubsearch.Classes;

/**
 * Created by Илья on 04.05.2017.
 */

public class UserShort {
    public String login;
    public String avatarUrl;

    public UserShort(String _login, String _avatarUrl) {
        login = _login;
        avatarUrl = _avatarUrl;
    }
}