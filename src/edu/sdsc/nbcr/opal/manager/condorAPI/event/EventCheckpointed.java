package condorAPI.event;
import condorAPI.*;
import condor.classad.*;

public class EventCheckpointed extends Event {
  public EventCheckpointed(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















