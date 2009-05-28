package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventGlobusResourceUp extends Event {
  public EventGlobusResourceUp(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















