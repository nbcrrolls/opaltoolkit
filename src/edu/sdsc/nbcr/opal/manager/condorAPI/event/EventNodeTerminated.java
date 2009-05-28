package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventNodeTerminated extends Event {
  public EventNodeTerminated(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















