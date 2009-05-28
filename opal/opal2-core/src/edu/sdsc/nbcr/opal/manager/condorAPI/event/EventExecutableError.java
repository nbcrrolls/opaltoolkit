package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventExecutableError extends Event {
  public EventExecutableError(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















