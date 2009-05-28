package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventGlobusSubmitFailed extends Event {
  public EventGlobusSubmitFailed(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















