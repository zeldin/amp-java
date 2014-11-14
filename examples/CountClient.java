import com.twistedmatrix.amp.*;
import com.twistedmatrix.internet.*;

/*
  To compile: ant buildexamples
  To run: ant runexclient
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
	System.out.println("connected");

	CountArgs ca = new CountArgs(1);
	CountResp cr = new CountResp(true);

	Deferred dfd = callRemote("Count", ca, cr);
	dfd.addCallback(new CountHandler());
	dfd.addErrback(new ErrHandler());
    }

    public void connectionLost(Throwable reason) {
	System.out.println("connection lost 1:" + reason);
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
	reactor.connectTCP("127.0.0.1", 7113, new IClientFactory() {
		public IProtocol buildProtocol(Object addr) {
		    System.out.println("building protocol");
		    return new CountClient(reactor);
		}

		public void startedConnecting(IConnector connector) {
		    System.out.println("connecting");
		}

		public void clientConnectionFailed(IConnector connector,
						   Throwable reason) {
		    System.out.println("connectiion failed:" + reason);
		    System.exit(0);
		}
		public void clientConnectionLost(IConnector connector,
						 Throwable reason) {
		    System.out.println("connection lost 2:" + reason);
		}

	    });

	reactor.run();
    }
}
