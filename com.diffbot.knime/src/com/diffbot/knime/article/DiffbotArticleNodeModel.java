package com.diffbot.knime.article;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.json.JSONCell;
import org.knime.core.data.json.JSONCellFactory;
import org.knime.core.data.json.JSONValue;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.diffbot.clients.DiffbotClient;
import com.diffbot.clients.DiffbotClient.ResponseType;

/**
 * This is the model implementation of DiffbotArticle. Invokes Diffbot article
 * service and returns the extracted information as JSON values from the
 * specified (remote) URL(s). nThe optional input table can be a source of input
 * URLs.
 *
 * @author Diffbot Corp.
 */
class DiffbotArticleNodeModel extends NodeModel {
	private DiffbotArticleNodeSettings settings = new DiffbotArticleNodeSettings();

	/**
	 * Constructor for the node model.
	 */
	protected DiffbotArticleNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE_OPTIONAL },
				new PortType[] { BufferedDataTable.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {
		BufferedDataTable ret;
		if (inData.length > 0 && inData[0] != null) {
			final BufferedDataTable data = (BufferedDataTable) inData[0];
			ret = exec.createColumnRearrangeTable(data,
					createColumnRearranger(data.getSpec()), exec);
		} else {
			final BufferedDataContainer container = exec
					.createDataContainer(singleColumnSpec());
			container.addRowToTable(new DefaultRow(RowKey.createRowKey(0),
					createCell(settings.getUrl(), settings.settingsAsMap())));
			container.close();
			ret = container.getTable();
		}
		return new BufferedDataTable[] { ret };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// No internal state
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		if (settings.isUseUrlColumn() && inSpecs.length > 0
				&& inSpecs[0] != null) {
			final ColumnRearranger rearranger = createColumnRearranger((DataTableSpec) inSpecs[0]);
			return new DataTableSpec[] { rearranger.createSpec() };
		}
		final DataTableSpec spec = singleColumnSpec();
		return new DataTableSpec[] { spec };
	}

	/**
	 * @return
	 */
	private DataTableSpec singleColumnSpec() {
		final DataTableSpecCreator ret = new DataTableSpecCreator();
		ret.addColumns(new DataColumnSpecCreator(settings.getOutputColumn(),
				JSONCell.TYPE).createSpec());
		final DataTableSpec spec = ret.createSpec();
		return spec;
	}

	/**
	 * @param dataTableSpec
	 * @return
	 */
	private ColumnRearranger createColumnRearranger(
			final DataTableSpec dataTableSpec) {
		final ColumnRearranger ret = new ColumnRearranger(dataTableSpec);
		final DataColumnSpec newColSpec = new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(dataTableSpec,
						settings.getOutputColumn()), JSONCell.TYPE)
				.createSpec();
		final int columnIndex = dataTableSpec.findColumnIndex(settings
				.getUrlColumn());
		final Map<String, String> settingsAsMap = settings.settingsAsMap();
		ret.append(new SingleCellFactory(newColSpec) {
			@Override
			public DataCell getCell(final DataRow row) {
				final DataCell cell = row.getCell(columnIndex);
				if (cell instanceof StringValue) {
					final StringValue sv = (StringValue) cell;
					try {
						return createCell(
								settings.isUseUrlColumn() ? sv.getStringValue()
										: settings.getUrl(), settingsAsMap);
					} catch (IOException | RuntimeException e) {
						throw new RuntimeException("In row" + row.getKey()
								+ " for url: " + sv.getStringValue()
								+ "\nProblem: " + e.getMessage(), e);
					}
				}
				return DataType.getMissingCell();
			}
		});
		return ret;
	}

	/**
	 * @param url
	 * @param settingsAsMap
	 * @return The value returned from Diffbot as a {@link JSONValue}.
	 * @throws IOException
	 */
	protected DataCell createCell(final String url,
			final Map<String, String> settingsAsMap) throws IOException {
		return JSONCellFactory.create(
				(String) new DiffbotClient(settings.getToken(), String
						.valueOf(settings.getVersion())).callApi("article",
						ResponseType.String, url, settingsAsMap), false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		try {
			this.settings.saveSettingsTo(settings);
		} catch (final InvalidSettingsException e) {
			throw new IllegalStateException("Should not happen", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		this.settings.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		final DiffbotArticleNodeSettings dummy = new DiffbotArticleNodeSettings();
		dummy.loadSettingsFrom(settings);
		final List<? extends String> warnings = dummy.validateConsistency();
		if (!warnings.isEmpty()) {
			setWarningMessage(warnings.toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec)/*
										 * throws IOException,
										 * CanceledExecutionException
										 */{
		// No internal state
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec)/*
										 * throws IOException,
										 * CanceledExecutionException
										 */{
		// No internal state
	}
}
