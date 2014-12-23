package com.diffbot.knime.analyze;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
class DiffbotAnalyzeNodeDialog extends NodeDialogPane {
	private static final Pattern GUID_PATTERN = Pattern.compile("[0-9a-f]{32}");
	private final DiffbotAnalyzeNodeSettings nodeSettings = new DiffbotAnalyzeNodeSettings();
	private final JTextField token = new JTextField(32), url = new JTextField(
			55), outputColumn = new JTextField(
			DiffbotAnalyzeNodeSettings.DEFAULT_OUTPUT_COLUMN, 22),
			fields = new JTextField(44), userAgent = new JTextField(33),
			referrer = new JTextField(33), cookie = new JTextField(44),
			acceptLanguage = new JTextField(11);
	private final JSpinner version = new JSpinner(new SpinnerNumberModel(3, 2,
			11, 1)), timeout = new JSpinner(new SpinnerNumberModel(30_000, 0,
			Integer.MAX_VALUE, 500));
	private final JCheckBox useUserAgent = new JCheckBox("User agent"),
			useReferrer = new JCheckBox("Referrer"), useCookie = new JCheckBox(
					"Cookie"), useAcceptLanguage = new JCheckBox(
					"Accept language");
	@SuppressWarnings("unchecked")
	private final ColumnSelectionComboxBox urlColumn = new ColumnSelectionComboxBox(
			(Border) null, StringValue.class);
	private final JComboBox<String> mode = new JComboBox<>(new String[] {
			"article", "product", "image", "discussion", "video" });
	private Border origBorder;
	private Border errorBorder;
	private FlowVariableModelButton urlFlowVariableButton;

	/**
	 * New pane for configuring the DiffbotArticle node.
	 */
	protected DiffbotAnalyzeNodeDialog() {
		addTab("Settings", new JScrollPane(createSettings()));
		addTab("Headers", new JScrollPane(createHeader()));
		listener(useUserAgent, userAgent, false);
		listener(useReferrer, referrer, false);
		listener(useCookie, cookie, false);
		listener(useAcceptLanguage, acceptLanguage, false);
		mode.setEditable(true);
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
		origBorder = token.getBorder();
		errorBorder = new LineBorder(Color.RED, 2);
		token.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(final DocumentEvent e) {
				checkTokenText();
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				checkTokenText();
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				checkTokenText();
			}
		});
		token.setBorder(errorBorder);
		tokenPanel.add(token);
		final FlowVariableModel tokenFlowVariableModel = createFlowVariableModel(
				DiffbotAnalyzeNodeSettings.TOKEN, Type.STRING);
		final FlowVariableModelButton tokenFlowVariableButton = new FlowVariableModelButton(
				tokenFlowVariableModel);
		tokenFlowVariableButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				token.setEnabled(!tokenFlowVariableModel
						.isVariableReplacementEnabled());
				checkTokenText();
			}
		});
		tokenPanel.add(tokenFlowVariableButton);
		gbc.gridx = 1;
		ret.add(tokenPanel, gbc);
		gbc.gridy++;

		gbc.gridx = 0;
		ret.add(new JLabel("URL:"), gbc);
		gbc.gridx = 1;
		final JPanel urlPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		urlPanel.add(url);
		final FlowVariableModel urlFlowVariableModel = createFlowVariableModel(
				DiffbotAnalyzeNodeSettings.URL, Type.STRING);
		urlFlowVariableButton = new FlowVariableModelButton(
				urlFlowVariableModel);
		urlFlowVariableButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				url.setEnabled(!urlFlowVariableModel
						.isVariableReplacementEnabled());
			}
		});
		urlPanel.add(urlFlowVariableButton);
		urlPanel.add(urlColumn);
		ret.add(urlPanel, gbc);
		gbc.gridy++;

		gbc.gridx = 0;
		ret.add(new JLabel("Output column"), gbc);
		gbc.gridx = 1;
		ret.add(outputColumn, gbc);
		gbc.gridy++;

		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.NONE;
		ret.add(new JLabel("Mode"), gbc);
		gbc.gridx = 1;
		ret.add(mode, gbc);
		gbc.gridy++;

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		ret.add(new JLabel("Fields"), gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.LINE_START;
		ret.add(fields, gbc);
		gbc.gridy++;

		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		ret.add(new JLabel("Diffbot version"), gbc);
		gbc.gridx = 1;
		ret.add(version, gbc);
		gbc.gridy++;

		gbc.gridx = 0;
		ret.add(new JLabel("Timeout"), gbc);
		gbc.gridx = 1;
		ret.add(timeout, gbc);
		return ret;
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
		nodeSettings.setToken(token.getText());
		nodeSettings.setUrl(url.getText());
		nodeSettings.setUrlColumn(urlColumn.getSelectedColumn());
		nodeSettings.setOutputColumn(outputColumn.getText());
		nodeSettings.setMode((String) mode.getSelectedItem());
		nodeSettings.setVersion(((Integer) version.getValue()).intValue());
		nodeSettings.setTimeout(((Integer) timeout.getValue()).intValue());
		nodeSettings.setUseUserAgent(useUserAgent.isSelected());
		nodeSettings.setUserAgent(userAgent.getText());
		nodeSettings.setUseReferrer(useReferrer.isSelected());
		nodeSettings.setReferrer(referrer.getText());
		nodeSettings.setUseCookie(useCookie.isSelected());
		nodeSettings.setCookie(cookie.getText());
		nodeSettings.setUseAcceptLanguage(useAcceptLanguage.isSelected());
		nodeSettings.setAcceptLanguage(acceptLanguage.getText());
		nodeSettings.saveSettingsTo(settings);
	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObjectSpec[] specs) throws NotConfigurableException {
		nodeSettings.loadSettingsFrom(settings, specs);
		final boolean columnInput = specs.length > 0 && specs[0] != null;
		if (columnInput) {
			urlColumn.update((DataTableSpec) specs[0],
					nodeSettings.getUrlColumn());
		}
		url.setVisible(!columnInput);
		urlFlowVariableButton.setVisible(!columnInput);
		urlColumn.setVisible(columnInput);
		token.setText(nodeSettings.getToken());
		url.setText(nodeSettings.getUrl());
		urlColumn.setSelectedColumn(nodeSettings.getUrlColumn());
		outputColumn.setText(nodeSettings.getOutputColumn());
		mode.setSelectedItem(nodeSettings.getMode());
		version.setValue(Integer.valueOf(nodeSettings.getVersion()));
		timeout.setValue(Integer.valueOf(nodeSettings.getTimeout()));
		useUserAgent.setSelected(nodeSettings.isUseUserAgent());
		userAgent.setText(nodeSettings.getUserAgent());
		useReferrer.setSelected(nodeSettings.isUseReferrer());
		referrer.setText(nodeSettings.getReferrer());
		useCookie.setSelected(nodeSettings.isUseCookie());
		cookie.setText(nodeSettings.getCookie());
		useAcceptLanguage.setSelected(nodeSettings.isUseAcceptLanguage());
		acceptLanguage.setText(nodeSettings.getAcceptLanguage());
	}

	private void checkTokenText() {
		final String text = token.getText();
		token.setBorder(GUID_PATTERN.matcher(text).matches()
				|| !token.isEnabled() ? origBorder : errorBorder);
	}
}
