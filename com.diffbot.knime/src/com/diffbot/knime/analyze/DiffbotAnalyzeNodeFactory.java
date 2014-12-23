package com.diffbot.knime.analyze;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "DiffbotArticle" Node. Invokes Diffbot
 * article service and returns the extracted information as JSON values from the
 * specified (remote) URL(s). nThe optional input table can be a source of input
 * URLs.
 *
 * @author Diffbot Corp.
 */
public class DiffbotAnalyzeNodeFactory extends
		NodeFactory<DiffbotAnalyzeNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DiffbotAnalyzeNodeModel createNodeModel() {
		return new DiffbotAnalyzeNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<DiffbotAnalyzeNodeModel> createNodeView(
			final int viewIndex, final DiffbotAnalyzeNodeModel nodeModel) {
		throw new IllegalStateException("There are no views for this node: "
				+ viewIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new DiffbotAnalyzeNodeDialog();
	}
}
