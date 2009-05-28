package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventJobDisconnected extends Event {
  public EventJobDisconnected(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















