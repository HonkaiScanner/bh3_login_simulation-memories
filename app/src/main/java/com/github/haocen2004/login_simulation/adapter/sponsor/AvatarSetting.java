package com.github.haocen2004.login_simulation.adapter.sponsor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AvatarSetting {
    public @NonNull
    final String name;
    public @NonNull
    final String desc;
    public String avatarUrl;
    public @Nullable
    String url;

    public AvatarSetting(String avatarUrl, @NonNull String name, @NonNull String desc) {
        this(avatarUrl, name, desc, null);
    }

    public AvatarSetting(
            String avatarUrl,
            @NonNull String name,
            @NonNull String desc,
            @Nullable String url) {

        this.avatarUrl = avatarUrl;
        this.name = name;
        this.desc = desc;
        this.url = url;
    }
}
