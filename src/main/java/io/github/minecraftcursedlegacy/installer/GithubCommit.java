/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.minecraftcursedlegacy.installer;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class GithubCommit {

    public String sha;

    @SerializedName("html_url")
    public String htmlUrl;

    public static GithubCommitArrayList getCommits() throws IOException {
        GithubCommitArrayList versions;
        Gson gson = new Gson();

        URL githubURL = new URL("https://api.github.com/repos/minecraft-cursed-legacy/Cursed-fabric-loader/commits");
        InputStream githubStream = githubURL.openStream();

        versions = gson.fromJson(new InputStreamReader(githubStream), GithubCommitArrayList.class);

        return versions;
    }
}
