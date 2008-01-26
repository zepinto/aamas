package ec.gep.statsprinter;

/**
 * Implements the decorator pattern by wrapping a StatsPrinter object and adding
 * new functionality to it.
 * 
 * @author Rui Meireles
 * 
 */
public abstract class StatsPrinterDecorator implements IStatsPrinter {

	public static final String P_DECORATOR = "decorator";

	protected IStatsPrinter decoratedResultPrinter; // the ResultPrinter being

	// decorated

	/**
	 * Default empty constructor.
	 */
	public StatsPrinterDecorator() {

	}

	/**
	 * Construct a new decorator by wrapping an existing result printer.
	 */
	public StatsPrinterDecorator(IStatsPrinter decoratedResultPrinter) {
		this.decoratedResultPrinter = decoratedResultPrinter;
	}

	/**
	 * Setter for the inner decorated printer.
	 * 
	 * @param decoratedResultPrinter
	 */
	public void setDecoratedResultPrinter(IStatsPrinter decoratedResultPrinter) {
		this.decoratedResultPrinter = decoratedResultPrinter;
	}

}
