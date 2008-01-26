package ec.gep;

public class GEPIndividual2 extends GEPIndividual {

	private static final long serialVersionUID = 1L;

	public long depth() {

		// make sure we have the parsed expression to work with
		if (parsedGeneExpressions == null)
			parseGenes();

		// compute depth for all gene expressions
		long d = 0;
		for (GEPExpressionTreeNode parsedGeneExpression : parsedGeneExpressions) {
			long newdepth = depth(parsedGeneExpression);
			d = newdepth > d ? newdepth : d;
		}
		return d;
	}

	// recursively compute the expresion's depth
	private long depth(GEPExpressionTreeNode expr) {

		long d = 0, newdepth = 0;
		if (expr.parameters != null)
			for (GEPExpressionTreeNode arg : expr.parameters) {
				newdepth = depth(arg);
				if (newdepth > d)
					d = newdepth;
			}
		return d + 1;
	}
}
