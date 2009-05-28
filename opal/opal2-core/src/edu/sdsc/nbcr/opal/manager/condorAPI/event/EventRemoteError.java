package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventRemoteError extends Event {
  public EventRemoteError(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















