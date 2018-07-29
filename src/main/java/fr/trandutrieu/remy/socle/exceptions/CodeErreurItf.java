package fr.trandutrieu.remy.socle.exceptions;

import java.io.Serializable;



public interface CodeErreurItf<E extends Enum<E>, L extends LabelErreurItf> extends Serializable {
		
	public default CodeErreurItf<E, L> get() {
		return this;
	}
	
	public L getLabelErreur();
	
}
