package org.x3chaos.rtd.exception;

public class InvalidOutcomeException extends Exception {

	private static final long serialVersionUID = -5598156354611764181L;

	private static final String FORMAT_TOSTR = "Invalid outcome %s: %s";
	private String outcome;
	private String message;

	public InvalidOutcomeException(String outcome, String message) {
		this.outcome = outcome;
		this.message = message;
	}

	public InvalidOutcomeException(String message) {
		this("", message);
	}

	public InvalidOutcomeException() {
		this("", "");
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	public String getOutcome() {
		return outcome;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return String.format(FORMAT_TOSTR, outcome, message);
	}

}
