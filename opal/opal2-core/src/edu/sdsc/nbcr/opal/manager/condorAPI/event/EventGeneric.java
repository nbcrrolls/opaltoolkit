package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventGeneric extends Event {
  public EventGeneric(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















