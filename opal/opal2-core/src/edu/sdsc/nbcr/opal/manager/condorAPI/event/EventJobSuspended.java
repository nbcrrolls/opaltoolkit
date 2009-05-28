package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventJobSuspended extends Event {
  public EventJobSuspended(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















