package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventPostScriptTerminated extends Event {
  public EventPostScriptTerminated(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















