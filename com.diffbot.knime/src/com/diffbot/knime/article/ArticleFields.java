/*
 *
 */
package com.diffbot.knime.article;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.config.Config;

/**
 * The settings and UI for the fields section.
 *
 * @author Gabor Bakos
 */
class ArticleFields {
	/**
	 *
	 */
	private static final String TAGS = "tags";
	/**
	 *
	 */
	private static final String TEXT = "text";
	/**
	 *
	 */
	private static final String QUERY_STRING = "queryString";
	/**
	 *
	 */
	private static final String META = "meta";
	/**
	 *
	 */
	private static final String LINKS = "links";
	/**
	 *
	 */
	private static final String ADD = "add";
	/**
	 *
	 */
	private static final String USE_CHECK_BOX_VALUES = "useCheckBoxValues";
	/**
	 *
	 */
	private static final String VIDEOS = "videos";
	/**
	 *
	 */
	private static final String IMAGES = "images";
	/**
	 *
	 */
	private static final String NATURAL_HEIGHT = "naturalHeight";
	/**
	 *
	 */
	private static final String NATURAL_WIDTH = "naturalWidth";
	/**
	 *
	 */
	private static final String HEIGHT = "height";
	/**
	 *
	 */
	private static final String WIDTH = "width";
	private boolean useCheckBoxes, tags, images, imagesHeight, imagesWidth,
			videos, videosHeight, videosWidth, links, meta, queryString;
	private String fieldsText;

	private static final class ArticleFieldsPanel extends JPanel {
		private static final long serialVersionUID = -8466536934461652911L;
		private final ButtonGroup group = new ButtonGroup();
		private final JRadioButton useCheckBoxes = new JRadioButton(
				"Optional fields settings"), useTextField = new JRadioButton(
				"Custom optional fields settings");
		{
			group.add(useCheckBoxes);
			group.add(useTextField);
		}
		// TODO tooltips
		private final JCheckBox tags = new JCheckBox(TAGS),
				images = new JCheckBox("images"), imagesHeight = new JCheckBox(
						"height"), imagesWidth = new JCheckBox("width"),
				videos = new JCheckBox("videos"), videosHeight = new JCheckBox(
						"height"), videosWidth = new JCheckBox("width"),
				links = new JCheckBox("links"), meta = new JCheckBox("meta"),
				queryString = new JCheckBox("query string");
		private final JTextField text = new JTextField(33);
		private final JLabel asText = new JLabel();

		/**
		 * @param fields
		 */
		ArticleFieldsPanel(final ArticleFields fields) {
			super(new GridBagLayout());
			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 3;
			gbc.weighty = 1;
			gbc.anchor = GridBagConstraints.LINE_START;

			useCheckBoxes.setSelected(true);
			add(useCheckBoxes, gbc);
			gbc.gridy++;
			gbc.gridx++;
			add(tags, gbc);
			gbc.gridy++;
			add(images, gbc);
			gbc.gridy++;
			gbc.gridx++;
			add(imagesWidth, gbc);
			gbc.gridy++;
			add(imagesHeight, gbc);
			gbc.gridy++;
			gbc.gridx--;
			add(videos, gbc);
			gbc.gridy++;
			gbc.gridx++;
			add(videosWidth, gbc);
			gbc.gridy++;
			add(videosHeight, gbc);
			gbc.gridy++;
			gbc.gridx--;
			add(links, gbc);
			gbc.gridy++;
			add(meta, gbc);
			gbc.gridy++;
			add(queryString, gbc);
			gbc.gridy++;
			gbc.gridwidth = 7;
			gbc.gridx--;
			gbc.weightx = 1;
			add(asText, gbc);
			gbc.weightx = 0;
			gbc.gridwidth = 1;
			gbc.gridy++;
			add(useTextField, gbc);
			gbc.gridy++;
			gbc.gridx++;
			gbc.gridwidth = 4;
			add(text, gbc);

			images.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(final ChangeEvent e) {
					imagesHeight.setEnabled(images.isSelected());
					imagesWidth.setEnabled(images.isSelected());
				}
			});
			videos.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(final ChangeEvent e) {
					videosHeight.setEnabled(videos.isSelected());
					videosWidth.setEnabled(videos.isSelected());
				}
			});
			useCheckBoxes.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(final ChangeEvent e) {
					final boolean enable = useCheckBoxes.isSelected();
					tags.setEnabled(enable);
					images.setEnabled(enable);
					imagesHeight.setEnabled(enable);
					imagesWidth.setEnabled(enable);
					videos.setEnabled(enable);
					videosHeight.setEnabled(enable);
					videosWidth.setEnabled(enable);
					links.setEnabled(enable);
					meta.setEnabled(enable);
					queryString.setEnabled(enable);
					text.setEnabled(!enable);
				}
			});
			final ChangeListener updateAsTextLabel = new ChangeListener() {
				@Override
				public void stateChanged(final ChangeEvent e) {
					final ArticleFields articleFields = new ArticleFields();
					articleFields.loadFromPanel(ArticleFieldsPanel.this);
					asText.setText("Fields: " + articleFields.fieldSettings());
				}
			};
			for (final JCheckBox checkBox : new JCheckBox[] { images,
					imagesHeight, imagesWidth, videos, videosHeight,
					videosWidth, links, meta, queryString }) {
				checkBox.addChangeListener(updateAsTextLabel);
			}
		}
	}

	/**
	 *
	 */
	public ArticleFields() {
		super();
	}

	/**
	 * @param version
	 * @return
	 */
	List<? extends String> validateConsistency(final int version) {
		// Nothing to check
		return Collections.emptyList();
	}

	/**
	 * @return The {@code &fields} parameter for diffbot.
	 */
	String fieldSettings() {
		if (useCheckBoxes) {
			final StringBuilder sb = new StringBuilder();
			if (tags) {
				sb.append(TAGS).append(',');
			}
			if (images) {
				sb.append(IMAGES);
				if (imagesHeight || imagesWidth) {
					sb.append('(');
					if (imagesHeight) {
						sb.append(HEIGHT);
						if (imagesWidth) {
							sb.append(',').append(WIDTH);
						}
					} else {
						sb.append(WIDTH);
					}
					sb.append(')');
				}
				sb.append(",");
			}
			if (videos) {
				sb.append(VIDEOS);
				if (videosHeight || videosWidth) {
					sb.append('(');
					if (videosHeight) {
						sb.append(NATURAL_HEIGHT);
						if (videosWidth) {
							sb.append(',').append(NATURAL_WIDTH);
						}
					} else {
						sb.append(NATURAL_WIDTH);
					}
					sb.append(')');
				}
				sb.append(',');
			}
			if (links) {
				sb.append(LINKS).append(',');
			}
			if (meta) {
				sb.append(META).append(',');
			}
			if (queryString) {
				sb.append(QUERY_STRING).append(',');
			}
			if (sb.length() > 0) {
				sb.setLength(sb.length() - 1);
			}
			return sb.toString();
		}
		return fieldsText;
	}

	/**
	 * @return The control to set the optional fields values.
	 */
	ArticleFieldsPanel createControl() {
		return new ArticleFieldsPanel(this);
	}

	void loadFromPanel(final ArticleFieldsPanel panel) {
		useCheckBoxes = panel.useCheckBoxes.isSelected();
		tags = panel.tags.isSelected();
		images = panel.images.isSelected();
		imagesHeight = panel.imagesHeight.isSelected();
		imagesWidth = panel.imagesWidth.isSelected();
		videos = panel.videos.isSelected();
		videosHeight = panel.videosHeight.isSelected();
		videosWidth = panel.videosWidth.isSelected();
		links = panel.links.isSelected();
		meta = panel.meta.isSelected();
		queryString = panel.queryString.isSelected();
	}

	/**
	 * @param config
	 */
	void saveSettingsTo(final Config config) {
		config.addBoolean(USE_CHECK_BOX_VALUES, useCheckBoxes);
		config.addBoolean(TAGS, tags);
		final Config addImages = config.addConfig(IMAGES);
		addImages.addBoolean(ADD, images);
		addImages.addBoolean(WIDTH, imagesWidth);
		addImages.addBoolean(HEIGHT, imagesHeight);
		final Config addVideos = config.addConfig(VIDEOS);
		addVideos.addBoolean(ADD, videos);
		addVideos.addBoolean(WIDTH, videosWidth);
		addVideos.addBoolean(HEIGHT, videosHeight);
		config.addBoolean(LINKS, links);
		config.addBoolean(META, meta);
		config.addBoolean(QUERY_STRING, queryString);
		config.addString(TEXT, fieldsText);
	}

	/**
	 * @param config
	 * @throws InvalidSettingsException
	 */
	void loadSettingsFrom(final Config config) throws InvalidSettingsException {
		useCheckBoxes = config.getBoolean(USE_CHECK_BOX_VALUES);
		tags = config.getBoolean(TAGS);
		images = config.getConfig(IMAGES).getBoolean(ADD);
		imagesWidth = config.getConfig(IMAGES).getBoolean(WIDTH);
		imagesHeight = config.getConfig(IMAGES).getBoolean(HEIGHT);
		videos = config.getConfig(VIDEOS).getBoolean(ADD);
		videosWidth = config.getConfig(VIDEOS).getBoolean(WIDTH);
		videosHeight = config.getConfig(VIDEOS).getBoolean(HEIGHT);
		links = config.getBoolean(LINKS);
		meta = config.getBoolean(META);
		queryString = config.getBoolean(QUERY_STRING);
		fieldsText = config.getString(TEXT);
	}
}
