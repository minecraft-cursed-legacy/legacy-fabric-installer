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

package io.github.minecraftcursedlegacy.installer.util.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

public class GithubCommit {
	public String sha;

	@SerializedName("html_url")
	public String htmlUrl;

	public GithubCommitInternal commit;

	public static List<GithubCommit> getCommits() throws IOException {
		List<GithubCommit> versions;
		Gson gson = new Gson();

		URL githubURL = new URL("https://api.github.com/repos/minecraft-cursed-legacy/Cursed-fabric-loader/commits");
		InputStream githubStream = githubURL.openStream();

		versions = gson.fromJson(new InputStreamReader(githubStream), new TypeToken<List<GithubCommit>>() {}.getType());

		return versions;
	}
	
	public static class GithubCommitInternal {
		public GithubAuthor author;
	}

	public static class GithubAuthor {
		public String date;
	}
}
