package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventExecute extends Event {
  public EventExecute(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















