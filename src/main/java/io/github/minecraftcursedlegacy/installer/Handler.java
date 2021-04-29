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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.LayoutManager;
import java.io.IOException;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import io.github.minecraftcursedlegacy.installer.util.ArgumentParser;
import io.github.minecraftcursedlegacy.installer.util.InstallerProgress;
import io.github.minecraftcursedlegacy.installer.util.MetaHandler;
import io.github.minecraftcursedlegacy.installer.util.MetaHandler.GameVersion;
import io.github.minecraftcursedlegacy.installer.util.Utils;

public abstract class Handler implements InstallerProgress {

	public JButton buttonInstall;

	public JComboBox<GameVersion> gameVersionComboBox;
	public JComboBox<GameVersion> loaderVersionComboBox;
	public JTextField installLocation;
	public JButton selectFolderButton;
	public JLabel statusLabel;
	private JPanel extraOptions;

	private JPanel pane;

	public abstract String name();

	public abstract void install();

	public abstract void installCli(ArgumentParser args) throws Exception;

	public abstract String cliHelp();

	//this isnt great, but works

	public abstract void setupSidedOptions(JPanel pane, InstallerGui installerGui);

	public JPanel makePanel(InstallerGui installerGui) {
		pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

		addRow(pane, jPanel -> {
			try {
				jPanel.add(new JLabel(new ImageIcon(Utils.getNestedImage("profile_icon.png"))));
			} catch (IOException e) {
				e.printStackTrace();
			}

			JLabel welcome = new JLabel("<html><div style='text-align: center;'>" + Utils.BUNDLE.getString("installer.welcome") + "</div></html>");
			welcome.setFont(new Font(welcome.getFont().getName(), Font.BOLD, 24));

			jPanel.add(welcome);
		}, new FlowLayout(FlowLayout.CENTER, 35, 5));

		addRow(pane, jPanel -> {
			jPanel.add(statusLabel = new JLabel());
			statusLabel.setText(Utils.BUNDLE.getString("prompt.loading.versions"));
		});

		// Initialise here so button can access it
		extraOptions = makeExtraOptionsPanel(installerGui);
		extraOptions.setVisible(false);

		addRow(pane, jPanel -> {
			JPanel centre = new JPanel(new FlowLayout());
			jPanel.setPreferredSize(new Dimension(525, 35));

			centre.add(gameVersionComboBox = new JComboBox<>());
			centre.add(buttonInstall = new JButton(Utils.BUNDLE.getString("prompt.install")));
			buttonInstall.addActionListener(e -> {
				buttonInstall.setEnabled(false);
				install();
			});

			jPanel.add(centre, BorderLayout.CENTER);

			try {
				// Choco and I pushed this exact same fix lmao
				JButton button = new JButton("", new ImageIcon(
						Utils.getNestedImage("options.png").getScaledInstance(30, 30, Image.SCALE_SMOOTH)
						));
				button.setOpaque(false);
				button.setContentAreaFilled(false);
				button.setBorderPainted(false);
				button.setPreferredSize(new java.awt.Dimension(30, 30));

				button.addActionListener(e -> {
					extraOptions.setVisible(!extraOptions.isVisible());
					installerGui.pack();
				});

				jPanel.add(button, BorderLayout.EAST);
				button.setAlignmentX(SwingConstants.RIGHT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, new BorderLayout());

		extraOptions.setPreferredSize(new Dimension(525, 100));
		pane.add(extraOptions);

		return pane;
	}

	public final void hideOptions() {
		extraOptions.setVisible(false);
	}

	private JPanel makeExtraOptionsPanel(InstallerGui installerGui) {
		JPanel extraOptions = new JPanel();

		extraOptions.setLayout(new BoxLayout(extraOptions, BoxLayout.PAGE_AXIS));

		Main.GAME_VERSION_META.onComplete(versions -> {
			updateGameVersions();
		});

		addRow(extraOptions, jPanel -> {
			jPanel.add(new JLabel(Utils.BUNDLE.getString("prompt.loader.version")));
			jPanel.add(loaderVersionComboBox = new JComboBox<>());
		});

		addRow(extraOptions, jPanel -> {
			jPanel.add(new JLabel(Utils.BUNDLE.getString("prompt.select.location")));
			jPanel.add(installLocation = new JTextField());
			jPanel.add(selectFolderButton = new JButton());

			selectFolderButton.setText("...");
			selectFolderButton.addActionListener(e -> InstallerGui.selectInstallLocation(() -> installLocation.getText(), s -> installLocation.setText(s)));
		});

		setupSidedOptions(extraOptions, installerGui);

		Main.LOADER_META.onComplete(versions -> {
			int stableIndex = -1;
			for (int i = 0; i < versions.size(); i++) {
				MetaHandler.GameVersion version = versions.get(i);
				loaderVersionComboBox.addItem(version);
				if(version.isStable()){
					stableIndex = i;
				}
			}
			//If no stable versions are found, default to the latest version
			if(stableIndex == -1){
				stableIndex = 0;
			}
			loaderVersionComboBox.setSelectedIndex(stableIndex);
			statusLabel.setText(Utils.BUNDLE.getString("prompt.ready.install"));
		});

		return extraOptions;
	}

	private void updateGameVersions() {
		gameVersionComboBox.removeAllItems();
		for (MetaHandler.GameVersion version : Main.GAME_VERSION_META.getVersions()) {
			gameVersionComboBox.addItem(version);
		}
		gameVersionComboBox.setSelectedIndex(0);
	}

	@Override
	public void updateProgress(String text) {
		statusLabel.setText(text);
		statusLabel.setForeground(UIManager.getColor("Label.foreground"));
	}

	private void appendException(StringBuilder errorMessage, String prefix, String name, Throwable e) {
		String prefixAppend = "  ";

		errorMessage.append(prefix).append(name).append(": ").append(e.getLocalizedMessage()).append('\n');
		for (StackTraceElement traceElement : e.getStackTrace()) {
			errorMessage.append(prefix).append("- ").append(traceElement).append('\n');
		}

		if (e.getCause() != null) {
			appendException(errorMessage, prefix + prefixAppend, Utils.BUNDLE.getString("prompt.exception.caused.by"), e.getCause());
		}

		for (Throwable ec : e.getSuppressed()) {
			appendException(errorMessage, prefix + prefixAppend, Utils.BUNDLE.getString("prompt.exception.suppressed"), ec);
		}
	}

	@Override
	public void error(Exception e) {
		StringBuilder errorMessage = new StringBuilder();
		appendException(errorMessage, "", Utils.BUNDLE.getString("prompt.exception"), e);

		System.err.println(errorMessage);

		statusLabel.setText(e.getLocalizedMessage());
		statusLabel.setForeground(Color.RED);

		JOptionPane.showMessageDialog(
				pane,
				errorMessage,
				Utils.BUNDLE.getString("prompt.exception.occurrence"),
				JOptionPane.ERROR_MESSAGE
				);
	}

	protected void addRow(Container parent, Consumer<JPanel> consumer) {
		this.addRow(parent, consumer, new FlowLayout());
	}

	private void addRow(Container parent, Consumer<JPanel> consumer, LayoutManager layout) {
		JPanel panel = new JPanel(layout);
		consumer.accept(panel);
		parent.add(panel);
	}

	protected String getGameVersion(ArgumentParser args) {
		return args.getOrDefault("mcversion", () -> {
			System.out.println("Using latest game version");
			try {
				Main.GAME_VERSION_META.load();
			} catch (IOException e) {
				throw new RuntimeException("Failed to load latest versions", e);
			}
			return Main.GAME_VERSION_META.getLatestVersion(args.has("snapshot")).getVersion();
		});
	}

	protected String getLoaderVersion(ArgumentParser args) {
		return args.getOrDefault("loader", () -> {
			System.out.println("Using latest loader version");
			try {
				Main.LOADER_META.load();
			} catch (IOException e) {
				throw new RuntimeException("Failed to load latest versions", e);
			}
			return Main.LOADER_META.getLatestVersion(false).getVersion();
		});
	}

}
