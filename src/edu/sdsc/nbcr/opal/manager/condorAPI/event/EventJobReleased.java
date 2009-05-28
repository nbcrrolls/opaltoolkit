package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventJobReleased extends Event {
  public EventJobReleased(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















