package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventJobHeld extends Event {
  public EventJobHeld(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















