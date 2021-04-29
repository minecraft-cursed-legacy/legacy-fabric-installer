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

package io.github.minecraftcursedlegacy.installer.server;

import javax.swing.JPanel;

import io.github.minecraftcursedlegacy.installer.Handler;
import io.github.minecraftcursedlegacy.installer.InstallerGui;
import io.github.minecraftcursedlegacy.installer.util.ArgumentParser;
import io.github.minecraftcursedlegacy.installer.util.InstallerProgress;
import io.github.minecraftcursedlegacy.installer.util.LauncherMeta;
import io.github.minecraftcursedlegacy.installer.util.Utils;
import io.github.minecraftcursedlegacy.installer.util.MetaHandler.GameVersion;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

public class ServerHandler extends Handler {

	@Override
	public String name() {
		return "Server";
	}

	@Override
	public void install() {
		String gameVersion = ((GameVersion) gameVersionComboBox.getSelectedItem()).getVersion();
		String loaderVersion = ((GameVersion) loaderVersionComboBox.getSelectedItem()).getVersion();
		new Thread(() -> {
			try {
				ServerInstaller.install(new File(installLocation.getText()), loaderVersion, gameVersion, this);
				ServerPostInstallDialog.show(this);
			} catch (Exception e) {
				error(e);
			}
			buttonInstall.setEnabled(true);
		}).start();
	}

	@Override
	public void installCli(ArgumentParser args) throws Exception {
		File file = new File(args.getOrDefault("dir", () -> "."));
		if (!file.exists()) {
			throw new FileNotFoundException("Server directory not found at " + file.getAbsolutePath());
		}
		String loaderVersion = getLoaderVersion(args);
		String gameVersion = getGameVersion(args);
		ServerInstaller.install(file.getAbsoluteFile(), loaderVersion, gameVersion, InstallerProgress.CONSOLE);

		if(args.has("downloadMinecraft")){
			File serverJar = new File(file, "server.jar");
			InstallerProgress.CONSOLE.updateProgress(Utils.BUNDLE.getString("progress.download.minecraft"));
			Utils.downloadFile(new URL(LauncherMeta.getLauncherMeta().getVersion(gameVersion).getVersionMeta().downloads.get("server").url), serverJar);
			InstallerProgress.CONSOLE.updateProgress(Utils.BUNDLE.getString("progress.done"));
		}
	}

	@Override
	public String cliHelp() {
		return "-dir <install dir, default current dir> -mcversion <minecraft version, default latest> -loader <loader version, default latest> -downloadMinecraft";
	}

	@Override
	public void setupSidedOptions(JPanel pane, InstallerGui installerGui) {
		installLocation.setText(new File("").getAbsolutePath());
	}

}
