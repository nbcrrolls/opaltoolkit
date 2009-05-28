package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventGlobusResourceDown extends Event {
  public EventGlobusResourceDown(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















