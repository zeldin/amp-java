import com.twistedmatrix.amp.*;
import com.twistedmatrix.internet.*;

/*
  To compile first build the packages and then:
    javac -d ../classes -sourcepath ../src/ CountClient.java

  Once compiled to run:
    java -cp ../classes CountClient
 */

public class CountClient extends AMP {
    Reactor _reactor = null;

    public CountClient(Reactor reactor) { _reactor = reactor; }

    /*
      This annotation is used to send the data. The name is the name of the 
      method and arguments is a space delimited list of the arguments in the 
      "arguments" class you hand to callRemote().
    */
    
    @AMP.Command(name="Count", arguments="n")
    public CountResp count (int n) { // Class must be public
	System.out.println("received: " + n + " ");
	
	CountArgs ca = new CountArgs(n+1);
	CountResp cr = new CountResp(true);
	
	if (ca.getArg() < 10) {
	    System.out.println("sending: " + ca.getArg());
	    
	    Deferred dfd = callRemote("Count", ca, cr); 
	    dfd.addCallback(new CountHandler());
	    dfd.addErrback(new ErrHandler());
	} else { _reactor.stop(); }
	
	return cr;
    }
    
    public void connectionMade() {
	CountArgs ca = new CountArgs(1);
	CountResp cr = new CountResp(true);

	/*
	  callRemote: command -> name of the remote command,
	      args class -> class containing values to pass, 
	      resp proto -> class defining response variables, data ignored
	  callBack:   initially passed a populated response class
	      return object is handed to next callback, if any
	  errBack:    initially passed a Deferred.Failure
	      return object is handed to next errback, if any
	*/
    
	Deferred dfd = callRemote("Count", ca, cr);
	dfd.addCallback(new CountHandler());
	dfd.addErrback(new ErrHandler());
    }
    
    public void connectionLost(Throwable reason) {
	System.out.println("connection lost:" + reason);
    }
    
    class CountHandler implements Deferred.Callback {
	public Object callback(Object retval) {
	    CountResp ret = (CountResp) retval;
	    
	    System.out.println("response: " + ret.getResponse());
	    return null; 
	}
    }
    
    class ErrHandler implements Deferred.Callback {
	public Object callback(Object retval) {
	    Deferred.Failure err = (Deferred.Failure) retval;
	    
	    System.out.println("error: " + err.get());
	    
	    //Class tc = err.trap(Exception.class);
	    
	    System.exit(0);
	    
	    return null;
	}
    }
    
    public class CountResp { // Return values, class/vars must be public
	public boolean ok = true;
	
	public CountResp(boolean b) { ok = b; }
	public boolean getResponse() { return ok; }
    }
    
    public class CountArgs { // Values sent, class/vars must be public
	public int n = 0;
	
	public CountArgs(int i) { n = i; }
	public int getArg() { return n; }
    }
    
    public static void main(String[] args) throws Throwable {
	Reactor reactor = Reactor.get();
	reactor.connectTCP("127.0.0.1", 7113, new IFactory() {
		public IProtocol buildProtocol(Object addr) {
		    return new CountClient(reactor);
		}
	    });
	// Need to be able to add errback here
	
	
	reactor.run();
    }
}

/*
  for (int i = 0; i < data.length; i++) {
  System.out.println("HOWDY Got: " + Character.toString((char) data[i])); 
  }

    public class CountResp { // Return values, class/vars must be public
	public int n = 0;
	public boolean ok = true;
	
	public CountResp(boolean b) { ok = b; }
	public int     getVal() { return n; }
	public boolean getStatus() { return ok; }
    }
    
*/