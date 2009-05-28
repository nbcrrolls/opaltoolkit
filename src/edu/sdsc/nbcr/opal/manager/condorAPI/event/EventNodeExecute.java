package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventNodeExecute extends Event {
  public EventNodeExecute(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















