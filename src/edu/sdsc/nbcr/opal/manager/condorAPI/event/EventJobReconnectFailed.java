package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventJobReconnectFailed extends Event {
  public EventJobReconnectFailed(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















