package protocols.application.timers;

import pt.unl.fct.di.novasys.babel.generic.ProtoTimer;

public class IssueOperation extends ProtoTimer {
	public static final short TIMER_CODE = 400;

	public IssueOperation() {
		super(TIMER_CODE);
	}

	@Override
	public ProtoTimer clone() {
		return this;
	}

}
