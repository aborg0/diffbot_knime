package com.diffbot.knime.article;

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
public class DiffbotArticleNodeFactory extends
		NodeFactory<DiffbotArticleNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DiffbotArticleNodeModel createNodeModel() {
		return new DiffbotArticleNodeModel();
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
	public NodeView<DiffbotArticleNodeModel> createNodeView(
			final int viewIndex, final DiffbotArticleNodeModel nodeModel) {
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
		return new DiffbotArticleNodeDialog();
	}
}
