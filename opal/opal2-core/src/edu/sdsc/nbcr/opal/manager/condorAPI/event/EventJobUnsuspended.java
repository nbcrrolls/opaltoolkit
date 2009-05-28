package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventJobUnsuspended extends Event {
  public EventJobUnsuspended(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















