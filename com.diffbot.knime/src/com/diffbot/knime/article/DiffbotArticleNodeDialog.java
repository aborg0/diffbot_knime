package com.diffbot.knime.article;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.FlowVariableModelButton;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnSelectionComboxBox;
import org.knime.core.node.workflow.FlowVariable.Type;
import org.knime.core.util.Pair;

/**
 * <code>NodeDialog</code> for the "DiffbotArticle" Node. Invokes Diffbot
 * article service and returns the extracted information as JSON values from the
 * specified (remote) URL(s). nThe optional input table can be a source of input
 * URLs.
 *
 * @author Diffbot Corp.
 */
class DiffbotArticleNodeDialog extends NodeDialogPane {
	private static final Pattern GUID_PATTERN = Pattern.compile("[0-9a-f]{32}");
	private final DiffbotArticleNodeSettings nodeSettings = new DiffbotArticleNodeSettings();
	private final JTextField token = new JTextField(32), url = new JTextField(
			55), outputColumn = new JTextField(
			DiffbotArticleNodeSettings.DEFAULT_OUTPUT_COLUMN, 22),
			userAgent = new JTextField(33), referrer = new JTextField(33),
			cookie = new JTextField(44), acceptLanguage = new JTextField(11),
			extraArguments = new JTextField(44);
	private final JSpinner version = new JSpinner(new SpinnerNumberModel(3, 2,
			11, 1)), timeout = new JSpinner(new SpinnerNumberModel(30_000, 0,
			Integer.MAX_VALUE, 500));
	private final ButtonGroup urlOrUrlColumnGroup = new ButtonGroup();
	private final JRadioButton useUrl = new JRadioButton("URL:"),
			useUrlColumn = new JRadioButton("URL column:");
	{
		urlOrUrlColumnGroup.add(useUrl);
		urlOrUrlColumnGroup.add(useUrlColumn);
	}
	private final JCheckBox useUserAgent = new JCheckBox("User agent"),
			useReferrer = new JCheckBox("Referrer"), useCookie = new JCheckBox(
					"Cookie"), useAcceptLanguage = new JCheckBox(
					"Accept language"), useExtraArguments = new JCheckBox(
					"Extra parameters"), paging = new JCheckBox("Paging");
	@SuppressWarnings("unchecked")
	private final ColumnSelectionComboxBox urlColumn = new ColumnSelectionComboxBox(
			(Border) null, StringValue.class);

	/**
	 * New pane for configuring the DiffbotArticle node.
	 */
	protected DiffbotArticleNodeDialog() {
		addTab("Settings", new JScrollPane(createSettings()));
		addTab("Fields", new JScrollPane(createFields()));
		addTab("Headers", new JScrollPane(createHeader()));
		listener(useUserAgent, userAgent, false);
		listener(useReferrer, referrer, false);
		listener(useCookie, cookie, false);
		listener(useAcceptLanguage, acceptLanguage, false);
		listener(useExtraArguments, extraArguments, false);
		final ChangeListener versionListener = new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				final boolean enable = !version
						.getValue()
						.equals(Integer
								.valueOf(DiffbotArticleNodeSettings.DEFAULT_VERSION));
				useExtraArguments.setEnabled(enable);
				extraArguments.setEnabled(false);
			}
		};
		versionListener.stateChanged(null);
		version.addChangeListener(versionListener);
		useUrl.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				url.setEnabled(useUrl.isSelected());
				urlColumn.setEnabled(!useUrl.isSelected());
			}
		});
		useUrl.setSelected(true);
	}

	/**
	 * @param use
	 * @param comp
	 * @param enabledByDefault
	 */
	private void listener(final JCheckBox use, final JComponent comp,
			final boolean enabledByDefault) {
		comp.setEnabled(enabledByDefault);
		use.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				comp.setEnabled(use.isSelected());
			}
		});
	}

	/**
	 * @return
	 */
	private JPanel createFields() {
		final JPanel ret = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.LINE_START;
		ret.add(nodeSettings.getFields().createControl(), gbc);
		gbc.gridy++;

		final JPanel extra = new JPanel(new FlowLayout(FlowLayout.LEADING));
		ret.add(extra, gbc);
		extra.add(useExtraArguments);
		extra.add(extraArguments);
		return ret;
	}

	/**
	 * @return
	 */
	private JPanel createHeader() {
		final JPanel ret = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 0;
		gbc.insets = new Insets(2, 4, 2, 4);
		gbc.anchor = GridBagConstraints.LINE_START;
		for (final Pair<JCheckBox, JTextField> controls : Arrays.asList(
				Pair.create(useUserAgent, userAgent),
				Pair.create(useReferrer, referrer),
				Pair.create(useCookie, cookie),
				Pair.create(useAcceptLanguage, acceptLanguage))) {
			gbc.gridx = 0;
			gbc.weightx = 0;
			ret.add(controls.getFirst(), gbc);
			gbc.gridx = 1;
			gbc.weightx = 1;
			ret.add(controls.getSecond(), gbc);
			gbc.gridy++;
		}
		gbc.weighty = 1;
		ret.add(new JPanel(), gbc);

		return ret;
	}

	/**
	 * @return
	 */
	private JPanel createSettings() {
		final JPanel ret = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(2, 4, 2, 4);
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		ret.add(new JLabel("Token: "), gbc);
		final JPanel tokenPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		final Border origBorder = token.getBorder();
		final Border errorBorder = new LineBorder(Color.RED, 2);
		token.getDocument().addDocumentListener(new DocumentListener() {

			private void checkText() {
				final String text = token.getText();
				token.setBorder(GUID_PATTERN.matcher(text).matches() ? origBorder
						: errorBorder);
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				checkText();
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				checkText();
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				checkText();
			}
		});
		token.setBorder(errorBorder);
		tokenPanel.add(token);
		final FlowVariableModel tokenFlowVariableModel = createFlowVariableModel(
				DiffbotArticleNodeSettings.TOKEN, Type.STRING);
		final FlowVariableModelButton tokenFlowVariableButton = new FlowVariableModelButton(
				tokenFlowVariableModel);
		tokenFlowVariableButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				token.setEnabled(!tokenFlowVariableModel
						.isVariableReplacementEnabled());
			}
		});
		tokenPanel.add(tokenFlowVariableButton);
		gbc.gridx = 1;
		ret.add(tokenPanel, gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		ret.add(useUrl, gbc);
		gbc.gridx = 1;
		final JPanel urlPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		urlPanel.add(url);
		final FlowVariableModel urlFlowVariableModel = createFlowVariableModel(
				DiffbotArticleNodeSettings.URL, Type.STRING);
		final FlowVariableModelButton urlFlowVariableButton = new FlowVariableModelButton(
				urlFlowVariableModel);
		urlFlowVariableButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				url.setEnabled(!urlFlowVariableModel
						.isVariableReplacementEnabled());
			}
		});
		urlPanel.add(urlFlowVariableButton);
		ret.add(urlPanel, gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		ret.add(useUrlColumn, gbc);
		gbc.gridx = 1;
		ret.add(urlColumn, gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		ret.add(new JLabel("Output column"), gbc);
		gbc.gridx = 1;
		ret.add(outputColumn, gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		ret.add(new JLabel("Diffbot version"), gbc);
		gbc.gridx = 1;
		ret.add(version, gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		ret.add(new JLabel("Timeout"), gbc);
		gbc.gridx = 1;
		ret.add(timeout, gbc);
		gbc.gridy++;
		gbc.gridx = 1;
		ret.add(paging, gbc);
		return ret;
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
		nodeSettings.setToken(token.getText());
		nodeSettings.setUrl(url.getText());
		nodeSettings.setUseUrlColumn(useUrlColumn.isSelected());
		nodeSettings.setUrlColumn(urlColumn.getSelectedColumn());
		nodeSettings.setOutputColumn(outputColumn.getText());
		nodeSettings.setVersion(((Integer) version.getValue()).intValue());
		nodeSettings.setPaging(paging.isSelected());
		nodeSettings.setTimeout(((Integer) timeout.getValue()).intValue());
		nodeSettings.setUseUserAgent(useUserAgent.isSelected());
		nodeSettings.setUserAgent(userAgent.getText());
		nodeSettings.setUseReferrer(useReferrer.isSelected());
		nodeSettings.setReferrer(referrer.getText());
		nodeSettings.setUseCookie(useCookie.isSelected());
		nodeSettings.setCookie(cookie.getText());
		nodeSettings.setUseAcceptLanguage(useAcceptLanguage.isSelected());
		nodeSettings.setAcceptLanguage(acceptLanguage.getText());
		nodeSettings.setUseExtraArguments(useExtraArguments.isSelected());
		nodeSettings.setExtraArguments(extraArguments.getText());
		nodeSettings.saveSettingsTo(settings);
	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObjectSpec[] specs) throws NotConfigurableException {
		nodeSettings.loadSettingsFrom(settings, specs);
		if (specs.length > 0 && specs[0] != null) {
			useUrlColumn.setEnabled(true);
			useUrlColumn.setSelected(true);
			urlColumn.update((DataTableSpec) specs[0],
					nodeSettings.getUrlColumn());
		} else {
			useUrlColumn.setEnabled(false);
			urlColumn.setEnabled(false);
			useUrl.setSelected(true);
		}
		token.setText(nodeSettings.getToken());
		url.setText(nodeSettings.getUrl());
		useUrlColumn.setSelected(nodeSettings.isUseUrlColumn());
		urlColumn.setSelectedColumn(nodeSettings.getUrlColumn());
		outputColumn.setText(nodeSettings.getOutputColumn());
		version.setValue(Integer.valueOf(nodeSettings.getVersion()));
		paging.setSelected(nodeSettings.isPaging());
		timeout.setValue(Integer.valueOf(nodeSettings.getTimeout()));
		useUserAgent.setSelected(nodeSettings.isUseUserAgent());
		userAgent.setText(nodeSettings.getUserAgent());
		useReferrer.setSelected(nodeSettings.isUseReferrer());
		referrer.setText(nodeSettings.getReferrer());
		useCookie.setSelected(nodeSettings.isUseCookie());
		cookie.setText(nodeSettings.getCookie());
		useAcceptLanguage.setSelected(nodeSettings.isUseAcceptLanguage());
		acceptLanguage.setText(nodeSettings.getAcceptLanguage());
		useExtraArguments.setSelected(nodeSettings.isUseExtraArguments());
		extraArguments.setText(nodeSettings.getExtraArguments());
	}

}
